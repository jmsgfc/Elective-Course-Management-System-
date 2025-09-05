package admin;

import info.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

/**
 * 教师管理面板类，负责教师信息的显示、添加等操作
 */
public class TeacherManagementPanel extends JPanel {
    private JTable teacherTable;
    private DefaultTableModel tableModel;
    private JTabbedPane tabbedPane;
    private JFrame parentFrame; // 父组件引用，用于对话框定位和消息提示

    /**
     * 构造函数，初始化面板
     * @param tabbedPane 选项卡面板引用，用于刷新面板

     */
    public TeacherManagementPanel(JTabbedPane tabbedPane, JFrame parentFrame) {
        this.tabbedPane = tabbedPane;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout()); // 设置边框布局
        initializeComponents(); // 初始化组件
        loadTeacherData(); // 加载教师数据
    }

    /**
     * 初始化面板组件（表格、按钮等）
     */
    private void initializeComponents() {
        // 创建表格模型（列名：教师工号、姓名、所教课程数）
        tableModel = new DefaultTableModel(
                new String[]{"教师工号", "姓名", "所教课程数"},
                0
        );

        // 创建表格和滚动面板
        teacherTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(teacherTable);
        add(scrollPane, BorderLayout.CENTER); // 添加到面板中心位置

        // 按钮面板：添加教师按钮
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加教师");
        JButton deleteButton = new JButton("删除教师");
        JButton selectButton = new JButton("课程查询");
        buttonPanel.add(addButton);buttonPanel.add(deleteButton);buttonPanel.add(selectButton);
        add(buttonPanel, BorderLayout.SOUTH); // 添加到面板底部

        // 添加教师按钮事件处理
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddTeacherDialog(); // 显示添加教师对话框
            }
        });

        // 删除按钮事件处理
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取选中行
                int selectedRow = teacherTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(parentFrame, "请选择要删除的教师", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 确认删除
                int confirm = JOptionPane.showConfirmDialog(
                        parentFrame,
                        "确认要删除该教师吗？\n（删除后将级联删除其授课记录）",
                        "删除确认",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // 获取选中教师工号
                        String tno = (String) tableModel.getValueAt(selectedRow, 0);
                        // 执行删除逻辑
                        teacher_info.deleteTeacher(tno);
                        JOptionPane.showMessageDialog(parentFrame, "教师删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        refreshPanel(); // 刷新面板
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(
                                parentFrame,
                                "删除失败：" + ex.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCourseQueryDialog();
            }
        });
    }

    /**
     * 从数据库加载教师数据并填充到表格
     */
    private void loadTeacherData() {
        try {
            // 清空表格现有数据
            tableModel.setRowCount(0);
            // 获取教师数据
            List<teacher_info> teachers = teacher_info.getAllTea();
            for (teacher_info teacher : teachers) {
                tableModel.addRow(new Object[]{
                        teacher.getNo(),    // 教师工号
                        teacher.getName(),  // 姓名
                        teacher.getCount()  // 所教课程数
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    parentFrame,
                    "加载教师数据失败：" + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
            );
            //ex.printStackTrace();
        }
    }

    /**
     * 显示添加教师对话框
     */
    private void showAddTeacherDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(parentFrame), // 获取父窗口
                "添加教师",
                true
        );
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(parentFrame); // 居中显示
        dialog.setLayout(new BorderLayout());

        // 输入面板：使用GridLayout布局，添加空行和输入字段
        JPanel addPanel = new JPanel(new GridLayout(5, 2, 10, 10)); // 5行2列，水平/垂直间距10
        addPanel.add(new JLabel()); // 空标签（第一行左）
        addPanel.add(new JLabel()); // 空标签（第一行右）

        JLabel teacherIdLabel = new JLabel("教师工号:");
        JTextField teacherIdField = new JTextField();
        JLabel nameLabel = new JLabel("姓名:");
        JTextField nameField = new JTextField();
        JLabel passwordLabel = new JLabel("密码:");
        JTextField passwordField = new JTextField();

        addPanel.add(teacherIdLabel);
        addPanel.add(teacherIdField);
        addPanel.add(nameLabel);
        addPanel.add(nameField);
        addPanel.add(passwordLabel);
        addPanel.add(passwordField);

        dialog.add(addPanel, BorderLayout.CENTER); // 添加输入面板到对话框中心

        // 按钮面板：确定和取消按钮
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH); // 添加按钮面板到对话框底部

        // 确定按钮事件处理
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String teacherId = teacherIdField.getText().trim();
                String name = nameField.getText().trim();
                String password = passwordField.getText().trim();

                // 输入验证
                if (teacherId.isEmpty() || name.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "请填写所有必填字段（教师工号、姓名、密码）",
                            "输入错误",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                // 创建教师对象
                teacher_info teacher = new teacher_info(teacherId, name, password);

                try {
                    teacher_info.addTeacher(teacher); // 调用数据层添加教师
                    JOptionPane.showMessageDialog(
                            parentFrame,
                            "教师添加成功",
                            "成功",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    dialog.dispose(); // 关闭对话框
                    refreshPanel(); // 刷新面板数据
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                            parentFrame,
                            "添加教师失败：" + ex.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                    );

                }
            }
        });

        // 取消按钮事件处理
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true); // 显示对话框
    }


    private void showCourseQueryDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(parentFrame),
                "查询课程",
                true
        );
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());

        // 输入面板：教师编号输入框
        JPanel inputPanel = new JPanel(new FlowLayout());
        JLabel tnoLabel = new JLabel("请输入教师编号：");
        JTextField tnoField = new JTextField(15);
        inputPanel.add(tnoLabel);
        inputPanel.add(tnoField);
        dialog.add(inputPanel, BorderLayout.NORTH);

        // 结果表格
        DefaultTableModel resultModel = new DefaultTableModel(
                new String[]{"课程编号", "课程名称"},
                0
        );
        JTable resultTable = new JTable(resultModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板：查询和取消按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton queryButton = new JButton("查询");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(queryButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 查询按钮事件处理
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tno = tnoField.getText().trim();
                if (tno.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "请输入教师编号", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    // 调用数据库查询方法
                    List<String[]> courses = course_info.getCoursesByTno(tno);
                    // 清空表格并填充结果
                    resultModel.setRowCount(0);
                    for (String[] course : courses) {
                        resultModel.addRow(course);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                            dialog,
                            "查询失败：" + ex.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // 取消按钮事件处理
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * 刷新面板（重新加载数据并更新选项卡）
     */
    private void refreshPanel() throws SQLException {
        tabbedPane.removeAll(); // 清空选项卡
        tabbedPane.addTab("学生管理", new StudentManagementPanel(tabbedPane, parentFrame));
        tabbedPane.addTab("教师管理", new TeacherManagementPanel(tabbedPane, parentFrame));
        tabbedPane.addTab("信息查询", new StatisticsPanel(parentFrame));
    }


}
