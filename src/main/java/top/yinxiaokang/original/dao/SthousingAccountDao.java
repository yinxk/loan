package top.yinxiaokang.original.dao;

import top.yinxiaokang.original.entity.SthousingAccount;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author yinxk
 * @date 2018/8/6 11:08
 */
public class SthousingAccountDao extends BaseDao {

    public SthousingAccountDao(Connection connection) {
        super(connection);
    }

    public SthousingAccount getAccountByDkzh(String dkzh) throws IllegalAccessException, SQLException, InstantiationException {
        String sql = "SELECT acc.id, acc.dkffe, acc.DKFFRQ, acc.DKLL, acc.DKQS, acc.DKYE, acc.DKZH, loan.DKHKFS, accex.DKGBJHQS, accex.DKGBJHYE, accex.DKXFFRQ  , accex.DQQC  " +
                "FROM st_housing_personal_account acc " +
                "INNER JOIN st_housing_personal_loan loan ON acc.contract = loan.id " +
                "INNER JOIN c_loan_housing_personal_account_extension accex ON acc.extenstion = accex.id " +
                "WHERE acc.DKZH = ? AND acc.deleted = 0";
        List<SthousingAccount> list = list(SthousingAccount.class, sql, dkzh);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
