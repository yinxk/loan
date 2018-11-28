package top.yinxiaokang.original.component;

/**
 * @author yinxk
 * @date 2018/6/15 9:32
 */

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.List;

@Slf4j
public class Conn {
    // b4
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://172.18.20.100:3306/product_0103_b4?characterEncoding=utf8&useSSL=false";
    private static final String NAME = "root";
    private static final String PASSWORD = "zlgj9YAf02zt21ZYv1QwXzVHttUAZv";

    static {
        try {
            log.info("加载驱动开始...");
            Class.forName(DRIVER);
            log.info("加载驱动结束...");
        } catch (ClassNotFoundException e) {
            log.info("加载驱动失败...");
        }
    }

    public Connection getConnection() {
        Connection conn = null;
        try {
            log.info("获取连接开始...");
            conn = DriverManager.getConnection(URL, NAME, PASSWORD);
            log.info("获取到的连接 : " + conn.toString());
        } catch (SQLException e) {
            log.info("获取连接失败...");
        }
        return conn;
    }

    public static void closeResource(Connection conn, Statement statement, ResultSet resultSet) {
        log.error("关闭连接相关资源");
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    /**
     * 判断数据库是否支持批处理
     */
    public static boolean supportBatch(Connection con) {
        try {
            // 得到数据库的元数据
            DatabaseMetaData md = con.getMetaData();
            return md.supportsBatchUpdates();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 执行一批SQL语句
     */
    public static int[] goBatch(Connection con, List<String> sqls) throws Exception {
        if (sqls == null) {
            return null;
        }
        Statement sm = null;
        try {
            sm = con.createStatement();
            for (int i = 0; i < sqls.size(); i++) {
                sm.addBatch(sqls.get(i));// 将所有的SQL语句添加到Statement中
            }
            // 一次执行多条SQL语句
            return sm.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sm.close();
        }
        return null;
    }
}
