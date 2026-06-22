package com.zkhf.epmis.auth.system.domain.vo;

import com.zkhf.epmis.core.utils.StringUtils;

/**
 * 路由显示信息
 */
public class MetaVo {
    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    private String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     */
    private boolean noCache;

    /**
     * 内链地址（http(s)://开头）
     */
    private String link;

    private String outPutCode;

    private String mnNum;

    private String entCode;

    public MetaVo() {
    }

    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
        setOutPutCodeByTitle(title);
    }

    public MetaVo(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        setOutPutCodeByTitle(title);
    }

    public MetaVo(String title, String icon, String link) {
        this.title = title;
        this.icon = icon;
        this.link = link;
        setOutPutCodeByTitle(title);
    }

    public MetaVo(String title, String icon, boolean noCache, String link) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        if (StringUtils.ishttp(link)) {
            this.link = link;
        }
        setOutPutCodeByTitle(title);
    }

    private void setOutPutCodeByTitle(String title) {
        if (StringUtils.startsWith(title, "outPutCode")) {
            String[] split = title.split("-");
            if (split.length > 1 && StringUtils.isNotEmpty(split[1])) {
                this.outPutCode = split[1];
            }
            if (split.length > 2 && StringUtils.isNotEmpty(split[2])) {
                this.title = split[2];
            }
            if (split.length > 3 && StringUtils.isNotEmpty(split[3])) {
                this.mnNum = split[3];
            }
            if (split.length > 4 && StringUtils.isNotEmpty(split[4])) {
                this.entCode = split[4];
            }

        }
    }

    public String getOutPutCode() {
        return outPutCode;
    }

    public void setOutPutCode(String outPutCode) {
        this.outPutCode = outPutCode;
    }

    public boolean isNoCache() {
        return noCache;
    }

    public void setNoCache(boolean noCache) {
        this.noCache = noCache;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMnNum() {
        return mnNum;
    }

    public void setMnNum(String mnNum) {
        this.mnNum = mnNum;
    }

    public String getEntCode() {
        return entCode;
    }

    public void setEntCode(String entCode) {
        this.entCode = entCode;
    }
}
