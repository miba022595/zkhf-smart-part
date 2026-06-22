package com.zkhf.epmis.core.utils;

/**
 * 媒体类型工具类
 */
public class MimeTypeUtils {
    public static final String IMAGE_PNG = "image/png";

    public static final String IMAGE_JPG = "image/jpg";

    public static final String IMAGE_JPEG = "image/jpeg";

    public static final String IMAGE_BMP = "image/bmp";

    public static final String IMAGE_GIF = "image/gif";

    /**
     * Excel 2007+ (.xlsx)
     */
    public static final String EXCEL_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Excel 97-2003 (.xls)
     */
    public static final String EXCEL_XLS = "application/vnd.ms-excel";

    public static final String[] IMAGE_EXTENSION = {"bmp", "gif", "jpg", "jpeg", "png"};

    public static final String[] FLASH_EXTENSION = {"swf", "flv"};

    public static final String[] MEDIA_EXTENSION = {"swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg",
            "asf", "rm", "rmvb"};

    public static final String[] VIDEO_EXTENSION = {"mp4", "avi", "rmvb"};

    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            // 图片
            "bmp", "gif", "jpg", "jpeg", "png", "webp", "svg", "ico",
            // Word Excel PowerPoint
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt",
            // 压缩文件
            "rar", "zip", "gz", "bz2", "7z", "tar", "xz",
            // 音频格式
            "mp3", "wav", "aac", "flac", "ogg", "m4a",
            // 视频格式
            "mp4", "avi", "rmvb", "mov", "wmv", "flv", "mkv", "webm", "3gp",
            // 电子书 & PDF
            "pdf", "epub", "mobi"
    };

    public static String getExtension(String prefix) {
        switch (prefix) {
            case IMAGE_PNG:
                return "png";
            case IMAGE_JPG:
                return "jpg";
            case IMAGE_JPEG:
                return "jpeg";
            case IMAGE_BMP:
                return "bmp";
            case IMAGE_GIF:
                return "gif";
            case EXCEL_XLSX:
                return "xlsx";
            case EXCEL_XLS:
                return "xls";
            default:
                return "";
        }
    }
}
