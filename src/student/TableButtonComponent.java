package student;

import DButil.DButil;
import gui.stuGUI;
import info.course_info;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.table.TableCellEditor;

/**
 * 表格按钮渲染器和编辑器的组合类
 * 实现了在表格单元格中显示可点击按钮的功能
 */
public class TableButtonComponent extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor {

    private JButton renderButton; // 用于渲染的按钮
    private JButton editButton;   // 用于编辑的按钮
    private String studentId;
    private DefaultTableModel tableModel;
    private JFrame parentFrame;
    private String label;
    private int row;
    private int column;
    private JTable table; // 保存表格引用

    public TableButtonComponent(String studentId, DefaultTableModel tableModel, JFrame parentFrame) {
        this.studentId = studentId;
        this.tableModel = tableModel;
        this.parentFrame = parentFrame;

        // 初始化渲染按钮
        renderButton = new JButton();
        renderButton.setOpaque(true);

        // 初始化编辑按钮
        editButton = new JButton();
        editButton.setOpaque(true);

        // 处理按钮点击事件
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 记录点击时的选中状态
                boolean wasSelected = table.isRowSelected(row);

                // 触发编辑停止事件，会调用getCellEditorValue()
                fireEditingStopped();

                // 恢复选中状态
                if (wasSelected) {
                    table.setRowSelectionInterval(row, row);
                }
            }
        });
    }

    // ===== 实现 TableCellRenderer 接口 =====

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        // 保存表格引用
        this.table = table;

        // 设置按钮文本
        label = (value == null) ? "" : value.toString();
        renderButton.setText(label);



        return renderButton;
    }

    // ===== 实现 TableCellEditor 接口 =====

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        // 保存当前行、列和表格引用
        this.row = row;
        this.column = column;
        this.table = table;

        // 设置按钮文本
        label = (value == null) ? "" : value.toString();
        editButton.setText(label);



        return editButton;
    }



    @Override
    public Object getCellEditorValue() {
        try {
            String courseNo = tableModel.getValueAt(row, 0).toString();

            if ("选课".equals(label)) {
                handleCourseSelection(courseNo);
            } else if ("退课".equals(label)) {
                handleCourseDrop(courseNo);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }

        return label;
    }

    // ===== 业务逻辑方法 =====

    /**
     * 处理选课逻辑
     */
    private void handleCourseSelection(String courseNo) throws SQLException {
        int maxStudents = Integer.parseInt(tableModel.getValueAt(row, 4).toString());//最大值
        int currentStudents = Integer.parseInt(tableModel.getValueAt(row, 5).toString());//目前学生
        String term = tableModel.getValueAt(row, 6).toString();//学年学期

        if (maxStudents > currentStudents) {
            course_info.addTo_course(studentId, courseNo, term);
            showSuccessMessage("选课成功");
            refreshPanel();//刷新面板
        } else {
            showErrorMessage("选课失败，课程已满");
//            String sql1 = "select cname from courses where cno =?;";
//
//            String sql2 = "insert into info (cno, cname, infos, cdate) " +
//                    "values (?, ?, '选课人数已满', CURDATE()) " +
//                    "on duplicate key update infos = values(infos)";
//            Connection conn = DButil.connection();
//            PreparedStatement ps1 = conn.prepareStatement(sql1);
//            ps1.setString(1,courseNo);
//            ResultSet rs1 = ps1.executeQuery();//查询
//            String courseName = "";
//            while (rs1.next()) courseName = rs1.getString("cname");
//            PreparedStatement ps2 = conn.prepareStatement(sql2);
//            ps2.setString(1, courseNo);
//            ps2.setString(2, courseName);
//            ps2.executeUpdate();//更新
        }
    }

    /**
     * 处理退课逻辑
     */
    private void handleCourseDrop(String courseNo) throws SQLException {
        String semester = tableModel.getValueAt(row, 4).toString();

        if (semester.equals("2024-2025-1") && course_info.dropTo_course(studentId, courseNo)) {
            showSuccessMessage("退课成功");
            refreshPanel();
        } else {
            showErrorMessage("退课失败，非本学期课程");
        }
    }


     //刷新面板
    private void refreshPanel() throws SQLException {
//        try {
//            if (parentFrame instanceof stuGUI) {
                stuGUI.clearPanel();
                parentFrame.dispose();
                new stuGUI(studentId).setVisible(true);
//            }
//        } catch (SQLException e) {
//            handleDatabaseError(e);
//        }
    }

    /**
     * 显示成功消息
     */
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 显示错误消息
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "失败", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 处理数据库异常
     */
    private void handleDatabaseError(SQLException e) {
        showErrorMessage("数据库操作失败：" + e.getMessage());
    }
}