package com.yuweix.tripod.http.request;


import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yuweix.tripod.http.HttpConstant;
import com.yuweix.tripod.http.HttpMethod;
import com.yuweix.tripod.http.response.ErrorHttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import com.yuweix.tripod.http.response.HttpResponse;


/**
 * 表单请求
 * @author yuwei
 */
public class HttpFormRequest extends AbstractHttpRequest<HttpFormRequest> {
	private List<FormField> fieldList;


	private HttpFormRequest() {
		super();
		fieldList = new ArrayList<>();
		method(HttpMethod.GET);
	}
	public static HttpFormRequest create() {
		return new HttpFormRequest();
	}


	public HttpFormRequest fieldList(Map<String, ?> map) {
		if (map == null || map.isEmpty()) {
			return this;
		}

		List<FormField> fieldList = new ArrayList<>();
		for (Map.Entry<String, ?> entry: map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (key == null || value == null) {
				continue;
			}
			fieldList.add(new FormField(key, value));
		}
		return fieldList(fieldList);
	}
	public HttpFormRequest fieldList(List<FormField> fieldList) {
		this.fieldList.clear();
		this.fieldList.addAll(fieldList);
		return this;
	}
	public HttpFormRequest addField(String key, String value) {
		if (key == null || "".equals(key) || value == null || "".equals(value)) {
			return this;
		}

		fieldList.add(new FormField(key, value));
		return this;
	}


	private <B>HttpResponse<B> doGet() {
		try {
			URIBuilder uriBuilder = new URIBuilder(this.getUrl());
			if (fieldList != null && fieldList.size() > 0) {
				for (FormField ff: fieldList) {
					String k = ff.getKey();
					String v = ff.getValue();
					if (k == null || "".equals(k) || v == null || "".equals(v)) {
						continue;
					}
					uriBuilder.setParameter(k, v);
				}
			}

			URI uri = uriBuilder.build();
			this.setHttpUriRequest(new HttpGet(uri));
			return execute0();
		} catch (URISyntaxException e) {
			return new ErrorHttpResponse<>(HttpStatus.SC_NOT_FOUND, e.getMessage());
		}
	}

	private static List<NameValuePair> toNameValuePairList(List<FormField> fieldList) {
		List<NameValuePair> list = new ArrayList<>();
		if (fieldList == null || fieldList.size() <= 0) {
			return list;
		}

		for (FormField ff: fieldList) {
			String k = ff.getKey();
			String v = ff.getValue();
			if (k == null || "".equals(k) || v == null || "".equals(v)) {
				continue;
			}
			list.add(new BasicNameValuePair(k, v));
		}
		return list;
	}

	@Override
	public <B>HttpResponse<B> execute() {
		HttpMethod method = getMethod();
		if (method == null || HttpMethod.GET.equals(method)) {
			return doGet();
		} else {
			String charset = getCharset();
			charset = charset != null ? charset : HttpConstant.ENCODING_UTF_8;

			List<NameValuePair> list = toNameValuePairList(fieldList);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, Charset.forName(charset));

			HttpEntityEnclosingRequestBase requestBase = getRequestBase();
			requestBase.setEntity(entity);
			this.setHttpUriRequest(requestBase);
			return execute0();
		}
	}

	@Override
	protected ContentType getHeaderContentType() {
		String charset = getCharset();
		charset = charset != null ? charset : HttpConstant.ENCODING_UTF_8;
		return ContentType.APPLICATION_FORM_URLENCODED.withCharset(Charset.forName(charset));
	}
}
