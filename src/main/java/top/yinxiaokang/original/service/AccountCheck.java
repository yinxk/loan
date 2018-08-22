package top.yinxiaokang.original.service;

import top.yinxiaokang.original.Conn;
import top.yinxiaokang.original.LoanRepaymentAlgorithm;
import top.yinxiaokang.original.Utils;
import top.yinxiaokang.original.dao.SthousingAccountDao;
import top.yinxiaokang.original.dao.SthousingDetailDao;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.loan.repayment.RepaymentItem;
import top.yinxiaokang.original.loan.repayment.RepaymentMethod;
import top.yinxiaokang.original.loan.repayment.RepaymentMonthRateScale;
import top.yinxiaokang.original.loan.repayment.RepaymentPlan;
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

    /**
     * 根据贷款账号查询贷款信息
     *
     * @param dkzh
     * @return
     */
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

    /**
     * 查询某账号的所有已入账的业务记录
     *
     * @param account
     * @return
     */
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


    /**
     * 剩余期数
     *
     * @param yhqs    已还期数
     * @param account 账号对象
     * @return
     */
    public BigDecimal syqs(BigDecimal yhqs, SthousingAccount account) {
        return account.getDkqs().subtract(yhqs);
    }

    /**
     * 已还期数
     *
     * @param ranges 根据核算时间得到的list
     * @return
     */
    public BigDecimal yhqs(List<CurrentPeriodRange> ranges) {
        BigDecimal yhqs = BigDecimal.ZERO;
        if (ranges != null && !ranges.isEmpty()) {
            yhqs = new BigDecimal(ranges.get(0).getCurrentPeriod() - 1);
        }
        return yhqs;

    }


    /**
     * @param account         账号
     * @param ranges          核算list
     * @param initDkye        初始贷款余额
     * @param initOverdueBjje 初始逾期本金
     * @param isSubtract      是否减
     * @return
     */
    public List<RepaymentItem> repaymentItems(SthousingAccount account,
                                              List<CurrentPeriodRange> ranges,
                                              BigDecimal initDkye,
                                              BigDecimal initOverdueBjje,
                                              Boolean isSubtract) {
        BigDecimal yhqs = yhqs(ranges);
        BigDecimal syqs = syqs(yhqs, account);
        Date dkxffrq = getFirstDkxffrq(ranges);

        if (initOverdueBjje == null) {
            initOverdueBjje = BigDecimal.ZERO;
        }
        // 贷款新发放额, 减去逾期再生成还款计划,与以前的记录做对比
        if (isSubtract == null) {
            isSubtract = true;
        }
        BigDecimal dkxffe = initDkye;
        if (isSubtract) {
            dkxffe = initDkye.subtract(initOverdueBjje);
        }
        // 初始还款计划,如果后面发生提前还款 , 那么还款计划会发生改变
        List<RepaymentItem> repaymentItems = RepaymentPlan.listRepaymentPlan(dkxffe, dkxffrq, syqs.intValue(), account.getDkll(),
                RepaymentMethod.getRepaymentMethodByCode(account.getDkhkfs()), yhqs.intValue(), RepaymentMonthRateScale.YES);
        return repaymentItems;
    }

    /**
     * @param dkzh            贷款账号
     * @param initDkye        导入系统初始贷款余额
     * @param initOverdueBjje 导入系统初始逾期本金(可为null)
     * @param isSubtract      是否将初始贷款余额减去初始逾期本金再计算还款计划
     * @return 还款计划
     */
    public List<RepaymentItem> repaymentItems(String dkzh, BigDecimal initDkye, BigDecimal initOverdueBjje, Boolean isSubtract) {
        SthousingAccount account = getSthousingAccount(dkzh);
        List<CurrentPeriodRange> ranges = listHSRange(account, null);
        List<RepaymentItem> repaymentItems = repaymentItems(account, ranges, initDkye, initOverdueBjje, isSubtract);
        return repaymentItems;
    }

    /**
     * 核算 第一期
     *
     * @param ranges
     * @return
     */
    public CurrentPeriodRange getFirstCurrentPeriodRange(List<CurrentPeriodRange> ranges) {
        CurrentPeriodRange currentPeriodRange = null;
        if (!ranges.isEmpty()) {
            currentPeriodRange = ranges.get(0);
        }
        return currentPeriodRange;
    }

    /**
     * 根据 核算 , 确定dkxffrq
     *
     * @param ranges
     * @return
     */
    public Date getFirstDkxffrq(List<CurrentPeriodRange> ranges) {
        return getFirstDkxffrq(getFirstCurrentPeriodRange(ranges));
    }

    /**
     * 根据 核算第一期, 确定dkxffrq
     *
     * @param first
     * @return
     */
    public Date getFirstDkxffrq(CurrentPeriodRange first) {
        Date dkxffrq = first == null ? null : first.getBeforeTime();
        return dkxffrq;
    }

    /**
     * 计算从2017-12-1 到 核算时间(如:2018-8-20)期间的 (期次, 对应开始时间, 结束时间)
     *
     * @param account
     * @param hssj    核算时间(核算日期)
     * @return
     */
    public List<CurrentPeriodRange> listHSRange(SthousingAccount account, Date hssj) {
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
