package top.yinxiaokang.original.service;

import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.component.Conn;
import top.yinxiaokang.original.component.LoanRepaymentAlgorithm;
import top.yinxiaokang.original.entity.*;
import top.yinxiaokang.util.Utils;
import top.yinxiaokang.original.dao.CLoanHousingPersonInformationBasicDao;
import top.yinxiaokang.original.dao.StOverdueDao;
import top.yinxiaokang.original.dao.SthousingAccountDao;
import top.yinxiaokang.original.dao.SthousingDetailDao;
import top.yinxiaokang.original.dto.AccountInformations;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.enums.LoanBusinessType;
import top.yinxiaokang.original.loan.repayment.RepaymentItem;
import top.yinxiaokang.original.loan.repayment.RepaymentMethod;
import top.yinxiaokang.original.loan.repayment.RepaymentMonthRateScale;
import top.yinxiaokang.original.loan.repayment.RepaymentPlan;
import top.yinxiaokang.others.CurrentPeriodRange;
import top.yinxiaokang.util.Common;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/8/6 10:57
 */
@SuppressWarnings("UnnecessaryLocalVariable")
@Slf4j
public class AccountCheck {


    private Connection connection;

    private SthousingAccountDao sthousingAccountDao;
    private SthousingDetailDao sthousingDetailDao;
    private StOverdueDao stOverdueDao;
    private CLoanHousingPersonInformationBasicDao cLoanHousingPersonInformationBasicDao;

    public AccountCheck() {
        Conn conn = new Conn();
        connection = conn.getConnection();
        sthousingAccountDao = new SthousingAccountDao(connection);
        sthousingDetailDao = new SthousingDetailDao(connection);
        stOverdueDao = new StOverdueDao(connection);
        cLoanHousingPersonInformationBasicDao = new CLoanHousingPersonInformationBasicDao(connection);
    }

