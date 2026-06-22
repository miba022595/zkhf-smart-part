package com.zkhf.epmis.platform.task.spider.generator;

import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.util.List;

/**
 * 排污许可证副本PDF生成器，将多张图片合并为单个PDF文件
 */
@Slf4j
public class LicensePdfGenerator {

    private LicensePdfGenerator() {}

    /**
     * 合并图片为PDF（不创建临时文件，直接在同一Document中处理）
     *
     * <p>将多张图片合并为一个PDF文件，自动识别图片方向（横向/纵向），
     * 并调整图片大小以适应A4页面</p>
     *
     * @param imagePaths 图片文件路径列表
     * @param pdfPath    输出PDF路径
     */
    public static void mergeImagesToPdf(List<String> imagePaths, String pdfPath) {
        try {
            if (imagePaths == null || imagePaths.isEmpty()) {
                return;
            }

            com.itextpdf.text.Document document = null;

            for (int i = 0; i < imagePaths.size(); i++) {
                Image image = Image.getInstance(imagePaths.get(i));

                float imgWidth = image.getWidth();
                float imgHeight = image.getHeight();
                boolean isLandscape = imgWidth > imgHeight;

                com.itextpdf.text.Rectangle pageSize;
                if (isLandscape) {
                    pageSize = PageSize.A4.rotate();
                } else {
                    pageSize = PageSize.A4;
                }

                float pageWidth = pageSize.getWidth();
                float pageHeight = pageSize.getHeight();

                float scaleX = pageWidth / imgWidth;
                float scaleY = pageHeight / imgHeight;
                float scale = Math.min(scaleX, scaleY);
                image.scalePercent(scale * 100);

                float scaledWidth = image.getScaledWidth();
                float scaledHeight = image.getScaledHeight();
                float x = (pageWidth - scaledWidth) / 2;
                float y = (pageHeight - scaledHeight) / 2;
                image.setAbsolutePosition(x, y);

                if (document == null) {
                    document = new com.itextpdf.text.Document(pageSize);
                    com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
                    document.open();
                    document.add(image);
                } else {
                    document.setPageSize(pageSize);
                    document.newPage();
                    document.add(image);
                }
            }

            if (document != null) {
                document.close();
            }
            log.info("PDF合并完成: {} 页", imagePaths.size());

        } catch (Exception e) {
            log.error("PDF合并失败", e);
        }
    }
}