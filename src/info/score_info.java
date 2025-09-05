package info;

import DButil.DButil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class score_info {
    private String cname;
    private String tname;
    private int credit;
    private int score;
    private int sno;
    private String date;

    score_info(String cname , String tname , int credit , int score, String date){
       this.cname = cname;
       this.tname = tname;
       this.credit = credit;
       this.score = score;
       this.date = date;
    }

    public static List<score_info> getAllscore(String sno) throws SQLException {
        List<score_info> scoreinfos = new ArrayList<>();
        try(Connection conn = DButil.connection()) {
            String sql1 = "select c.cname as cname,min(t.tname) as tname,  c.credit as credit, s.grade as grade ,s.cdate as cdate\n" +
                    "from scores s \n" +
                    "join courses c ON s.cno = c.cno \n" +
                    "join tcourse tc ON c.cno = tc.cno \n" +
                    "join teacher t ON tc.tno = t.tno \n" +
                    "where s.sno = ?\n" +
                    "group by s.cno, c.cname, c.credit, s.grade\n" +
                    "order by s.cdate desc;";

            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,sno);
            ResultSet rs1 = ps1.executeQuery();//执行查找
            while (rs1.next()){
                String Cname = rs1.getString("cname");
                String Tname = rs1.getString("tname");
                int Credit = rs1.getInt("credit");
                int Grade = rs1.getInt("grade");
                String Date = rs1.getString("cdate");
                scoreinfos.add(new score_info(Cname,Tname,Credit,Grade,Date));//逐步加入
            }
        }

        return scoreinfos;
    }


    public static void saveScores(DefaultTableModel tableModel, String courseInfo, JPanel scorePanel) {
        if (courseInfo == null || courseInfo.isEmpty()) {
            JOptionPane.showMessageDialog(scorePanel, "请先选择课程", "提示", JOptionPane.WARNING_MESSAGE);
            return ;
        }

        String courseId = courseInfo.split(" - ")[0];
        int confirm = JOptionPane.showConfirmDialog(scorePanel,
                "确定要保存所有成绩吗？", "确认保存", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return ;

        try (Connection conn = DButil.connection()) {
            String sql = "INSERT INTO scores (sno, cno, grade) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE grade = VALUES(grade)";

            PreparedStatement ps1 = conn.prepareStatement(sql);
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                // 获取学号
                String studentid =  tableModel.getValueAt(i, 0).toString();//将object强转string
                // 获取成绩
                String gradeS =  tableModel.getValueAt(i, 6).toString();
                // 处理空成绩
                if (gradeS == null || gradeS.trim().isEmpty()) {
                    ps1.setString(1, studentid);
                    ps1.setString(2, courseId);
                    ps1.setInt(3, -1);
                } else {
                    int grade = Integer.parseInt(gradeS);
                    // 检查成绩范围
                    if (grade < -1 || grade > 100) {
                        JOptionPane.showMessageDialog(scorePanel,"成绩超出范围","错误",JOptionPane.ERROR_MESSAGE);
                        throw new IllegalArgumentException("成绩超出范围");
                    }

                    ps1.setString(1, studentid);
                    ps1.setString(2, courseId);
                    ps1.setInt(3, grade);
                }
                ps1.executeUpdate();//更新
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(scorePanel, "保存失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(scorePanel, "保存成绩成功！", "提示", JOptionPane.INFORMATION_MESSAGE);

    }

    //获取课程的学年学期
    public static void loadCourseTerms(JComboBox<String> termsCB,String Tno) throws SQLException {
        String sql1 = "select distinct cdate \n" +
                "from scores s \n" +
                "join tcourse on tcourse.cno = s.cno\n" +
                "where tcourse.tno = ?;";
        try(Connection conn = DButil.connection()){
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,Tno);
            ResultSet rs1 = ps1.executeQuery();
            while(rs1.next()){
                String Date = rs1.getString("cdate");
                termsCB.addItem(Date);//加入学期
            }
        }
    }


    public static List<String> courseByscores(String term, String teacherId) throws SQLException {
        List<String> courses = new ArrayList<>();//根据提供的学期和教师id查到对应具有的课程
        String sql1 = "select  c.cname as cname from  courses c\n" +
                "join  tcourse tc on c.cno = tc.cno\n" +
                "where tc.tno = ?  and c.cdate = ?;  ";
        try(Connection conn = DButil.connection()){
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,teacherId);
            ps1.setString(2,term);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()){
                courses.add(rs1.getString("cname"));
            }
        }
        return courses;
    }



    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public int getSno() {
        return sno;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTname() {
        return tname;
    }

    public void setTname(String tname) {
        this.tname = tname;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

//    public static void main(String[] args) throws SQLException {
//        List<score_info> scoreinfos = getAllscore("001");
//        for (score_info s : scoreinfos) {
//            System.out.printf("课程名：%s，授课教师：%s，学分：%d，成绩：%d%n",
//                    s.getCname(), s.getTname(), s.getCredit(), s.getScore());
//        }
//    }
}
