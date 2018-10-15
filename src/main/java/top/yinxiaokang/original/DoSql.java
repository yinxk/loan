package top.yinxiaokang.original;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DoSql {

    public static void main(String[] args) {
        String sql = "";
        Conn conn = new Conn();
        Connection connection = conn.getConnection();
        try {
            connection.setAutoCommit(false);
            new DoSql().doUpdate(connection, sql);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("回滚失败!");
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            Conn.closeResource(connection, null, null);
        }
        System.out.println("结束运行");

    }

    public void doUpdate(Connection connection, String sql) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        int i = preparedStatement.executeUpdate();
        System.out.println("处理更新了: " + i);
    }

}
