package com.yc.chatroot.web.filters;

import com.google.gson.Gson;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public abstract class BaseFilter implements Filter {
    protected void writeJson(Object obj, HttpServletResponse resp) throws IOException {
        Gson gson = new Gson();
        String jsonString = gson.toJson(obj);
        resp.setContentType("text/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.print(jsonString);
        out.flush();
    }
}
