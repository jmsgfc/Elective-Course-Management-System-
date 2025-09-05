package admin;

import DButil.DButil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * 信息查询大面板类，包含成绩总表、成绩统计、选课查询等子面板
 */
public class StatisticsPanel extends JPanel {
    private JTabbedPane statsTabbedPane;
    private JFrame parentJFrame;
    private Connection conn;

    public StatisticsPanel(JFrame parentJFrame) throws SQLException {
        this.parentJFrame = parentJFrame;
        setLayout(new BorderLayout());
        init();
    }

    private void init() {
        statsTabbedPane = new JTabbedPane();
        statsTabbedPane.addTab("成绩总表", new ScorePanel());
        statsTabbedPane.addTab("成绩统计", new DepartmentStatisticsPanel());
        statsTabbedPane.addTab("选课查询", new CourseStatisticsPanel());
        statsTabbedPane.addTab("学年学期统计", new CreditStatisticsPanel()); // 新增标签页
        add(statsTabbedPane, BorderLayout.CENTER);
    }

    // 数据库连接初始化
//    private void initDatabaseConnection() {
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            conn = DriverManager.getConnection(
//                    "jdbc:mysql://localhost:3306/studentoperation?useSSL=false&serverTimezone=UTC",
//                    "root", "yourpassword"
//            );
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this, "数据库连接失败", "错误", JOptionPane.ERROR_MESSAGE);
//        }
//    }

    /**
     * 成绩总表面板
     */
    private class ScorePanel extends JPanel {

        public ScorePanel() {
            setLayout(new BorderLayout());

            JPanel searchPanel = new JPanel();
            JTextField stuNo2 = new JTextField(10);
            JButton searchButton = new JButton("搜索");
            JButton downloadB = new JButton("下载成绩");
            JLabel stuName1 = new JLabel("学生搜索：");
            searchPanel.add(stuName1);
            searchPanel.add(stuNo2);
            searchPanel.add(searchButton);
            searchPanel.add(downloadB);

            // 创建表格模型
            DefaultTableModel tableModel = new DefaultTableModel(
                    new String[]{"院系", "专业", "班级","学号", "姓名", "学期", "课程名称", "成绩"},
                    0
            );

            // 创建表格
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);
            add(searchPanel, BorderLayout.NORTH);
            // 实际应用中从数据库加载数据
            loadCreditData(tableModel);

