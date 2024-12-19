package com.yc.chatroot.web.controller;


import com.yc.chatroot.bean.Users;
import com.yc.chatroot.dao.DbHelper;
import com.yc.chatroot.model.ResponseResult;
import com.yc.chatroot.utils.EmailVerification;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j;

import javax.mail.MessagingException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Log4j
@WebServlet("/login.action")
public class LoginServlet extends BaseServlet{

    private String code;
    public ResponseResult login(HttpServletRequest req, HttpServletResponse resp ) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Users users =super.parseRequestParamToT(req,Users.class);
        System.out.println(users);
        if(users.getAccount() == null || users.getAccount().equals("")){
            return ResponseResult.error().setData("账号不能为空");
        }
        HttpSession session = req.getSession();
        DbHelper db = new DbHelper();
        String sql = "select * from users where account=? and pwd=?";
        Users uu = db.selectOne((rs,rownum)->{
            Users user = new Users();
            user.setUname(rs.getString("uname"));
            user.setAccount(rs.getString("account"));
            user.setPwd(rs.getString("pwd"));
            session.setAttribute("user",user);
            return user;
        },sql,users.getAccount(),users.getPwd());

        if(uu == null){
            return ResponseResult.error().setData("账号或密码错误");
        }


        return ResponseResult.ok().setData("登录成功");
    }

    public ResponseResult checkLogin(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        HttpSession session = req.getSession();
        if(session.getAttribute("user") == null){
            return ResponseResult.error().setData("请先登录");
        }
        Users resuser = (Users) session.getAttribute("user");
        resuser.setPwd("别偷看");
        return ResponseResult.ok().setData(session.getAttribute("user"));
    }

    public ResponseResult register(HttpServletRequest req, HttpServletResponse resp ) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Users rusers =super.parseRequestParamToT(req,Users.class);
        log.info(rusers);
        if(rusers.getVerifyCode() == null || rusers.getVerifyCode().equals("")){
            return ResponseResult.error().setData("验证码不能为空");
        }
        if(!rusers.getVerifyCode().equals(code) ){
            return ResponseResult.error().setData("验证码错误");
        }
        DbHelper db = new DbHelper();
        String sql = "insert into users(uname,account,pwd,email) values(?,?,?,?)";
        int result = db.doUpdate(sql,rusers.getUname(),rusers.getAccount(),rusers.getPwd(),rusers.getEmail());
        if(result == 0){
            return ResponseResult.error().setData("注册失败");
        }
        return ResponseResult.ok();
    }


    public ResponseResult sendEmail(HttpServletRequest req, HttpServletResponse resp ) throws IOException {
        try {
            String email = req.getParameter("email");
            EmailVerification ev = new EmailVerification("1841248198@qq.com",email);
            code = ev.sendVerificationCode();
            log.info("验证码为："+code);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseResult.ok();
    }

    public ResponseResult logout(HttpServletRequest req, HttpServletResponse resp )
    {
        HttpSession session = req.getSession();
        session.invalidate();
        return ResponseResult.ok();
    }
}
