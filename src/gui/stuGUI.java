package gui;

import Login.*;
import info.*;
import student.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class stuGUI extends JFrame {
    private String studentId;

    // 创建表格模型
    static DefaultTableModel tableHead2 = new DefaultTableModel(
            new String[]{"课程编号", "课程名称", "授课教师", "学分", "学期","操作"},
            0
    );
    // 创建表格模型
    static DefaultTableModel tableHead1 = new DefaultTableModel(
            new String[]{"课程ID", "课程名称", "授课教师", "学分", "最大人数", "已选人数", "学期", "操作"},
            0
    );





    public stuGUI(String studentId) throws SQLException {
        this.studentId = studentId;
        setTitle("选修课管理系统 - 学生");
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//默认关闭操作
        setLocationRelativeTo(null);//居中
        setLayout(null); // 设置为绝对布局

        // 创建标签页面板
        JTabbedPane Tab = new JTabbedPane();
        Tab.setBounds(0, 50, 800, 500); // 设置位置和大小

        //个人通知
        Tab.addTab("个人通知",new PersonalInfoPanel(studentId));

        // 添加可选课程标签页
        Tab.addTab("可选课程", new CourseSelectionPanel(this,studentId));//标签页1具体内容

        // 添加已选课程标签页
        Tab.addTab("已选课程", new CourseViewPanel(this,studentId));

        //添加成绩查询面板
        Tab.addTab("成绩查询",new ScoreQueryPanel(this,studentId));


        // 添加顶部信息面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(null); // 设置为绝对布局
        infoPanel.setBounds(0, 0, 800, 50);
        List<student_info> si = new ArrayList<>();
        si = student_info.getAlluserinfo(studentId);

        JLabel top = new JLabel("欢迎您，   " + si.getFirst().getSname() + " 同学！");
        top.setBounds(20, 15, 200, 20);
        infoPanel.add(top);

        // 添加底部按钮面板
        JPanel under = new JPanel();
        under.setLayout(null); // 设置为绝对布局
        under.setBounds(0, 550, 800, 50);
        JButton left_button = new JButton("退出登录");
        left_button.setBounds(350, 10, 100, 30);
        under.add(left_button);

        // 添加退出登录按钮事件
        left_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {//弹出一个对话框，在主窗口，
                int confirm = JOptionPane.showConfirmDialog(stuGUI.this,
                        "确定要退出登录吗？",
                        "确认退出",
                        JOptionPane.YES_NO_OPTION);//是或否选项

                if (confirm == JOptionPane.YES_OPTION) {//如果是
                    dispose();//关闭
                    new Login().setVisible(true);

                }
            }
        });

        // 添加组件到窗口
        this.add(infoPanel);
        this.add(Tab);
        this.add(under);
        this.setVisible(true);
    }


    public static void clearPanel(){
        tableHead1.setRowCount(0);
        tableHead2.setRowCount(0);
    }

    public static void main(String[] args) throws SQLException {
                new stuGUI("108").setVisible(true);
    }

}//gui



