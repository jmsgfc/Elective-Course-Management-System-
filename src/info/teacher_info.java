package info;

import DButil.DButil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class teacher_info {
    private String name;
    private String no;
    private int count;
    private String password;

    public teacher_info(String no, String name, String password){
        this.no = no;
        this.name = name;
        this.password = password;
    }


    public teacher_info(String no, String name, int count) {
        this.name = name;
        this.no = no;
        this.count = count;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public static List<teacher_info> getAllTea() throws SQLException {
        List<teacher_info> teacher_infos = new ArrayList<>();
        try(Connection conn = DButil.connection()){
            //查询老师全部信息
            String sql1 = "select t.tno as tno,t.tname as tname,count(tc.tno) as count from teacher t\n"  +
                    "left join tcourse tc on t.tno = tc.tno group by t.tno,t.tname";
            PreparedStatement ps1 = conn.prepareStatement(sql1);//执行对象
            ResultSet rs1 = ps1.executeQuery();//结果集查询
            while(rs1.next()){
                String Tno = rs1.getString("tno");
                String Tname = rs1.getString("tname");
                int count = rs1.getInt("count");
                teacher_infos.add(new teacher_info(Tno,Tname,count));
            }
        }
        return teacher_infos;//返回老师的全部信息
    }



    public static void addTeacher(teacher_info ta) throws SQLException {
        String sql1 = "insert into teacher(tno,tname) values (?,?)";
        String sql2 = "insert into users(uname,pwd,role) values (?,?,1)";
        try(Connection conn = DButil.connection()){
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1, ta.getNo());
            ps1.setString(2,ta.getName());
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setString(1,ta.getNo());
            ps2.setString(2,ta.getPassword());
            ps1.executeUpdate();//执行更新
            ps2.executeUpdate();
        }
    }

    // 若 teacher.tno 关联 users.uname（如示例中的用户表）
    public static void deleteTeacher(String tno) throws SQLException {
        String sql = "DELETE FROM tcourse WHERE tno = ?";
        try (Connection conn = DButil.connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tno);
            pstmt.executeUpdate();
        }

        // 再删除教师记录
        sql = "DELETE FROM teacher WHERE tno = ?";
        try (Connection conn = DButil.connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tno);
            pstmt.executeUpdate();
        }

        // 删除用户表记录（如果需要）
        sql = "DELETE FROM users WHERE uname = ?";
        try (Connection conn = DButil.connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tno);
            pstmt.executeUpdate();
        }
    }
}//class



