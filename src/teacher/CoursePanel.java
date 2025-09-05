package teacher;

import DButil.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

// 课程管理面板
public class CoursePanel extends JPanel {
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JButton addCourseBtn, deleteCourseBtn;
    private String teacherId;

    public CoursePanel(String teacherId) {
        this.teacherId = teacherId;
        setLayout(new BorderLayout());

        // 课程表格
        String[] columnNames = {"课程编号", "课程名称", "学分", "最大人数", "目前人数"};
        tableModel = new DefaultTableModel(columnNames, 0);
        courseTable = new JTable(tableModel);
        loadCourses();

        JScrollPane scrollPane = new JScrollPane(courseTable);
        add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel();

        // 添加课程按钮
        addCourseBtn = new JButton("添加课程");
        addCourseBtn.addActionListener(e -> new AddCourseDialog());
        buttonPanel.add(addCourseBtn);

        // 删除课程按钮
        deleteCourseBtn = new JButton("删除课程");
        deleteCourseBtn.addActionListener(e -> deleteSelectedCourse());
        buttonPanel.add(deleteCourseBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }
    //加载课程
    public void loadCourses() {
        tableModel.setRowCount(0);
        try (Connection conn = DButil.connection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT c.cno, c.cname, c.credit, c.max_come, COUNT(sc.sno) as current_students " +
                             "FROM courses c " +
                             "JOIN tcourse tc ON c.cno = tc.cno " +
                             "LEFT JOIN scores sc ON c.cno = sc.cno " +
                             "WHERE tc.tno = ? " +
                             "GROUP BY c.cno " +
                             "ORDER BY c.cno")) {
            stmt.setString(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("cno"),
                        rs.getString("cname"),
                        rs.getInt("credit"),
                        rs.getInt("max_come"),
                        rs.getInt("current_students")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "课程加载失败");
        }
    }

    // 删除选中的课程
    private void deleteSelectedCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的课程", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseId = tableModel.getValueAt(selectedRow, 0).toString();
        String courseName = tableModel.getValueAt(selectedRow, 1).toString();

        // 确认对话框
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要删除课程 " + /*courseId*/  " [ " + courseName + " ] 吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DButil.connection()) {
                // 开启事务
                conn.setAutoCommit(false);

                try {
                    // 1. 删除课程-学生关联
                    String sql1 = "DELETE FROM scores WHERE cno = ?";
                    try (PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
                        pstmt1.setString(1, courseId);
                        pstmt1.executeUpdate();
                    }

                    // 2. 删除课程-教师关联
                    String sql2 = "DELETE FROM tcourse WHERE cno = ? AND tno = ?";
                    try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                        pstmt2.setString(1, courseId);
                        pstmt2.setString(2, teacherId);
                        pstmt2.executeUpdate();
                    }

                    // 3. 删除课程
                    String sql3 = "DELETE FROM courses WHERE cno = ?";
                    try (PreparedStatement pstmt3 = conn.prepareStatement(sql3)) {
                        pstmt3.setString(1, courseId);
                        pstmt3.executeUpdate();
                    }

                    // 提交事务
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "课程删除成功");
                    loadCourses(); // 刷新表格
                } catch (SQLException ex) {
                    // 回滚事务
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "删除课程失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 添加课程对话框
    class AddCourseDialog extends JDialog {
        private JTextField  CnameF, CreditF, MaxF;
        private JButton yesBtn, noBtn;

        public AddCourseDialog() {
            setSize(400, 300);
            setLocationRelativeTo(CoursePanel.this);

            JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            panel.add(new JLabel("课程名称:"));
            CnameF = new JTextField();
            panel.add(CnameF);

            panel.add(new JLabel("学分:"));
            CreditF = new JTextField();
            panel.add(CreditF);

            panel.add(new JLabel("最大人数:"));
            MaxF = new JTextField();
            panel.add(MaxF);

            yesBtn = new JButton("确认");
            noBtn = new JButton("取消");

            yesBtn.addActionListener(e -> {
                addCourse();
                dispose();
            });

            noBtn.addActionListener(e -> dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(yesBtn);
            buttonPanel.add(noBtn);

            add(panel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            setVisible(true);
        }
//添加课程
        private void addCourse() {
            // 输入验证
            String Cname = CnameF.getText();
            int Credit = Integer.parseInt(CreditF.getText());
            int Max = Integer.parseInt(MaxF.getText());
            int autoCno = -1;
            if (Cname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "课程名称不能为空");
                return;
            }

            try {
                if (Credit < 1 || Credit > 5) {
                    JOptionPane.showMessageDialog(this, "学分在1-5之间");
                    return ;

                }
                if (Max <= 0) {
                    JOptionPane.showMessageDialog(this, "最大人数必须大于0");
                    return ;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "输入错误: " + e.getMessage());
                return;
            }

            try (Connection conn = DButil.connection()) {
                conn.setAutoCommit(false);

                try {
                    // 插入课程
                    //String sql2 = "INSERT INTO courses (cname, credit, max_come) VALUES (?, ?, ?)";
                    String sql2 = "INSERT INTO courses (cname, credit, max_come, cdate) VALUES (?, ?, ?, '2024-2025-1')";
                    try (PreparedStatement ps2 = conn.prepareStatement(sql2, Statement.RETURN_GENERATED_KEYS)) {
                        ps2.setString(1, Cname);
                        ps2.setInt(2, Credit);
                        ps2.setInt(3, Max);
                        ps2.executeUpdate();

                        try (ResultSet generatedKeys = ps2.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                autoCno = generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("创建课程失败，无法获取自动生成的ID");
                            }
                        }
                    }

                    // 插入教师-课程关联
                    String sql3 = "INSERT INTO tcourse (tno, cno) VALUES (?, ?)";
                    try (PreparedStatement ps3 = conn.prepareStatement(sql3)) {
                        ps3.setString(1, teacherId);
                        ps3.setInt(2, autoCno); // 使用获取到的自动生成的cno
                        ps3.executeUpdate();
                    }

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "课程添加成功");
                    CoursePanel.this.loadCourses();
                } catch (SQLException ex) {
                    conn.rollback();
                    ex.printStackTrace();
                    String message = "课程添加失败: " + ex.getMessage();
                    if (ex.getMessage().contains("Duplicate entry")) {
                        message = "课程名称已存在";
                    }
                    JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException | NumberFormatException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "输入错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}