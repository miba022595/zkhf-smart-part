package com.zkhf.epmis.platform.task.spider.handler;

import com.alibaba.fastjson2.JSON;
import com.zkhf.epmis.platform.task.spider.domain.*;
import com.zkhf.epmis.platform.task.spider.generator.LicensePdfGenerator;
import com.zkhf.epmis.platform.task.spider.parser.LicenseHtmlParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * 排污许可证爬取核心客户端
 *
 * <p>封装所有HTTP会话管理、API调用、HTML解析委托、附件下载、重试队列等爬取逻辑。
 * 设计为独立可复用的组件，便于迁移到其他业务场景使用。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 *   LicenseSpiderClient client = new LicenseSpiderClient();
 *   client.initSession();
 *   client.setDownloadDir("/data/license_pdfs");
 *   List<LicenseFullInfo> results = client.crawlCompanies(companyNames);
 *   client.processRetryQueue();
 *   client.shutdown();
 *   List<FailedDownloadTask> retried = client.getRetriedTasks();
 * }</pre>
 */
@Slf4j
@Component
public class LicenseSpiderClient {

    private static final String BASE_URL = "https://permit.mee.gov.cn";
    private static final String API_SEARCH_URL = BASE_URL + "/perxxgkinfo/syssb/xkgg/xkgg!licenseInformation.action";
    private static final String DETAIL_CONTENT_URL = BASE_URL + "/perxxgkinfo/xkgkAction!xkgk.action";

    private static final int MIN_DELAY_SECONDS = 5;
    private static final int MAX_DELAY_SECONDS = 10;
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 30000;

    private final Random random = new Random();
    private Map<String, String> sessionCookies = new HashMap<>();
    private String downloadDir = "license_pdfs";

    // ========== 重试队列 ==========
    private final Queue<FailedDownloadTask> failedTasks = new ConcurrentLinkedQueue<>();
    private final Queue<FailedDownloadTask> retriedTasks = new ConcurrentLinkedQueue<>();
    private final ExecutorService retryExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean retryRunning = false;

    // ========== 公开方法 ==========

    /** 设置下载目录（必须在 crawlCompanies 前调用） */
    public void setDownloadDir(String downloadDir) {
        this.downloadDir = downloadDir;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    /**
     * 初始化Session，获取Cookie
     */
    public void initSession() throws IOException {
        log.info("正在初始化Session");
        Connection.Response response = Jsoup.connect(BASE_URL + "/permitExt/defaults/default-index!getInformation.action")
                .method(Connection.Method.GET)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30000)
                .execute();

        sessionCookies = response.cookies();
        if (!sessionCookies.isEmpty()) {
            log.info("获取到Session Cookie");
        } else {
            log.warn("未获取到Cookie");
        }
    }

    /**
     * 批量爬取多家企业
     *
     * @param companyNames 企业名称列表
     * @param nameToEntCode 企业名称 → 编码映射（用于重试时关联）
     * @return 企业许可信息列表
     */
    public List<LicenseFullInfo> crawlCompanies(List<String> companyNames, Map<String, String> nameToEntCode) {
        List<LicenseFullInfo> results = new ArrayList<>();
        int total = companyNames.size();

        log.info("开始爬取 {} 家企业的排污许可信息", total);

        for (int i = 0; i < total; i++) {
            String name = companyNames.get(i);
            log.info("[{}/{}] {}", i + 1, total, name);
            try {
                String entCode = nameToEntCode != null ? nameToEntCode.get(name) : null;
                LicenseFullInfo info = crawlCompany(name, entCode);
                results.add(info);
            } catch (Exception e) {
                log.error("爬取异常: {}", name, e);
            }
            if (i < total - 1) {
                randomDelay();
            }
        }

        return results;
    }

