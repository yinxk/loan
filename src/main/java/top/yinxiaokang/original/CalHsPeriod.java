package top.yinxiaokang.original;

import top.yinxiaokang.others.CurrentPeriodRange;
import top.yinxiaokang.others.ReducePlanEntity;

import javax.rmi.CORBA.Util;
import javax.sound.midi.Soundbank;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by where on 2018/7/27.
 */
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
            System.out.println("贷款发放日期:" + Utils.SDF_YEAR_MONTH_DAY.format(dkffrq));
            Date hssj = Utils.SDF_YEAR_MONTH_DAY.parse("2018-7-27");

            Date soutStartDate = Utils.SDF_YEAR_MONTH_DAY.parse("2017-12-01");
            CurrentPeriodRange currentPeriodRange = LoanRepaymentAlgorithm.calHSRange(dkffrq, hssj, soutStartDate);
//            System.out.println(currentPeriodRange);

            long endTime = System.currentTimeMillis();
            System.out.println("正常结束，时间：" + (endTime - startTime) + " ms");
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
