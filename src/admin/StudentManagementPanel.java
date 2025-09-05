package admin;

import info.student_info;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 * 学生管理面板类，支持学院-专业-班级级联下拉搜索
 */
public class StudentManagementPanel extends JPanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JTabbedPane tabbedPane;
    private Component parentComponent;

    // 搜索条件下拉框
    private JComboBox<String> departComboBox;
    private JComboBox<String> majorComboBox;
    private JComboBox<String> classComboBox;

    // 数据缓存
    private List<student_info> allStudents;
    private Map<String, Set<String>> majorByDepart;  // 学院对应的专业集合
    private Map<String, Set<String>> classByMajor;   // 专业对应的班级集合
    private Set<String> allDepartments;
    private Set<String> allMajors;
    private Set<String> allClasses;

    public StudentManagementPanel(JTabbedPane tabbedPane, Component parentComponent) {
        this.tabbedPane = tabbedPane;
        this.parentComponent = parentComponent;
        initializeUI();
        loadAllStudents();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // ---------------------- 搜索面板 ----------------------
        JPanel searchPanel = new JPanel();

        // 初始化下拉框
        departComboBox = new JComboBox<>();
        majorComboBox = new JComboBox<>();
        classComboBox = new JComboBox<>();

        // 添加"全部"选项
        departComboBox.addItem("全部学院");
        majorComboBox.addItem("全部专业");
        classComboBox.addItem("全部班级");

        JButton searchButton = new JButton("搜索");

        searchPanel.add(new JLabel("学院:"));
        searchPanel.add(departComboBox);
        searchPanel.add(new JLabel("专业:"));
        searchPanel.add(majorComboBox);
        searchPanel.add(new JLabel("班级:"));
        searchPanel.add(classComboBox);
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);

        // ---------------------- 表格面板 ----------------------
        String[] columnNames = {"院系", "专业", "班级", "学号", "姓名", "性别"};
        tableModel = new DefaultTableModel(columnNames, 0);

        studentTable = new JTable(tableModel);
        studentTable.setPreferredScrollableViewportSize(new Dimension(800, 400));
        JScrollPane scrollPane = new JScrollPane(studentTable);
        add(scrollPane, BorderLayout.CENTER);

        // ---------------------- 操作按钮面板 ----------------------
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加学生");
        JButton deleteButton = new JButton("删除学生");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // ---------------------- 下拉框事件监听 ----------------------
        departComboBox.addActionListener(e -> {
            String depart = departComboBox.getSelectedItem().toString();
            updateMajorComboBox(depart);
            // 选择学院后自动清空班级下拉框
            classComboBox.setSelectedIndex(0);
        });

        majorComboBox.addActionListener(e -> {
            String major = majorComboBox.getSelectedItem().toString();
            updateClassComboBox(major);
        });

        // ---------------------- 搜索功能 ----------------------
        searchButton.addActionListener(e -> {
            String depart = departComboBox.getSelectedItem().toString();
            String major = majorComboBox.getSelectedItem().toString();
            String className = classComboBox.getSelectedItem().toString();

            // 将"全部"转换为null
            if ("全部学院".equals(depart)) depart = null;
            if ("全部专业".equals(major)) major = null;
            if ("全部班级".equals(className)) className = null;

            try {
                searchStudents(depart, major, className);
            } catch (SQLException ex) {
                handleError("搜索学生失败", ex);
            }
        });

        // ---------------------- 添加学生功能 ----------------------
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddStudentDialog();
            }
        });

        // ---------------------- 删除学生功能 ----------------------
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDeleteStudentDialog();
            }
        });
    }

    // 加载全部学生数据并初始化下拉框
    private void loadAllStudents() {
        try {
            tableModel.setRowCount(0);
            allStudents = student_info.getAlluserinfo();

            // 初始化数据结构
            allDepartments = new HashSet<>();
            allMajors = new HashSet<>();
            allClasses = new HashSet<>();
            majorByDepart = new HashMap<>();
            classByMajor = new HashMap<>();

            for (student_info stu : allStudents) {
                String depart = stu.getDepart();
                String major = stu.getMajor();
                String className = stu.getSclass();

                allDepartments.add(depart);
                allMajors.add(major);
                allClasses.add(className);

                // 构建学院-专业映射
                majorByDepart.computeIfAbsent(depart, k -> new HashSet<>()).add(major);
                // 构建专业-班级映射
                classByMajor.computeIfAbsent(major, k -> new HashSet<>()).add(className);

                tableModel.addRow(new Object[]{
                        depart, major, className, stu.getSno(), stu.getSname(), stu.getSex()
                });
            }

            // 更新下拉框选项
            updateComboBoxItems();

        } catch (SQLException ex) {
            handleError("加载学生数据失败", ex);
        }
    }

    // 更新下拉框选项
    private void updateComboBoxItems() {
        // 清空现有选项（保留"全部"）
        clearComboBox(departComboBox);
        clearComboBox(majorComboBox);
        clearComboBox(classComboBox);

        // 添加学院选项
        for (String depart : allDepartments) {
            departComboBox.addItem(depart);
        }

        // 初始时专业和班级下拉框只显示"全部"
    }

    // 清空下拉框选项（保留"全部"）
    private void clearComboBox(JComboBox<String> comboBox) {
        int size = comboBox.getItemCount();
        for (int i = size - 1; i >= 1; i--) {
            comboBox.removeItemAt(i);
        }
    }

    // 根据选择的学院更新专业下拉框
    private void updateMajorComboBox(String depart) {
        clearComboBox(majorComboBox);
        //majorComboBox.addItem("全部专业");

        if ("全部".equals(depart)) {
            // 选择"全部"学院时，显示所有专业
            for (String major : allMajors) {
                majorComboBox.addItem(major);
            }
        } else {
            // 选择特定学院时，只显示该学院的专业
            Set<String> majors = majorByDepart.getOrDefault(depart, new HashSet<>());
            for (String major : majors) {
                majorComboBox.addItem(major);
            }
        }

        // 清空班级下拉框
        clearComboBox(classComboBox);
        classComboBox.addItem("全部班级");
    }

    // 根据选择的专业更新班级下拉框
    private void updateClassComboBox(String major) {
        clearComboBox(classComboBox);
        //classComboBox.addItem("全部");

        if ("全部专业".equals(major)) {
            // 选择"全部"专业时，显示所有班级
            for (String className : allClasses) {
                classComboBox.addItem(className);
            }
        } else {
            // 选择特定专业时，只显示该专业的班级
            Set<String> classes = classByMajor.getOrDefault(major, new HashSet<>());
            for (String className : classes) {
                classComboBox.addItem(className);
            }
        }
    }

    // 按学院、专业、班级搜索学生
    private void searchStudents(String depart, String major, String className) throws SQLException {
        tableModel.setRowCount(0);

        // 从缓存中过滤数据
        List<student_info> filteredStudents = new ArrayList<>();
        for (student_info stu : allStudents) {
            if ((depart == null || depart.equals(stu.getDepart())) &&
                    (major == null || major.equals(stu.getMajor())) &&
                    (className == null || className.equals(stu.getSclass()))) {
                filteredStudents.add(stu);
            }
        }

        if (filteredStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未找到匹配的学生", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (student_info stu : filteredStudents) {
            tableModel.addRow(new Object[]{
                    stu.getDepart(), stu.getMajor(),
                    stu.getSclass(), stu.getSno(),
                    stu.getSname(), stu.getSex()
            });
        }
    }

    // 添加学生对话框
    private void showAddStudentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parentComponent), "添加学生", true);
        dialog.setSize(600, 350);
        dialog.setLocationRelativeTo(parentComponent);
        dialog.setLayout(new GridLayout(7, 2, 20, 20));

        // 表单组件
        JLabel snoLabel = new JLabel("学号:");
        JTextField snoField = new JTextField();
        JLabel nameLabel = new JLabel("姓名:");
        JTextField nameField = new JTextField();
        JLabel departLabel = new JLabel("院系:");
        JTextField departField = new JTextField();
        JLabel majorLabel = new JLabel("专业:");
        JTextField majorField = new JTextField();
        JLabel classNameLabel = new JLabel("班级:");
        JTextField classNameField = new JTextField();
        JLabel sexLabel = new JLabel("性别:");
        JTextField sexField = new JTextField();

        dialog.add(snoLabel); dialog.add(snoField);
        dialog.add(nameLabel); dialog.add(nameField);
        dialog.add(departLabel); dialog.add(departField);
        dialog.add(majorLabel); dialog.add(majorField);
        dialog.add(classNameLabel); dialog.add(classNameField);
        dialog.add(sexLabel); dialog.add(sexField);

        // 确定按钮
        JButton okButton = new JButton("确定");
        okButton.addActionListener(e -> {
            String sno = snoField.getText().trim();
            String name = nameField.getText().trim();
            String depart = departField.getText().trim();
            String major = majorField.getText().trim();
            String className = classNameField.getText().trim();
            String sex = sexField.getText().trim();
            String pwd = "111222";

            if (sno.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "学号和姓名为必填项", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            student_info stu = new student_info(depart, major, className, sno, name, sex, pwd);
            try {
                student_info.setStu(stu);
                JOptionPane.showMessageDialog(dialog, "学生添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadAllStudents(); // 刷新数据
            } catch (SQLException ex) {
                handleError("添加学生失败", ex);
            }
        });

        JButton noButton = new JButton("取消");
        noButton.addActionListener(e -> dialog.dispose());

        dialog.add(okButton);
        dialog.add(noButton);
        dialog.setVisible(true);
    }

    // 删除学生对话框
    private void showDeleteStudentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parentComponent), "删除学生", true);
        dialog.setSize(300, 180);
        dialog.setLocationRelativeTo(parentComponent);
        dialog.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout());
        JLabel snoLabel = new JLabel("请输入要删除的学号:");
        JTextField snoField = new JTextField(10);
        inputPanel.add(snoLabel);
        inputPanel.add(snoField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("确定删除");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        okButton.addActionListener(e -> {
            String sno = snoField.getText().trim();
            if (sno.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入学号", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "确定要删除学号为 " + sno + " 的学生吗？",
                    "确认删除", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    student_info.deleteStu(sno);
                    JOptionPane.showMessageDialog(dialog, "学生删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllStudents(); // 刷新数据
                } catch (SQLException ex) {
                    handleError("删除学生失败", ex);
                }
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // 错误处理方法
    private void handleError(String message, Throwable throwable) {
        JOptionPane.showMessageDialog(this, message + ": " + throwable.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        throwable.printStackTrace();
    }

    // 刷新面板
    public void refresh() {
        loadAllStudents();
    }
}