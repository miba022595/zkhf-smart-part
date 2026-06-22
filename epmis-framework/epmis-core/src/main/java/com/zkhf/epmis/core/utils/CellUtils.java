package com.zkhf.epmis.core.utils;

import org.apache.poi.ss.usermodel.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CellUtils {

    /**
     * 统一行移动（获取单元格样式之后）
     */
    public static void shiftRows(Sheet sheet, int rowIndex, int size) {
        if (size > 1) {
            // 如果数据多于1行，向下移动模板中的空行，为数据行腾出空间
            sheet.shiftRows(rowIndex, sheet.getLastRowNum(), size - 1);
        }
    }

    public static Cell getCell(Row row, int index, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellStyle(style);
        return cell;
    }

    /**
     * 获取单元格格式
     * @param workbook 工作簿对象
     * @param sheet 工作表对象
     * @param rowIndex 行索引（从0开始），用于获取参考单元格
     */
    public static CellStyle getCellStyle(Workbook workbook, Sheet sheet, int rowIndex) {
        return getCellStyle(workbook, sheet, rowIndex, 0, null);
    }

    /**
     * 获取单元格格式
     * @param workbook 工作簿对象
     * @param sheet 工作表对象
     * @param rowIndex 行索引（从0开始），用于获取参考单元格
     * @param cellIndex 列索引（从0开始），用于获取参考单元格
     */
    public static CellStyle getCellStyle(Workbook workbook, Sheet sheet, int rowIndex, int cellIndex) {
        return getCellStyle(workbook, sheet, rowIndex, cellIndex, null);
    }

    /**
     * 获取单元格格式
     * @param workbook 工作簿对象
     * @param sheet 工作表对象
     * @param rowIndex 行索引（从0开始），用于获取参考单元格
     * @param cellIndex 列索引（从0开始），用于获取参考单元格
     * @param decimalPlaces 小数位数配置：
     *                      - null: 保持原单元格的格式
     *                      - 0: 设置为整数格式（无小数位）
     *                      - 正整数: 设置指定的小数位数
     */
    public static CellStyle getCellStyle(Workbook workbook, Sheet sheet, int rowIndex, int cellIndex, Integer decimalPlaces) {
        if (null != decimalPlaces && decimalPlaces < 0) {
            throw new IllegalArgumentException("小数位数不能为负数");
        }
        Row templateRow = sheet.getRow(rowIndex);
        if (null == templateRow) {
            throw new IllegalArgumentException("目标行为空");
        }
        // 获取第一列的样式
        Cell cell = templateRow.getCell(cellIndex);
        if (null == cell) {
            throw new IllegalArgumentException("目标单元格为空");
        }
        CellStyle templateStyle = cell.getCellStyle();
        // 创建单元格样式
        CellStyle style = workbook.createCellStyle();
        copyCellStyle(templateStyle, style);
        if (null != decimalPlaces) { // 数字单元格格式
            /*
             * 动态构建格式字符串：使用更灵活的自定义格式（推荐）
             * 使用0.###格式，会去掉后边的无用0；0.0000格式则会补充0; #.#时0.0则显示为.，不使用
             * 0.###格式，这样只要设置最大的小数位就能通用了
             */
            String formatPattern;
            if (decimalPlaces > 0) {
                StringBuilder sb = new StringBuilder("0.0");
                for (int i = 1; i < decimalPlaces; i++) {
                    sb.append("#");
                }
                formatPattern = sb.toString();
            } else {
                formatPattern = "0";
            }
            // 创建数据格式
            DataFormat dataFormat = workbook.createDataFormat();
            style.setDataFormat(dataFormat.getFormat(formatPattern));
        }
        return style;
    }

    private static void copyCellStyle(CellStyle source, CellStyle to) {
        to.cloneStyleFrom(source); // 复制模板样式

        // 修复：必须在设置FillPattern之前或同时设置FillForegroundColor
        // 检查源样式是否有背景色
        if (source.getFillPattern() != FillPatternType.NO_FILL && source.getFillForegroundColor() > 0) {
            // 保留源样式的背景色
            to.setFillForegroundColor(source.getFillForegroundColor());
            to.setFillPattern(source.getFillPattern());
        } else {
            // 设置白色背景
            to.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            to.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        to.setAlignment(HorizontalAlignment.CENTER); // 设置水平居中
        to.setBorderTop(BorderStyle.THIN);
        to.setBorderBottom(BorderStyle.THIN);
        to.setBorderLeft(BorderStyle.THIN);
        to.setBorderRight(BorderStyle.THIN);
        to.setTopBorderColor(IndexedColors.BLACK.getIndex());
        to.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        to.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        to.setRightBorderColor(IndexedColors.BLACK.getIndex());
    }

    public static String getCellStringVal(Row row, int index) {
        Cell cell = row.getCell(index);
        if (null == cell) {
            return null;
        }
        String value = null;
        if (CellType.NUMERIC.equals(cell.getCellType())) {
            value = (long) cell.getNumericCellValue() + "";
        } else if (CellType.STRING.equals(cell.getCellType())) {
            value = cell.getStringCellValue().trim();
        }
        return value;
    }

    public static String getCellStringDateVal(Row row, int index) {
        Cell cell = row.getCell(index);
        if (null == cell) {
            return null;
        }
        String value = null;
        if (CellType.NUMERIC.equals(cell.getCellType())) {
            if (DateUtil.isCellDateFormatted(cell)) {
                // 处理格式化的日期单元格
                Date javaDate = cell.getDateCellValue();
                value = javaDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime().format(DateUtils.yy_m_d);
            } else {
                // 处理Excel内部日期数值
                double excelDateValue = cell.getNumericCellValue();
                // Excel的日期基准是1900-01-01（Windows版）
                // 注意：Excel错误地将1900年视为闰年
                LocalDate baseDate = LocalDate.of(1899, 12, 30);
                // 调整Excel的日期计算错误（1900年不是闰年）
                if (excelDateValue >= 61) {
                    excelDateValue -= 1; // 修正Excel的闰年错误
                }
                value = baseDate.plusDays((long) excelDateValue).toString();
            }
        } else if (CellType.STRING.equals(cell.getCellType())) {
            value = cell.getStringCellValue().trim();
        }
        return value;
    }

    public static LocalDate getCellLocalDateVal(Row row, int index) {
        Cell cell = row.getCell(index);
        if (null == cell) {
            return null;
        }
        LocalDate value = null;
        if (CellType.NUMERIC.equals(cell.getCellType())) {
            if (DateUtil.isCellDateFormatted(cell)) {
                // 处理格式化的日期单元格
                Date javaDate = cell.getDateCellValue();
                value = javaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } else {
                // 处理Excel内部日期数值
                double excelDateValue = cell.getNumericCellValue();
                // Excel的日期基准是1900-01-01（Windows版）
                // 注意：Excel错误地将1900年视为闰年
                LocalDate baseDate = LocalDate.of(1899, 12, 30);
                // 调整Excel的日期计算错误（1900年不是闰年）
                if (excelDateValue >= 61) {
                    excelDateValue -= 1; // 修正Excel的闰年错误
                }
                value = baseDate.plusDays((long) excelDateValue);
            }
        } else if (CellType.STRING.equals(cell.getCellType())) {
            value = strToLocalDate(cell.getStringCellValue().trim());
        }
        return value;
    }

    public static LocalDate strToLocalDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Excel单元格安全获取Integer类型数据
     * 支持多种单元格类型（数字、字符串、公式等）的转换
     * // 单元格内容为123.45 → 返回123
     * // 单元格内容为"456" → 返回456
     * // 单元格内容为true → 返回1
     * // 单元格内容为空 → 返回null
     */
    public static Integer getCellIntegerVal(Row row, int index) {
        Integer value = null;
        Cell cell = row.getCell(index);
        if (null == cell || cell.getCellType() == CellType.BLANK) {
            return value;
        }
        try {
            CellType cellType = cell.getCellType();
            // 处理公式单元格，获取计算结果类型
            if (cellType == CellType.FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            if (CellType.NUMERIC.equals(cellType)) {
                // 数字类型：直接取整
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是日期格式，返回时间戳的整数值
                    Date date = cell.getDateCellValue();
                    value = (int) (date.getTime() / 1000);
                } else {
                    // 普通数字，四舍五入取整
                    double numericValue = cell.getNumericCellValue();
                    value = (int) Math.round(numericValue);
                }
            } else if (CellType.STRING.equals(cell.getCellType())) {
                // 字符串类型：尝试解析为整数
                String stringValue = cell.getStringCellValue().trim();
                if (StringUtils.isNotEmpty(stringValue)) {
                    // 移除可能存在的千分位分隔符
                    stringValue = stringValue.replace(",", "");
                    value = Integer.parseInt(stringValue);
                }
            } else if (CellType.BOOLEAN.equals(cell.getCellType())) {
                // 布尔类型：true→1, false→0
                value = cell.getBooleanCellValue() ? 1 : 0;
            }
        } catch (Exception ignore) {}
        return value;
    }

    /**
     * 从Excel单元格安全获取BigDecimal类型数据
     * 支持多种单元格类型（数字、字符串、公式等）的转换
     * 使用示例：
     * // 单元格内容为123.456789 → 返回BigDecimal("123.456789")
     * // 单元格内容为"456.78" → 返回BigDecimal("456.78")
     * // 单元格内容为true → 返回BigDecimal.ONE
     * // 单元格内容为false → 返回BigDecimal.ZERO
     * // 单元格内容为空 → 返回null
     * // 单元格内容为"123,456.99" → 返回BigDecimal("123456.99")
     *
     * @param row 表格行
     * @param index 单元格索引
     * @return BigDecimal值，解析失败返回null
     */
    public static BigDecimal getCellBigDecimalVal(Row row, int index) {
        return getCellBigDecimalVal(row, index, null, null);
    }

    /**
     * 从Excel单元格安全获取BigDecimal类型数据
     *
     * @param row 表格行
     * @param index 单元格索引
     * @param scale 小数位数（可为null，表示不处理小数位数）
     * @return BigDecimal值，解析失败返回null
     */
    public static BigDecimal getCellBigDecimalVal(Row row, int index, Integer scale) {
        return getCellBigDecimalVal(row, index, scale, RoundingMode.HALF_UP);
    }

    /**
     * 从Excel单元格安全获取BigDecimal类型数据
     *
     * @param row 表格行
     * @param index 单元格索引
     * @param scale 小数位数（可为null，表示不处理小数位数）
     * @param roundingMode 舍入模式（可为null，默认四舍五入）
     * @return BigDecimal值，解析失败返回null
     */
    public static BigDecimal getCellBigDecimalVal(Row row, int index, Integer scale, RoundingMode roundingMode) {
        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
            CellType cellType = cell.getCellType();
            // 处理公式单元格，获取计算结果类型
            if (cellType == CellType.FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            BigDecimal result = null;
            if (CellType.NUMERIC.equals(cellType)) {
                result = handleNumericCellForBigDecimal(cell);
            } else if (CellType.STRING.equals(cellType)) {
                result = handleStringCellForBigDecimal(cell);
            } else if (CellType.BOOLEAN.equals(cellType)) {
                result = handleBooleanCellForBigDecimal(cell);
            }
            // 处理小数位数和舍入
            if (null != result && scale != null) {
                roundingMode = roundingMode != null ? roundingMode : RoundingMode.HALF_UP;
                result = result.setScale(scale, roundingMode);
            }

            return result;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 处理数字类型单元格 - BigDecimal版本
     */
    private static BigDecimal handleNumericCellForBigDecimal(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            // 如果是日期格式，返回时间戳的BigDecimal值（毫秒）
            Date date = cell.getDateCellValue();
            return BigDecimal.valueOf(date.getTime());
        } else {
            // 普通数字，直接转换为BigDecimal
            double numericValue = cell.getNumericCellValue();
            // 使用Double.toString避免精度丢失
            return new BigDecimal(Double.toString(numericValue));
        }
    }

    /**
     * 处理字符串类型单元格 - BigDecimal版本
     */
    private static BigDecimal handleStringCellForBigDecimal(Cell cell) {
        String stringValue = cell.getStringCellValue().trim();
        if (StringUtils.isEmpty(stringValue)) {
            return null;
        }

        try {
            // 移除可能存在的千分位分隔符和其他非数字字符（保留小数点、负号）
            String cleanedValue = stringValue.replace(",", "")
                    .replace(" ", "")
                    .replace("￥", "")
                    .replace("$", "")
                    .replace("€", "");

            // 检查是否为百分比
            if (cleanedValue.endsWith("%")) {
                cleanedValue = cleanedValue.substring(0, cleanedValue.length() - 1);
                BigDecimal percentage = new BigDecimal(cleanedValue);
                return percentage.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            }

            return new BigDecimal(cleanedValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 处理布尔类型单元格 - BigDecimal版本
     */
    private static BigDecimal handleBooleanCellForBigDecimal(Cell cell) {
        boolean booleanValue = cell.getBooleanCellValue();
        return booleanValue ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    /**
     * 设置单元格的字符串内容
     */
    public static void setStringVal(Row row, int index, String val, CellStyle style) {
        Cell cell = CellUtils.getCell(row, index, style);
        if (null != val) {
            cell.setCellValue(val);
        }
    }

    /**
     * 设置单元格的Integer内容
     */
    public static void setIntegerVal(Row row, int index, Integer val, CellStyle style) {
        Cell cell = CellUtils.getCell(row, index, style);
        if (null != val) {
            cell.setCellValue(val);
        }
    }

    /**
     * 设置单元格的Long内容
     */
    public static void setLongVal(Row row, int index, Long val, CellStyle style) {
        Cell cell = CellUtils.getCell(row, index, style);
        if (null != val) {
            cell.setCellValue(val);
        }
    }

    /**
     * 设置单元格的BigDecimal内容
     */
    public static void setBigDecimalVal(Row row, int index, BigDecimal val, CellStyle style) {
        Cell cell = CellUtils.getCell(row, index, style);
        if (null != val) {
            cell.setCellValue(val.doubleValue());
        }
    }

    /**
     * 设置单元格的BigDecimal内容
     */
    public static void setDoubleVal(Row row, int index, Double val, CellStyle style) {
        Cell cell = CellUtils.getCell(row, index, style);
        if (null != val) {
            cell.setCellValue(val);
        }
    }

    /**
     * 设置单元格的Float内容
     */
    public static void setFloatVal(Row row, int index, Float val, CellStyle style) {
        Cell cell = CellUtils.getCell(row, index, style);
        if (null != val) {
            cell.setCellValue(val);
        }
    }

    /**
     * 设置单元格的LocalDateTime内容
     */
    public static void setLocalDateTimeStr(Row row, int index, LocalDateTime val, DateTimeFormatter formatter, CellStyle style) {
        Cell cell = CellUtils.getCell(row, index, style);
        if (null != val) {
            cell.setCellValue(val.format(formatter));
        }
    }

    /**
     * 设置单元格的LocalDate内容
     */
    public static void setLocalDateStr(Row row, int index, LocalDate val, DateTimeFormatter formatter, CellStyle style) {
        Cell cell = CellUtils.getCell(row, index, style);
        if (null != val) {
            cell.setCellValue(val.format(formatter));
        }
    }
}
