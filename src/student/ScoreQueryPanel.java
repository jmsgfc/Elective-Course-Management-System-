package student;

import gui.stuGUI;
import info.score_info;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 学生成绩查询面板类
 */
public class ScoreQueryPanel extends JPanel {
    private final stuGUI parentGUI;
    private final String studentId;
    private DefaultTableModel tableModel;
    private JTable scoreTable;

    /**
     * 构造函数
     * @param parentGUI 父窗口引用
     * @param studentId 学生ID
     * @throws SQLException 数据库操作异常
     */
    public ScoreQueryPanel(stuGUI parentGUI, String studentId) throws SQLException {
        super(new BorderLayout(10, 10));
        this.parentGUI = parentGUI;
        this.studentId = studentId;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        loadScoreData();
    }

    /**
     * 初始化界面组件
     */
    private void initComponents() {
        // 初始化表格模型
        tableModel = new DefaultTableModel(
                new String[]{"学年学期","课程名称", "学分", "成绩","备注"},
                0
        );

        scoreTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton printButton = new JButton("打印全部成绩");
        printButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        printButton.addActionListener(e -> printScores());
        buttonPanel.add(printButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 加载成绩数据
     * @throws SQLException 数据库操作异常
     */
    private void loadScoreData() throws SQLException {
        List<score_info> scoreinfos = score_info.getAllscore(studentId);
        for (score_info scoreinfo : scoreinfos) {
            String type = "";
            if(scoreinfo.getScore() == -1) type = "成绩未录入或暂未考试";
            tableModel.addRow(new Object[]{
                    scoreinfo.getDate(),
                    scoreinfo.getCname(),
                    scoreinfo.getCredit(),
                    scoreinfo.getScore(),
                    type//备注
            });
        }
    }

    /**
     * 打印成绩到文件
     */
    private void printScores() {
        try {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(parentGUI, "没有成绩数据可供打印", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Path filePath = Paths.get(System.getProperty("user.dir"), "成绩_" + studentId + ".txt");
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write("学生成绩表 - 学号: " + studentId + "\n");
                writer.write("=".repeat(50) + "\n");
                writer.write(String.format("%-20s%-15s%-8s%-8s\n","学年学期", "课程名称", "学分", "成绩"));
                writer.write("-".repeat(50) + "\n");

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String date = tableModel.getValueAt(i,0).toString();
                    String cname = tableModel.getValueAt(i, 1).toString();
                    String credit = tableModel.getValueAt(i, 2).toString();
                    String grade = tableModel.getValueAt(i, 3).toString();
                    writer.write(String.format("%-20s%-15s%-12s%-12s\n",date, cname, credit, grade));
                }

                writer.write("=".repeat(50) + "\n");
                writer.write("打印时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                JOptionPane.showMessageDialog(parentGUI,
                        "成绩已保存到: " + filePath.toAbsolutePath(),
                        "保存成功",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parentGUI,
                    "保存文件失败: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}    