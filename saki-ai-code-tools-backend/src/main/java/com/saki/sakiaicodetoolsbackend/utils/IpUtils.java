package com.saki.sakiaicodetoolsbackend.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author saki酱
 * @version 1.0
 * @since 2025-10-17
 */
public class IpUtils {

    /**
     * 获取客户端IP地址
     * @param request HttpServletRequest
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果通过多个代理服务器，X-Forwarded-For 可能会包含多个 IP 地址，取第一个非unknown的
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.split(",")[0];
        }

        return ip;
    }
}
