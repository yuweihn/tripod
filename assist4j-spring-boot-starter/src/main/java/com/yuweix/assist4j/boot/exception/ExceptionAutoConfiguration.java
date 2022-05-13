package com.yuweix.assist4j.boot.exception;


import com.yuweix.assist4j.core.Response;
import com.yuweix.assist4j.core.exception.ExceptionHandler;
import com.yuweix.assist4j.core.exception.ExceptionViewResolver;
import com.yuweix.assist4j.core.json.Json;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractView;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author yuwei
 */
@Configuration
@ConditionalOnProperty(name = "assist4j.boot.exception.enabled")
public class ExceptionAutoConfiguration {
	@Configuration
	@ConditionalOnProperty(name = "assist4j.boot.exception.handler.enabled", matchIfMissing = true)
	protected static class ErrorControllerConfiguration {
		@Value("${assist4j.boot.exception.errorCode:}")
		private String errorCode;

		@Controller
		public class ErrorController {
			@Resource
			private Json json;

			@RequestMapping(value = { "/error", "/error/**" })
			@ResponseBody
			public String toErrorPage(HttpServletResponse response) {
				int status = response.getStatus();
				HttpStatus httpStatus = HttpStatus.valueOf(status);

				Response<String, Void> resp = new Response<>(errorCode == null || "".equals(errorCode) ? "" + status : errorCode
						, httpStatus.getReasonPhrase() + "[" + status + "]");
				return json.toJSONString(resp);
			}
		}

		@ConditionalOnMissingBean(ExceptionViewResolver.class)
		@Bean
		public ExceptionViewResolver exceptionViewResolver(Json json) {
			return new ExceptionViewResolver() {
				@SuppressWarnings("unchecked")
				@Override
				public ModelAndView createView(String content) {
					AbstractView view = new AbstractView() {
						@Override
						protected void renderMergedOutputModel(Map<String, Object> map, HttpServletRequest req, HttpServletResponse resp) throws Exception {
							resp.setContentType("application/json; charset=" + StandardCharsets.UTF_8);
							ServletOutputStream out = resp.getOutputStream();
							out.write(json.toJSONString(map).getBytes(StandardCharsets.UTF_8));
							out.flush();
						}
					};
					String text = json.toJSONString(
							new Response<String, Void>(errorCode == null || "".equals(errorCode) ? "500" : errorCode, content));
					Map<String, Object> attributes = json.parseObject(text, Map.class);
					view.setAttributesMap(attributes);
					return new ModelAndView(view);
				}
			};
		}
	}

	@ConditionalOnMissingBean(ClassMessagePair.class)
	@ConfigurationProperties(prefix = "assist4j.boot.exception", ignoreUnknownFields = true)
	@Bean
	public ClassMessagePair classMessagePair() {
		return new ClassMessagePair() {
			private Map<String, String> map = new HashMap<>();

			@Override
			public Map<String, String> getDefaultMessage() {
				return map;
			}
		};
	}

	@ConditionalOnMissingBean(ExceptionHandler.class)
	@Bean
	public ExceptionHandler exceptionHandler(ClassMessagePair classMessagePair, ExceptionViewResolver viewResolver
			, @Value("${assist4j.boot.exception.showExceptionName:false}") boolean showExceptionName) {
		Map<Class<?>, String> errorMsgMap = new HashMap<>();

		Map<String, String> classMessageMap = classMessagePair.getDefaultMessage();
		if (classMessageMap != null) {
			Set<Map.Entry<String, String>> entrySet = classMessageMap.entrySet();
			for (Map.Entry<String, String> entry : entrySet) {
				try {
					errorMsgMap.put(Class.forName(entry.getKey()), entry.getValue());
				} catch (ClassNotFoundException ignored) {
				}
			}
		}

		ExceptionHandler exceptionHandler = new ExceptionHandler();
		exceptionHandler.setViewResolver(viewResolver);
		exceptionHandler.setErrorMsgMap(errorMsgMap);
		exceptionHandler.setShowExceptionName(showExceptionName);
		return exceptionHandler;
	}
}
