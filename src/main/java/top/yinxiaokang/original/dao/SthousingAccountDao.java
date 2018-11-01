package top.yinxiaokang.original.dao;

import top.yinxiaokang.original.entity.SomedayInformation;
import top.yinxiaokang.original.entity.SthousingAccount;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
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

    public List<SomedayInformation> listSomedayInformation(Integer kkdayEnd, Date nextkkrqEnd, String initDkzhsStr) throws IllegalAccessException, SQLException, InstantiationException {
        String sql = "SELECT " +
                " a.*, " +
                " if ((a.ffday = a.xffday) =1 , '是' , '否') AS ffdaysfxd  " +
                "FROM " +
                " ( " +
                " SELECT " +
                "  acc.DKZH, " +
                "  DATE_FORMAT(acc.DKFFRQ,'%Y-%m-%d') AS dkffrq, " +
                "  acc.dkqs - accex.DKGBJHQS + accex.DQQC AS qc, " +
                "  accex.DKXFFRQ, " +
                "  accex.DQQC, " +
                "  DATE_FORMAT( DATE_ADD( accex.DKXFFRQ, INTERVAL accex.DQQC MONTH ), '%Y-%m-%d' ) AS nextkkrq, " +
                "  DATE_FORMAT( acc.DKFFRQ, '%d' ) ffday, " +
                "  CASE basic.DKZHZT  " +
                "   WHEN 0 THEN '待签合同'  " +
                "   WHEN 1 THEN '待放款'  " +
                "   WHEN 2 THEN '正常'  " +
                "   WHEN 3 THEN '已结清'  " +
                "   WHEN 4 THEN '呆账'  " +
                "   WHEN 5 THEN '逾期'  " +
                "   WHEN 6 THEN '待确认'  " +
                "   WHEN 7 THEN '所有'  " +
                "   WHEN 8 THEN '已作废'  " +
                "   WHEN 9 THEN '暂停计息'  " +
                "  END as DKZHZT, " +
                "  basic.JKRXM, " +
                "  if(fbasic.WTKHYJCE = 1 , '是','否') AS sfwtkr , " +
                "  DATE_FORMAT( accex.DKXFFRQ, '%d' ) xffday  " +
                " FROM " +
                "  st_housing_personal_account acc " +
                "  INNER JOIN c_loan_housing_personal_account_extension accex ON acc.extenstion = accex.id " +
                "  INNER JOIN c_loan_housing_person_information_basic basic on basic.personalAccount = acc.id " +
                "  LEFT JOIN c_loan_funds_information_basic fbasic ON fbasic.id = basic.fundsBasic  " +
                " WHERE " +
                "  basic.DKZHZT <> 3 " +
                "  AND acc.DKZH IN (?)  " +
                " ) a  " +
                "WHERE " +
                " a.xffday <= ? " +
                " AND nextkkrq BETWEEN '2017-12-01 00:00:00'  " +
                " AND ? " +
                "ORDER BY nextkkrq DESC";

        return list(SomedayInformation.class, sql, initDkzhsStr, kkdayEnd, nextkkrqEnd);
    }
}
