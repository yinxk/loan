package top.yinxiaokang.original;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yinxk
 * @date 2018/6/19 13:18
 */
public class CalOverdueXqdkye {

    public static void main(String[] args)  {
        long startTime = System.currentTimeMillis();
        Conn conn = new Conn();
        Connection connection = conn.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String dkzh = "23863057300000205";
        BigDecimal ljXqdkye = new BigDecimal("161100.91");
        try {
            String selectSql = "SELECT\n" +
                    "\t@rowno :=@rowno + 1 AS rowno,\n" +
                    "\ta.*\n" +
                    "FROM\n" +
                    "\t(\n" +
                    "\t\tSELECT\n" +
                    "\t\t\tde.DKZH,\n" +
                    "\t\t\tde.DQQC,\n" +
                    "\t\t\tde.BJJE,\n" +
                    "\t\t\tde.LXJE,\n" +
                    "\t\t\tex.XQDKYE\n" +
                    "\t\tFROM\n" +
                    "\t\t\tst_housing_business_details de\n" +
                    "\t\tINNER JOIN c_housing_business_details_extension ex ON de.extenstion = ex.id\n" +
                    "\t\tWHERE\n" +
                    "\t\t\tde.DKZH = '"+dkzh +  "'   " +
                    "\t\tAND de.DKYWMXLX = '04'\n" +
                    "\t\tAND ex.YWZT = '已入账'  \n" +
                    "\t\tORDER BY\n" +
                    "\t\t\tde.jzrq DESC,\n" +
                    "\t\t\tex.XQDKYE ASC\n" +
                    "\t) a,\n" +
                    "\t(SELECT @rowno := 0) t";
            preparedStatement = connection.prepareStatement(selectSql);
            resultSet = preparedStatement.executeQuery();



            List<String> updateSqls = new ArrayList<>();
            boolean isFirst = true;
            BigDecimal oddBjje = null;
            while (resultSet.next()){
                int rowno =  resultSet.getInt("rowno");
                String dkzh1 = resultSet.getString("dkzh");
                BigDecimal dqqc = resultSet.getBigDecimal("dqqc");
                BigDecimal bjje = resultSet.getBigDecimal("bjje");
                BigDecimal lxje = resultSet.getBigDecimal("lxje");
                BigDecimal xqdkye = resultSet.getBigDecimal("xqdkye");

                if (isFirst){
                    isFirst = false;
                    oddBjje = bjje;
                }else {
                    ljXqdkye = ljXqdkye.add(oddBjje);
                    oddBjje = bjje;
                }
                String updateSql= ("\t update st_housing_business_details de\n" +
                        "\t\tINNER JOIN c_housing_business_details_extension ex ON de.extenstion = ex.id\n" +
                        "set ex.xqdkye = " + ljXqdkye + " " +
                        "\t\tWHERE\n" +
                        "\t\t\tde.DKZH = '" + dkzh + "'  "+
                        "\t\tAND de.DKYWMXLX = '04'\n" +
                        "\t\tAND ex.YWZT = '已入账'  " +
                        "and de.dqqc = "+ dqqc );
                updateSqls.add(updateSql);
            }
            // 判断是否支持批处理
            boolean supportBatch = Conn.supportBatch(connection);
            System.out.println("支持批处理？ " + supportBatch);
            if (supportBatch) {
                // 执行一批SQL语句
                int[] results = Conn.goBatch(connection, updateSqls);
                // 分析执行的结果
                for (int i = 0; i < updateSqls.size(); i++) {
                    if (results[i] >= 0) {
                        System.out.println("\n\n语句: " + updateSqls.get(i) + " \n 执行成功，影响了"
                                + results[i] + "行数据");
                    } else if (results[i] == Statement.SUCCESS_NO_INFO) {
                        System.out.println("\n\n语句: " + updateSqls.get(i)  + " \n执行成功，影响的行数未知");
                    } else if (results[i] == Statement.EXECUTE_FAILED) {
                        System.out.println("\n\n语句: " + updateSqls.get(i)  + "\n 执行失败");
                    }
                }
            }
            System.out.println("应该更新的条数："+ updateSqls.size());
            long endTime = System.currentTimeMillis();
            System.out.println("正常结束，时间："+(endTime - startTime) + " ms");
        }catch (Exception e){
            e.printStackTrace();
            conn.closeResource(connection,preparedStatement,resultSet);
        }finally {
            conn.closeResource(connection,preparedStatement,resultSet);
        }


    }
}