    public List<SthousingAccount> listSthousingAccountByDkzhs(String dkzhs) {
        List<SthousingAccount> sthousingAccounts = new ArrayList<>();
        try {
            sthousingAccounts = sthousingAccountDao.listAccountByDkzhs(dkzhs);
        } catch (IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        return sthousingAccounts;
    }

    public List<SomedayInformation> listSomedayInformation(Integer kkdayEnd, Date nextkkrqEnd, String initDkzhsStr) {
        List<SomedayInformation> somedayInformations = new ArrayList<>();
        try {
            somedayInformations = sthousingAccountDao.listSomedayInformation(kkdayEnd, nextkkrqEnd, initDkzhsStr);
        } catch (IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        return somedayInformations;
    }

    public List<SomedayInformation> listSomedayInformationByOverdueDkzh(String dkzhsStr) {
        List<SomedayInformation> somedayInformations = new ArrayList<>();
        try {
            somedayInformations = sthousingAccountDao.listSomedayInformationByOverdueDkzh(dkzhsStr);
        } catch (IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        return somedayInformations;
    }

    /**
     * 根据贷款账号获取basic
     *
     * @param dkzh
     * @return
     */
    public CLoanHousingPersonInformationBasic getBasicByDkzh(String dkzh) {
        try {
            CLoanHousingPersonInformationBasic basicByDkzh = cLoanHousingPersonInformationBasicDao.getBasicByDkzh(dkzh);
            return basicByDkzh;
        } catch (IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CLoanHousingPersonInformationBasic> listBasicByDkzh(String dkzhsStr) {
        try {
            List<CLoanHousingPersonInformationBasic> cLoanHousingPersonInformationBasics = cLoanHousingPersonInformationBasicDao.listBasicByDkzhs(dkzhsStr);
            return cLoanHousingPersonInformationBasics;
        } catch (IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据贷款账号查询逾期信息 , 逾期里面没有发现除了已作废这类状态, 根据期次排序
     *
     * @param dkzh
     * @return
     */
    public List<StOverdue> listOverdueByDkzh(String dkzh) {
        List<StOverdue> stOverdues = null;
        try {
            stOverdues = stOverdueDao.listByDkzh(dkzh);
            Collections.sort(stOverdues, Comparator.comparing(StOverdue::getYqqc));
        } catch (IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        return stOverdues;
    }

    public List<StOverdue> listOverdueDkzhsInTheDkzhsStr(String dkzhsStr) {
        List<StOverdue> stOverdues = null;
        try {
            stOverdues = stOverdueDao.listOverdueDkzhsInTheDkzhsStr(dkzhsStr);
        } catch (IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        return stOverdues;
    }


    public List<StOverdue> listInitOverdue(List<StOverdue> overdues, BigDecimal initFirstQc) {
        List<StOverdue> initOverdues = new ArrayList<>();
        for (StOverdue overdue : overdues) {
            if (initFirstQc.compareTo(overdue.getYqqc()) > 0) {
                initOverdues.add(overdue);
            }
        }
        return initOverdues;
    }

    /**
     * 将每个账号有关的信息转换整理
     *
     * @param initInformation
     * @return
     */
    public AccountInformations toAccountInformations(InitInformation initInformation) {
        log.info("处理  " + initInformation);
        AccountInformations accountInformations = new AccountInformations();
        SthousingAccount account = getSthousingAccount(initInformation.getDkzh());
        // 初始逾期本金大于0 , 则导入系统存在逾期记录
        if (initInformation.getCsyqbj().compareTo(BigDecimal.ZERO) > 0) {
            accountInformations.setInitHasOverdue(true);
        }
        if (account == null)
            return null;
        CLoanHousingPersonInformationBasic basic = getBasicByDkzh(initInformation.getDkzh());
        accountInformations.setCLoanHousingPersonInformationBasic(basic);
        //region 如果可以的话, 使用扩展表的dkxxffrq中的日来作为还款日
//        try {
//            String dkffrqStr = Utils.SDF_YEAR_MONTH_DAY.format(account.getDkffrq());
//            String dkxffrqStr = Utils.SDF_YEAR_MONTH_DAY.format(account.getDkxffrq());
//            String nowDkffrqStr = dkffrqStr.substring(0, 7) + dkxffrqStr.substring(7, 10);
//            account.setDkffrq(Utils.SDF_YEAR_MONTH_DAY.parse(nowDkffrqStr));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        //endregion
        List<CurrentPeriodRange> ranges = listHSRange(account, null);
        BigDecimal yhqs = yhqs(ranges);
        BigDecimal initFirstQc = yhqs.add(BigDecimal.ONE);
        if (accountInformations.isInitHasOverdue()) {
            List<StOverdue> stOverdues = listOverdueByDkzh(account.getDkzh());
            List<StOverdue> initOverdues = listInitOverdue(stOverdues, initFirstQc);
            accountInformations.setInitOverdueList(initOverdues);
        }

        // 该账号已入账的业务记录
        List<SthousingDetail> sthousingDetails = listDetails(account);
        Collections.sort(sthousingDetails, Comparator.comparing(SthousingDetail::getDqqc));
        // 还款计划
        List<RepaymentItem> repaymentItems = repaymentItems(account, ranges, initInformation.getCsye(), initInformation.getCsyqbj(), true);
        accountInformations.setSthousingAccount(account);
        accountInformations.setCurrentPeriodRanges(ranges);
        accountInformations.setYhqs(yhqs);
        accountInformations.setSyqs(syqs(yhqs, account));
        accountInformations.setInitFirstQc(initFirstQc);
        accountInformations.setDetails(sthousingDetails);
        accountInformations.setInitInformation(initInformation);
        accountInformations.setRepaymentItems(repaymentItems);
        return accountInformations;
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
     * 获取本息反了的期次(只能参考)
     *
     * @param informations
     * @return
     */
    public List<Integer> analyzeReverseBx(AccountInformations informations) {
        List<Integer> reverseQc = new ArrayList<>();
        List<RepaymentItem> repaymentItems = repaymentItems(informations.getSthousingAccount(),
                informations.getCurrentPeriodRanges(),
                informations.getInitInformation().getCsye(),
                informations.getInitInformation().getCsyqbj(),
                false);
        // 前一项是否为提前还款
        boolean isPreItemPrepayment = false;
        // 已经根据期次顺序排序
        List<SthousingDetail> details = informations.getDetails();
        for (SthousingDetail detail : details) {
            // 过滤不是我们系统的期次的业务
            if (detail.getDqqc().compareTo(informations.getInitFirstQc()) < 0)
                continue;
            RepaymentItem item = Common.getRepaymentItemByDqqc(repaymentItems, detail.getDqqc().intValue());
            if (item == null)
                continue;

            // 存在提前还款 , 需要新的还款计划 , 根据业务中提前还款剩余的余额进行推算, 期次也是 ,如果该业务的期次或者期末余额有一项不对, 那么提前还款后的本息倒置得不到有效的结果
            if (LoanBusinessType.提前还款.getCode().equals(detail.getDkywmxlx())) {
                repaymentItems = RepaymentPlan.listRepaymentPlan(detail.getXqdkye(), informations.getSthousingAccount().getDkffrq()
                        , informations.getSthousingAccount().getDkqs().subtract(detail.getDqqc()).intValue(), informations.getSthousingAccount().getDkll(),
                        RepaymentMethod.getRepaymentMethodByCode(informations.getSthousingAccount().getDkhkfs()), detail.getDqqc().intValue(), RepaymentMonthRateScale.NO);
                isPreItemPrepayment = true;
            }
            // 提前还款或者结清没有本息倒置的情况, 与还款计划比较, 自动过滤了, 不需要考虑

            // 前一项为提前还款 , 由于提前还款后第一期利息比还款计划多, 那么只能比较本金来 , 可能是本息颠倒
            if (isPreItemPrepayment) {
                isPreItemPrepayment = false;
                if (item.getHkbjje().subtract(detail.getLxje()).abs().compareTo(Common.ERROR_RANGE) <= 0) {
                    reverseQc.add(detail.getDqqc().intValue());
                }
            } else if (item.getHkbjje().subtract(detail.getLxje()).abs().compareTo(Common.ERROR_RANGE) <= 0
                    && item.getHklxje().subtract(detail.getBjje()).abs().compareTo(Common.ERROR_RANGE) <= 0) {
                reverseQc.add(detail.getDqqc().intValue());
            }

        }
        return reverseQc;
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
                RepaymentMethod.getRepaymentMethodByCode(account.getDkhkfs()), yhqs.intValue(), RepaymentMonthRateScale.NO);
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
     * 计算从2017-11-30 到 核算时间(如:2018-8-20)期间的 (期次, 对应开始时间, 结束时间)
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
            Date soutStartDate = Utils.SDF_YEAR_MONTH_DAY.parse("2017-11-30");
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
