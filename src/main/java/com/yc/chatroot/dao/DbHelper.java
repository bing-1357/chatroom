package com.yc.chatroot.dao;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;


/**
 * 数据库jdbc操作的帮助类,简化数据操作. 
 * @author zy
 *
 */
public class DbHelper {
	
	private static Logger log = Logger.getLogger(DbHelper.class.getName());
	
	static {
		// 静态块: 在jvm加载此类的字节码到jvm时，就运行，且只运行一次.
		try {
			Class.forName(DbProperties.getInstance().getProperty("driverClassName"));
		} catch (ClassNotFoundException e) {
			log.error("驱动加载失败,程序结束.");
			e.printStackTrace();
			System.exit(0); // 驱动加载不了，则系统退出.
		}
		log.info("驱动加载成功");
	}
	
	/**
	 * 获取与数据库的联接.
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		String url = DbProperties.getInstance().getProperty("url");
		String username = DbProperties.getInstance().getProperty("username");
		String password = DbProperties.getInstance().getProperty("password");
		Connection con = DriverManager.getConnection(url, username, password);
		return con;
	}
	
	/**
	 * 
	 * @param sql: 待执行的更新语句
	 * @param params: 更新语句? 占位符对应的值， 可以有0-n个,   按顺序
	 * @return 受影响的行数
	 */
	public int doUpdate(  String sql,  Object... params  ) {
		int r=-1;
		try(
				Connection con = getConnection();
				PreparedStatement stmt = con.prepareStatement(sql); 
		){
			//TODO: 如何处理 ?占位符, 有n个占位符》 
			doParams(    stmt, params);	
			r=stmt.executeUpdate();
		}catch(Exception ex) {
			ex.printStackTrace();
			log.error(  ex.getMessage() );
		}
		return r;
	}
	
	/**
	 * 处理 sql语句中的  参数值
	 * @param stmt
	 * @param params
	 * @throws SQLException 
	 */
	public void doParams(   PreparedStatement stmt,     Object... params   ) throws SQLException {
		//循环params
		if(  params!=null && params.length>0) {
			for( int i=0;i<params.length;i++ ) {
				stmt.setObject(   i+1   , params[i] );
			}
		}
	}
	
	//只查询一条数据 => 聚合函数查询和登录查询( 用户不能重复，所以最多只能查到一条数据 )
	public  <T> T selectOne(   RowMapper<T> rowMapper, String sql, Object... params) {
		T t=null;
		try(
				Connection con = getConnection();
				PreparedStatement stmt = con.prepareStatement(sql); 
		){
			//TODO: 如何处理 ?占位符, 有n个占位符》 
			doParams(    stmt, params);	
			ResultSet rs=stmt.executeQuery();
			int i=0;
			while(  rs.next() ) {
				  //对于rs这一行数据到底有多少列，每个列如何处理.   =>交给用户
				t=rowMapper.mapRow(   rs,   i   );
				i++;
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			log.error(  ex.getMessage() );
		}
		return t;
	}

	
	public  <T> List<T> select(   RowMapper<T> rowMapper, String sql, Object... params) {
		List<T> list=new ArrayList<>();
		try(
				Connection con = getConnection();
				PreparedStatement stmt = con.prepareStatement(sql); 
		){
			//TODO: 如何处理 ?占位符, 有n个占位符》 
			doParams(    stmt, params);	
			ResultSet rs=stmt.executeQuery();
			int i=0;
			while(  rs.next() ) {
				  //对于rs这一行数据到底有多少列，每个列如何处理.   =>交给用户
				T t=rowMapper.mapRow(   rs,   i   );
				i++;
				list.add( t );
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			log.error(  ex.getMessage() );
		}
		return list;
	}

	public List<Map<String, Object>> find(String sql, List<Object> params) {
		List<Map<String, Object>> list = new ArrayList<>();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = getConnection();
			ps = con.prepareStatement(sql);
			setParams(ps, params);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			String[] columns = new String[rsmd.getColumnCount()];
			for (int i = 0; i < columns.length; i++) {
				columns[i] = rsmd.getColumnName(i + 1);
			}

			while (rs.next()) {
				Map<String, Object> map = new HashMap<>();
				for (int i = 0; i < columns.length; i++) {
					map.put(columns[i].toLowerCase(), rs.getObject(columns[i]));
				}
				list.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeAll(con, ps);
		}
		return list;
	}
	
	public  Map<String,Object> selectOneMap(   String sql, Object... params) {
		Map<String,Object> map=new LinkedHashMap<>();
		try(
				Connection con = getConnection();
				PreparedStatement stmt = con.prepareStatement(sql); 
		){
			//TODO: 如何处理 ?占位符, 有n个占位符》 
			doParams(    stmt, params);	
			ResultSet rs=stmt.executeQuery();
			//1.查看rs中有多少列,取出每个列的列名，存到listColumnName
		    ResultSetMetaData rsmd=rs.getMetaData();
		    int columnCount=rsmd.getColumnCount();
		    List<String> listColumnName=new ArrayList<String>();
		    for( int i=1;i<=columnCount;i++) {
		    	String columnName=rsmd.getColumnName(   i ).toLowerCase();   //将数据查出业的列名转成小写. 
		    	listColumnName.add(  columnName);
		    }
			while(  rs.next() ) {
				//2. 循环这些这些列，一列一列的取数据，存到一个map中去
				for( int i=1;i<=columnCount;i++) {
					Object value=rs.getObject(  i );
					//        小写的列名                    值 
					map.put( listColumnName.get(i-1) ,    value    );
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			log.error(  ex.getMessage() );
		}
		return map;
	}
	
	public  List<Map<String,Object>> select(   String sql, Object... params) {
		List<Map<String,Object>> list=new ArrayList<>();
		try(
				Connection con = getConnection();
				PreparedStatement stmt = con.prepareStatement(sql); 
		){
			//TODO: 如何处理 ?占位符, 有n个占位符》 
			doParams(    stmt, params);	
			ResultSet rs=stmt.executeQuery();
			//1.查看rs中有多少列,取出每个列的列名，存到listColumnName
		    ResultSetMetaData rsmd=rs.getMetaData();
		    int columnCount=rsmd.getColumnCount();
		    List<String> listColumnName=new ArrayList<String>();
		    for( int i=1;i<=columnCount;i++) {
		    	String columnName=rsmd.getColumnName(   i ).toLowerCase();   //将数据查出业的列名转成小写. 
		    	listColumnName.add(  columnName);
		    }
			while(  rs.next() ) {
				Map<String,Object> map=new HashMap<String,Object>();
				//2. 循环这些这些列，一列一列的取数据，存到一个map中去
				for( int i=1;i<=columnCount;i++) {
					Object value=rs.getObject(  i );
					//        小写的列名                    值 
					map.put( listColumnName.get(i-1) ,    value    );
				}
				//3。 将这个map存到list中去
				list.add( map );
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			log.error(  ex.getMessage() );
		}
		return list;
	}

	private void setParams(PreparedStatement ps, List<Object> params) {
		if (ps != null && params.size() > 0) {
			for (int i = 0; i < params.size(); i++) {
				try {
					ps.setObject(i + 1, params.get(i));
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void closeAll(Connection con, PreparedStatement ps) {
		try {
			if (con != null) {
				con.close();
			}
			if (ps != null) {
				ps.close();
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

}
