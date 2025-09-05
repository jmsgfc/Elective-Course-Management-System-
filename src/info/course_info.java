package info;
import DButil.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class course_info { // 类名建议首字母大写
    // 成员变量（建议改为私有，通过Getter/Setter访问）
    private String cno;
    private String cname;
    private int credit;
    private int max_come; // 驼峰命名法
    private int current_come;
    private String tname;
    private String date;

    // 构造方法（初始化课程信息）
    public course_info(String cno, String cname, String tname , int credit, int max_come, int current_come , String date) {
        this.cno = cno;
        this.cname = cname;
        this.credit = credit;
        this.max_come = max_come;
        this.current_come = current_come;
        this.tname = tname;
        this.date = date;
    }

    // Getter方法（省略Setter，如需修改数据可添加）
    public String getCno() { return cno; }
    public String getCname() { return cname; }
    public int getCredit() { return credit; }
    public int getMax_come() { return max_come; }
    public int getCurrent_come() { return current_come; }
    public String getTname() { return tname; }
    public String getDate() {return date; }
    // 获取所有课程信息（含选课人数和教师姓名）
    public static List<course_info> getAllCourses() throws SQLException {
        List<course_info> cours = new ArrayList<>();
        try (Connection conn = DButil.connection()) { // 假设DBUtil.getConnection()获取连接

            // 1. 查询所有课程基本信息
            String sql1 = "select cno, cname, credit, max_come ,cdate from courses order by cdate desc";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();

            // 2. 遍历每门课程，查询对应的选课人数和教师姓名
            while (rs1.next())
            { // 使用while遍历所有行（rs1存在时候）
                String cno = rs1.getString("cno");//获取课程编号
                String cname = rs1.getString("cname");//获取课程名
                int credit = rs1.getInt("credit");//获取学分
                int max_come = rs1.getInt("max_come");//获取最大人数
                String Date = rs1.getString("cdate"); //获取学期
                // 2.1 查询选课人数（基于course表中的cno获取）
                String sql2 = "select count(sno) AS current_come from scores where cno = ?";
                PreparedStatement ps2 = conn.prepareStatement(sql2);
                ps2.setString(1, cno);
                ResultSet rs2 = ps2.executeQuery();
                int current_come = 0;
                if (rs2.next()) {
                    current_come = rs2.getInt("current_come");
                }

                // 2.2 查询授课教师姓名(同样基于course中的cno获取）
                String sql3 = "select t.tname from tcourse tc JOIN teacher t ON tc.tno = t.tno WHERE tc.cno = ?";
                PreparedStatement ps3 = conn.prepareStatement(sql3);
                ps3.setString(1, cno);
                ResultSet rs3 = ps3.executeQuery();
                String tname = "";
                if (rs3.next()) {
                    tname = rs3.getString("tname");
                }

                // 3. 封装课程对象并添加到列表

                cours.add(new course_info(cno,cname,tname,credit,max_come,current_come,Date));

                // 关闭内层PreparedStatement（ResultSet会随PreparedStatement关闭而关闭）
                ps2.close();
                ps3.close();
            }

            // 关闭外层PreparedStatement
            ps1.close();
        }
        return cours;
    }

    public static List<String> getTeaCourses(String teacherId) throws SQLException {
        List<String> teachers = new ArrayList<>();
        String sql1 = "select c.cname as cname \n" +
                "from courses c \n" +
                "join tcourse tc on c.cno = tc.cno\n" +
                "where tc.tno = ?;";
        try(Connection conn = DButil.connection()){
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,teacherId);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()){
                teachers.add(rs1.getString("cname"));
            }
        }

        return teachers;
    }

    public static boolean newCourse(String Sno , String Cno) throws SQLException {
        try(Connection conn = DButil.connection()){
            //cuurent可能为null?
            int max = 0;
            int current = 0;
            String sql1 = "select max_come from courses where cno = ?";//查询课程号的人数
            String sql2 = "select count(sno) AS current_come from scores where cno = ?";//查询当前人数
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps1.setString(1,Cno);
            ps2.setString(1,Cno);
            ResultSet rs1 = ps1.executeQuery();
            ResultSet rs2 = ps2.executeQuery();
            while(rs1.next())
                max = rs1.getInt("max_come");

            while(rs2.next())
                current = rs2.getInt("current_come");


            return max > current;


        }
    }

