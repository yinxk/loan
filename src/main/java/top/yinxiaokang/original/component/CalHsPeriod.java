package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.others.CurrentPeriodRange;
import top.yinxiaokang.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by where on 2018/7/27.
 */
@Slf4j
public class CalHsPeriod {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Conn conn = new Conn();
        Connection connection = conn.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        CalHsPeriod calHsPeriod = new CalHsPeriod();

        String dkzh = "52001069403600000000532951";
        try {

            Date dkffrq = calHsPeriod.getDkffrq(dkzh, connection);
            log.info("贷款发放日期:" + Utils.SDF_YEAR_MONTH_DAY.format(dkffrq));
            Date hssj = Utils.SDF_YEAR_MONTH_DAY.parse("2018-7-27");

            Date soutStartDate = Utils.SDF_YEAR_MONTH_DAY.parse("2017-12-01");
            CurrentPeriodRange currentPeriodRange = LoanRepaymentAlgorithm.calHSRange(dkffrq, hssj, soutStartDate);
//            log.info(currentPeriodRange);

            long endTime = System.currentTimeMillis();
            log.info("正常结束，时间：" + (endTime - startTime) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.closeResource(connection, preparedStatement, resultSet);
        }

    }

    public Date getDkffrq(String dkzh, Connection connection) throws SQLException {
        String selectSql = "SELECT acc.DKFFRQ FROM st_housing_personal_account acc WHERE acc.DKZH = '" + dkzh + "'";
        PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
        ResultSet resultSet = preparedStatement.executeQuery();
        Date dkffrq = null;
        if (resultSet.next()) {
            dkffrq = resultSet.getDate("dkffrq");

        }
        return dkffrq;
    }
}
