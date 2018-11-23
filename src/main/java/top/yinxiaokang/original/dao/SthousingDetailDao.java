package top.yinxiaokang.original.dao;

import top.yinxiaokang.original.entity.SthousingDetail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SthousingDetailDao extends BaseDao {

    public SthousingDetailDao(Connection conn) {
        super(conn);
    }

    public List<SthousingDetail> listByDkzh(String dkzh) throws IllegalAccessException, SQLException, InstantiationException {
        return listByDkzh(dkzh, true);
    }

    public List<SthousingDetail> listByDkzh(String dkzh, boolean showLog) throws IllegalAccessException, SQLException, InstantiationException {
        String sql = "SELECT de.id, de.DQQC, de.DKYWMXLX, de.BJJE, de.LXJE, de.FXJE, de.FSE, de.YWFSRQ, deex.XQDKYE, de.created_at, de.extenstion , deex.remark  " +
                "FROM st_housing_business_details de INNER JOIN c_housing_business_details_extension deex ON de.extenstion = deex.id " +
                "WHERE de.dkzh = ? AND de.JZRQ IS NOT NULL AND de.deleted = 0 AND deex.deleted = 0 ";
        List<SthousingDetail> list = listIsShowLog(SthousingDetail.class, sql, showLog, dkzh);
        return list;
    }
}
