package top.yinxiaokang.original;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/8/3 9:06
 */
public class ExchangeOverdueBx {


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Conn conn = new Conn();
        Connection connection = conn.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String dkzh = "52001069403600000000245309";

        ExchangeOverdueBx exchangeOverdueBx = new ExchangeOverdueBx();
        // 根据逗号分割
        String[] yqqcs = {"104",
                "105" ,
                "93" ,
                "94" ,
                "95" ,
                "96" ,
                "97" ,
                "98" ,
                "99" ,
                "100" ,
                "101" ,
                "102" ,
                "103"};

        try {
            List<Overdue> overdueList = exchangeOverdueBx.getOverdueList(dkzh, connection, yqqcs);
            Collections.sort(overdueList,Comparator.comparing(Overdue::getYqqc));
            System.out.println("=====================即将更新=====================");
            for (Overdue overdue : overdueList) {
                System.out.println(overdue);
            }
            System.out.println("=====================开始更新=====================");

            //exchangeOverdueBx.exchangeOverdues(overdueList,connection);

            overdueList.clear();
            overdueList = exchangeOverdueBx.getOverdueList(dkzh, connection, yqqcs);
            Collections.sort(overdueList,Comparator.comparing(Overdue::getYqqc));
            System.out.println("=====================更新之后=====================");
            for (Overdue overdue : overdueList) {
                System.out.println(overdue);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("正常结束，时间：" + (endTime - startTime) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.closeResource(connection, preparedStatement, resultSet);
        }

    }

    public void exchangeOverdues(List<Overdue> list,Connection connection) throws SQLException {
        for (Overdue overdue : list) {
            int i = updateOverdue(overdue.getId(), overdue.getYqlx(), overdue.getYqbj(), connection);
            System.out.println("贷款账号: " + overdue.getDkzh() + " , 逾期期次: "+overdue.getYqqc() + " , 更新了 " + i + " 行");
        }
    }

    public int updateOverdue(String id, BigDecimal yqbj, BigDecimal yqlx,Connection connection) throws SQLException {
        String updateSql = "UPDATE st_housing_overdue_registration SET YQBJ = ?, YQLX =? WHERE id = ? ";
        PreparedStatement preparedStatement = connection.prepareStatement(updateSql);
        preparedStatement.setBigDecimal(1,yqbj);
        preparedStatement.setBigDecimal(2,yqlx);
        preparedStatement.setString(3,id);
        return  preparedStatement.executeUpdate();
    }

    public List<Overdue> getOverdueList(String dkzh , Connection connection , String[] dqqcs) throws SQLException {
        List<Overdue> result = new ArrayList<>();
        for (String dqqc : dqqcs){
            Overdue overdueByDkzhAndYqqc = getOverdueByDkzhAndYqqc(dkzh, connection, dqqc);
            result.add(overdueByDkzhAndYqqc);
        }
        return result;
    }

    public Overdue getOverdueByDkzhAndYqqc(String dkzh, Connection connection,String dqqc) throws SQLException {
        String selectSql = "SELECT over.id, over.YQQC, over.YQBJ, over.YQLX, over.dkzh " +
                "FROM st_housing_overdue_registration over INNER JOIN c_housing_overdue_registration_extension ex ON over.extenstion = ex.id " +
                "WHERE over.dkzh = ? AND over.yqqc = ? ";
        PreparedStatement preparedStatement = connection.prepareStatement(selectSql);
        preparedStatement.setString(1,dkzh);
        preparedStatement.setString(2,dqqc);
        ResultSet resultSet = preparedStatement.executeQuery();
        Overdue overdue = null;
        if (resultSet.next()) {
            overdue = new Overdue();
            overdue.setId(resultSet.getString("id"));
            overdue.setYqqc(resultSet.getBigDecimal("yqqc"));
            overdue.setYqbj(resultSet.getBigDecimal("yqbj"));
            overdue.setYqlx(resultSet.getBigDecimal("yqlx"));
            overdue.setDkzh(resultSet.getString("dkzh"));
        }
        return overdue;
    }

    class Overdue{
        private String id;

        private BigDecimal yqqc;

        private BigDecimal yqbj;

        private BigDecimal yqlx;

        private String dkzh;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public BigDecimal getYqqc() {
            return yqqc;
        }

        public void setYqqc(BigDecimal yqqc) {
            this.yqqc = yqqc;
        }

        public BigDecimal getYqbj() {
            return yqbj;
        }

        public void setYqbj(BigDecimal yqbj) {
            this.yqbj = yqbj;
        }

        public BigDecimal getYqlx() {
            return yqlx;
        }

        public void setYqlx(BigDecimal yqlx) {
            this.yqlx = yqlx;
        }

        public String getDkzh() {
            return dkzh;
        }

        public void setDkzh(String dkzh) {
            this.dkzh = dkzh;
        }

        @Override
        public String toString() {
            return "Overdue{" +
                    "id='" + id + '\'' +
                    ", yqqc=" + yqqc +
                    ", yqbj=" + yqbj +
                    ", yqlx=" + yqlx +
                    ", dkzh='" + dkzh + '\'' +
                    '}';
        }
    }
}
