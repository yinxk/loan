package top.yinxiaokang.original.dao;

import top.yinxiaokang.original.entity.CLoanHousingPersonInformationBasic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author yinxk
 * @date 2018/10/26 14:42
 */
@SuppressWarnings("UnnecessaryLocalVariable")
public class CLoanHousingPersonInformationBasicDao extends BaseDao {

    public CLoanHousingPersonInformationBasicDao(Connection conn) {
        super(conn);
    }

    public CLoanHousingPersonInformationBasic getBasicByDkzh(String dkzh) throws IllegalAccessException, SQLException, InstantiationException {
        return getBasicByDkzh(dkzh, true);
    }

    public CLoanHousingPersonInformationBasic getBasicByDkzh(String dkzh, boolean showLog) throws IllegalAccessException, SQLException, InstantiationException {
        String sql = "SELECT * FROM c_loan_housing_person_information_basic basic WHERE basic.DKZH = ? AND basic.deleted = 0";
        List<CLoanHousingPersonInformationBasic> list = listIsShowLog(CLoanHousingPersonInformationBasic.class, sql, showLog, dkzh);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public List<CLoanHousingPersonInformationBasic> listBasicByDkzhs(String dkzhsStr) throws IllegalAccessException, SQLException, InstantiationException {
        String sql = "SELECT * FROM c_loan_housing_person_information_basic basic WHERE basic.DKZH in ( ? ) AND basic.deleted = 0";
        List<CLoanHousingPersonInformationBasic> list = list(CLoanHousingPersonInformationBasic.class, sql, dkzhsStr);
        return list;
    }

}
