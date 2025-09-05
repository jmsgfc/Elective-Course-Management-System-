package student;

import gui.stuGUI;
import info.course_info;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;

/**
 * 可选课程面板类
 */
public class CourseSelectionPanel extends JPanel {
    private final String studentId;
    private final stuGUI parentGUI;
    private JTable courseTable;
    private DefaultTableModel tableModel;

    public CourseSelectionPanel(stuGUI parentGUI, String studentId) throws SQLException {
        super(null); // 保持绝对布局
        this.parentGUI = parentGUI;
        this.studentId = studentId;
        initComponents();
        loadCourseData();
    }

    /**
     * 初始化界面组件
     */
    private void initComponents() {
        // 初始化表格模型
        tableModel = new DefaultTableModel(
                new String[]{"课程编号", "课程名", "教师名", "学分", "最大人数", "当前人数", "开课时间", "操作"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // 只有"操作"列可编辑
            }
        };

        courseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBounds(10, 10, 760, 430);
        add(scrollPane);
    }

    /**
     * 加载可选课程数据
     */
    private void loadCourseData() throws SQLException {
        List<course_info> cours = course_info.getAllCourses();
        for (course_info courseinfo : cours) {
            // 检查是否已选且学年对应
            if (courseinfo.getDate().equals("2024-2025-1") && course_info.checkIn_choose(studentId, courseinfo.getCno())) {
                tableModel.addRow(new Object[]{
                        courseinfo.getCno(), courseinfo.getCname(),
                        courseinfo.getTname(), courseinfo.getCredit(),
                        courseinfo.getMax_come(), courseinfo.getCurrent_come(),
                        courseinfo.getDate(), "选课"
                });
            }
        }

        // 设置选课按钮渲染器和编辑器（选课操作细节）
        TableButtonComponent TBC = new TableButtonComponent(studentId,tableModel,parentGUI);
        courseTable.getColumn("操作").setCellEditor(TBC);//操作器
        courseTable.getColumn("操作").setCellRenderer(TBC);//渲染器
        course_info.getCourseCount(tableModel);//执行表格检查
    }

}