package top.yinxiaokang.original.dao;

import top.yinxiaokang.original.entity.SomedayInformation;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.util.Utils;

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
        String sql = "SELECT\n" +
                "\ta.*,\n" +
                "\tif ((a.ffday = a.xffday) =1 , '是' , '否') AS ffdaysfxd \n" +
                "FROM\n" +
                "\t(\n" +
                "\tSELECT\n" +
                "\t\tacc.DKZH,\n" +
                "\t\tDATE_FORMAT(acc.DKFFRQ,'%Y-%m-%d') AS dkffrq,\n" +
                "\t\tacc.dkqs - accex.DKGBJHQS + accex.DQQC AS qc,\n" +
                "\t\taccex.DKXFFRQ,\n" +
                "\t\taccex.DQQC,\n" +
                "\t\tDATE_FORMAT( DATE_ADD( accex.DKXFFRQ, INTERVAL accex.DQQC MONTH ), '%Y-%m-%d' ) AS nextkkrq,\n" +
                "\t\tDATE_FORMAT( acc.DKFFRQ, '%d' ) ffday,\n" +
                "\t\tCASE basic.DKZHZT \n" +
                "\t\t\tWHEN 0 THEN '待签合同' \n" +
                "\t\t\tWHEN 1 THEN '待放款' \n" +
                "\t\t\tWHEN 2 THEN '正常' \n" +
                "\t\t\tWHEN 3 THEN '已结清' \n" +
                "\t\t\tWHEN 4 THEN '呆账' \n" +
                "\t\t\tWHEN 5 THEN '逾期' \n" +
                "\t\t\tWHEN 6 THEN '待确认' \n" +
                "\t\t\tWHEN 7 THEN '所有' \n" +
                "\t\t\tWHEN 8 THEN '已作废' \n" +
                "\t\t\tWHEN 9 THEN\t'暂停计息' \n" +
                "\t\tEND as DKZHZT,\n" +
                "\t\tbasic.JKRXM,\n" +
                "\t\tif(fbasic.WTKHYJCE = 1 , '是','否') AS sfwtkr ,\n" +
                "\t\tDATE_FORMAT( accex.DKXFFRQ, '%d' ) xffday \n" +
                "\tFROM\n" +
                "\t\tst_housing_personal_account acc\n" +
                "\t\tINNER JOIN c_loan_housing_personal_account_extension accex ON acc.extenstion = accex.id\n" +
                "\t\tINNER JOIN c_loan_housing_person_information_basic basic on basic.personalAccount = acc.id\n" +
                "\t\tLEFT JOIN c_loan_funds_information_basic fbasic ON fbasic.id = basic.fundsBasic \n" +
                "\tWHERE\n" +
                "\t\tbasic.DKZHZT <> 3\n" +
                "\t\tAND acc.DKZH IN (?) \n" +
                "\t) a \n" +
                "WHERE\n" +
                "\ta.xffday <= ?\n" +
                "\tAND nextkkrq BETWEEN '2017-12-01 00:00:00' \n" +
                "\tAND ?\n" +
                "ORDER BY nextkkrq DESC";

        return list(SomedayInformation.class, sql, initDkzhsStr, kkdayEnd, Utils.SDF_YEAR_MONTH_DAY.format(nextkkrqEnd));
    }
}
