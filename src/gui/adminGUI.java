package gui;

import Login.*;
import admin.StatisticsPanel;
import admin.StudentManagementPanel;
import admin.TeacherManagementPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class adminGUI extends JFrame {

    private final JTabbedPane Tab;

    public adminGUI() throws SQLException {
        setTitle("选修课管理系统 - 管理员");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建主面板
        JPanel main_panel = new JPanel(new BorderLayout());

        // 创建标签页面板
        Tab = new JTabbedPane();

        // 添加学生管理标签页
        Tab.addTab("学生管理", new StudentManagementPanel(Tab,this));

        // 添加教师管理标签页
        Tab.addTab("教师管理",new TeacherManagementPanel(Tab,this));


        // 添加信息查询标签页
        Tab.addTab("信息查询", new StatisticsPanel(this));

        // 添加主面板
        main_panel.add(Tab, BorderLayout.CENTER);//边框中部

        // 添加顶部信息面板
        JPanel infoPanel = new JPanel();
        JLabel welcomeLabel = new JLabel("欢迎，管理员");
        infoPanel.add(welcomeLabel);
        main_panel.add(infoPanel, BorderLayout.NORTH);//北部

        // 添加底部按钮面板
        JPanel buttonPanel = new JPanel();
        JButton logoutButton = new JButton("退出登录");
        buttonPanel.add(logoutButton);
        main_panel.add(buttonPanel, BorderLayout.SOUTH);//南部

        // 添加退出登录按钮事件
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(adminGUI.this, "确定要退出登录吗？", "确认退出", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    new Login().setVisible(true);
                    dispose();
                }
            }
        });

        // 添加主面板到窗口
        add(main_panel);
    }




    public static void main(String[] args) throws SQLException {
        new adminGUI().setVisible(true);
    }
}

















