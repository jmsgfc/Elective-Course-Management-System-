package student;

import DButil.DButil;
import info.student_info;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 学生个人信息面板
 */
public class PersonalInfoPanel extends JPanel {
    private final String studentId;
    private JLabel nameLabel;
    private JLabel departLabel;
    private JLabel majorLabel;
    private JLabel classLabel;
    private JLabel snoLabel;
    private JLabel sexLabel;
    private JTextArea noticeArea;
    private String messages;
    /**
     * 构造函数
     * @param studentId 学生ID
     * @throws SQLException 数据库操作异常
     */
    public PersonalInfoPanel(String studentId) throws SQLException {
        super(new BorderLayout(15, 15));
        this.studentId = studentId;
        setBorder(BorderFactory.createTitledBorder("个人信息"));
        initComponents();
        loadStudentInfo();
    }

    /**
     * 初始化界面组件
     */
    private void initComponents() {
        // 左侧信息面板
        JPanel infoPanel = new JPanel(new GridLayout(7, 1, 5, 40));
        infoPanel.setPreferredSize(new Dimension(200, 150));

        // 初始化标签
        nameLabel = new JLabel("姓名：");
        departLabel = new JLabel("学院：");
        majorLabel = new JLabel("专业：");
        classLabel = new JLabel("班级：");
        snoLabel = new JLabel("学号：");
        sexLabel = new JLabel("性别：");
        JButton reviseButton = new JButton("修改密码");
        //-------------修改密码按钮监听器---------------------
        reviseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame revise = new JFrame("密码修改");
                revise.setLayout(new GridLayout(4,2,60,80));
                revise.setLocationRelativeTo(null);
                revise.setSize(300,400);
                JLabel pwd0 = new JLabel("请输入旧密码");
                JTextField pwd00 = new JTextField(20);
                JLabel pwd1 = new JLabel("请输入新密码");
                JTextField pwd11 = new JTextField(20);
                JLabel pwd2 = new JLabel("请再次输入密码");
                JTextField pwd22 = new JTextField(20);
                revise.add(pwd0);revise.add(pwd00);revise.add(pwd1);revise.add(pwd11);revise.add(pwd2);revise.add(pwd22);
                JButton yesButton = new JButton("确定");
                JButton noButton = new JButton("取消");
                revise.add(yesButton);revise.add(noButton);
                yesButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String sql1 = "select pwd from users where uname =?;";
                        String sql2 = "update users \n" +
                                "set pwd = ? \n" +
                                "where uname = ?; ";
                        String password = "";
                        try(Connection conn = DButil.connection()){
                            PreparedStatement ps1 = conn.prepareStatement(sql1);
                            ps1.setString(1,studentId);
                            ResultSet rs1 = ps1.executeQuery();
                            while (rs1.next()){
                                password = rs1.getString("pwd");
                            }
                            if(!pwd11.getText().equals(pwd22.getText())) {
                                JOptionPane.showMessageDialog(revise, "两次密码不一致！", "错误", JOptionPane.ERROR_MESSAGE);
                                return;
                            }else if(pwd11.getText().isEmpty() || pwd22.getText().isEmpty()){
                                JOptionPane.showMessageDialog(revise,"请输入完整信息！","错误",JOptionPane.ERROR_MESSAGE);
                                return;
                            }else if(!password.equals(pwd00.getText())){
                                JOptionPane.showMessageDialog(revise,"原密码输入错误！","错误",JOptionPane.ERROR_MESSAGE);
                                return;
                            }else if (password.equals(pwd00.getText()) && (pwd11.getText().equals(pwd00.getText()))){
                                JOptionPane.showMessageDialog(revise,"新旧密码一致！！","错误",JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            PreparedStatement ps2 = conn.prepareStatement(sql2);
                            ps2.setString(1,pwd22.getText());
                            ps2.setString(2,studentId);
                            ps2.executeUpdate();//更新
                            JOptionPane.showMessageDialog(revise,"密码修改成功","通知",JOptionPane.INFORMATION_MESSAGE);
                            revise.setVisible(false);
                        }catch (SQLException ex){
                            JOptionPane.showMessageDialog(revise,"数据库连接错误！","错误",JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                noButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        revise.setVisible(false);
                    }
                });

                revise.setVisible(true);

            }
        });
        //-------------修改密码监听器---------------------


        // 添加标签到面板
        infoPanel.add(nameLabel);infoPanel.add(departLabel);infoPanel.add(majorLabel);
        infoPanel.add(classLabel);infoPanel.add(snoLabel);infoPanel.add(sexLabel);
        infoPanel.add(reviseButton);//修改密码按钮
        // 右侧通知面板
        JPanel noticePanel = new JPanel(new BorderLayout());//使用边界布局
        noticePanel.setBorder(BorderFactory.createTitledBorder("通知公告"));

        noticeArea = new JTextArea(8, 40);
        noticeArea.setEditable(false);//不可编辑
        noticeArea.setLineWrap(true);//自动换行
        noticeArea.setFont(new Font("微软雅黑", Font.BOLD, 14));

        noticePanel.add(new JScrollPane(noticeArea), BorderLayout.CENTER);

        // 添加到主面板
        add(infoPanel, BorderLayout.WEST);//西
        add(noticePanel, BorderLayout.CENTER);//中
    }

    /**
     加载学生信息
     */
    private void loadStudentInfo() throws SQLException {
        List<student_info> users = student_info.getAlluserinfo(studentId);
        if (!users.isEmpty()) {
            student_info student = users.getFirst();
            nameLabel.setText("姓名：" + student.getSname());
            departLabel.setText("学院：" + student.getDepart());
            majorLabel.setText("专业：" + student.getMajor());
            classLabel.setText("班级：" + student.getSclass());
            snoLabel.setText("学号：" + student.getSno());
            sexLabel.setText("性别：" + student.getSex());

            // 加载通知（实际应用中应从数据库获取）
            loadNotices();
        }
    }


     // 加载通知公告（方法存根，需根据实际数据库结构实现）

    private void loadNotices() throws SQLException {

        // 实际应用中应从数据库加载通知
        String sql1 = "select cno ,cname ,infos ,cdate from info";
        noticeArea.setText("暂无通知公告");
        try(Connection conn = DButil.connection()){
        PreparedStatement ps1 = conn.prepareStatement(sql1);
        ResultSet rs1 = ps1.executeQuery();
        boolean hasData = false;
        while (rs1.next()){
            if (!hasData) {
                // 第一条数据时清空"暂无通知"
                noticeArea.setText("");
                hasData = true;
            }
           String cno =  rs1.getString("cno");
           String cname = rs1.getString("cname");
           String infos = rs1.getString("infos");
           String cdate = rs1.getString("cdate");
           messages = " (" + cno + "-" + cname + ") " + infos + "   日期：" + cdate + '\n'; //创建
           noticeArea.append(messages);

            messages = "";
        }
        }
    }


}