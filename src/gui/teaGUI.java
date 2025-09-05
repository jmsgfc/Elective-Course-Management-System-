package gui;

import teacher.CoursePanel;
import teacher.ProfilePanel;
import teacher.ScorePanel;

import javax.swing.*;
import java.sql.SQLException;

public class teaGUI extends JFrame {
    public teaGUI(String teacherId) throws SQLException {
        setTitle("选修课管理系统-教师");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane Tab = new JTabbedPane();
        Tab.addTab("个人信息", new ProfilePanel(teacherId));
        Tab.addTab("课程管理", new CoursePanel(teacherId));
        Tab.addTab("成绩管理", new ScorePanel(teacherId));


        add(Tab);
        setVisible(true);
    }

    public static void main(String[] args) throws SQLException {
        new teaGUI("0001").setVisible(true);
    }
}