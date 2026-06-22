package com.zkhf.epmis.protocol.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间格式化
 */
public class DateUtil {
	public static final DateTimeFormatter YY_M_D_H_S = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	public static String getCurrentDateStr() {
		return getCurrentDateStr(YY_M_D_H_S);
	}

	public static String getCurrentDateStr(DateTimeFormatter format) {
		return getCurrentDate().format(format);
	}

	public static LocalDateTime getCurrentDate() {
		return LocalDateTime.now();
	}

}
