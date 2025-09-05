package teacher;

import DButil.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfilePanel extends JPanel {
    private JLabel teacherInfoLabel;
    private JTextArea noticeArea;
    private String teacherId;
    String messages ;
    public ProfilePanel(String teacherId) throws SQLException {
        this.teacherId = teacherId;
        setLayout(new BorderLayout());

        // 左侧个人信息
//        JPanel infoPanel = new JPanel(new GridLayout(5, 1, 100, 10));
        JPanel infoPanel = new JPanel(null);
        teacherInfoLabel = new JLabel();
        loadTeacherInfo();//显示教师姓名


        JLabel Jname = new JLabel("教师编号：" + teacherId) ;
        Jname.setBounds(20,80,140,30);
        infoPanel.add(Jname);
        infoPanel.add(teacherInfoLabel);
        infoPanel.add(new JLabel("")); // 空行
        JButton reviseButton = new JButton("修改密码");
        reviseButton.setBounds(35,300,150,40);
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
                            ps1.setString(1,teacherId);
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
                            ps2.setString(2,teacherId);
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
        infoPanel.add(reviseButton); // 可扩展功能

        // 右侧通知公告
        noticeArea = new JTextArea();
        noticeArea.setEditable(false);
        noticeArea.setFont(new Font("微软雅黑",Font.BOLD,14));
        noticeArea.setText("暂无通知公告");
        loadnotices(teacherId);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPanel, new JScrollPane(noticeArea));
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadTeacherInfo() {
        try (Connection conn = DButil.connection();
             PreparedStatement stmt = conn.prepareStatement(
                     "select tname from teacher where tno = ?")) {
            stmt.setString(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String tname = rs.getString("tname");
                teacherInfoLabel.setText("教师姓名：" + tname);
                teacherInfoLabel.setBounds(20,40,140,30);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            teacherInfoLabel.setText("信息加载失败");
        }
    }

    private void loadnotices(String teacherId) throws SQLException {
        // 实际应用中应从数据库加载通知
        String sql1 = "select info.cno as cno,info.cname as cname,info.infos as infos ,info.cdate as cdate from info\n" +
                "join tcourse tc on tc.cno = info.cno where tc.tno =?;";
        noticeArea.setText("暂无通知公告");
        try(Connection conn = DButil.connection()){
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1,teacherId);
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
                messages = "您所教的 (" + cno + "-" + cname + ") " + infos + "   日期：" + cdate + '\n'; //创建
                noticeArea.append(messages);
                messages = "";
            }
        }
    }
}