package com.author.commons.configs;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
/* @Configuration */
@SuppressWarnings("all")
public class FilterConfig {
	@Bean
	public FilterRegistrationBean modifyParametersFilter() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new ModifyParametersFilter());
		registration.addUrlPatterns("/*"); // 拦截路径
		registration.setName("modifyParametersFilter"); // 拦截器名称
		registration.setOrder(1); // 顺序
		return registration;
	}

	class ModifyParametersFilter extends OncePerRequestFilter {
		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
			// 修改请求头
			Map<String, String> map = new HashMap<String, String>();
			modifyHeaders(map, request);

			// 修改cookie
			ModifyHttpServletRequestWrapper requestWrapper = new ModifyHttpServletRequestWrapper(request);
			String token = request.getHeader("token");
			if (token != null && !"".equals(token)) {
				requestWrapper.putCookie("SHIROSESSIONID", token);
			}

			// finish
			filterChain.doFilter(requestWrapper, response);
		}
	}

	class ModifyHttpServletRequestWrapper extends HttpServletRequestWrapper {
		private Map<String, String> mapCookies;

		ModifyHttpServletRequestWrapper(HttpServletRequest request) {
			super(request);
			this.mapCookies = new HashMap<String, String>();
		}

		public void putCookie(String name, String value) {
			this.mapCookies.put(name, value);
		}

		public Cookie[] getCookies() {
			HttpServletRequest request = (HttpServletRequest) getRequest();
			Cookie[] cookies = request.getCookies();
			if (mapCookies == null || mapCookies.isEmpty()) {
				return cookies;
			}
			if (cookies == null || cookies.length == 0) {
				List<Cookie> cookieList = new LinkedList<Cookie>();
				for (Map.Entry<String, String> entry : mapCookies.entrySet()) {
					String key = entry.getKey();
					if (key != null && !"".equals(key)) {
						cookieList.add(new Cookie(key, entry.getValue()));
					}
				}
				if (cookieList.isEmpty()) {
					return cookies;
				}
				return cookieList.toArray(new Cookie[cookieList.size()]);
			} else {
				List<Cookie> cookieList = new ArrayList<Cookie>(Arrays.asList(cookies));
				for (Map.Entry<String, String> entry : mapCookies.entrySet()) {
					String key = entry.getKey();
					if (key != null && !"".equals(key)) {
						for (int i = 0; i < cookieList.size(); i++) {
							if (cookieList.get(i).getName().equals(key)) {
								cookieList.remove(i);
							}
						}
						cookieList.add(new Cookie(key, entry.getValue()));
					}
				}
				return cookieList.toArray(new Cookie[cookieList.size()]);
			}
		}
	}

	private void modifyHeaders(Map<String, String> headerses, HttpServletRequest request) {
		if (headerses == null || headerses.isEmpty()) {
			return;
		}
		Class<? extends HttpServletRequest> requestClass = request.getClass();
		try {
			Field request1 = requestClass.getDeclaredField("request");
			request1.setAccessible(true);
			Object o = request1.get(request);
			Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
			coyoteRequest.setAccessible(true);
			Object o1 = coyoteRequest.get(o);
			Field headers = o1.getClass().getDeclaredField("headers");
			headers.setAccessible(true);
			MimeHeaders o2 = (MimeHeaders) headers.get(o1);
			for (Map.Entry<String, String> entry : headerses.entrySet()) {
				o2.removeHeader(entry.getKey());
				o2.addValue(entry.getKey()).setString(entry.getValue());
			}
		} catch (Throwable ex) {
			log.error("请求头更新失败:{}", ex.getMessage());
		}
	}
}
