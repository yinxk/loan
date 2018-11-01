package top.yinxiaokang.original.dao;

import org.junit.Test;
import top.yinxiaokang.original.component.Conn;
import top.yinxiaokang.original.entity.SthousingDetail;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SthousingDetailDaoTest {
    @Test
    public void listByDkzh() throws Exception {
        Conn conn = new Conn();
        SthousingDetailDao sthousingDetailDao = new SthousingDetailDao(conn.getConnection());
        String dkzh = "52001069403600000000291951";
        List<SthousingDetail> sthousingDetails = sthousingDetailDao.listByDkzh(dkzh);
        Collections.sort(sthousingDetails, Comparator.comparing(SthousingDetail::getDqqc));
        for (SthousingDetail detail : sthousingDetails) {
            System.out.println(detail);
        }
    }

}