package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.others.ReducePlanEntity;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yinxk
 * @date 2018/7/6 14:11
 */
@Slf4j
public class ReducePlan {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        ArrayList<SthousingAccount> accountArrayList = new ArrayList<>();
        // region 通过excel进行第一期更新
//        String fileName = "src/test/resources/20180828-徐灏手动入账账号修复.xlsx";
//        Collection<Map> importExcel = Common.xlsToList(fileName);
//        for (Map m : importExcel) {
//            SthousingAccount account = new SthousingAccount();
//            account.setDkzh((String) m.get("dkzh"));
//            accountArrayList.add(account);
//        }
        // endregion

        // region 直接通过贷款账号进行更新
        SthousingAccount account1 = new SthousingAccount();
        account1.setDkzh("23791057800004389");
        accountArrayList.add(account1);
        // endregion

        Conn conn = new Conn();
        Connection connection = conn.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ReducePlan reducePlan = new ReducePlan();

        try {

            List<ReducePlanEntity> reducePlanEntityList = new ArrayList<>();
            for (SthousingAccount account : accountArrayList) {
                ReducePlanEntity reducePlanLoan2 = reducePlan.getLoan2(connection, preparedStatement, resultSet, account);
                reducePlanEntityList.add(reducePlanLoan2);
            }

            BigDecimal bigDecimal = null;
            BigDecimal currentBX = null;
            BigDecimal overdueThisPeriodLX = null;
            BigDecimal bjje = null;
            int count = 0;
            for (ReducePlanEntity entity : reducePlanEntityList) {
                String id = entity.getId();
                BigDecimal dkll = entity.getDkll();
                BigDecimal llfdbl = entity.getLlfdbl();

                BigDecimal dkgbjhqs = entity.getDkgbjhqs();
                BigDecimal dkgbjhye = entity.getDkgbjhye();
                BigDecimal dqqc = entity.getDqqc();

                String dkhkfs = entity.getDkhkfs();
                if (dkgbjhqs.compareTo(BigDecimal.ZERO) > 0) {
                    bigDecimal = CommLoanAlgorithm.lendingRate(dkll, llfdbl);
                    currentBX = CommLoanAlgorithm.currentBX(dkgbjhye, Integer.parseInt(dkgbjhqs.toString()), dkhkfs, bigDecimal, Integer.parseInt(dqqc.toString())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    overdueThisPeriodLX = CommLoanAlgorithm.overdueThisPeriodLX(dkgbjhye, Integer.parseInt(dqqc.toString()), dkhkfs, bigDecimal, Integer.parseInt(dkgbjhqs.toString())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    bjje = currentBX.subtract(overdueThisPeriodLX);

                    int i = reducePlan.updateLoanAccountPlan(connection, currentBX, bjje, overdueThisPeriodLX, id);
                    if (i > 0) {
                        count++;
                        log.info("更新了id为: " + id + " 的记录  ");
                    }
                }
            }

            log.info("总共更新的记录数:" + count);
            long endTime = System.currentTimeMillis();
            log.info("正常结束，时间：" + (endTime - startTime) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Conn.closeResource(connection, preparedStatement, resultSet);
        }


    }


    /**
     * 查询更新该期扣款需要的信息
     * @param connection
     * @param preparedStatement
     * @param resultSet
     * @param account
     * @return
     * @throws SQLException
     */
    public ReducePlanEntity getLoan2(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet, SthousingAccount account) throws SQLException {
        String selectSql = "SELECT\n" +
                "\taccount.id,\n" +
                "\taccount.dkll,\n" +
                "\taccount.llfdbl,\n" +
                "\tex.dkgbjhqs,\n" +
                "\tex.dkgbjhye,\n" +
                "\tex.dqqc,\n" +
                "\tpersonalLoan.dkhkfs\n" +
                "FROM\n" +
                "\tst_housing_personal_account account\n" +
                "JOIN c_loan_housing_personal_account_extension ex on account.extenstion =ex.id\n" +
                "JOIN st_housing_personal_loan personalLoan on account.contract = personalLoan.id\n" +
                "JOIN c_loan_housing_person_information_basic basic on basic.personalAccount = account.id " +
                "WHERE \n" +
                "\t basic.dkzhzt IN ('2', '3', '5')\n" +
                "AND basic.dkzh = '" + account.getDkzh() + "'";

        preparedStatement = connection.prepareStatement(selectSql);
        resultSet = preparedStatement.executeQuery();
        ReducePlanEntity reducePlanEntity = null;
        while (resultSet.next()) {
            reducePlanEntity = new ReducePlanEntity();
            String id = resultSet.getString("id");
            BigDecimal dkll = resultSet.getBigDecimal("dkll");
            BigDecimal llfdbl = resultSet.getBigDecimal("llfdbl");
            BigDecimal dkgbjhqs = resultSet.getBigDecimal("dkgbjhqs");
            BigDecimal dkgbjhye = resultSet.getBigDecimal("dkgbjhye");
            BigDecimal dqqc = resultSet.getBigDecimal("dqqc");
            String dkhkfs = resultSet.getString("dkhkfs");
            reducePlanEntity.setId(id);
            reducePlanEntity.setDkll(dkll);
            reducePlanEntity.setLlfdbl(llfdbl);
            reducePlanEntity.setDkgbjhqs(dkgbjhqs);
            reducePlanEntity.setDkgbjhye(dkgbjhye);
            reducePlanEntity.setDqqc(dqqc);
            reducePlanEntity.setDkhkfs(dkhkfs);
        }
        return reducePlanEntity;
    }

    public int updateLoanAccountPlan(Connection connection, BigDecimal dqyhje, BigDecimal dqyhbj, BigDecimal dqyhlx, String id) throws SQLException {
        String sql = "update st_housing_personal_account set " +
                "DQYHJE = ? " +
                ", DQYHBJ = ?" +
                ", DQYHLX = ? " +
                ", DQJHHKJE = ?" +
                ", DQJHGHLX = ?" +
                ", DQJHGHBJ = ? " +
                ", updated_at = now() " +
                "where id = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setBigDecimal(1, dqyhje);
        preparedStatement.setBigDecimal(2, dqyhbj);
        preparedStatement.setBigDecimal(3, dqyhlx);
        preparedStatement.setBigDecimal(4, dqyhje);
        preparedStatement.setBigDecimal(5, dqyhlx);
        preparedStatement.setBigDecimal(6, dqyhbj);

        preparedStatement.setString(7, id);
        return preparedStatement.executeUpdate();
    }
}
