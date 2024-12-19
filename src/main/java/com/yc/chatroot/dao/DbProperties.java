package com.yc.chatroot.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * 因为继承自Properties, 所以它有Properties功能
 * 同时要将这个类 Dbproperties 设计单例
 * @author zy
 *
 */
public class DbProperties extends Properties {
	private static  DbProperties instance;

	private DbProperties() {
		InputStream iis=DbProperties.class.getClassLoader().getResourceAsStream("db.properties");
		//在这里利用流加载数据  
		try {
			super.load(  iis );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized DbProperties getInstance() {
		if( instance==null ) {
			instance=new DbProperties();   
		}
		return instance;
	}
}
