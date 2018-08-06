package top.yinxiaokang.original.dao;

import top.yinxiaokang.original.entity.SthousingAccount;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author yinxk
 * @date 2018/8/6 11:08
 */
public class SthousingAccountDao extends BaseDao{

    public SthousingAccountDao(Connection connection) {
        super(connection);
    }

    public SthousingAccount getAccountByDkzh(String dkzh) throws IllegalAccessException, SQLException, InstantiationException {
        String sql = "SELECT * FROM st_housing_personal_account acc WHERE acc.DKZH = ?";
        List<SthousingAccount> list = list(SthousingAccount.class, sql, dkzh);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}
