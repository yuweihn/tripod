package com.yuweix.assist4j.core;


import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


/**
 * @author yuwei
 */
public abstract class ActionUtil {
	/**
	 * 获得客户端IP
	 * @return
	 */
	public static String getRequestIP() {
		HttpServletRequest request = getRequest();
		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("x-forwarded-for");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		if (ip == null) {
			return null;
		}
		return ip.split(",")[0];
	}

	/**
	 * 获取本机内网IP
	 */
	public static String getLocalInnerIP() {
		String reqIp = null;
		try {
			reqIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return reqIp;
	}

	/**
	 * 获取本机外网IP
	 */
	public static String getLocalOuterIP() {
		String netIp = null;

		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
			return null;
		}
		InetAddress ip = null;
		boolean found = false;// 是否找到外网IP
		while (netInterfaces != null && netInterfaces.hasMoreElements() && !found) {
			NetworkInterface ni = netInterfaces.nextElement();
			Enumeration<InetAddress> address = ni.getInetAddresses();
			while (address.hasMoreElements()) {
				ip = address.nextElement();
				if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {
					netIp = ip.getHostAddress();
					found = true;
					break;
				}
			}
		}

		if (netIp != null && !"".equals(netIp.trim())) {
			return netIp.trim();
		} else {
			return null;
		}
	}

	/**
	 * 获得Request对象
	 * @return
	 */
	public static HttpServletRequest getRequest() {
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return sra == null ? null : sra.getRequest();
	}

	/**
	 * 获得Response对象
	 * @return
	 */
	public static HttpServletResponse getResponse() {
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return sra == null ? null : sra.getResponse();
	}

	/**
	 * 将站点URL存入ServletContext中
	 */
	public static void addContextPath() {
		addContextPath(getRequest());
	}

	/**
	 * 将站点URL存入ServletContext中
	 */
	public static void addContextPath(HttpServletRequest request) {
		try {
			URI uri = new URI(request.getRequestURL().toString());
			URI effectiveURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
			String contextPath = effectiveURI.toString().replaceFirst("http:", "").replaceFirst("https:", "");
			request.getServletContext().setAttribute(Constant.CONTEXT_PATH_KEY, contextPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取站点URL
	 * @return
	 */
	public static String getContextPath() {
		return getContextPath(getRequest());
	}

	/**
	 * 获取站点URL
	 * @return
	 */
	public static String getContextPath(HttpServletRequest request) {
		return (String) request.getServletContext().getAttribute(Constant.CONTEXT_PATH_KEY);
	}

	/**
	 * 将静态资源地址存入ServletContext中
	 */
	public static void addStaticPath(String staticPath) {
		addStaticPath(getRequest(), staticPath);
	}

	/**
	 * 将静态资源地址存入ServletContext中
	 */
	public static void addStaticPath(HttpServletRequest request, String staticPath) {
		request.getServletContext().setAttribute(Constant.STATIC_PATH_KEY, staticPath);
	}

	/**
	 * 获取静态资源地址
	 * @return
	 */
	public static String getStaticPath() {
		return getStaticPath(getRequest());
	}

	/**
	 * 获取静态资源地址
	 * @return
	 */
	public static String getStaticPath(HttpServletRequest request) {
		return (String) request.getServletContext().getAttribute(Constant.STATIC_PATH_KEY);
	}

	public static void output(String str) {
		output(str.getBytes(StandardCharsets.UTF_8), "text/html");
	}

	public static void output(byte[] content, String contentType) {
		output(content, contentType, null);
	}

	/**
	 * contentType 不含字符集
	 */
	public static void output(byte[] content, String contentType, Map<String, String> headers) {
		HttpServletResponse response = getResponse();
		assert response != null;
		response.setContentType(contentType + "; charset=" + Constant.ENCODING_UTF_8);
		response.setHeader("Cache-Control", "no-cache, no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);

		if (headers != null) {
			for (Map.Entry<String, String> entry: headers.entrySet()) {
				response.setHeader(entry.getKey(), entry.getValue());
			}
		}

		try {
			response.getOutputStream().write(content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 转发到指定URL
	 * @param url
	 * @return
	 */
	public static String forward(String url) {
		return "forward:" + url;
	}

	/**
	 * 重定向到指定URL
	 * @param url
	 * @return
	 */
	public static String redirect(String url) {
		return "redirect:" + url;
	}
}
