package com.zkhf.epmis.core.utils;

import com.zkhf.epmis.core.enums.DataEnum;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * 时间工具类
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static DateTimeFormatter iso_dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static DateTimeFormatter dtfC = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static DateTimeFormatter d_h_m_s = DateTimeFormatter.ofPattern("dd HH:mm:ss");

    public static DateTimeFormatter m_d_h_m = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    public static DateTimeFormatter m_d_h = DateTimeFormatter.ofPattern("MM-dd HH");

    public static DateTimeFormatter y_m_d = DateTimeFormatter.ofPattern("yy-MM-dd");

    public static DateTimeFormatter yy_m_d = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static DateTimeFormatter yy_m_d_h = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");

    public static DateTimeFormatter yy_m_d_h_m = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static DateTimeFormatter yy_m_d_h_m_s = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static DateTimeFormatter yy_m_d_h_m_s_S = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static DateTimeFormatter yymdhms = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static DateTimeFormatter yymdH = DateTimeFormatter.ofPattern("yyyyMMddHH");

    public static DateTimeFormatter yymd = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final DateTimeFormatter YMD_ZH = DateTimeFormatter.ofPattern("yyyy年M月d日");

    public static final DateTimeFormatter YMD_H_ZH = DateTimeFormatter.ofPattern("yyyy年M月d日H时");

    public static DateTimeFormatter yym = DateTimeFormatter.ofPattern("yyyyMM");

    private static final String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算时间差
     *
     * @param endDate   最后时间
     * @param startTime 开始时间
     * @return 时间差（天/小时/分钟）
     */
    public static String timeDistance(Date endDate, Date startTime) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startTime.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 将分钟数转换为"xx天xx小时xx分钟"格式
     * @param totalMinutes 总分钟数
     * @return 格式化后的字符串
     */
    public static String convertMinutes(Long totalMinutes) {
        if (totalMinutes == null) {
            return null;
        }
        if (totalMinutes <= 0) {
            return "0分钟";
        }
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append("天");
        }
        if (hours > 0) {
            result.append(hours).append("小时");
        }
        if (minutes > 0) {
            result.append(minutes).append("分钟");
        }
        return result.toString();
    }

    /**
     * 增加 LocalDateTime ==> Date
     */
    public static Date toDate(LocalDateTime temporalAccessor) {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 增加 LocalDate ==> Date
     */
    public static Date toDate(LocalDate temporalAccessor) {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    public static String localDateTimeToStr(LocalDateTime dateTime, String defaultVal) {
        if (null == dateTime) {
            return defaultVal;
        }
        return dateTime.format(dtf);
    }

    public static String localDateTimeToStrByType(LocalDateTime dateTime, String dataEnum, String defaultVal) {
        if (null == dateTime) {
            return defaultVal;
        }
        if (DataEnum.real.name().equals(dataEnum)) {
            return dateTime.format(d_h_m_s);
        } else if (DataEnum.minute.name().equals(dataEnum)) {
            return dateTime.format(m_d_h_m);
        } else if (DataEnum.hour.name().equals(dataEnum)) {
            return dateTime.format(m_d_h);
        } else if (DataEnum.day.name().equals(dataEnum)) {
            return dateTime.format(y_m_d);
        }
        return defaultVal;
    }

    /**
     * 规范化时间
     */
    public static void standardizedTime(Map<String, Object> param, String dataEnumName) {
        if (null == param) {
            return;
        }
        if (DataEnum.hour.name().equals(dataEnumName)) {
            if (param.containsKey("beginTime")) {
                param.put("beginTime", param.get("beginTime") + ":00:00");
            }
            if (param.containsKey("endTime")) {
                param.put("endTime", param.get("endTime") + ":59:59");
            }
        } else if (DataEnum.day.name().equals(dataEnumName)) {
            if (param.containsKey("beginTime")) {
                param.put("beginTime", param.get("beginTime") + " 00:00:00");
            }
            if (param.containsKey("endTime")) {
                param.put("endTime", param.get("endTime") + " 23:59:59");
            }
        } else if (DataEnum.minute.name().equals(dataEnumName)) {
            if (param.containsKey("beginTime")) {
                param.put("beginTime", param.get("beginTime") + ":00");
            }
            if (param.containsKey("endTime")) {
                param.put("endTime", param.get("endTime") + ":59");
            }
        }
    }

    /**
     * 获取表中的年份
     * 5年一个表
     */
    public static Integer getTableYear(LocalDateTime dateTime) {
        if (null == dateTime) {
            return null;
        }
        return (dateTime.getYear() / 5) * 5;
    }

    public static LocalDateTime strToLocalDateTime(String val, DateTimeFormatter formatter) {
        try {
            return LocalDateTime.parse(val, formatter);
        } catch (Exception ignore) {
        }
        return null;
    }
}
