package DButil;
import java.sql.*;
/*
本代码作为连接数据库的工具类
studentoperation中含有7个表
*/
public class DButil {
    static String url = "jdbc:mysql://127.0.0.1:3306/?useSSL=false&serverTimezone=UTC&allowMultiQueries=true";
    static String name = "root";
    static String pwd = "111222"; // 请改为你的数据库密码


    public static Connection connection() throws SQLException, ClassNotFoundException {
        Connection conn = null;
        Statement stmt = null; // 多语句执行必须用Statement，不支持PreparedStatement
        try {
            conn = DriverManager.getConnection(url, name, pwd);
            if (conn.isValid(3)) {
                System.out.println("数据库连接成功");
            } else {
                throw new SQLException("数据库连接失败");
            }

            String initSql = """
                    CREATE DATABASE IF NOT EXISTS studentoperation;
                    USE studentoperation;
                    DROP TABLE IF EXISTS scores, tcourse, info, student, teacher, courses, users;
                    
                    -- 创建用户表
                    CREATE TABLE users (
                        uname CHAR(30) NOT NULL PRIMARY KEY,
                        pwd CHAR(30) NOT NULL,
                        role INT NOT NULL
                    );
                    
                    -- 创建课程表
                    CREATE TABLE courses (
                        cno INT AUTO_INCREMENT PRIMARY KEY,
                        cname VARCHAR(20) NOT NULL,
                        credit INT NOT NULL,
                        max_come INT NOT NULL,
                        cdate CHAR(15) NULL
                    );
                    
                    -- 创建学生表
                    CREATE TABLE student (
                        sno CHAR(25) NOT NULL PRIMARY KEY,
                        sname CHAR(25) NOT NULL,
                        sex CHAR(10) DEFAULT '男',
                        depart VARCHAR(30),
                        major CHAR(30),
                        sclass CHAR(30)
                    );
                    
                    -- 创建成绩表
                    CREATE TABLE scores (
                        cno INT NOT NULL,
                        sno CHAR(30) NOT NULL,
                        grade INT,
                        cdate CHAR(30) NOT NULL DEFAULT '2024-2025-1',
                        PRIMARY KEY (cno, sno),
                        FOREIGN KEY (cno) REFERENCES courses(cno),
                        FOREIGN KEY (sno) REFERENCES student(sno)
                    );
                    
                    -- 创建教师表
                    CREATE TABLE teacher (
                        tno CHAR(30) NOT NULL PRIMARY KEY,
                        tname CHAR(30) NOT NULL
                    );
                    
                    -- 创建教师-课程关联表
                    CREATE TABLE tcourse (
                        tno CHAR(30) NOT NULL,
                        cno INT NOT NULL,
                        PRIMARY KEY (tno, cno),
                        FOREIGN KEY (tno) REFERENCES teacher(tno),
                        FOREIGN KEY (cno) REFERENCES courses(cno)
                    );
                    
                    -- 创建课程信息补充表
                    CREATE TABLE IF NOT EXISTS info (
                        cno INT NOT NULL,
                        cname VARCHAR(30) NOT NULL,
                        infos VARCHAR(500),
                        cdate DATE,
                        PRIMARY KEY (cno, cdate),
                        FOREIGN KEY (cno) REFERENCES courses(cno)
                    );
                    
                    -- 插入管理员账号
                    INSERT INTO users (uname, pwd, role) VALUES ('admin', 'admin', 0);
                    
                    -- 插入教师数据
                    INSERT INTO teacher (tno, tname) VALUES
                    ('0001', '张明'), ('0002', '李娜'), ('0003', '王宇'), ('0004', '陈芳'),
                    ('0005', '刘伟'), ('0006', '杨丽'), ('0007', '黄凯'), ('0008', '周敏'),
                    ('0009', '吴俊'), ('0010', '郑洁');
                    
                    -- 插入教师用户账号
                    INSERT INTO users (uname, pwd, role) VALUES
                    ('0001', '123456', 1), ('0002', '123456', 1), ('0003', '123456', 1), ('0004', '123456', 1),
                    ('0005', '123456', 1), ('0006', '123456', 1), ('0007', '123456', 1), ('0008', '123456', 1),
                    ('0009', '123456', 1), ('0010', '123456', 1);
                    
                    -- 插入课程数据
                    INSERT INTO courses (cname, credit, max_come, cdate) VALUES
                    ('C语言程序设计', 4, 30, '2022-2023-1'), ('Java程序设计', 4, 30, '2022-2023-2'),
                    ('离散数学', 3, 30, '2023-2024-1'), ('数据结构', 4, 30, '2023-2024-2'),
                    ('算法设计与分析', 3, 3, '2024-2025-1'), ('数据库原理', 3, 30, '2024-2025-1'),
                    ('计算机网络', 2, 30, '2024-2025-1'), ('操作系统', 4, 30, '2022-2023-1'),
                    ('计算机组成原理', 4, 30, '2022-2023-2'), ('概率论与数理统计', 3, 30, '2024-2025-1'),
                    ('中国近现代史纲要', 3, 30, '2024-2025-1'), ('思想道德与法治', 3, 30, '2024-2025-1'),
                    ('Python程序设计', 4, 3, '2024-2025-1'), ('高等数学', 4, 30, '2024-2025-1'),
                    ('大学物理', 4, 30, '2024-2025-1');
                    
                    -- 插入学生数据
                    INSERT INTO student (sno, sname, sex, depart, major, sclass) VALUES
                    ('101', '张三', '男', '互联网经贸学院', '网络工程', '2302'), ('102', '李四', '男', '互联网经贸学院', '网络工程', '2302'),
                    ('103', '王五', '女', '计算机科学与数学学院', '计算机科学与技术', '2302'), ('104', '赵六', '男', '计算机科学与数学学院', '计算机科学与技术', '2301'),
                    ('105', '孙七', '女', '互联网经贸学院', '电子商务', '2301'), ('106', '周八', '男', '电子信息工程学院', '电子信息工程', '2301'),
                    ('107', '吴九', '男', '互联网经贸学院', '网络工程', '2303'), ('108', '郑十', '女', '计算机科学与数学学院', '计算机科学与技术', '2301'),
                    ('109', '钱十一', '男', '互联网经贸学院', '电子商务', '2302'), ('110', '孙十二', '女', '电子信息工程学院', '电子信息工程', '2303'),
                    ('111', '李十三', '男', '计算机科学与数学学院', '计算机科学与技术', '2301'), ('112', '周十四', '女', '互联网经贸学院', '网络工程', '2302'),
                    ('113', '吴十五', '男', '电子信息工程学院', '电子信息工程', '2303'), ('114', '郑十六', '女', '计算机科学与数学学院', '计算机科学与技术', '2301'),
                    ('115', '王十七', '男', '互联网经贸学院', '电子商务', '2302'), ('116', '赵十八', '女', '电子信息工程学院', '电子信息工程', '2303'),
                    ('117', '钱十九', '男', '计算机科学与数学学院', '计算机科学与技术', '2301'), ('118', '孙二十', '女', '互联网经贸学院', '网络工程', '2302'),
                    ('119', '周二一', '男', '电子信息工程学院', '电子信息工程', '2303'), ('120', '吴二二', '女', '计算机科学与数学学院', '计算机科学与技术', '2301'),
                    ('121', '郑二三', '男', '互联网经贸学院', '电子商务', '2302'), ('122', '王二四', '女', '电子信息工程学院', '电子信息工程', '2303'),
                    ('123', '赵二五', '男', '计算机科学与数学学院', '计算机科学与技术', '2301'), ('124', '钱二六', '女', '互联网经贸学院', '网络工程', '2302'),
                    ('125', '孙二七', '男', '电子信息工程学院', '电子信息工程', '2303'), ('126', '周二八', '女', '计算机科学与数学学院', '计算机科学与技术', '2301');
                    
                    -- 插入学生用户账号
                    INSERT INTO users (uname, pwd, role) VALUES
                    ('101', '111222', 2), ('102', '111222', 2), ('103', '111222', 2), ('104', '111222', 2),
                    ('105', '111222', 2), ('106', '111222', 2), ('107', '111222', 2), ('108', '111222', 2),
                    ('109', '111222', 2), ('110', '111222', 2), ('111', '111222', 2), ('112', '111222', 2),
                    ('113', '111222', 2), ('114', '111222', 2), ('115', '111222', 2), ('116', '111222', 2),
                    ('117', '111222', 2), ('118', '111222', 2), ('119', '111222', 2), ('120', '111222', 2),
                    ('121', '111222', 2), ('122', '111222', 2), ('123', '111222', 2), ('124', '111222', 2),
                    ('125', '111222', 2), ('126', '111222', 2);
                    
                    -- 分配教师授课关系
                    INSERT INTO tcourse (tno, cno) VALUES
                    ('0001', 1), ('0002', 1), ('0003', 2), ('0004', 2),
                    ('0005', 3), ('0006', 3), ('0007', 4), ('0008', 4),
                    ('0001', 5), ('0003', 5), ('0002', 6), ('0004', 6),
                    ('0005', 7), ('0007', 7), ('0006', 8), ('0008', 8),
                    ('0009', 9), ('0010', 9), ('0005', 10), ('0006', 10),
                    ('0007', 11), ('0008', 11), ('0009', 12), ('0010', 12),
                    ('0001', 13), ('0002', 13), ('0003', 14), ('0004', 14),
                    ('0005', 15), ('0006', 15);
                    
                    -- 生成历史成绩数据
                    INSERT INTO scores (cno, sno, grade, cdate)
                    SELECT c.cno, s.sno, FLOOR(RAND()*41+60), c.cdate
                    FROM student s CROSS JOIN courses c
                    WHERE c.cdate NOT LIKE '2024-2025%'
                    AND NOT EXISTS (SELECT 1 FROM scores sc WHERE sc.cno = c.cno AND sc.sno = s.sno);
                    
                    -- 插入测试学生数据
                    INSERT INTO student (sno, sname, sex, depart, major, sclass) VALUES
                    ('228', '林云', '男', '计算机科学与数学学院', '计算机科学与技术', '2301'),
                    ('229', '杨建', '男', '计算机科学与数学学院', '计算机科学与技术', '2302');
                    
                    INSERT INTO users (uname, pwd, role) VALUES
                    ('228', '111222', 2), ('229', '111222', 2);
                    """;

            stmt = conn.createStatement();
            stmt.execute(initSql);
            System.out.println("数据库初始化完成（建库、建表、插数据成功）");

        } catch (SQLException e) {
            System.err.println("数据库操作异常：" + e.getMessage());
            throw e; // 抛出异常供调用者处理
        } finally {
            // 5. 关闭资源（先关Statement，再关Connection，避免内存泄漏）
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        return conn;
    }


    public static void main(String[] args) {
        try {

            Connection conn = DButil.connection();


            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("数据库连接已关闭");
            }
        }  catch (SQLException | ClassNotFoundException e) {
            System.err.println("测试失败：" + e.getMessage());
        }
    }
}