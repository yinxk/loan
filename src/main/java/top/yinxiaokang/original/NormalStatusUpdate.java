package top.yinxiaokang.original;

import java.sql.Connection;
import java.sql.SQLException;

public class NormalStatusUpdate {

    public static void main(String[] args) {
        for (int i = 0; ; i++) {

            System.out.println("开始运行第 " + i + " 次!");

            Conn conn = new Conn();
            Connection connection = conn.getConnection();

            try {
                connection.setAutoCommit(false);

                new DoSql().doUpdate(connection, "update  c_loan_housing_business_process pro set pro.normalstatus = 1 where pro.normalstatus = 0");

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
            System.out.println("结束运行第 " + i + " 次!");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
