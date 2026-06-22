package com.zkhf.epmis.platform.utils.file;

import com.zkhf.epmis.core.config.EPMISConfig;
import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.utils.DateUtils;
import com.zkhf.epmis.core.utils.MimeTypeUtils;
import com.zkhf.epmis.core.utils.StringUtils;
import com.zkhf.epmis.core.utils.uuid.Seq;
import com.zkhf.epmis.platform.exception.FileInvalidException;
import com.zkhf.epmis.platform.exception.FileNameLimitException;
import com.zkhf.epmis.platform.exception.FileSizeLimitException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * 文件上传工具类
 */
@Slf4j
public class FileUploadUtils {

    public static String getDefaultBaseDir() {
        return EPMISConfig.getProfile();
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件名称
     */
    public static String upload(MultipartFile file) throws IOException {
        try {
            return upload(getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir 相对应用的基目录
     * @param file    上传的文件
     * @return 文件名称
     */
    public static String upload(String baseDir, MultipartFile file) throws IOException {
        try {
            return upload(baseDir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 文件上传
     *
     * @param baseDir          相对应用的基目录
     * @param file             上传的文件
     * @param allowedExtension 上传文件类型
     * @return 返回上传成功的文件名
     * @throws FileSizeLimitException       如果超出最大大小
     * @throws FileNameLimitException       文件名太长
     * @throws IOException                  比如读写文件出错时
     * @throws FileInvalidException    文件校验异常
     */
    public static String upload(String baseDir, MultipartFile file, String[] allowedExtension) throws IOException {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > EPMISConfig.getMaxFileNameLength()) {
            throw new FileNameLimitException(EPMISConfig.getMaxFileNameLength(), fileNameLength);
        }

        assertAllowed(file, allowedExtension);

        String fileName = extractFilename(file);

        String absPath = getAbsoluteFile(baseDir, fileName).getAbsolutePath();
        file.transferTo(Paths.get(absPath));
        return getPathFileName(baseDir, fileName);
    }

    /**
     * 编码文件名
     */
    public static String extractFilename(MultipartFile file) {
        return StringUtils.format("{}/{}_{}.{}", DateUtils.datePath(),
                FilenameUtils.getBaseName(file.getOriginalFilename()), Seq.getId(Seq.uploadSeqType), getExtension(file));
    }

    public static File getAbsoluteFile(String uploadDir, String fileName) {
        File desc = new File(uploadDir + File.separator + fileName);

        if (!desc.exists()) {
            if (!desc.getParentFile().exists()) {
                desc.getParentFile().mkdirs();
            }
        }
        return desc;
    }

    public static String getPathFileName(String uploadDir, String fileName) {
        int dirLastIndex = EPMISConfig.getProfile().length() + 1;
        String currentDir = StringUtils.substring(uploadDir, dirLastIndex);
        return Constants.RESOURCE_PREFIX + "/" + currentDir + "/" + fileName;
    }

    /**
     * 文件大小校验
     *
     * @param file 上传的文件
     * @throws FileSizeLimitException 如果超出最大大小
     * @throws FileInvalidException 类型异常
     */
    public static void assertAllowed(MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitException, FileInvalidException {
        long size = file.getSize();
        if (size > EPMISConfig.getMaxFileSize()) {
            throw new FileSizeLimitException(EPMISConfig.getMaxFileSize(), size);
        }

        String fileName = file.getOriginalFilename();
        String extension = getExtension(file);
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
            throw new FileInvalidException(fileName, extension, allowedExtension);
        }
    }

    /**
     * 判断MIME类型是否是允许的MIME类型
     */
    public static boolean isAllowedExtension(String extension, String[] allowedExtension) {
        for (String str : allowedExtension) {
            if (str.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件名的后缀
     *
     * @param file 表单文件
     * @return 后缀名
     */
    public static String getExtension(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isEmpty(extension)) {
            extension = MimeTypeUtils.getExtension(Objects.requireNonNull(file.getContentType()));
        }
        return extension;
    }

    /**
     * 文件删除(更改为移动到备份目录)
     */
    public static void deleteFile(String filePath) {
        String baseDir = EPMISConfig.getProfile();
        if (filePath.startsWith(Constants.RESOURCE_PREFIX)) {
            filePath = filePath.substring(Constants.RESOURCE_PREFIX.length());
        }
        Path path = Paths.get(baseDir + File.separator + filePath);
        if (!Files.exists(path)) {
            log.error("文件不存在 {} {}", baseDir, filePath);
            return;
        }
        if (Files.isDirectory(path)) {
            log.error("路径是目录不是文件 {} {}", baseDir, filePath);
            return;
        }
        try {
            Files.delete(path);
        } catch (Exception e) {
            log.error("删除失败", e);
        }
    }
}