    /**
     * 爬取单家企业（不含附件注册和编码转换）
     *
     * @param companyName 企业名称
     * @param entCode 企业编码（用于重试时关联附件注册）
     * @return 完整许可信息（包含已下载的附件）
     */
    public LicenseFullInfo crawlCompany(String companyName, String entCode) throws IOException {
        LicenseFullInfo result = new LicenseFullInfo();
        result.setCompanyName(companyName);
        result.setFound(false);

        randomDelay();

        CompanySummary summary = searchCompanyViaApi(companyName);
        if (summary == null) {
            return result;
        }

        result.setLicenseNo(summary.getLicenseNumber());
        result.setIndustry(summary.getIndustry());
        result.setValidStart(summary.getValidStart());
        result.setValidEnd(summary.getValidEnd());
        result.setIssueDate(summary.getIssueDate());
        result.setManagementType(summary.getManagementType());
        result.setDataId(summary.getDataId());
        result.setFound(true);

        log.debug("许可证编号: {}", summary.getLicenseNumber());

        if (summary.getDataId() != null && !summary.getDataId().isEmpty()) {
            randomDelay();
            LicenseDetailDetail detail = fetchLicenseDetail(summary.getDataId());
            if (detail != null) {
                result.setIssueOrg(detail.getIssueOrg());
                result.setMainProducts(detail.getMainProducts());
                result.setOutput(detail.getOutput());
                result.setRemarks(detail.getRemarks());
                result.setReportRequirements(detail.getReportRequirements());
                result.setEmissionInfo(detail.getEmissionInfo());
                result.setPermitLimits(detail.getPermitLimits());
                result.setAttachments(detail.getAttachments());
                log.debug("发证机关: {}", detail.getIssueOrg());

                // 下载附件文件到磁盘
                if (detail.getAttachments() != null && !detail.getAttachments().isEmpty()) {
                    log.info("下载附件: {} 个", detail.getAttachments().size());
                    downloadAttachments(detail.getAttachments(), companyName, entCode);
                }
            } else {
                log.warn("获取详情失败: {}", companyName);
            }
        }
        return result;
    }

    // ========== 重试管理 ==========

    /**
     * 启动后台重试线程，处理失败下载任务
     */
    public void processRetryQueue() {
        if (retryRunning) {
            return;
        }
        retryRunning = true;
        retryExecutor.submit(() -> {
            int total = failedTasks.size();
            int current = 0;
            int successCount = 0;
            int finalFailCount = 0;

            log.info("开始处理 {} 个失败下载任务", total);
            FailedDownloadTask task;
            while ((task = failedTasks.poll()) != null) {
                if (task == null) break;

                current++;
                log.info("重试 [{}/{}]: {} - {} (第{}次)", current, total, task.getCompanyName(), task.getType(), task.getRetryCount() + 1);

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                try {
                    if ("副本".equals(task.getType())) {
                        retryDownloadCopyAsPdf(task);
                    } else {
                        retryDownloadFile(task);
                    }
                    retriedTasks.add(task);
                    successCount++;
                    log.info("重试成功: {} - {}", task.getCompanyName(), task.getType());
                } catch (Exception e) {
                    task.setRetryCount(task.getRetryCount() + 1);
                    log.error("重试失败 ({}/{}): {} - {}", task.getRetryCount(), MAX_RETRY_COUNT, task.getCompanyName(), task.getType(), e);

                    if (task.getRetryCount() < MAX_RETRY_COUNT) {
                        failedTasks.offer(task);
                        log.info("{} - {} 将再次重试", task.getCompanyName(), task.getType());
                    } else {
                        finalFailCount++;
                        log.error("重试最终失败: {} - {}", task.getCompanyName(), task.getType());
                        saveFailedTaskToFile(task);
                    }
                }
            }

            log.info("重试完成: 成功{}个, 最终失败{}个", successCount, finalFailCount);
            retryRunning = false;
        });
    }

    /**
     * 获取重试成功的任务列表（供外部调用 registerFileToAnnex）
     */
    public List<FailedDownloadTask> getRetriedTasks() {
        List<FailedDownloadTask> list = new ArrayList<>();
        FailedDownloadTask task;
        while ((task = retriedTasks.poll()) != null) {
            list.add(task);
        }
        return list;
    }

    /** 等待重试线程完成并关闭 */
    public void shutdown() {
        retryExecutor.shutdown();
    }

    // ========== 持久化辅助 ==========

