package top.yinxiaokang.original.dao;

import org.junit.Test;
import top.yinxiaokang.original.component.Conn;
import top.yinxiaokang.original.entity.SthousingDetail;

import java.util.Collection;

public class BaseDaoTest {
    @Test
    public void list() throws Exception {
        Conn conn = new Conn();
        BaseDao baseDao = new BaseDao(conn.getConnection());
        String sql = "SELECT de.id, de.DQQC, de.DKYWMXLX, de.BJJE, de.LXJE, de.FXJE, de.FSE, de.YWFSRQ, deex.XQDKYE, de.created_at, de.extenstion  " +
                "FROM st_housing_business_details de INNER JOIN c_housing_business_details_extension deex ON de.extenstion = deex.id " +
                "WHERE de.JZRQ IS NOT NULL";
        Object[] params = {};
        long start = System.currentTimeMillis();
        Collection<SthousingDetail> list = baseDao.list(SthousingDetail.class, sql, params);
        long end = System.currentTimeMillis();
        for (SthousingDetail detail : list)
            System.out.println(detail);

        System.out.printf("所用的时间是 %s \n: "  , (end - start));
    }

    @Test
    public void selectCommon() throws Exception {
    }

    @Test
    public void updateCommon() throws Exception {
    }

}