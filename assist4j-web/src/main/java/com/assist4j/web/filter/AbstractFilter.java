package com.assist4j.web.filter;


import com.assist4j.core.ActionUtil;
import com.assist4j.core.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * @author yuwei
 */
public abstract class AbstractFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(AbstractFilter.class);


	private static final String DEFAULT_METHOD_PARAM = "_method";
	private static final String DEFAULT_ENCODING = Constant.ENCODING_UTF_8;
	private static final String DEFAULT_STATIC_PATH = "/static/";

	private String methodParam = DEFAULT_METHOD_PARAM;
	private String encoding = DEFAULT_ENCODING;
	private String staticPath = DEFAULT_STATIC_PATH;
	private String protocol = null;


	public void setMethodParam(String methodParam) {
		Assert.hasText(methodParam, "'methodParam' is required.");
		this.methodParam = methodParam;
	}

	public void setEncoding(String encoding) {
		Assert.hasText(encoding, "'encoding' is required.");
		this.encoding = encoding;
	}

	public void setStaticPath(String staticPath) {
		Assert.hasText(staticPath, "'staticPath' is required.");
		this.staticPath = staticPath;
	}

	public void setProtocol(String protocol) {
		Assert.hasText(protocol, "'protocol' is required.");
		this.protocol = protocol;
	}


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		long startTimeMillis = System.currentTimeMillis();
		beforeFilter(request, response);

		HttpServletRequest requestWrapper = wrapMethod(request);
		printRequest(requestWrapper);
		setCharacterEncoding(requestWrapper, response);
		setContextPath(requestWrapper);
		setAccessControl(requestWrapper, response);

		filterChain.doFilter(requestWrapper, response);

		afterFilter(requestWrapper, response);
		long endTimeMillis = System.currentTimeMillis();
		log.info("Status: {}. Time Cost: {} ms.", response.getStatus(), endTimeMillis - startTimeMillis);
	}



	/**
	 * 浏览器不支持put,delete等method,由该filter将/service?_method=delete转换为标准的http delete方法
	 **/
	private HttpServletRequest wrapMethod(HttpServletRequest request) {
		String paramValue = request.getParameter(methodParam);
		if ("POST".equalsIgnoreCase(request.getMethod()) && paramValue != null && !"".equals(paramValue.trim())) {
			String method = paramValue.trim().toUpperCase(Locale.ENGLISH);
			return new HttpServletRequestWrapper(request) {
				@Override
				public String getMethod() {
					return method;
				}
			};
		} else {
			return request;
		}
	}

	/**
	 * 打印请求参数
	 */
	protected void printRequest(HttpServletRequest request) {
		String ip = ActionUtil.getRequestIP();
		String url = request.getRequestURL().toString();
		try {
			url = URLDecoder.decode(url, Constant.ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
		}
		String method = request.getMethod().toLowerCase();
		Map<String, String[]> params = request.getParameterMap();

		if (params == null || params.isEmpty()) {
			log.info("ip: {}, method: {}, url: {}", ip, method, url);
		} else {
			log.info("ip: {}, method: {}, url: {}, params: {}", ip, method, url, params);
		}
	}

	/**
	 * 设置字符集
	 **/
	protected void setCharacterEncoding(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setCharacterEncoding(encoding);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		response.setCharacterEncoding(encoding);
	}

	/**
	 * 将站点域名和static资源地址存入context
	 **/
	protected void setContextPath(HttpServletRequest request) {
		ActionUtil.addContextPath(request, protocol);
		ActionUtil.addStaticPath(request, staticPath);
	}

	/**
	 * 跨域请求设置
	 */
	protected void setAccessControl(HttpServletRequest request, HttpServletResponse response) {
		String referrer = request.getHeader("Referer");
		if (referrer != null) {
			try {
				referrer = URLDecoder.decode(referrer, Constant.ENCODING_UTF_8);
			} catch (Exception e) {
				log.error("", e);
			}
			log.info("Referrer: {}", referrer);
		}

		if (!response.containsHeader("Access-Control-Allow-Origin")) {
			String requestOrigin = request.getHeader("origin");
			if (requestOrigin != null && !"".equals(requestOrigin.trim()) && !"null".equals(requestOrigin.trim())) {
				response.setHeader("Access-Control-Allow-Origin", requestOrigin);
				response.setHeader("Access-Control-Allow-Credentials", "true");
			} else {
				response.setHeader("Access-Control-Allow-Origin", "*");
			}
			response.setHeader("Access-Control-Max-Age", "3600");
		}

		if (!response.containsHeader("Access-Control-Allow-Methods")) {
			List<HttpMethod> allowedMethods = Arrays.asList(HttpMethod.values());
			response.setHeader("Access-Control-Allow-Methods", StringUtils.collectionToCommaDelimitedString(allowedMethods));
		}
		if (!response.containsHeader("Access-Control-Allow-Headers")) {
			response.setHeader("Access-Control-Allow-Headers", "*");
		}
	}


	protected void beforeFilter(HttpServletRequest request, HttpServletResponse response) {

	}

	protected void afterFilter(HttpServletRequest request, HttpServletResponse response) {

	}
}
