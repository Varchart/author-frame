package com.author.commons.configs.servlets;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.alibaba.druid.support.http.StatViewServlet;

@WebServlet(
    urlPatterns = {"/druid/*"},
    initParams = {
        @WebInitParam(name ="allow", value =""),
        @WebInitParam(name ="loginUsername", value ="admin"),
        @WebInitParam(name ="loginPassword", value ="admin"),
        @WebInitParam(name ="resetEnable", value ="false")
        }
    )
public class DruidStatViewServlet extends StatViewServlet implements Servlet {
  private static final long serialVersionUID = 3096059293508159459L;
}
