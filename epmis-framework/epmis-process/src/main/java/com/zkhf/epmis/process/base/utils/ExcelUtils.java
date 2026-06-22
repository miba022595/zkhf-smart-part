package com.zkhf.epmis.process.base.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

@Slf4j
public class ExcelUtils {

    public static XSSFWorkbook getSheetAt(String fileName) {
        try {
            String templatePath = "template/" + fileName;
            InputStream fis = ExcelUtils.class.getClassLoader().getResourceAsStream(templatePath);
            if (fis == null) {
                log.error("无法从路径加载资源 {}", templatePath);
                return null;
            }
            return new XSSFWorkbook(fis);
        } catch (Exception e) {
            log.error(" {} 模板资源获取失败", fileName, e);
            return null;
        }
    }
}