            searchButton.addActionListener(e -> {
                String searchText = stuNo2.getText().trim();
                if (searchText.isEmpty()) {
                    // 如果搜索框为空，重新加载所有数据
                    loadCreditData(tableModel);
                } else {
                    // 根据学生学号搜索
                    String sql = "SELECT s.depart as depart, s.major as major, s.sclass as sclass, s.sno as sno, " +
                            "s.sname as sname, sc.cdate as cdate, co.cname as cname, sc.grade as grade " +
                            "FROM student s " +
                            "JOIN scores sc ON s.sno = sc.sno " +
                            "JOIN courses co ON sc.cno = co.cno " +
                            "WHERE s.sno LIKE ? " +
                            "ORDER BY s.sno, co.cno";

                    try (Connection conn = DButil.connection()) {
                        tableModel.setRowCount(0); // 清空表格
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, "%" + searchText + "%");
                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                            tableModel.addRow(new Object[]{
                                    rs.getString("depart"),
                                    rs.getString("major"),
                                    rs.getString("sclass"),
                                    rs.getString("sno"),
                                    rs.getString("sname"),
                                    rs.getString("cdate"),
                                    rs.getString("cname"),
                                    rs.getInt("grade")
                            });
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "搜索失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            // 添加下载按钮事件
            // 在ScorePanel类中修改下载按钮的事件处理
            downloadB.addActionListener(e -> {
                if (tableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "表格中没有数据可导出", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // 获取当前工作目录
                String currentPath = System.getProperty("user.dir");
                // 生成文件名
                String fileName = "成绩报表_" + System.currentTimeMillis() + ".csv";
                // 创建文件对象
                File fileToSave = new File(currentPath, fileName);

                try (FileWriter writer = new FileWriter(fileToSave)) {
                    // 写入表头
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        writer.write(tableModel.getColumnName(i));
                        if (i < tableModel.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");

                    // 写入数据
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        for (int j = 0; j < tableModel.getColumnCount(); j++) {
                            Object value = tableModel.getValueAt(i, j);
                            // 处理可能包含逗号的值
                            if (value != null && value.toString().contains(",")) {
                                writer.write("\"" + value + "\"");
                            } else {
                                writer.write(String.valueOf(value));
                            }
                            if (j < tableModel.getColumnCount() - 1) {
                                writer.write(",");
                            }
                        }
                        writer.write("\n");
                    }

                    JOptionPane.showMessageDialog(this,
                            "成绩报表已成功保存到: \n" + fileToSave.getAbsolutePath(),
                            "成功",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "保存成绩报表时出错: " + ex.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });
        }



        private void loadCreditData(DefaultTableModel model) {
            String sql1 = "SELECT s.depart as depart, s.major as major, s.sclass as sclass,s.sno as sno, s.sname as sname, \n" +
                    "sc.cdate as cdate, co.cname as cname, sc.grade as grade \n" +
                    "FROM student s \n" +
                    "JOIN scores sc ON s.sno = sc.sno \n" +
                    "JOIN courses co ON sc.cno = co.cno \n" +
                    "ORDER BY s.sno, co.cno";
            try (Connection conn = DButil.connection()) {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ResultSet rs1 = ps1.executeQuery();
                while (rs1.next()) {
                    model.addRow(new Object[]{
                            rs1.getString("depart"),
                            rs1.getString("major"),
                            rs1.getString("sclass"),
                            rs1.getString("sno"),
                            rs1.getString("sname"),
                            rs1.getString("cdate"),
                            rs1.getString("cname"),
                            rs1.getInt("grade")
                    });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "加载成绩数据失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 成绩统计（绩点排名）面板
     */
    private class DepartmentStatisticsPanel extends JPanel {
        public DepartmentStatisticsPanel() {
            setLayout(new BorderLayout());

            JPanel searchPanel = new JPanel();
            JComboBox<String> departCB = new JComboBox<>();
            JComboBox<String> majorCB = new JComboBox<>();
            JComboBox<String> classCB = new JComboBox<>();
            majorCB.addItem("请选择专业");classCB.addItem("请选择班级");
            loadDepartments(departCB);
            departCB.addActionListener(e -> loadMajorsByDepart(majorCB, (String) departCB.getSelectedItem()));
            majorCB.addActionListener(e -> loadClassesByMajor(classCB, (String) majorCB.getSelectedItem()));
            searchPanel.add(departCB);searchPanel.add(majorCB);searchPanel.add(classCB);

            JLabel one1 = new JLabel("学分区间：");
            JTextField minField = new JTextField(4);
            JLabel one2 = new JLabel("——");
            JTextField maxField = new JTextField(4);

            JButton searchButton = new JButton("搜索");
            JButton downloadButton = new JButton("下载报表");

            searchPanel.add(one1);searchPanel.add(minField);searchPanel.add(one2);searchPanel.add(maxField);
            searchPanel.add(searchButton);searchPanel.add(downloadButton);


            add(searchPanel, BorderLayout.NORTH);

            // 创建表格模型
            DefaultTableModel tableModel = new DefaultTableModel(
                    new String[]{"院系", "专业", "班级", "姓名", "总学分", "绩点", "排名"},
                    0
            );

            // 创建表格
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);

            // 初始加载数据
            loadDepartmentData(tableModel, null, null, null,null,null);

            // 搜索按钮事件
            searchButton.addActionListener(e -> {

                String depart = (String) departCB.getSelectedItem();//院系
                String major = (String) majorCB.getSelectedItem();//专业
                String clazz = (String) classCB.getSelectedItem();//班级

                String minStr =minField.getText().trim();
                String maxStr =maxField.getText().trim();

                // 校验空值：若未输入则设为null
                String min = minStr.isEmpty() ? null : minStr;
                String max = maxStr.isEmpty() ? null : maxStr;

                loadDepartmentData(tableModel,
                        depart.equals("全部学院") ? null : depart,
                        major.equals("全部专业") ? null : major,
                        clazz.equals("全部班级") ? null : clazz,
                        min,
                        max
                );
            });


        }

        private void loadDepartments(JComboBox<String> comboBox) {
            comboBox.addItem("全部学院");
            String sql1 = "SELECT DISTINCT depart FROM student";
            try (Connection conn = DButil.connection()) {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ResultSet rs = ps1.executeQuery();
                while (rs.next()) {
                    comboBox.addItem(rs.getString("depart"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void loadMajorsByDepart(JComboBox<String> comboBox, String depart) {
            comboBox.removeAllItems();
            comboBox.addItem("全部专业");
            if (depart == null || depart.equals("全部学院")) return;
            String sql1 = "SELECT DISTINCT major FROM student WHERE depart = ?";
            try (Connection conn = DButil.connection()) {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ps1.setString(1, depart);
                try (ResultSet rs = ps1.executeQuery()) {
                    while (rs.next()) {
                        comboBox.addItem(rs.getString("major"));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void loadClassesByMajor(JComboBox<String> comboBox, String major) {
            comboBox.removeAllItems();
            comboBox.addItem("全部班级");
            if (major == null || major.equals("全部专业")) return;
            String sql1 = "SELECT DISTINCT sclass FROM student WHERE major = ?";
            try (Connection conn = DButil.connection()) {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ps1.setString(1, major);
                try (ResultSet rs = ps1.executeQuery()) {
                    while (rs.next()) {
                        comboBox.addItem(rs.getString("sclass"));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void loadDepartmentData(DefaultTableModel model, String depart, String major, String clazz,String min , String max) {
            model.setRowCount(0);
            try {
                StringBuilder sql = new StringBuilder(
                        "SELECT s.depart, s.major, s.sclass, s.sname, " +
                                "SUM(c.credit) AS total_credit, " +
                                "ROUND(SUM(c.credit * CASE " +
                                "    WHEN sc.grade >= 90 THEN 4.0 " +
                                "    WHEN sc.grade >= 85 THEN 3.7 " +
                                "    WHEN sc.grade >= 80 THEN 3.3 " +
                                "    WHEN sc.grade >= 75 THEN 3.0 " +
                                "    WHEN sc.grade >= 70 THEN 2.7 " +
                                "    WHEN sc.grade >= 65 THEN 2.3 " +
                                "    WHEN sc.grade >= 60 THEN 2.0 " +
                                "    ELSE 0 END) / SUM(c.credit), 2) AS gpa " +
                                "FROM student s " +
                                "JOIN scores sc ON s.sno = sc.sno " +
                                "JOIN courses c ON sc.cno = c.cno " +
                                "WHERE 1=1 "
                );

                int index = 1;
                if (depart != null) {
                    sql.append("and s.depart = ? ");
                }
                if (major != null) {
                    sql.append("and s.major = ? ");
                }
                if (clazz != null) {
                    sql.append("and s.sclass = ? ");
                }

                sql.append("group by s.sno, s.depart, s.major, s.sclass, s.sname  "); // 修正GROUP BY

                if (min != null) {
                    if (max != null) {
                        sql.append("having sum(c.credit) between ? AND ? "); // 完整区间
                    } else {
                        sql.append("having sum(c.credit) >= ? "); // 下限
                    }
                } else if (max != null) { // 新增：处理单独上限
                    sql.append("having sum(c.credit) <= ? ");
                }

                sql.append(" order by gpa DESC ");

                try (Connection conn = DButil.connection()) {
                    PreparedStatement ps1 = conn.prepareStatement(sql.toString());
                    if(depart != null)ps1.setString(index++,depart);
                    if(major != null) ps1.setString(index++,major);
                    if(clazz != null)ps1.setString(index++,clazz);
                    if(min!=null){
                        if(max != null) {
                            ps1.setInt(index++, Integer.parseInt(min));
                            ps1.setInt(index++,Integer.parseInt(max));
                        }
                        else ps1.setInt(index++,Integer.parseInt(min));
                    }



                    try (ResultSet rs = ps1.executeQuery()) {
                        int rank = 1;
                        while (rs.next()) {
                            model.addRow(new Object[]{
                                    rs.getString("depart"),
                                    rs.getString("major"),
                                    rs.getString("sclass"),
                                    rs.getString("sname"),
                                    rs.getInt("total_credit"),
                                    rs.getDouble("gpa"),
                                    rank++
                            });
                        }
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "加载统计数据失败", "错误", JOptionPane.ERROR_MESSAGE);
                System.out.println(ex);
            }
        }

    }

    /**
     * 选课查询面板
     */
    private class CourseStatisticsPanel extends JPanel {
        private DefaultTableModel tableModel;
        private JComboBox<String> courseCB;
        JLabel tea1 = new JLabel("教师工号");
        JTextField tea11 = new JTextField(8);
        JLabel stu1 = new JLabel("学生学号：");
        JTextField stu11 = new JTextField(8);
        public CourseStatisticsPanel() {
            setLayout(new BorderLayout());

            // 创建搜索面板
            JPanel searchPanel = new JPanel();
            searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

            // 课程下拉框
            courseCB = new JComboBox<>();
            courseCB.addItem("全部课程");
            loadCourseList();


            // 搜索按钮
            JButton searchButton = new JButton("搜索");
            searchButton.addActionListener(e -> performSearch());

            // 下载报表按钮
            JButton downloadButton = new JButton("下载报表");
            downloadButton.addActionListener(e -> downloadReport());

            // 添加组件到搜索面板
            searchPanel.add(courseCB);
            searchPanel.add(tea1);searchPanel.add(tea11);searchPanel.add(stu1);searchPanel.add(stu11);
            searchPanel.add(searchButton);
            searchPanel.add(downloadButton);

            // 将搜索面板添加到顶部
            add(searchPanel, BorderLayout.NORTH);

            // 创建表格模型
            tableModel = new DefaultTableModel(
                    new String[]{"课程名称","任课教师", "专业", "班级", "学号", "姓名", "性别"},
                    0
            );

            // 创建表格
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);
            add(scrollPane, BorderLayout.CENTER);

            // 初始加载所有数据
            loadCourseData(tableModel, null, null, null);
        }

        // 加载课程列表
        private void loadCourseList() {
            String sql1 = "SELECT cno, cname FROM courses";
            try (Connection conn = DButil.connection()) {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ResultSet rs1 = ps1.executeQuery();
                while (rs1.next()) {
                    courseCB.addItem(rs1.getString("cname"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println(ex);
            }
        }


        // 执行搜索（一级搜索）
        private void performSearch() {
            tableModel.setRowCount(0); // 清空表格

            String course = (String) courseCB.getSelectedItem();
            String teacherId = tea11.getText();
            String studentId = stu11.getText();

            // 根据选择的条件加载数据
            loadCourseData(tableModel,
                    course.equals("全部课程") ? null : course,
                    teacherId,
                    studentId
            );
        }

        // 加载课程数据（多级搜索）
        private void loadCourseData(DefaultTableModel model, String courseName, String teacherId, String studentId) {
            try {
                StringBuilder sql = new StringBuilder(
                        "SELECT co.cname AS cname, t.tname AS tname, s.major AS major, " +
                                "s.sclass AS sclass, s.sno AS sno, s.sname AS sname, s.sex AS sex " +
                                "FROM student s " +
                                "JOIN scores sc ON s.sno = sc.sno " +
                                "JOIN courses co ON sc.cno = co.cno " +
                                // 使用子查询确保每个课程只关联一个教师
                                "JOIN ( " +
                                "  SELECT cno, MIN(tno) AS tno " +
                                "  FROM tcourse " +
                                "  GROUP BY cno " +
                                ") tc ON co.cno = tc.cno " +
                                "JOIN teacher t ON tc.tno = t.tno " +
                                "WHERE 1=1 "
                );

                List<Object> params = new ArrayList<>();

                // 添加课程名称条件
                if (courseName != null && !courseName.trim().isEmpty()) {
                    sql.append("AND co.cname = ? ");
                    params.add(courseName.trim());
                }

                // 添加教师ID条件
                if (teacherId != null && !teacherId.trim().isEmpty()) {
                    sql.append("AND t.tno = ? ");
                    params.add(teacherId.trim());
                }

                // 添加学生ID条件
                if (studentId != null && !studentId.trim().isEmpty()) {
                    sql.append("AND s.sno = ? ");
                    params.add(studentId.trim());
                }

                sql.append("ORDER BY co.cname, s.sno");

                try (Connection conn = DButil.connection();
                     PreparedStatement ps1 = conn.prepareStatement(sql.toString())) {

                    // 设置参数
                    for (int i = 0; i < params.size(); i++) {
                        ps1.setObject(i + 1, params.get(i));
                    }

                    System.out.println("执行SQL: " + sql.toString());
                    System.out.println("参数: " + params);

                    try (ResultSet rs1 = ps1.executeQuery()) {
                        while (rs1.next()) {
                            model.addRow(new Object[]{
                                    rs1.getString("cname"),
                                    rs1.getString("tname"),
                                    rs1.getString("major"),
                                    rs1.getString("sclass"),
                                    rs1.getString("sno"),
                                    rs1.getString("sname"),
                                    rs1.getString("sex")
                            });
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "加载选课数据失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }

        // 下载报表
        private void downloadReport() {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "表格中没有数据可导出", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("保存报表");
            fileChooser.setSelectedFile(new java.io.File("选课报表_" + System.currentTimeMillis() + ".txt"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                try (java.io.FileWriter writer = new java.io.FileWriter(fileToSave)) {
                    // 写入表头
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        writer.write(tableModel.getColumnName(i) + "\t");
                    }
                    writer.write("\n");

                    // 写入数据
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        for (int j = 0; j < tableModel.getColumnCount(); j++) {
                            writer.write(tableModel.getValueAt(i, j) + "\t");
                        }
                        writer.write("\n");
                    }

                    JOptionPane.showMessageDialog(this, "报表已成功保存到: " + fileToSave.getAbsolutePath(),
                            "成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "保存报表时出错: " + ex.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 学分统计面板
     */
    private class CreditStatisticsPanel extends JPanel {
        private JComboBox<String> yearCB,monthCB;
        private JButton selectButton;
        private DefaultTableModel tableModel;



        public CreditStatisticsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // 结果表格
            tableModel = new DefaultTableModel(
                    new String[]{"院系", "专业", "班级", "学号", "姓名","学分","总绩点"},
                    0
            );
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);

            // 搜索面板
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));

            // 学年学期下拉框
            yearCB = new JComboBox<>();
            yearCB.addItem("请选择学年");
            loadYearCB(yearCB);
            monthCB = new JComboBox<>();
            monthCB.addItem("请选择学期");
            monthCB.addItem("1");
            monthCB.addItem("2");

            // 统计按钮
            selectButton = new JButton("查询");
            selectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!yearCB.getSelectedItem().equals("请选择学年")){
                        String year = (String)yearCB.getSelectedItem();
                        String month = (String)monthCB.getSelectedItem();
                        try {
                            getStatistInfo(tableModel,year,month);//获取学年学期结果

                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            });


            searchPanel.add(new JLabel("学年学期："));
            searchPanel.add(yearCB);searchPanel.add(monthCB);//增加学年学期

            searchPanel.add(selectButton);//搜索按钮



            add(searchPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
        }

        /**
         * 加载所有学年学期
         */
        private void loadYearCB(JComboBox<String> yearCB) {
            String sql1 = "select distinct cdate from courses";
            Set<String> yearSet = new TreeSet<>();
            try (Connection conn = DButil.connection()) {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ResultSet rs = ps1.executeQuery();
                String year = "";
                while (rs.next()) {
                    if(rs.getString("cdate" ) != null) {
                        String[] years = rs.getString("cdate").split("-");
                        year = years[0] + "-" + years[1];
                    }

                    yearSet.add(year);
                    //yearCB.addItem(year);
                }
                for (String s : yearSet) {
                    yearCB.addItem(s);
                }
            }
             catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "加载学期数据失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }


        private void getStatistInfo(DefaultTableModel tableModel,String year,String month) throws SQLException {
            tableModel.setRowCount(0);
            String sql1 = "SELECT s.depart AS depart, s.major AS major, s.sclass AS sclass, " +
                    "s.sno AS sno, s.sname AS sname, " +
                    "SUM(c.credit) AS sumcredit, " +
                    "SUM(CASE WHEN sc.grade >= 60 THEN (sc.grade / 10 - 5) * c.credit ELSE 0 END) AS sumpoint " +
                    "FROM student s " +
                    "LEFT JOIN scores sc ON s.sno = sc.sno AND sc.cdate LIKE ? " + // 使用 LIKE 匹配学年或学期
                    "LEFT JOIN courses c ON sc.cno = c.cno " +
                    "GROUP BY s.sno, s.sname, s.depart, s.major, s.sclass " +
                    "ORDER BY sumpoint DESC";

            String add_sql = "";

            if(!month.equals("请选择学期"))
                add_sql+=year + "-" + month;
            else add_sql = year + "-%";

            try(Connection conn = DButil.connection()){
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ps1.setString(1,add_sql);
                ResultSet rs1 = ps1.executeQuery();
                while (rs1.next()){
                    String depart = rs1.getString("depart");
                    String major = rs1.getString("major");
                    String sclass = rs1.getString("sclass");
                    String sno = rs1.getString("sno");
                    String sname = rs1.getString("sname");
                    String sumcredit = rs1.getString("sumcredit");
                    String sumpoint = rs1.getString("sumpoint");
                    tableModel.addRow(new Object[]{
                            depart,major,sclass,sno,sname,sumcredit,sumpoint
                    });
                }
            }
        }

        /**
         * 加载学院列表
         */
        private void loadDepartments(JComboBox<String> comboBox) {
            String sql1 = "SELECT DISTINCT depart FROM student WHERE depart IS NOT NULL";
            try (Connection conn = DButil.connection()) {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ResultSet rs = ps1.executeQuery();
                    while (rs.next()) {
                        comboBox.addItem(rs.getString("depart"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * 加载专业列表（根据学院）
         */
        private void loadMajors(JComboBox<String> comboBox, String depart) {
            comboBox.removeAllItems();
            comboBox.addItem("全部专业");
            if (depart == null) return;
            String sql1 = "SELECT DISTINCT major FROM student WHERE depart = ? AND major IS NOT NULL";
            try (Connection conn = DButil.connection()) {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ps1.setString(1, depart);
                ResultSet rs = ps1.executeQuery();
                    while (rs.next()) {
                        comboBox.addItem(rs.getString("major"));
                    }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * 加载班级列表（根据专业）
         */
        private void loadClasses(JComboBox<String> comboBox, String major) {
            comboBox.removeAllItems();
            comboBox.addItem("全部班级");
            if (major == null) return;
            String sql1 = "SELECT DISTINCT sclass FROM student WHERE major = ? AND sclass IS NOT NULL";
            try (Connection conn = DButil.connection()) {
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                ps1.setString(1, major);
                try (ResultSet rs = ps1.executeQuery()) {
                    while (rs.next()) {
                        comboBox.addItem(rs.getString("sclass"));
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * 执行学分统计查询
         */
        private List<Object[]> queryCreditStatistics(String semester, String depart, String major, String clazz, String range)
                throws SQLException {

            List<Object[]> result = new ArrayList<>();
            String sql = "SELECT " +
                    "s.depart AS depart, " +
                    "s.major AS major, " +
                    "s.sclass AS sclass, " +
                    "CASE WHEN total_credit BETWEEN 0 AND 10 THEN '0-10' " +
                    "WHEN total_credit BETWEEN 11 AND 20 THEN '10-20' " +
                    "ELSE '20-30' END AS credit_range, " +
                    "COUNT(DISTINCT s.sno) AS student_count " +
                    "FROM student s " +
                    "JOIN scores sc ON s.sno = sc.sno " +
                    "JOIN courses c ON sc.cno = c.cno " +
                    "WHERE c.cdate = ? " +
                    "AND c.cno IN ('0005', '0006', '0007') " + // 选修课编号
                    "AND s.depart = COALESCE(?, s.depart) " +
                    "AND s.major = COALESCE(?, s.major) " +
                    "AND s.sclass = COALESCE(?, s.sclass) " +
                    "GROUP BY depart, major, sclass, credit_range ";

            if (!range.equals("全部")) {
                sql += "HAVING credit_range = ?";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int idx = 1;
                pstmt.setString(idx++, semester);
                pstmt.setString(idx++, depart);
                pstmt.setString(idx++, major);
                pstmt.setString(idx++, clazz);
                if (!range.equals("全部")) {
                    pstmt.setString(idx++, range);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        result.add(new Object[]{
                                rs.getString("depart"),
                                rs.getString("major"),
                                rs.getString("sclass"),
                                rs.getString("credit_range"),
                                rs.getInt("student_count")
                        });
                    }
                }
            }
            return result;
        }

        /**
         * 更新表格数据
         */
        private void updateTable(List<Object[]> data) {
            tableModel.setRowCount(0);
            for (Object[] row : data) {
                tableModel.addRow(row);
            }
        }
    }
}