    /**
     * 保存结果为JSON文件 - 覆盖模式
     */
    public void saveToJson(List<LicenseFullInfo> results, String filename) {
        File oldFile = new File(filename);
        oldFile.delete();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            writer.write("[\n");
            for (int i = 0; i < results.size(); i++) {
                writer.write(JSON.toJSONString(results.get(i)));
                if (i < results.size() - 1) writer.write(",\n");
                else writer.write("\n");
            }
            writer.write("]\n");
            log.debug("JSON保存至 {}", filename);
        } catch (IOException e) {
            log.error("保存JSON失败", e);
        }
    }

    // ==================== HTTP连接构建 ====================

    private Connection buildPostConnection(String url) {
        Connection conn = Jsoup.connect(url)
                .method(Connection.Method.POST)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9")
                .timeout(30000);
        if (sessionCookies != null && !sessionCookies.isEmpty()) {
            conn.cookies(sessionCookies);
        }
        return conn;
    }

    private Connection buildGetConnection(String url) {
        Connection conn = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(30000);
        if (sessionCookies != null && !sessionCookies.isEmpty()) {
            conn.cookies(sessionCookies);
        }
        return conn;
    }

    private void randomDelay() {
        int delay = MIN_DELAY_SECONDS + random.nextInt(MAX_DELAY_SECONDS - MIN_DELAY_SECONDS + 1);
        try {
            Thread.sleep(delay * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ==================== API调用 ====================

    private CompanySummary searchCompanyViaApi(String companyName) throws IOException {
        int pageNo = 1;
        while (pageNo <= 3) {
            log.debug("查询第{}页", pageNo);

            Connection conn = buildPostConnection(API_SEARCH_URL);
            conn.data("page.pageNo", String.valueOf(pageNo));
            conn.data("registerentername", companyName);
            conn.data("page.orderBy", "");
            conn.data("page.order", "");
            conn.data("tempReportKey", "");
            conn.data("province", "");
            conn.data("city", "");
            conn.data("management", "");
            conn.data("xkznum", "");
            conn.data("treadname", "");
            conn.data("treadcode", "");
            conn.data("publishtime", "");

            Document doc = conn.post();
            Elements rows = doc.select("table tbody tr");

            if (rows.isEmpty()) break;

            for (Element row : rows) {
                Elements tds = row.select("td");
                if (tds.size() < 8) continue;

                String nameInResult = tds.get(3).text().trim();
                if (nameInResult.contains(companyName) || companyName.contains(nameInResult)) {
                    CompanySummary summary = new CompanySummary();
                    summary.setLicenseNumber(tds.get(2).text().trim());
                    summary.setCompanyName(nameInResult);
                    summary.setIndustry(tds.get(4).text().trim());

                    String validPeriod = tds.get(5).text().trim();
                    if (validPeriod.contains("至")) {
                        String[] parts = validPeriod.split("至");
                        summary.setValidStart(parts[0].trim());
                        summary.setValidEnd(parts[1].trim());
                    } else {
                        summary.setValidStart(validPeriod);
                        summary.setValidEnd("");
                    }

                    summary.setIssueDate(tds.get(6).text().trim());
                    summary.setManagementType(tds.get(7).text().trim());

                    Element viewLink = row.select("a:contains(查看)").first();
                    if (viewLink == null) viewLink = row.select("a").first();

                    if (viewLink != null) {
                        String href = viewLink.attr("href");
                        Pattern p = Pattern.compile("dataid=([a-f0-9]+)");
                        java.util.regex.Matcher m = p.matcher(href);
                        if (m.find()) summary.setDataId(m.group(1));
                    }

                    log.info("找到企业: {}, 许可证: {}, dataid: {}", summary.getCompanyName(), summary.getLicenseNumber(), summary.getDataId());
                    return summary;
                }
            }
            pageNo++;
        }
        return null;
    }

    private LicenseDetailDetail fetchLicenseDetail(String dataId) {
        try {
            String url = DETAIL_CONTENT_URL + "?xkgk=getxxgkContent&dataid=" + dataId;
            log.debug("获取详情");

            Document doc = buildGetConnection(url).get();
            LicenseDetailDetail detail = new LicenseDetailDetail();

            Map<String, String> allKeyValues = LicenseHtmlParser.parseAllKeyValues(doc);

            detail.setIssueOrg(LicenseHtmlParser.parseIssueOrg(doc));
            detail.setMainProducts(LicenseHtmlParser.parseMainProducts(doc, allKeyValues));
            detail.setOutput(LicenseHtmlParser.parseOutput(allKeyValues));
            detail.setRemarks(LicenseHtmlParser.parseRemarks(allKeyValues));
            detail.setReportRequirements(LicenseHtmlParser.parseReportRequirements(doc, allKeyValues));
            detail.setEmissionInfo(LicenseHtmlParser.parseEmissionInfo(allKeyValues));
            detail.setPermitLimits(LicenseHtmlParser.parsePermitLimits(doc));
            detail.setAttachments(LicenseHtmlParser.parseAttachments(doc, dataId));

            return detail;
        } catch (IOException e) {
            log.error("获取详情内容失败", e);
            return null;
        }
    }

    // ==================== 附件下载 ====================

    private void downloadAttachments(List<Attachment> attachments, String companyName, String entCode) {
        if (attachments == null || attachments.isEmpty()) return;

        File dir = new File(downloadDir);
        if (!dir.exists()) dir.mkdirs();

        String safeCompanyName = companyName.replaceAll("[\\\\/:*?\"<>|]", "_");

        for (Attachment att : attachments) {
            String url = att.getUrl();
            String type = att.getType();

            if (url == null || url.isEmpty()) {
                log.debug("附件URL为空: {}", type);
                continue;
            }

            if ("副本".equals(type)) {
                downloadCopyAsPdf(url, companyName, entCode);
            } else {
                String fileName = safeCompanyName + "_" + type + ".pdf";
                String filePath = downloadDir + File.separator + fileName;
                new File(filePath).delete();

                try {
                    log.debug("下载附件: {} -> {}", type, url);
                    downloadFile(url, filePath);
                    log.debug("附件下载完成: {}", fileName);
                    randomDelay();
                } catch (Exception e) {
                    log.error("附件下载失败: {}", type, e);
                    addFailedTask(url, filePath, companyName, entCode, type);
                }
            }
        }
    }

    private void downloadFile(String fileUrl, String filePath) throws IOException {
        Connection.Response response = Jsoup.connect(fileUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .ignoreContentType(true)
                .timeout(60000)
                .execute();

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(response.bodyAsBytes());
        }
    }

    private void downloadCopyAsPdf(String detailUrl, String companyName, String entCode) {
        String safeName = companyName.replaceAll("[\\\\/:*?\"<>|]", "_");
        String pdfPath = downloadDir + File.separator + safeName + "_副本.pdf";
        new File(pdfPath).delete();

        String pkid = null, dataid = null;
        int imgCount = 0;
        List<String> imagePaths = new ArrayList<>();
        int failedPageIndex = 1;

        try {
            log.info("开始处理副本下载");
            Document doc = buildGetConnection(detailUrl).get();

            String imgCountStr = doc.select("#imgCount").val();
            pkid = doc.select("#pkid").val();
            dataid = doc.select("#dataid").val();

            if (imgCountStr == null || pkid == null || dataid == null) {
                throw new IOException("无法解析副本页面参数");
            }

            imgCount = Integer.parseInt(imgCountStr);
            log.info("副本下载: {} 张图片", imgCount);

            File tempDir = new File(downloadDir + "/temp_" + safeName);
            if (tempDir.exists()) {
                File[] oldFiles = tempDir.listFiles();
                if (oldFiles != null) for (File f : oldFiles) f.delete();
                tempDir.delete();
            }
            tempDir.mkdirs();

            for (int i = 1; i <= imgCount; i++) {
                String imgUrl = BASE_URL + "/perxxgkinfo/syssb/xkgg/xkgg!downFilePng.action?datafileid=" + pkid + "_" + i + "&fileType=pdffile&dataid=" + dataid;
                String imagePath = tempDir.getAbsolutePath() + File.separator + String.format("page_%03d.png", i);

                try {
                    downloadImage(imgUrl, imagePath);
                    imagePaths.add(imagePath);
                } catch (Exception e) {
                    failedPageIndex = i;
                    log.warn("副本图片 {} 下载失败", i, e);
                    throw new IOException("图片 " + i + " 下载失败: " + e.getMessage(), e);
                }
                Thread.sleep(500);
            }

            LicensePdfGenerator.mergeImagesToPdf(imagePaths, pdfPath);

            for (String imgPath : imagePaths) new File(imgPath).delete();
            tempDir.delete();
            log.info("副本PDF生成成功: {}", pdfPath);

        } catch (Exception e) {
            log.error("处理副本失败", e);
            addFailedTask(detailUrl, pdfPath, companyName, entCode, "副本", pkid, dataid, imgCount, failedPageIndex, imagePaths);
        }
    }

    private void downloadImage(String imgUrl, String filePath) throws IOException {
        Connection.Response response = Jsoup.connect(imgUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .ignoreContentType(true)
                .timeout(30000)
                .execute();

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(response.bodyAsBytes());
        }
    }

    // ==================== 重试逻辑 ====================

    private void retryDownloadFile(FailedDownloadTask task) throws IOException {
        new File(task.getFilePath()).delete();
        downloadFile(task.getUrl(), task.getFilePath());
    }

    private void retryDownloadCopyAsPdf(FailedDownloadTask task) throws Exception {
        String safeName = task.getCompanyName().replaceAll("[\\\\/:*?\"<>|]", "_");

        String pkid = task.getPkid();
        String dataid = task.getDataid();
        int imgCount = task.getImgCount();
        int failedPageIndex = task.getFailedPageIndex();
        List<String> successImagePaths = task.getSuccessImagePaths();

        if (pkid == null || dataid == null || imgCount == 0) {
            log.info("副本参数不完整，重新解析HTML");
            Document doc = buildGetConnection(task.getUrl()).get();
            String imgCountStr = doc.select("#imgCount").val();
            pkid = doc.select("#pkid").val();
            dataid = doc.select("#dataid").val();
            if (imgCountStr == null || pkid == null || dataid == null) {
                throw new IOException("无法解析副本页面参数");
            }
            imgCount = Integer.parseInt(imgCountStr);
            failedPageIndex = 1;
            successImagePaths = new ArrayList<>();
        }

        log.info("使用已保存参数: pkid={}, dataid={}, 图片数={}", pkid, dataid, imgCount);
        log.info("从第{}页续传，已有{}张图片", failedPageIndex, successImagePaths.size());

        File tempDir = new File(downloadDir + "/temp_retry_" + safeName + "_" + System.currentTimeMillis());
        tempDir.mkdirs();

        List<String> allImagePaths = new ArrayList<>();
        for (int i = 0; i < successImagePaths.size(); i++) {
            String oldPath = successImagePaths.get(i);
            String newPath = tempDir.getAbsolutePath() + File.separator + String.format("page_%03d.png", i + 1);
            java.nio.file.Files.copy(new File(oldPath).toPath(), new File(newPath).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            allImagePaths.add(newPath);
        }

        for (int i = failedPageIndex; i <= imgCount; i++) {
            String imgUrl = BASE_URL + "/perxxgkinfo/syssb/xkgg/xkgg!downFilePng.action?datafileid=" + pkid + "_" + i + "&fileType=pdffile&dataid=" + dataid;
            String imagePath = tempDir.getAbsolutePath() + File.separator + String.format("page_%03d.png", i);
            downloadImage(imgUrl, imagePath);
            allImagePaths.add(imagePath);
            Thread.sleep(500);
        }

        LicensePdfGenerator.mergeImagesToPdf(allImagePaths, task.getFilePath());

        for (String imgPath : allImagePaths) new File(imgPath).delete();
        tempDir.delete();

        if (successImagePaths != null && !successImagePaths.isEmpty()) {
            File oldTempDir = new File(successImagePaths.get(0)).getParentFile();
            if (oldTempDir != null && oldTempDir.exists()) {
                for (String oldPath : successImagePaths) new File(oldPath).delete();
                oldTempDir.delete();
            }
        }
    }


    // ==================== 失败任务管理 ====================

    private void addFailedTask(String url, String filePath, String companyName, String entCode, String type) {
        FailedDownloadTask task = new FailedDownloadTask();
        task.setUrl(url);
        task.setFilePath(filePath);
        task.setCompanyName(companyName);
        task.setEntCode(entCode);
        task.setType(type);
        failedTasks.add(task);
        log.debug("添加失败任务: {} ({}次尝试)", task.getFilePath(), task.getRetryCount());
    }

    private void addFailedTask(String url, String filePath, String companyName, String entCode, String type,
                               String pkid, String dataid, int imgCount, int failedPageIndex, List<String> successImagePaths) {
        FailedDownloadTask task = new FailedDownloadTask();
        task.setUrl(url);
        task.setFilePath(filePath);
        task.setCompanyName(companyName);
        task.setEntCode(entCode);
        task.setType(type);
        task.setPkid(pkid);
        task.setDataid(dataid);
        task.setImgCount(imgCount);
        task.setFailedPageIndex(failedPageIndex);
        task.setSuccessImagePaths(successImagePaths);
        failedTasks.add(task);
        log.debug("添加副本失败任务: {} ({}次尝试)", task.getFilePath(), task.getRetryCount());
    }

    private void saveFailedTaskToFile(FailedDownloadTask task) {
        String failedLogFile = downloadDir + File.separator + "failed_downloads.log";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(failedLogFile, true))) {
            writer.write(String.format("%s|%s|%s|%s|%d\n",
                    LocalDateTime.now().toString(), task.getCompanyName(), task.getType(), task.getUrl(), task.getRetryCount()));
            writer.flush();
        } catch (IOException e) {
            log.error("保存失败记录出错", e);
        }
    }
}