public static boolean checkIn_choose(String Sno,String Cno)throws SQLException{
        try(Connection conn = DButil.connection()){
            String sql1 = "select cno,sno from scores where cno = ? and sno = ?";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,Cno);
            ps1.setString(2,Sno);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                if(rs1.getString("cno").equals(Cno) && rs1.getString("sno").equals(Sno))
                    return false;
            }

        }
        return true;
}

    public static boolean checkIn_Handchoose(String Sno,String Cno)throws SQLException{
        try(Connection conn = DButil.connection()){
            //检查内容是否存在成绩表
            String sql1 = "select cno,sno from scores where cno = ? and sno = ?";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,Cno);
            ps1.setString(2,Sno);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                if(rs1.getString("cno").equals(Cno) && rs1.getString("sno").equals(Sno))
                    return true;
            }

        }
        return false;
    }

    public static void addTo_course(String Sno , String Cno , String Date)throws SQLException{
            Connection conn = DButil.connection();
            String sql1 = "insert into scores(cno,sno,grade,cdate) values(?,?,-1,?)";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,Cno);
            ps1.setString(2,Sno);
            ps1.setString(3,Date);
            ps1.executeUpdate();//刷新


    }

    public static boolean dropTo_course(String Sno,String Cno)throws SQLException{
            Connection conn = DButil.connection();
            String sql1 = "delete from scores where sno = ? and cno = ?";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,Sno);
            ps1.setString(2,Cno);
            ps1.executeUpdate();//更新
            return true;

    }

    public static void loadTeacherCourses(JComboBox<String> courseCB,String Tno) throws SQLException {
        courseCB.removeAllItems();//先清空数据
        String sql1 = "select c.cno, c.cname from courses c " +
                "join tcourse tc on c.cno = tc.cno " +
                "where tc.tno = ?";
        courseCB.addItem("          请选择课程");
            Connection conn = DButil.connection();
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1, Tno);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                courseCB.addItem(rs1.getString("cno") + " - " + rs1.getString("cname"));
            }

    }


    public static void loadCourses(JComboBox<String>courseCB , String Tno) throws SQLException {
        String sql1 = "select c.cname as cname from courses c\n" +
                "join tcourse tc on c.cno = tc.cno\n" +
                "where tc.tno = ?;";
            Connection conn = DButil.connection();
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,Tno);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()){
                courseCB.addItem(rs1.getString("cname"));
            }

    }

    public static void getCourseCount(DefaultTableModel tableModel ) throws SQLException {
        Connection conn = DButil.connection();
        String sql = "delete from info;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.executeUpdate();//更新
        for(int row = 0 ; row < tableModel.getRowCount() ; row ++){
            String courseNo = tableModel.getValueAt(row,0).toString(); //选课程id
            int maxStudents = Integer.parseInt(tableModel.getValueAt(row, 4).toString());//最大值
            int currentStudents = Integer.parseInt(tableModel.getValueAt(row, 5).toString());//目前学生

            if(currentStudents >= maxStudents) {
                String sql1 = "select cname from courses where cno =?;";

                String sql2 = "insert into info (cno, cname, infos, cdate) " +
                        "values (?, ?, '选课人数已满', CURDATE()) " +
                        "on duplicate key update infos = values(infos)";

                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ps1.setString(1, courseNo);
                ResultSet rs1 = ps1.executeQuery();//查询
                String courseName = "";
                while (rs1.next()) courseName = rs1.getString("cname");
                PreparedStatement ps2 = conn.prepareStatement(sql2);
                ps2.setString(1, courseNo);
                ps2.setString(2, courseName);
                ps2.executeUpdate();//更新

                }

        }

    }

    // teacher_info.java 中新增方法
    public static List<String[]> getCoursesByTno(String tno) throws SQLException {
        List<String[]> courses = new ArrayList<>();
        String sql = "SELECT co.cno, co.cname " +
                "FROM tcourse tc " +
                "JOIN courses co ON tc.cno = co.cno " +
                "WHERE tc.tno = ?";

        try (Connection conn = DButil.connection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tno);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String[] course = {
                            rs.getString("cno"),
                            rs.getString("cname")
                    };
                    courses.add(course);
                }
            }
        }
        return courses;
    }


}