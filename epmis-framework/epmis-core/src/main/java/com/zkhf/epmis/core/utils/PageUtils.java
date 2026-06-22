package com.zkhf.epmis.core.utils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zkhf.epmis.core.constant.HttpStatus;
import com.zkhf.epmis.core.domain.AjaxResult;
import com.zkhf.epmis.core.page.PageDomain;
import com.zkhf.epmis.core.page.TableDataInfo;
import com.zkhf.epmis.core.page.TableSupport;
import com.zkhf.epmis.core.text.Convert;
import com.zkhf.epmis.core.utils.sql.SqlUtil;

import java.util.List;

/**
 * 分页工具类
 */
public class PageUtils {

    /**
     * 获取页大小
     */
    public static Integer getPageSize(Integer defaultValue) {
        return Convert.toInt(ServletUtils.getParameter("pageSize"), defaultValue);
    }

    /**
     * 获取页码
     */
    public static Integer getPageNum(Integer defaultValue) {
        return Convert.toInt(ServletUtils.getParameter("pageNum"), defaultValue);
    }

    /**
     * 获取偏移量
     * 为了上一页的最后一条，偏移量再往前移一位
     */
    public static Long getOffset(int pageNum, int pageSize, long count) {
        // 总数
        long offset = (long) (pageNum - 1) * pageSize;
        if (offset > count) { // 超出取最后一页
            offset = count - (count % pageSize);
        } else if (offset == count){ // 最后一页时还是返回最后一页
            offset = count - pageSize;
        }
        return offset - 1;
    }

    /**
     * 设置请求分页数据
     */
    public static void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 设置请求分页数据
     * 无分页参数时不分页
     *
     * @return false:未分页
     */
    public static boolean startPageCheckExists() {
        PageDomain pageDomain = TableSupport.getPageDomain(null, null);
        if (null == pageDomain.getPageNum() || null == pageDomain.getPageSize()) {
            return false;
        }
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
        return true;
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage() {
        PageHelper.clearPage();
    }

    /**
     * 响应请求分页数据
     */
    public static TableDataInfo getDataTable(List<?> list) {
        return getDataTable(list, true);
    }

    /**
     * 响应请求分页数据
     */
    public static TableDataInfo getDataTable(List<?> list, boolean page) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        if (page) {
            rspData.setTotal(new PageInfo<>(list).getTotal());
            // 清理分页参数
            clearPage();
        } else {
            rspData.setTotal(list.size());
        }
        return rspData;
    }

    /**
     * 响应请求分页数据
     */
    public static AjaxResult getAjaxResult(List<?> list, boolean page) {
        AjaxResult result = AjaxResult.success();
        result.put("data", list);
        if (page) {
            result.put("total", new PageInfo<>(list).getTotal());
            // 清理分页参数
            clearPage();
        } else {
            result.put("total", list.size());
        }
        return result;
    }
}
