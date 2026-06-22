package com.zkhf.epmis.platform.global;

import com.zkhf.epmis.core.constant.Constants;
import com.zkhf.epmis.core.constant.HttpStatus;
import com.zkhf.epmis.platform.base.domain.entity.SysEnt;
import com.zkhf.epmis.platform.base.domain.entity.SysRole;
import com.zkhf.epmis.platform.base.domain.entity.SysUser;
import com.zkhf.epmis.platform.base.domain.model.LoginUser;
import com.zkhf.epmis.core.exception.ServiceException;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GVarContainer
 * 线程全局变量容器类
 * 用于在一个线程内的不同类中传递需要使用的线程内的全局变量
 * 容器以线程的ID作为标记，用于区分不同线程的同一个变量
 */
public class GVarContainer {
    private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, Object>> container = new ConcurrentHashMap<>();

    /**
     * 根据当前线程的ID将需要传递的对象放置到ID对应的容器中
     *
     * @param name 需要被传递对象的标识, 之后可以根据标识取得此对象,标识不能为空
     * @param obj  需要被传递的对象, 对象不能为空
     */
    public static void setVar(String name, Object obj) {
        if (null == obj || null == name)
            return;
        Thread th = Thread.currentThread();
        ConcurrentHashMap<String, Object> varTab = container.get(th.getId());
        if (null == varTab) {
            varTab = new ConcurrentHashMap<>();
            varTab.put(name, obj);
        } else {
            varTab.put(name, obj);
        }
        container.put(th.getId(), varTab);
    }

    /**
     * 根据当前线程的ID取得标识所对应的对象，
     * 取得对象前必须确保调用过setVar设置对象
     *
     * @param name 对象对应的标识，须与在setVar时的相同,标识不能为空
     * @return 标识对应的对象，如果不存在返回null
     */
    public static Object getVar(String name) {
        if (null == name)
            return null;
        Thread th = Thread.currentThread();
        ConcurrentHashMap<String, Object> varTab = container.get(th.getId());
        if (null == varTab)
            return null;
        return varTab.get(name);
    }

    /**
     * 清除当前线程ID下对象标识对应的被传递的对象
     *
     * @param name 对象对应的标识，须与在setVar时的相同,标识不能为空
     */
    public static void removeVar(String name) {
        Thread th = Thread.currentThread();
        ConcurrentHashMap<String, Object> varTab = container.get(th.getId());
        if (null == varTab)
            return;
        varTab.remove(name);
    }

    /**
     * 清除当前线程ID下全部被传递的对象
     */
    public static void clearVar() {
        Thread th = Thread.currentThread();
        ConcurrentHashMap<String, Object> varTab = container.get(th.getId());
        if (varTab != null)
            varTab.clear();
        container.remove(th.getId());
    }

    /**
     * 用户ID
     **/
    public static Long getUserId() {
        try {
            return getLoginUser().getUserId();
        } catch (Exception e) {
            throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 用户名称
     **/
    public static String getUserName() {
        try {
            return getLoginUser().getUser().getUserName();
        } catch (Exception e) {
            throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户
     **/
    public static LoginUser getLoginUser() {
        try {
            return (LoginUser) getVar(Constants.LOGIN_USER_TV);
        } catch (Exception e) {
            throw new ServiceException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 获取用户权限
     **/
    public static List<String> getEntCodes() {
        List<String> entCodes = new ArrayList<>();
        List<SysEnt> entList = getLoginUser().getUser().getEntList();
        if (null == entList || entList.isEmpty()) {
            return entCodes;
        }
        entList.forEach( e -> {
            if (!entCodes.contains(e.getEntCode())) {
                entCodes.add(e.getEntCode());
            }
        });
        return entCodes;
    }

    /**
     * 判断不是管理员账号
     **/
    public static boolean isNotAdmin() {
        return !isAdmin();
    }

    /**
     * 判断是管理员账号
     **/
    public static boolean isAdmin() {
        SysUser user = getLoginUser().getUser();
        if (ObjectUtils.isEmpty(user.getRoles())) {
            return false;
        } else {
            List<SysRole> list = user.getRoles();
            for (SysRole sysRole : list) {
                if (sysRole.isAdmin()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是企业管理员账号：企业管理员
     **/
    public static boolean isEntAdmin() {
        SysUser user = getLoginUser().getUser();
        if (ObjectUtils.isEmpty(user.getRoles())) {
            return false;
        } else {
            List<SysRole> list = user.getRoles();
            for (SysRole sysRole : list) {
                if ("ent_admin".equals(sysRole.getRoleKey())) {
                    return true;
                }
            }
        }
        return false;
    }
}
