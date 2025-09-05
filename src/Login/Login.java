package Login;

import gui.*;
import gui.teaGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Random;

public class Login extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField captchaField;  // 验证码输入框
    private JLabel captchaLabel;      // 显示验证码
    private String generatedCaptcha;  // 保存生成的验证码

    String[] roles = {"学生", "教师", "管理员"};
    JComboBox<String> roleComboBox = new JComboBox<>(roles);

    JLabel name = new JLabel("账户：");
    JTextField nameIn = new JTextField(10);
    JLabel pwd = new JLabel("密码：");
    JPasswordField pwdIn = new JPasswordField(10);
    JButton jb1 = new JButton("登录");
    JButton jb2 = new JButton("退出");
    JButton refreshBtn = new JButton("刷新"); // 刷新验证码按钮

    public Login() {
        setTitle("选修课管理系统 - 登录");
        setLayout(null);
        setSize(500, 350); // 调整窗口高度以容纳验证码组件
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false); // 禁止调整大小
        setLocationRelativeTo(null);

        // 账户
        name.setBounds(40, 20, 80, 100);
        name.setFont(new Font("宋体", Font.BOLD, 20));

        nameIn.setBounds(120, 60, 180, 25);
        add(nameIn);
        add(name);

        // 密码
        pwd.setBounds(40, 80, 80, 80);
        pwd.setFont(new Font("宋体", Font.BOLD, 20));

        pwdIn.setBounds(120, 110, 180, 25);
        add(pwd);
        add(pwdIn);

        // 角色选择
        JLabel role = new JLabel("请选择你的身份");
        role.setBounds(360, 40, 120, 60);

        roleComboBox.setBounds(370, 80, 70, 20);
        add(role);
        add(roleComboBox);

        // 验证码
        JLabel captchaText = new JLabel("验证码：");
        captchaText.setBounds(40, 140, 100, 80);
        captchaText.setFont(new Font("宋体", Font.BOLD, 20));

        captchaField = new JTextField(10);
        captchaField.setBounds(120, 170, 100, 25);

        captchaLabel = new JLabel();
        captchaLabel.setBounds(230, 170, 120, 25);
        captchaLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        captchaLabel.setFont(new Font("Arial", Font.BOLD, 16));
        captchaLabel.setHorizontalAlignment(JLabel.CENTER);

        refreshBtn.setBounds(360, 170, 70, 25);

        add(captchaText);
        add(captchaField);
        add(captchaLabel);
        add(refreshBtn);

        // 生成初始验证码
        generateCaptcha();

        jb1.setBounds(50, 220, 80, 40);
        add(jb1);

        jb2.setBounds(330, 220, 80, 40);
        add(jb2);

        validate(); // 组件可视化

        jb1.addActionListener(this);
        jb2.addActionListener(this);
        refreshBtn.addActionListener(this);
    }

    // 生成验证码方法
    private void generateCaptcha() {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();

        // 生成四个随机数字
        for (int i = 0; i <2; i++) {
            captcha.append(random.nextInt(10));
        }

        // 生成两个随机字母 (A-Z)
        for (int i = 0; i < 2; i++) {
            char c = (char) (random.nextInt(26) + 'A');
            captcha.append(c);
        }

        // 打乱顺序
        generatedCaptcha = shuffleString(captcha.toString());
        captchaLabel.setText(generatedCaptcha);
    }

    // 打乱字符串顺序
    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        Random random = new Random();

        for (int i = 0; i < characters.length; i++) {
            int randomIndex = random.nextInt(characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }

        return new String(characters);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(jb1)) {
            String account = nameIn.getText();
            String password = new String(pwdIn.getPassword());
            String userCaptcha = captchaField.getText();

            // 验证验证码
            if (!userCaptcha.equals(generatedCaptcha)) {
                JOptionPane.showMessageDialog(Login.this, "验证码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
                generateCaptcha(); // 刷新验证码
                return;
            }

            if ("".equals(account) || password.isEmpty()) {
                JOptionPane.showMessageDialog(Login.this, "密码不能为空", "登录失败", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                String who = (String) roleComboBox.getSelectedItem();
                switch (who) {
                    case "学生":
                        if (userLogin.Login(account, password) == 3) {
                            JOptionPane.showMessageDialog(Login.this, "学生登录成功", "登录成功", JOptionPane.INFORMATION_MESSAGE);
                            try {
                                new stuGUI(account);
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(Login.this, "密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
                        }
                        break;

                    case "教师":
                        if (userLogin.Login(account, password) == 2) {
                            JOptionPane.showMessageDialog(Login.this, "教师登录成功", "登录成功", JOptionPane.INFORMATION_MESSAGE);
                            try {
                                new teaGUI(account).setVisible(true);
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(Login.this, "密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "管理员":
                        if (userLogin.Login(account, password) == 1) {
                            JOptionPane.showMessageDialog(Login.this, "管理员登录成功", "登录成功", JOptionPane.INFORMATION_MESSAGE);

                            try {
                                new adminGUI().setVisible(true);
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }

                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(Login.this, "密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                }
            }
        } else if (e.getSource().equals(jb2)) {
            System.exit(0);
        } else if (e.getSource().equals(refreshBtn)) {
            generateCaptcha(); // 刷新验证码
        }
    }

    public static void main(String[] args) {
    new Login().setVisible(true);
     // new adminGUI().setVisible(true);

//      new stuGUI("101").setVisible(true);
    //  new teaGUI("0001").setVisible(true);
    }
}