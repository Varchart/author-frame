package com.author.commons.configs.filters;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

import com.alibaba.druid.support.http.WebStatFilter;

@WebFilter(
    filterName = "druidWebStatFilter", urlPatterns = { "/*" },
    initParams = {@WebInitParam(name = "exclusions",value = "*.js,*.jpg,*.png,*.gif,*.ico,*.css,/druid/*")}
    )
public class DruidStatFilter extends WebStatFilter {
	
}
