package com.yc.chatroot.web.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

/*
*
* */
public abstract class BaseServlet extends HttpServlet {

    protected <T> T parseRequestParamToT(HttpServletRequest req, Class<T> cls) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        T t =cls.newInstance();
        Enumeration<String> names =  req.getParameterNames();

        while(names.hasMoreElements()){
                String name = names.nextElement();
                String value = req.getParameter(name);

                String setMethodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);

                // 反射调用set方法
                Method[] methode = cls.getDeclaredMethods();

                for (Method m : methode) {
                    if (m.getName().equals(setMethodName) ){
                        //获取参数类型名
                        String pType = m.getParameterTypes()[0].getName();
                        if("int".equals(pType) || "java.lang.Integer".equals(pType)){
                            int i = Integer.parseInt(value);
                            m.invoke(t, i);
                        }else if("double".equals(pType) || "java.lang.Double".equals(pType)){
                            double i = Double.parseDouble(value);
                            m.invoke(t, i);

                        }else if("float".equals(pType) || "java.lang.Float".equals(pType)){
                            float i = Float.parseFloat(value);
                            m.invoke(t, i);

                        }else if("shot".equals(pType) || "java.lang.Short".equals(pType)){
                            short i = Short.parseShort(value);
                            m.invoke(t, i);

                        }else if("long".equals(pType) || "java.lang.Long".equals(pType)){
                            long i = Long.parseLong(value);
                            m.invoke(t, i);

                        }else if("boolean".equals(pType) || "java.lang.Boolean".equals(pType)){
                            boolean i = Boolean.parseBoolean(value);
                            m.invoke(t, i);

                        }else if("byte".equals(pType) || "java.lang.Byte".equals(pType)){
                            byte i = Byte.parseByte(value);
                            m.invoke(t, i);
                        }else{
                            m.invoke(t, value);
                        }
                        break;

                    }
                }
        }

        return t;
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置CORS头，允许跨域请求
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Max-Age", "86400");
        String op = req.getParameter("op");
        Method methode = null;

        try {
            methode = this.getClass().getDeclaredMethod(op, HttpServletRequest.class, HttpServletResponse.class);
            if(methode == null){
                resp.sendError(404);
                resp.sendError(404, "Method not found for operation: " + op);
            }else{
                    Object rr = methode.invoke(this, req, resp);
                    writeJson(rr, resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500);
        }

    }

    protected void writeJson(Object obj, HttpServletResponse resp) throws IOException {
        Gson gson = new Gson();
        String jsonString = gson.toJson(obj);
        resp.setContentType("text/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.print(jsonString);
        out.flush();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
