package com.guanxuege.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * 可写请求头包装：用于在 JWT 过滤器中向后续 Controller 注入 X-User-Id 头，
 * 使单体应用内沿用与网关一致的「从请求头取当前用户」契约。
 */
public class MutableRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders = new HashMap<>();

    public MutableRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void putHeader(String name, String value) {
        customHeaders.put(name.toLowerCase(), value);
    }

    @Override
    public String getHeader(String name) {
        String v = customHeaders.get(name.toLowerCase());
        if (v != null) {
            return v;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String v = customHeaders.get(name.toLowerCase());
        if (v != null) {
            return Collections.enumeration(Collections.singletonList(v));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>();
        names.addAll(customHeaders.keySet());
        Enumeration<String> e = super.getHeaderNames();
        while (e != null && e.hasMoreElements()) {
            names.add(e.nextElement());
        }
        return Collections.enumeration(new ArrayList<>(names));
    }
}
