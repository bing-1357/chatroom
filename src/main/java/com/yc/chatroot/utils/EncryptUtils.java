package com.yc.chatroot.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密类
 * @author zy
 *
 */
public class EncryptUtils {
	//单向散列函数: MD5   ->  SHA
	
	//对一个字符串进行MD5的加密，   "字符串"=> "32位十六位进制数"
    public static String encryptToMD5(String str) {
        try {
        	//    此加密是jdk内置的   
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes());

            String hashedPwd = new BigInteger(1, md5.digest()).toString(16);
            return hashedPwd;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
    	  //md5散列函数. 
    	  System.out.println(  EncryptUtils.encryptToMD5("a"));
    	  System.out.println(  EncryptUtils.encryptToMD5("aaaabbbbbaaa"));
    	  //破解方式:  -> 暴力破解  -> 彩虹表
    	  
    	  //解决方案: 加盐:多次加密   -> 多次用不同的散列函数( MD5, SHA)  来加密
        System.out.println( EncryptUtils.encryptToMD5( EncryptUtils.encryptToMD5("a")));
    }
}
