package info;

import DButil.DButil;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class student_info {
    private String depart;
    private String major;
    private String sclass;
    private String sno;
    private String sname;
    private String sex;
    private String password;
    public student_info(String depart, String major, String sclass, String sno, String sname, String sex) {
        this.depart = depart;
        this.major = major;
        this.sclass = sclass;
        this.sno = sno;
        this.sname = sname;
        this.sex = sex;
    }

    public student_info(String depart, String major, String sclass, String sno, String sname, String sex, String password) {
        this.depart = depart;
        this.major = major;
        this.sclass = sclass;
        this.sno = sno;
        this.sname = sname;
        this.sex = sex;
        this.password = password;
    }

    public student_info() {

    }

    public static List<student_info> searchStudentsByName(String stuNo) throws SQLException {
        List<student_info> si = new ArrayList<>();
        String sql1 = "select * from student where sno=?;";
        try(Connection conn = DButil.connection()){
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,stuNo);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()){
                String major = rs1.getString("major");
                String sno = rs1.getString("sno");
                String sname = rs1.getString("sname");
                String sex = rs1.getString("sex");
                String depart = rs1.getString("depart");
                String sclass = rs1.getString("sclass");
                si.add(new student_info(depart,major,sclass,sno,sname,sex));
            }
        }

        return si;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getSclass() {
        return sclass;
    }

    public void setSclass(String sclass) {
        this.sclass = sclass;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public static List<student_info> getAlluserinfo(String stuId) throws SQLException {
        List<student_info> student_infos = new ArrayList<>();
        try(Connection conn = DButil.connection()){
            String sql = "select * from student where sno = ?";

            PreparedStatement ps1 = conn.prepareStatement(sql);
            ps1.setString(1,stuId);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                String Sno = rs1.getString("sno");
                String Sname = rs1.getString("sname");
                String Sex = rs1.getString("sex");
                String Depart = rs1.getString("depart");
                String Major = rs1.getString("major");
                String Sclass = rs1.getString("sclass");

                student_infos.add(new student_info(Depart,Major,Sclass,Sno,Sname,Sex));
            }
        }

        return student_infos;


    }

    public static List<student_info> getAlluserinfo() throws SQLException {
        List<student_info> student_infos = new ArrayList<>();
        try(Connection conn = DButil.connection()){
            String sql = "select * from student";

            PreparedStatement ps1 = conn.prepareStatement(sql);

            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                String Sno = rs1.getString("sno");
                String Sname = rs1.getString("sname");
                String Sex = rs1.getString("sex");
                String Depart = rs1.getString("depart");
                String Major = rs1.getString("major");
                String Sclass = rs1.getString("sclass");

                student_infos.add(new student_info(Depart,Major,Sclass,Sno,Sname,Sex));
            }
        }

        return student_infos;


    }


    public static void setStu(student_info us) throws SQLException {

        try(Connection conn = DButil.connection()){
        //传入学生表
            String sql1 = "insert into student(sno,sname,sex,depart,major,sclass) values(?,?,?,?,?,?)";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1, us.getSno());
            ps1.setString(2,us.getSname());
            ps1.setString(3,us.getSex());
            ps1.setString(4,us.getDepart());
            ps1.setString(5,us.getMajor());
            ps1.setString(6,us.getSclass());
            ps1.executeUpdate();
        //传入用户
            String sql2 = "insert into users(uname,pwd,role) values(?,?,2)";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setString(1,us.getSname());
            ps2.setString(2,us.getPassword());
            ps2.executeUpdate();


        }
    }

    public static void deleteStu(String uid) throws SQLException {
        try(Connection conn = DButil.connection()){
            String sql1 = "delete from student where sno = ?";
            String sql2 = "delete from users where uname =?";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,uid);
            ps1.executeUpdate();
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setString(1,uid);
            ps2.executeUpdate();
        }

    }




    public static void loadClass(JComboBox<String> classCB,String Tno) throws SQLException {
        classCB.removeAllItems();
        classCB.addItem("请选择班级");
        String sql1 = "select distinct sclass \n" +
                "from student \n" +
                "order by sclass asc;";

        try(Connection conn = DButil.connection()){
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()){
                String Class = rs1.getString("sclass");
                classCB.addItem(Class);
            }
        }
    }


    public static void loadMajors(JComboBox<String> majorsCB , String Tno) throws SQLException {
        String sql1 = "select DISTINCT s.major AS major\n" +
                "FROM student s\n" +
                "JOIN scores sc ON s.sno = sc.sno\n" +
                "JOIN courses c ON sc.cno = c.cno\n" +
                "JOIN tcourse tc ON c.cno = tc.cno\n" +
                "WHERE tc.tno = ?;";
        majorsCB.addItem("请选择专业");
        try(Connection conn = DButil.connection()){
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,Tno);
            ResultSet rs1 = ps1.executeQuery();//询问
            while (rs1.next()){
                String majors = rs1.getString("major");
                majorsCB.addItem(majors);
            }
        }
    }

    public static void loadColleges(JComboBox<String> departCB, String Tno) throws SQLException {
        String sql1 = "select distinct depart from student;";
        try(Connection conn = DButil.connection()){
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ResultSet rs1 = ps1.executeQuery();
            departCB.addItem("请选择学院");
            while (rs1.next()){
                departCB.addItem(rs1.getString("depart"));
            }
        }
    }

    public static void loadTerms(JComboBox<String>termCB , String Tno) throws SQLException{
        String sql1 = "SELECT DISTINCT c.cdate as cdate\n" +
                "FROM courses c\n" +
                "JOIN tcourse tc ON c.cno = tc.cno\n" +
                "WHERE tc.tno = ?;";
        try (Connection conn = DButil.connection()) {
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1, Tno);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()){
                termCB.addItem(rs1.getString("cdate"));
            }
        }
    }

    // student_info类中添加的方法
    public static List<student_info> multSearch(
            String depart, String major, String className) throws SQLException {
        List<student_info> result = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps1 = null;
        ResultSet rs1 = null;

            conn = DButil.connection();
            StringBuilder sql = new StringBuilder("SELECT * FROM student WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (depart != null && !depart.isEmpty()) {
                sql.append(" AND depart = ?");
                params.add(depart);
            }

            if (major != null && !major.isEmpty()) {
                sql.append(" AND major = ?");
                params.add(major);
            }

            if (className != null && !className.isEmpty()) {
                sql.append(" AND sclass = ?");
                params.add(className);
            }

            ps1 = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps1.setObject(i + 1, params.get(i));
            }

            rs1 = ps1.executeQuery();
            while (rs1.next()) {
                student_info stu = new student_info();
                stu.setDepart(rs1.getString("depart"));
                stu.setMajor(rs1.getString("major"));
                stu.setSclass(rs1.getString("sclass"));
                stu.setSno(rs1.getString("sno"));
                stu.setSname(rs1.getString("sname"));
                stu.setSex(rs1.getString("sex"));
                result.add(stu);
            }

        return result;
    }

}
