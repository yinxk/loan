package top.yinxiaokang.original.service;

import top.yinxiaokang.original.Conn;
import top.yinxiaokang.original.LoanRepaymentAlgorithm;
import top.yinxiaokang.original.Utils;
import top.yinxiaokang.original.dao.SthousingAccountDao;
import top.yinxiaokang.original.dao.SthousingDetailDao;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.others.CurrentPeriodRange;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author yinxk
 * @date 2018/8/6 10:57
 */
public class AccountCheck {


    private Connection connection;

    private SthousingAccountDao sthousingAccountDao = null;
    private SthousingDetailDao sthousingDetailDao = null;

    public AccountCheck() {
        Conn conn = new Conn();
        connection = conn.getConnection();
        sthousingAccountDao = new SthousingAccountDao(connection);
        sthousingDetailDao = new SthousingDetailDao(connection);
    }

    public SthousingAccount getSthousingAccount(String dkzh) {
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

    public List<SthousingDetail> listDetails(SthousingAccount account) {
        try {
            List<SthousingDetail> sthousingDetails = sthousingDetailDao.listByDkzh(account.getDkzh());
            return sthousingDetails;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CurrentPeriodRange> listHSRange(String dkzh,Date hssj) {
        SthousingAccount sthousingAccount = getSthousingAccount(dkzh);
        List<CurrentPeriodRange> ranges = listHSRange(sthousingAccount,hssj);
        return ranges;
    }


    public BigDecimal syqs(List<CurrentPeriodRange> ranges, SthousingAccount account) {
        BigDecimal yhqs = BigDecimal.ZERO;
        if (!ranges.isEmpty()) {
            yhqs = new BigDecimal(ranges.get(0).getCurrentPeriod() - 1);
        }
        return account.getDkqs().subtract(yhqs);
    }




    public List<CurrentPeriodRange> listHSRange(SthousingAccount account,Date hssj) {
        try {
            Date dkffrq = account.getDkffrq();
            if (hssj == null) {
                hssj = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(hssj);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                hssj = calendar.getTime();
            }
            Date soutStartDate = Utils.SDF_YEAR_MONTH_DAY.parse("2017-12-01");
            List<CurrentPeriodRange> ranges = LoanRepaymentAlgorithm.listHSRange(dkffrq, hssj, soutStartDate);
            return ranges;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Conn.closeResource(connection, null, null);
    }
}
