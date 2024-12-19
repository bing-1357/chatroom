package com.yc.chatroot.web.filters;

import com.yc.chatroot.model.ResponseResult;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j;

import java.io.IOException;

@Log4j
//@WebFilter(value = {"/"})
public class RightFilter extends BaseFilter{
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        if(session.getAttribute("user")==null){
            log.info("用户未登录");
            ResponseResult result = ResponseResult.error().setData("用户未登录");
        }else{
            log.info("用户已登录");
            chain.doFilter(request,response);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
    }
}
