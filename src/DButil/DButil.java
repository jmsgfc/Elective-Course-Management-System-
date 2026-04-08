package DButil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/*
本代码作为连接数据库的工具类
studentoperation中含有7个表
*/
public class DButil {

    private static final String CONFIG_FILE_NAME = "config.properties";

    private static final String url;
    private static final String name;
    private static final String pwd;

    static {
        Properties props = new Properties();

        // 1) 优先从 classpath 读取（更适合打包/分发）
        try (InputStream in = DButil.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (in != null) {
                props.load(in);
            } else {
                // 2) 兼容从项目根目录运行（IDEA 直接 Run 时常见）
                try (InputStream fin = new FileInputStream(CONFIG_FILE_NAME)) {
                    props.load(fin);
                }
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("读取 " + CONFIG_FILE_NAME + " 失败：" + e.getMessage());
        }

        url = require(props, "jdbc.url");
        name = require(props, "jdbc.username");
        pwd = require(props, "jdbc.password");
    }

    private static String require(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new ExceptionInInitializerError("配置文件缺少或为空：" + key);
        }
        return value.trim();
    }

    public static Connection connection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, name, pwd);
        if (conn.isValid(3)) System.out.println("数据库连接成功");
        else System.out.println("数据库连接失败");
        return conn;
    }

}
