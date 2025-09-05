package DButil;

import java.sql.*;
/*
本代码作为数据库的测试类，不参与其他代码编写
 */
public class connectionTo {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:mysql://127.0.0.1:3306/studb";
        String name = "root";
        String password = "111222";
        Connection conn = DriverManager.getConnection(url,name,password);

        if(conn.isValid(3))
            System.out.println("数据库连接成功");
        String sql = "select * from student";

        //执行对象
        Statement exec = conn.createStatement();

        //结果对象
        ResultSet rs = exec.executeQuery(sql);//执行这个sql语句

        while(rs.next()){
            String sno = rs.getString("Sno");
            String sname = rs.getString("Sname");
            String sex = rs.getString("Ssex");
            String sbirth = rs.getString("Sbirth");
            String saddress = rs.getString("Saddress");
            String date = rs.getString("comedate");
            System.out.printf("%s %s %s %s %s %s\n",sno,sname,sex,sbirth,saddress,date);
        }

    }
}
