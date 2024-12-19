package com.yc.chatroot.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 对ResultSet中一行数据的处理的处理接口
 */
@FunctionalInterface
public interface RowMapper<T> {
	/**
	 * 将ResultSet中第rowNum行的一条数据  处理成一个T对象
	 * @param rs： 结果集
	 * @param rowNum:是结果集中第几行》 
	 * @return  返回这一行对应的一条数据
	 */
	T mapRow(  ResultSet rs, int rowNum  ) throws SQLException;

}
