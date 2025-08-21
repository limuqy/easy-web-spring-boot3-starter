package io.github.limuqy.easyweb.core.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class ServletUtil {

    private static final String SUFFIX_XLSX = ".xlsx";

    /**
     * 获取HttpServletResponse
     */
    public static HttpServletResponse getHttpServletResponse() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return ((ServletRequestAttributes) requestAttributes).getResponse();
        }
        return null;
    }

    /**
     * HttpServletRequest
     */
    public static HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    /**
     * 请求是否移动端
     */
    public static Boolean isMobileDevice() {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        String userAgent = null;
        if (httpServletRequest != null) {
            userAgent = httpServletRequest.getHeader("User-Agent");
        }
        // 目前先暂时定这些作为移动端
        return userAgent != null &&
                (userAgent.contains("Mobile") ||
                        userAgent.contains("Android") ||
                        userAgent.contains("webOS") ||
                        userAgent.contains("iPhone") ||
                        userAgent.contains("iPad") ||
                        userAgent.contains("iPod"));
    }

    public static Map<String, String> getHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        HttpServletRequest request = getRequest();
        if (request == null) {
            return headerMap;
        }
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            headerMap.put(name, value);
        }
        return headerMap;
    }

    public static HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return null;
        }
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }


    /**
     * 获取本机的ip
     */
    public static String getLocalIp() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("getLocalHost异常！", e);
        }
        return addr == null ? UUID.randomUUID().toString() : addr.getHostAddress();
    }


    /**
     * 公共处理response
     *
     * @param fileName 导出文件名称
     * @param response http响应
     */
    public static void processResponse(String fileName, HttpServletResponse response) {
        fileName = fileName.endsWith(SUFFIX_XLSX) ? fileName : fileName + SUFFIX_XLSX;
        // 这里URLEncoder.encode可以防止中文乱码
        String fileTitle = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("content-disposition", "attachment; filename*=utf-8''" + fileTitle);
        // 设置二进制传输文件
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setHeader("Pragma", "public");
        response.setHeader("Cache-Control", "public");
    }

}
