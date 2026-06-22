package com.zkhf.epmis.generator;

import com.zkhf.epmis.generator.domain.GenTable;
import com.zkhf.epmis.generator.domain.GenTableColumn;
import com.zkhf.epmis.generator.util.GenUtils;
import com.zkhf.epmis.generator.util.VelocityUtils.TemplateMeta;
import com.zkhf.epmis.generator.util.VelocityUtils;
import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成入口。
 * 直接读取数据库表结构并生成后端代码文件。
 */
public class GeneratorMain {

    /**
     * 默认数据库连接地址
     */
    private static final String DEFAULT_DB_URL = "jdbc:mysql://182.92.7.39:13306/zkhf_poll_test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8";
    /**
     * 默认数据库用户名
     */
    private static final String DEFAULT_DB_USERNAME = "root";
    /**
     * 默认数据库密码
     */
    private static final String DEFAULT_DB_PASSWORD = "zkhf!123456";
    /**
     * 默认代码作者
     */
    private static final String DEFAULT_AUTHOR = "zkhf";
    /**
     * 默认生成包名
     */
    private static final String DEFAULT_PACKAGE_NAME = "com.zkhf.epmis";

    /**
     * 本地调试入口。
     */
    public static void main(String[] args) throws Exception {
        writeCode("sys_user", "d:/temp/generator-output");
    }

    /**
     * 使用默认数据库配置生成指定表代码。
     */
    public static void writeCode(String tableName, String outputDir) throws Exception {
        writeCode(
                tableName,
                outputDir,
                DEFAULT_DB_URL,
                DEFAULT_DB_USERNAME,
                DEFAULT_DB_PASSWORD,
                DEFAULT_PACKAGE_NAME,
                DEFAULT_AUTHOR
        );
    }

    /**
     * 按指定配置生成表对应的后端代码。
     */
    public static void writeCode(
            String tableName,
            String outputDir,
            String dbUrl,
            String dbUsername,
            String dbPassword,
            String packageName,
            String author
    ) throws Exception {
        GenTable table = loadTable(tableName, dbUrl, dbUsername, dbPassword, packageName, author);
        writeCode(table, Path.of(outputDir));
        System.out.println("Code generated to: " + Path.of(outputDir).toAbsolutePath());
    }

    /**
     * 读取表信息和字段信息，并补全生成代码需要的元数据。
     */
    private static GenTable loadTable(
            String tableName,
            String dbUrl,
            String dbUsername,
            String dbPassword,
            String packageName,
            String author
    ) throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            GenTable table = queryTable(connection, tableName);
            if (table == null) {
                throw new IllegalArgumentException("表不存在: " + tableName);
            }
            GenUtils.initTable(table, packageName, author);

            List<GenTableColumn> columns = queryColumns(connection, tableName);
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("未查询到字段: " + tableName);
            }
            for (GenTableColumn column : columns) {
                GenUtils.initColumnField(column);
            }
            table.setColumns(columns);
            table.setPkColumn(findPkColumn(columns));
            return table;
        }
    }

    /**
     * 查询表基础信息。
     */
    private static GenTable queryTable(Connection connection, String tableName) throws SQLException {
        String sql = """
                select table_name, table_comment
                from information_schema.tables
                where table_schema = (select database())
                  and table_name = ?
                  and table_name not like 'qrtz_%'
                  and table_name not like 'gen_%'
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                GenTable table = new GenTable();
                table.setTableName(rs.getString("table_name"));
                table.setTableComment(rs.getString("table_comment"));
                return table;
            }
        }
    }

    /**
     * 查询表字段信息。
     */
    private static List<GenTableColumn> queryColumns(Connection connection, String tableName) throws SQLException {
        String sql = """
                select column_name,
                       case when (is_nullable = 'NO' and column_key != 'PRI') then '1' else '0' end as is_required,
                       case when column_key = 'PRI' then '1' else '0' end as is_pk,
                       column_comment,
                       case when extra = 'auto_increment' then '1' else '0' end as is_increment,
                       column_type
                from information_schema.columns
                where table_schema = (select database())
                  and table_name = ?
                order by ordinal_position
                """;
        List<GenTableColumn> columns = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GenTableColumn column = new GenTableColumn();
                    column.setColumnName(rs.getString("column_name"));
                    column.setIsRequired(rs.getString("is_required"));
                    column.setIsPk(rs.getString("is_pk"));
                    column.setColumnComment(rs.getString("column_comment"));
                    column.setIsIncrement(rs.getString("is_increment"));
                    column.setColumnType(rs.getString("column_type"));
                    columns.add(column);
                }
            }
        }
        return columns;
    }

    /**
     * 查找主键字段，若未显式配置主键则回退为首列。
     */
    private static GenTableColumn findPkColumn(List<GenTableColumn> columns) {
        for (GenTableColumn column : columns) {
            if (column.isPk()) {
                return column;
            }
        }
        return columns.get(0);
    }

    /**
     * 根据模板集合写出所有代码文件。
     */
    private static void writeCode(GenTable table, Path outputDir) throws IOException {
        VelocityContext context = VelocityUtils.prepareContext(table);
        for (TemplateMeta template : VelocityUtils.getTemplateList()) {
            try (StringWriter sw = new StringWriter()) {
                org.apache.velocity.app.Velocity.evaluate(context, sw, template.name(), template.content());
                Path file = outputDir.resolve(VelocityUtils.getFileName(template, table));
                Files.createDirectories(file.getParent());
                Files.writeString(file, sw.toString(), StandardCharsets.UTF_8);
            }
        }
    }
}
