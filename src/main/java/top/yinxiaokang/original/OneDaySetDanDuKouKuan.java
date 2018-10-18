package top.yinxiaokang.original;

import top.yinxiaokang.util.Common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static top.yinxiaokang.util.FileCommon.inFileNameOneDay;

/**
 * 设置账号单独扣款
 */
public class OneDaySetDanDuKouKuan {
    public static void main(String[] args) {

        Collection<Map> oneDayMap = Common.xlsToList(inFileNameOneDay);
        Conn conn = new Conn();
        Connection connection = conn.getConnection();

        try {
            connection.setAutoCommit(false);

            new DoSql().doUpdate(connection, "UPDATE c_loan_housing_personal_account_extension accex \n" +
                    "SET accex.loanHousingPersonalPausedVice = NULL \n" +
                    "WHERE\n" +
                    "\taccex.loanHousingPersonalPausedVice = 'dandukoukuan'");

            OneDaySetDanDuKouKuan oneDaySetDanDuKouKuan = new OneDaySetDanDuKouKuan();
            Iterator<Map> iterator = oneDayMap.iterator();
            int sum = 0 ;
            while (iterator.hasNext()) {
                Map next = iterator.next();
                String dkzh = (String)next.get("dkzh");
                int i = oneDaySetDanDuKouKuan.UpdateAccountToDanDuKouKuan(dkzh, connection);
                sum += i;
                System.out.println("贷款账号: " + dkzh + "    更新了 " + i + "行");
            }
            System.out.println("总共更新了 : " + sum);

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


    public int UpdateAccountToDanDuKouKuan(String dkzh, Connection connection) throws SQLException {
        String sql = " UPDATE st_housing_personal_account acc INNER JOIN c_loan_housing_personal_account_extension accex ON acc.extenstion=accex.id " +
                " SET accex.loanHousingPersonalPausedVice='dandukoukuan' WHERE acc.DKZH = ? ";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, dkzh);
        int i = preparedStatement.executeUpdate();
        return i;
    }

}
