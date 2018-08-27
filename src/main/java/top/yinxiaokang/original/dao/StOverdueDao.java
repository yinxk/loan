package top.yinxiaokang.original.dao;

import top.yinxiaokang.original.entity.StOverdue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author yinxk
 * @date 2018/8/27 13:30
 */
public class StOverdueDao extends BaseDao {
    public StOverdueDao(Connection conn) {
        super(conn);
    }

    public List<StOverdue> listByDkzh(String dkzh) throws IllegalAccessException, SQLException, InstantiationException {
        String sql = "SELECT over.dkzh,over.SSRQ,over.YQBJ,over.YQLX,over.YQFX,over.YQQC,overex.YWZT " +
                "FROM st_housing_overdue_registration over " +
                "INNER JOIN c_housing_overdue_registration_extension overex ON over.extenstion=overex.id " +
                "WHERE over.DKZH=?";
        List<StOverdue> list = list(StOverdue.class, sql, dkzh);
        return list;
    }
}
