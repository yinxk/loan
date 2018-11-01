package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * 设置账号单独扣款
 */
@Slf4j
public class OneDaySetDanDuKouKuan {
    public static void main(String[] args) {
        new OneDaySetDanDuKouKuan().work();
    }

    public void work() {
        Collection<Map> oneDayMap = Common.xlsToList(Constants.TODAY_SHOULD_PAYMENT_ACCOUNT_XLS);
        Conn conn = new Conn();
        Connection connection = conn.getConnection();

        try {
            connection.setAutoCommit(false);

            new DoSql().doUpdate(connection, "UPDATE c_loan_housing_personal_account_extension accex \n" +
                    "SET accex.loanHousingPersonalPausedVice = NULL \n" +
                    "WHERE\n" +
                    "\taccex.loanHousingPersonalPausedVice = 'dandukoukuan'");

            Iterator<Map> iterator = oneDayMap.iterator();
            int sum = 0;
            while (iterator.hasNext()) {
                Map next = iterator.next();
                String dkzh = (String) next.get("dkzh");
                int i = updateAccountToDanDuKouKuan(dkzh, connection);
                sum += i;
                log.info("贷款账号: " + dkzh + "    更新了 " + i + "行");
            }
            log.info("总共更新了 : " + sum);

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                log.info("回滚失败!");
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            Conn.closeResource(connection, null, null);
        }
        log.info("单独扣款标记结束");
    }


    public int updateAccountToDanDuKouKuan(String dkzh, Connection connection) throws SQLException {
        String sql = " UPDATE st_housing_personal_account acc INNER JOIN c_loan_housing_personal_account_extension accex ON acc.extenstion=accex.id " +
                " SET accex.loanHousingPersonalPausedVice='dandukoukuan' WHERE acc.DKZH = ? ";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, dkzh);
        int i = preparedStatement.executeUpdate();
        return i;
    }

}
