package DButil;

import java.sql.*;

/*
本代码作为连接数据库的工具类
studentoperation中含有7个表
*/


public class DButil {
    static String url = "jdbc:mysql://127.0.0.1:3306/studentoperation1";
    static String name = "root";
    static String pwd = "111222";



    public static Connection connection() throws SQLException{
        Connection conn = DriverManager.getConnection(url,name,pwd);
        if(conn.isValid(3))System.out.println("数据库连接成功");
        else System.out.println("数据库连接失败");return conn;
    }

}
