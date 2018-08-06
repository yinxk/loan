package top.yinxiaokang.original.service;

import top.yinxiaokang.original.Conn;
import top.yinxiaokang.original.LoanRepaymentAlgorithm;
import top.yinxiaokang.original.Utils;
import top.yinxiaokang.original.dao.SthousingAccountDao;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.others.CurrentPeriodRange;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * @author yinxk
 * @date 2018/8/6 10:57
 */
public class AccountCheck {


    private SthousingAccountDao sthousingAccountDao = null;
    {
        Conn conn = new Conn();
        sthousingAccountDao = new SthousingAccountDao(conn.getConnection());
    }

    public SthousingAccount getSthousingAccount(String dkzh){
        try {
            SthousingAccount accountByDkzh = sthousingAccountDao.getAccountByDkzh(dkzh);
            return accountByDkzh;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CurrentPeriodRange> listHSRange(String dkzh) {
        SthousingAccount sthousingAccount = getSthousingAccount(dkzh);
        List<CurrentPeriodRange> ranges = listHSRange(sthousingAccount);
        return ranges;
    }

    public List<CurrentPeriodRange> listHSRange(SthousingAccount account) {
        try {
            Date dkffrq = account.getDkffrq();
            Date hssj = Utils.SDF_YEAR_MONTH_DAY.parse("2018-7-27");
            Date soutStartDate = Utils.SDF_YEAR_MONTH_DAY.parse("2017-12-01");
            List<CurrentPeriodRange> ranges = LoanRepaymentAlgorithm.listHSRange(dkffrq, hssj, soutStartDate);
            //LoanRepaymentAlgorithm.calHSRange(dkffrq, hssj, soutStartDate);
            return ranges;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
