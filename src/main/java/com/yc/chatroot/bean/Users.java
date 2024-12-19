package com.yc.chatroot.bean;

import lombok.Data;

@Data
public class Users {
    Integer uid;
    String uname;
    String pwd;
    String account;
    String email;
    String verifyCode;

}
