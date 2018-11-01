package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.others.StringUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by where on 2018/7/31.
 */
@Slf4j
public class CalDkye {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Conn conn = new Conn();
        Connection connection = conn.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        CalDkye calDkye = new CalDkye();

        String dkzh = "52001069403600000000532951";
        BigDecimal initDkye = new BigDecimal("1000");
        // 根据逗号分割
        String reverseBXDqqcs = "";

        BigDecimal dkye = initDkye;
        try {

            BigDecimal accountDkye = calDkye.getDkye(dkzh, connection);
            List<Detail> details = calDkye.getDetailsByDkzh(dkzh, connection);
            Collections.sort(details, Comparator.comparing(Detail::getDqqc));
            BigDecimal[] reverseBXDqqc = calDkye.getReverseBXDqqc(reverseBXDqqcs);
            Iterator<Detail> iterator = details.iterator();
            log.info("推算开始======");
            while (iterator.hasNext()) {
                Detail next = iterator.next();
                boolean reverseQc = calDkye.isReverseQc(reverseBXDqqc, next.getDqqc());
                BigDecimal bjje = next.getBjje();
                if (reverseQc) {
                    bjje = next.getLxje();
                }
                dkye = dkye.subtract(bjje);
                System.out.print(next);
                log.info("\t推算余额: "+dkye + "\t差额: "+dkye.subtract(next.getXqdkye()));
            }

            log.info("推算结束======");

            log.info("实际余额: " + accountDkye.toString() + "\t推算余额: "+ dkye.toString() + "\t差额: "+dkye.subtract(accountDkye));

            long endTime = System.currentTimeMillis();
            log.info("正常结束，时间：" + (endTime - startTime) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.closeResource(connection, preparedStatement, resultSet);
        }

    }


    public boolean isReverseQc(BigDecimal[] reverseQcs, BigDecimal dqqc) {
        if (null == reverseQcs) return false;
        for (BigDecimal r : reverseQcs) {
            if (dqqc.compareTo(r) == 0) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal[] getReverseBXDqqc(String reverseBXDqqcs) {
        if (StringUtil.isEmpty(reverseBXDqqcs)) return null;

        String[] split = reverseBXDqqcs.split(",");
        BigDecimal[] result = new BigDecimal[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = new BigDecimal(split[i]);
        }
        return result;
    }

    public BigDecimal getDkye(String dkzh, Connection connection) throws SQLException {
        String selectSql = "SELECT acc.dkye FROM st_housing_personal_account acc WHERE acc.DKZH = '" + dkzh + "'";
        PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
        ResultSet resultSet = preparedStatement.executeQuery();
        BigDecimal dkye = null;
        if (resultSet.next()) {
            dkye = resultSet.getBigDecimal("dkye");

        }
        return dkye;
    }

    public List<Detail> getDetailsByDkzh(String dkzh, Connection connection) throws SQLException {
        String selectSql = "SELECT de.DQQC, de.DKYWMXLX, de.BJJE, de.LXJE, de.FSE, deex.XQDKYE " +
                "FROM st_housing_business_details de INNER JOIN c_housing_business_details_extension deex ON de.extenstion = deex.id " +
                "WHERE de.DKZH = '" + dkzh + "' AND de.JZRQ IS NOT NULL";
        PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Detail> details = new ArrayList<>();
        while (resultSet.next()) {
            Detail detail = new Detail();
            detail.setDqqc(resultSet.getBigDecimal("dqqc"));
            detail.setDkywmxlx(resultSet.getString("dkywmxlx"));
            detail.setBjje(resultSet.getBigDecimal("bjje"));
            detail.setLxje(resultSet.getBigDecimal("lxje"));
            detail.setFse(resultSet.getBigDecimal("fse"));
            detail.setXqdkye(resultSet.getBigDecimal("xqdkye"));
            details.add(detail);
        }
        return details;
    }
}


class Detail {

    private BigDecimal dqqc;

    private String dkywmxlx;

    private BigDecimal bjje;

    private BigDecimal lxje;

    private BigDecimal fse;

    private BigDecimal xqdkye;

    public BigDecimal getDqqc() {
        return dqqc;
    }

    public void setDqqc(BigDecimal dqqc) {
        this.dqqc = dqqc;
    }

    public String getDkywmxlx() {
        return dkywmxlx;
    }

    public void setDkywmxlx(String dkywmxlx) {
        this.dkywmxlx = dkywmxlx;
    }

    public BigDecimal getBjje() {
        return bjje;
    }

    public void setBjje(BigDecimal bjje) {
        this.bjje = bjje;
    }

    public BigDecimal getLxje() {
        return lxje;
    }

    public void setLxje(BigDecimal lxje) {
        this.lxje = lxje;
    }

    public BigDecimal getFse() {
        return fse;
    }

    public void setFse(BigDecimal fse) {
        this.fse = fse;
    }

    public BigDecimal getXqdkye() {
        return xqdkye;
    }

    public void setXqdkye(BigDecimal xqdkye) {
        this.xqdkye = xqdkye;
    }

    @Override
    public String toString() {
        return "Detail{" +
                "dqqc=" + dqqc +
                ", dkywmxlx='" + dkywmxlx + '\'' +
                ", bjje=" + bjje +
                ", lxje=" + lxje +
                ", fse=" + fse +
                ", xqdkye=" + xqdkye +
                '}';
    }
}