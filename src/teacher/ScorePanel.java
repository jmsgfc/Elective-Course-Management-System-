package teacher;

import DButil.*;
import info.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.*;
import java.util.List;

// 成绩管理面板（优化后）
public class ScorePanel extends JPanel {
    private JTabbedPane tabbedPane;
    private String teacherId;

    public ScorePanel(String teacherId) throws SQLException {
        this.teacherId = teacherId;
        setLayout(new BorderLayout());

        // 创建标签页面板
        tabbedPane = new JTabbedPane();

        // 添加"成绩查询"标签页（原有功能）
        tabbedPane.addTab("成绩查询", createQueryPanel());

        // 添加"成绩录入"标签页（新增功能）
        tabbedPane.addTab("成绩录入", createInputPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // 创建成绩查询面板（原ScorePanel的功能）
    private JPanel createQueryPanel() throws SQLException {
        JPanel queryPanel = new JPanel(new BorderLayout());

        // 搜索和下载区域
        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(20);//初始化
        JButton searchBtn = new JButton("搜索");


        JButton downloadBtn = new JButton("下载成绩");
        downloadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadScores();//按钮监听
            }
        });


        JComboBox<String>  termCB , courseCB;//学院，专业，班级，学期四级搜索

        termCB = new JComboBox<>();courseCB = new JComboBox<>();
        termCB.addItem("请选择学期");courseCB.addItem("请选择课程");
        student_info.loadTerms(termCB,teacherId);
        termCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String term = (String) termCB.getSelectedItem();
                if (term != null && !term.equals("请选择学期")) {//如果有效
                    // 清空courseCB现有选项
                    courseCB.removeAllItems();
                    // 添加默认提示项（可选）
                    courseCB.addItem("请选择课程");
                    // 加载新选项
                    List<String> courses = null;
                    try {
                        courses = score_info.courseByscores(term, teacherId);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (String course : courses) {
                        courseCB.addItem(course);
                    }
                } else {
                    // 未选择学期时，重置为所有课程（可选逻辑）
                    courseCB.removeAllItems();
                    courseCB.addItem("请选择课程");
                    List<String> courses = new ArrayList<>();
                    try {
                           courses = course_info.getTeaCourses(teacherId);
                    } catch (SQLException o) {
                        throw new RuntimeException(o);
                    }
                    for (String course : courses) {
                        courseCB.addItem(course);
                    }
                }
            }
        });



        //-----搜索区加入
        searchPanel.add(termCB);searchPanel.add(courseCB);
        searchPanel.add(searchBtn);searchPanel.add(downloadBtn);//搜索+下载按钮
        queryPanel.add(searchPanel, BorderLayout.NORTH);//北布局



        // 成绩表格
        String[] columnNames = {"学号", "姓名", "学院", "专业", "班级", "课程名称", "成绩" , "排名"};//表头
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);//表格数据模型
        JTable scoreTable = new JTable(tableModel);//创建表格组件，并将数据模型绑定到表格上
        loadAllScores(tableModel);//加载数据（表格的数据加载）

        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String term = (String) termCB.getSelectedItem();
                String course = (String) courseCB.getSelectedItem();
                searchScores(tableModel,term,course);
            }
        });


        JScrollPane scrollPane = new JScrollPane(scoreTable);//增加滚动条
        queryPanel.add(scrollPane, BorderLayout.CENTER);

        return queryPanel;
    }

    // 创建成绩录入面板（新增功能）
    private JPanel createInputPanel() throws SQLException {
        JPanel inputPanel = new JPanel(new BorderLayout());

        // 搜索面板
        JPanel searchPanel = new JPanel();

        // 学生成绩表格（可编辑）
        String[] columnNames = {"学号", "姓名", "学院", "专业", "班级","课程名" , "成绩"};
        DefaultTableModel inputTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 只有成绩列可编辑
            }
        };
        JTable inputTable = new JTable(inputTableModel);//
        JScrollPane scrollPane = new JScrollPane(inputTable);
        inputPanel.add(scrollPane, BorderLayout.CENTER);
        JComboBox<String> courseCB;//学院，专业，班级，学期四级搜索



        courseCB = new JComboBox<>();
        course_info.loadTeacherCourses(courseCB,teacherId);//获取该教师所教课程
        courseCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });


        JButton loadBtn = new JButton("加载学生");
        loadBtn.addActionListener(e -> {
            String course = courseCB.getSelectedItem().toString();
            if (inputTable.getModel() instanceof DefaultTableModel) {
                DefaultTableModel model = (DefaultTableModel) inputTable.getModel();
                loadStudentsForCourse(course, model);
            }
        });

        searchPanel.add(courseCB);searchPanel.add(loadBtn);
        
        // 保存成绩功能区
        JButton saveBtn = new JButton("保存成绩");

        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //调用saveScores，同时带有不合法判定
                    score_info.saveScores(inputTableModel,courseCB.getSelectedItem().toString(),inputPanel);
            }
        });


        searchPanel.add(saveBtn);
        inputPanel.add(searchPanel, BorderLayout.NORTH);//将搜索面板加入主面板的北部
        return inputPanel;
    }


    // 加载所选课程的学生列表（加载学生按钮操作）
    private void loadStudentsForCourse(String courseInfo, DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        if (courseInfo == null || courseInfo.isEmpty()) return;
        String sql1 = "SELECT \n" +
                "    s.sno AS sno,s.sname AS sname,\n" +
                "    s.depart AS depart, s.major AS major,\n" +
                "    s.sclass AS sclass,c.cname AS cname,\n" +
                "    sc.grade AS grade\n" +
                "FROM student s\n" +
                "LEFT JOIN scores sc ON s.sno = sc.sno\n" +
                "JOIN courses c ON sc.cno = c.cno\n" +
                "JOIN tcourse tc ON c.cno = tc.cno and tc.tno = ?\n" +//独特教师
                "WHERE c.cno = ?\n" + //独特课程
                "ORDER BY s.sno;";
        String courseId = courseInfo.split(" - ")[0];
        try (Connection conn = DButil.connection()){
             PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setString(1, teacherId);
            ps1.setString(2,courseId);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                tableModel.addRow(new Object[]{
                        rs1.getString("sno"),
                        rs1.getString("sname"),
                        rs1.getString("depart"),
                        rs1.getString("major"),
                        rs1.getString("sclass"),
                        rs1.getString("cname"),//增加课程名
                        rs1.getObject("grade") // 可能为null
                });
            }

        } catch (SQLException ex) {
           // ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载学生失败");
        }
    }

    // 加载所有成绩（原有方法，修改为接受tableModel参数）
    private void loadAllScores(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        try (Connection conn = DButil.connection();
             PreparedStatement ps1 = conn.prepareStatement(
                     "SELECT st.sno, st.sname, st.depart, st.major, st.sclass, c.cname, sc.grade " +
                             "FROM scores sc " +
                             "JOIN courses c ON sc.cno = c.cno " +
                             "JOIN student st ON sc.sno = st.sno " +
                             "JOIN tcourse tc ON c.cno = tc.cno " +
                             "WHERE tc.tno = ? " +
                             "ORDER BY sc.grade desc")) {
            ps1.setString(1, teacherId);
            ResultSet rs = ps1.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("sno"),
                        rs.getString("sname"),
                        rs.getString("depart"),
                        rs.getString("major"),
                        rs.getString("sclass"),
                        rs.getString("cname"),
                        rs.getInt("grade")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "成绩加载失败");
        }
    }

    // 搜索成绩（原有方法，修改为接受tableModel和keyword参数）
    private void searchScores(DefaultTableModel tableModel, String term , String course) {
        tableModel.setRowCount(0);
        String sql1 = "select s.sno as sno,s.sname as sname,s.depart as depart,s.major as major,\n" +
                "    s.sclass as sclass,c.cname as cname,sc.grade as grade\n" +
                "from student s\n" +
                "join scores sc on s.sno = sc.sno\n" +
                "join courses c on sc.cno = c.cno\n" +
                "join tcourse tc on tc.cno = c.cno\n" +
                "where 1=1 and tc.tno = ?\n";
        if(term != null && !term.equals("请选择学期")) sql1 += "and sc.cdate = ?";
        if(course != null && !course.equals("请选择课程")) sql1 += "and c.cname = ?";
        sql1 += "order by sc.grade desc, s.sno , c.cno";//排序方式
        try (Connection conn = DButil.connection()){
             PreparedStatement ps1 = conn.prepareStatement(sql1);
            int index = 2;//定义传参索引
            int rank = 1;//定义排名
            ps1.setString(1,teacherId);
            if(term != null && !term.equals("请选择学期"))  ps1.setString(index++,term);
            if(course != null && !course.equals("请选择课程")) ps1.setString(index++,course);

            ResultSet rs = ps1.executeQuery();//查询结果集
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("sno"),
                        rs.getString("sname"),
                        rs.getString("depart"),
                        rs.getString("major"),
                        rs.getString("sclass"),
                        rs.getString("cname"),
                        rs.getInt("grade"),
                        rank++
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "搜索失败");
        }
    }

    // 下载成绩（原有方法，保持不变）
    private void downloadScores() {
        // 生成带时间戳的文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "成绩表_" + timestamp + ".txt";

        try (FileWriter writer = new FileWriter(fileName)) {
            // 获取当前选中的表格
            JTable currentTable = (JTable) ((JScrollPane)
                    ((JPanel) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex())).getComponent(1)).getViewport().getView();

            // 写入表头
            for (int i = 0; i < currentTable.getColumnCount(); i++) {
                writer.write(currentTable.getColumnName(i) + "       ");
            }
            writer.write("\n");

            // 写入表格数据
            for (int i = 0; i < currentTable.getRowCount(); i++) {
                for (int j = 0; j < currentTable.getColumnCount(); j++) {
                    Object value = currentTable.getValueAt(i, j);
                    writer.write((value != null ? value.toString() : "    ") + "    ");
                }
                writer.write("\n");
            }

            // 显示保存成功消息（包含完整路径）
            String filePath = new java.io.File(fileName).getAbsolutePath();
            JOptionPane.showMessageDialog(this, "成绩文件已保存到：\n" + filePath);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "导出文件失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}