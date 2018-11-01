package top.yinxiaokang.original.component;

import top.yinxiaokang.others.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Funnyboy on 2017/8/7.
 */
public final class CommLoanAlgorithm {

    private final static SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat ny = new SimpleDateFormat("yyyy-MM");


    /**
     * 等额本息计算月发生额计算
     *
     * @param dkffe 贷款发放额
     * @param hkqs  还款期数
     * @param dknlv 贷款年利率
     * @return 月发生额
     */
    private static BigDecimal calBxFse(BigDecimal dkffe, int hkqs, BigDecimal dknlv) {
        BigDecimal fse = BigDecimal.ZERO;
        BigDecimal monthRate = LoanRepaymentAlgorithm.convertYearRateToMonthRate(dknlv);
        fse = monthRate.multiply(BigDecimal.ONE.add(monthRate).pow(hkqs))
                .multiply(dkffe)
                .divide(BigDecimal.ONE.add(monthRate).pow(hkqs).subtract(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
        return fse;
    }

    /**
     * 等额本金月还本金计算
     *
     * @param dkffe 贷款发放额
     * @param hkqs  还款期数
     * @return 月还本金
     */
    private static BigDecimal calBjHkbjje(BigDecimal dkffe, int hkqs) {
        BigDecimal bjje = BigDecimal.ZERO;
        bjje = dkffe.divide(new BigDecimal(hkqs), 2, BigDecimal.ROUND_HALF_UP);
        return bjje;
    }

    /**
     * 本期本息：等额本息4/本金5
     *
     * @param DKFFE  合同贷款金额
     * @param DKQS   贷款期数
     * @param DKHKFS 贷款还款方式
     * @param DKLL   贷款利率(年)
     * @param DQQC   当期期次
     */
    public static BigDecimal currentBX(BigDecimal DKFFE, int DKQS, String DKHKFS, BigDecimal DKLL, int DQQC) {
        if (DKFFE == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款发放额为空");
        if (DKLL == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款利率为空");
        if (DKQS <= 0) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款期数不小于0");
        if (!StringUtil.notEmpty(DKHKFS) || (!DKHKFS.equals("02") && !DKHKFS.equals("01")))
            throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款还款方式为空,或者02本金01本息");
        if (DQQC <= 0) return BigDecimal.ZERO;
        if (DQQC > DKQS) return BigDecimal.ZERO;
        try {
            // 由于之前计算利息的方式, 对于某些期数存在0.01的误差, 导致本金也存在0.01的误差 , 所以统一使用还款计划来获取值
            LinkedList<HousingfundAccountPlanGetInformation> housingfundAccountPlanGetInformations = repaymentPlan(DKFFE, DKQS, DKHKFS, DKLL, null);
            if (housingfundAccountPlanGetInformations.size() <= 0)
                return BigDecimal.ZERO;
            HousingfundAccountPlanGetInformation housingfundAccountPlanGetInformation = housingfundAccountPlanGetInformations.get(DQQC - 1);
            return new BigDecimal(housingfundAccountPlanGetInformation.getFSE());
        } catch (Exception e) {
            throw new ErrorException(e);
        }
    }

    /**
     * 逾期利息（本期利息/等额本金、等额本息）(一期，循环调用得到总的逾期利息)  <br />
     * 瞎备注
     * DKFFE:合同贷款金额
     * YQQC:逾期期次
     * DKHKFS：贷款还款方式
     * DKLL:贷款利率
     * DKQS：贷款期数
     * //
     */
    /**
     * 计算某一期利息
     *
     * @param DKFFE  贷款发放额
     * @param DQQC   需要获取利息的期次
     * @param DKHKFS 贷款还款方式
     * @param DKLL   贷款年利率(年利率,并且是%表示的)
     * @param DKQS   贷款期数或者还款期数
     * @return 某一期的利息
     */
    public final static BigDecimal overdueThisPeriodLX(BigDecimal DKFFE, int DQQC, String DKHKFS, BigDecimal DKLL, int DKQS) {
        if (DKFFE == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款发放额为空");
        if (DKLL == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款利率为空");
        if (DKQS <= 0) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款期数小于0");
        if (!StringUtil.notEmpty(DKHKFS) || (!DKHKFS.equals("02") && !DKHKFS.equals("01")))
            throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款还款方式为空,或者02本金01本息");
        if (DQQC <= 0) return BigDecimal.ZERO;
        if (DQQC > DKQS) return BigDecimal.ZERO;
        try {
            // 由于之前计算利息的方式, 对于某些期数存在0.01的误差, 导致本金也存在0.01的误差 , 所以统一使用还款计划来获取值
            LinkedList<HousingfundAccountPlanGetInformation> housingfundAccountPlanGetInformations = repaymentPlan(DKFFE, DKQS, DKHKFS, DKLL, null);
            if (housingfundAccountPlanGetInformations.size() <= 0)
                return BigDecimal.ZERO;
            HousingfundAccountPlanGetInformation housingfundAccountPlanGetInformation = housingfundAccountPlanGetInformations.get(DQQC - 1);
            return new BigDecimal(housingfundAccountPlanGetInformation.getHKLXJE());
        } catch (Exception e) {
            throw new ErrorException(e);
        }
    }

    /**
     * 剩余利息
     *
     * @param dqqc   当期期次
     * @param dkffe  贷款发放额
     * @param dkll   贷款利率
     * @param dkqs   贷款期数
     * @param dkhkfs 贷款还款方式
     * @return 从dqqc到结束的利息的和
     */
    public final static BigDecimal residualInterestPrincipal(int dqqc, BigDecimal dkffe, BigDecimal dkll, int dkqs, String dkhkfs) {
        BigDecimal sum = BigDecimal.ZERO;
        try {
            LinkedList<HousingfundAccountPlanGetInformation> information = repaymentPlan(dkffe, dkqs, dkhkfs, dkll, null);
            int i = 1;
            for (HousingfundAccountPlanGetInformation item : information) {
                if (i >= dqqc)
                    sum = sum.add(new BigDecimal(item.getHKLXJE()));
                i++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sum;
    }

    /**
     * 对应的某一期
     * 逾期罚息：逾期本息×罚息利率（贷款年利率/360*1.5）×逾期天数(循环调用得到总的逾期罚息，逾期天数通过数据库带调用计算得到)
     * YQBJ:逾期本金
     * YQLX：逾期利息(等额本金，等额本息)
     * DKNLV：贷款年利率
     * YQTS:逾期天数
     */
    public final static BigDecimal overdueFX(BigDecimal YQBJ, BigDecimal YQLX, BigDecimal DKNLV, Date DKFFRQ, int YQQC, Date YDKKRQ) {
        if (YQBJ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "逾期本金为空");
        if (YQLX == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "逾期利息为空");
        if (DKNLV == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款年利率不小于0");
        if (YQQC <= 0) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "逾期期次不小于0");
        if (DKFFRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款发放日期不能为空");
        if (YDKKRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "约定扣款日期不能为空");

        int LXDay = interestDays(DKFFRQ, YQQC, YDKKRQ);
        return YQBJ.add(YQLX).multiply(LoanRepaymentAlgorithm.convertYearRateToDayRate(DKNLV).multiply(new BigDecimal(1.5)))
                .multiply(new BigDecimal(LXDay))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

    }

    /**
     * 罚息天数（正的）
     * YDKKRQ:约定扣款日期
     * YQQC:当期期次
     * DKSQ:贷款期数
     * DKFFRQ:YYYYMMDD
     * dj
     */
    public final static int interestDays(Date DKFFRQ, int YQQC, Date YDKKRQ) {
        if (DKFFRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款发放日期不能为空");
        if (YDKKRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "约定扣款日期不能为空");
        if (YQQC <= 0) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "逾期期次期数不能小于0");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DKFFRQ);
        int day = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);//每月还款日
        calendar.add(Calendar.MONTH, YQQC);
        int month = calendar.get(Calendar.MONTH) + 1;//放款月
        int year = calendar.get(Calendar.YEAR);//当期年份
        calendar.setTime(YDKKRQ);
        int days = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);//约定还款日
        int months = calendar.get(Calendar.MONTH);//约定扣款月
        int years = calendar.get(Calendar.YEAR);//扣款年份
        int subyear = years - year;
        int submonth = months - month;
        int subday = 30 - day + days;
        int LXDay = subyear * 12 * 30 + submonth * 30 + subday;
        if (LXDay < 0) LXDay = 0;
        return LXDay;

    }

    /**
     * 提前部分还款
     * 提前还款金额＝本次还款金额－（罚息＋逾期本息＋本期本息）
     */
    @Deprecated
    public final static BigDecimal prepaymentKAmount(BigDecimal BCHKJE, BigDecimal BQBX) {
        if (BCHKJE == null) throw new ErrorException("BCHKJE为空");
        if (BQBX == null) throw new ErrorException("BQBX为空");
        try {
            return new BigDecimal(BCHKJE.doubleValue()).subtract(new BigDecimal(BQBX.doubleValue())).setScale(6, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            throw new ErrorException(e);
        }
    }

    @Deprecated
    public final static int repaymentInterestdays(Date DKFFRQ, Date YDKKRQ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DKFFRQ);
        int days = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);//每月还款日
        calendar.setTime(YDKKRQ);
        int day = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);//每月还款日
        int tqts = 0;
        if (days >= day) {
            tqts = days - day;
        } else {
            tqts = 30 - day + days;
        }
        return tqts;

    }

    /**
     * 提前部分还款 负的
     * 提前还本金额=提前还款金额÷（1＋贷款月利率×计息天数÷30天）
     */
    @Deprecated
    public final static BigDecimal prepaymentBAmount(BigDecimal TQHKJE, BigDecimal DKLL, int JXTS) {
        return new BigDecimal(TQHKJE.doubleValue()).divide(new BigDecimal(1).add(new BigDecimal(DKLL.doubleValue()).divide(new BigDecimal(1200), 10, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(JXTS)).divide(new BigDecimal(30), 10, BigDecimal.ROUND_HALF_UP)), 10, BigDecimal.ROUND_HALF_UP).setScale(6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计息天数(负的)(提前还款、后面的部分)
     */
    public final static int beforeInterestDays(Date DKFFRQ, int DQQC, Date YDKKRQ) {
        if (DKFFRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款发放日期不能为空");
        if (YDKKRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "约定扣款日期不能为空");
        //为0 没问题
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DKFFRQ);
            int days = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);//每月还款日
            calendar.add(Calendar.MONTH, DQQC);
            calendar.setTime(YDKKRQ);//约定扣款日期
            int day = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);
            int actday = 0;
            if (day <= days) {
                actday = days - day;
            } else {
                actday = 30 - day + days;
            }
            return -actday;
        } catch (Exception e) {
            throw new ErrorException(e);
        }
    }

    /**
     * 按照天计算利息
     */
    @Deprecated
    public final static BigDecimal beforeInterestDaysall(BigDecimal DKFFE, int DKQS, String DKHKFS, BigDecimal DKLL, Date DKFFRQ, Date DQSJ) throws ParseException {
        if (!StringUtil.notEmpty(DKHKFS) || (!DKHKFS.equals("02") && !DKHKFS.equals("01")))
            throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款还款方式为空,或者02本金01本息");
        LinkedList<HousingfundAccountPlanGetInformation> plan = repaymentPlan(DKFFE, DKQS, DKHKFS, DKLL, sim.format(DKFFRQ));
        DQSJ = sim.parse(sim.format(DQSJ));
        DKFFRQ = sim.parse(sim.format(DKFFRQ));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DKFFRQ);
        int yueday = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);//约定天
        BigDecimal total = BigDecimal.ZERO;
        int qs = 0;
        BigDecimal ye = BigDecimal.ZERO;
        for (int i = 1; i <= DKQS; i++) {
            long beforeTime = calendar.getTime().getTime();
            calendar.add(Calendar.MONTH, 1);
            if (beforeTime < DQSJ.getTime() && DQSJ.getTime() <= calendar.getTime().getTime()) {
                qs = i;
                ye = ye.add(new BigDecimal(plan.get(i - 1).getDKYE()).add(new BigDecimal(plan.get(i - 1).getHKBJJE())));
                break;
            }
            total = total.add(new BigDecimal(plan.get(i - 1).getHKLXJE()));
        }
        calendar.setTime(DQSJ);//当前时间
        int nowday = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);//天数
        if (nowday > yueday) {
            total = total.add(ye.multiply(DKLL.divide(new BigDecimal(1200), 10, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal(30), 10, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(nowday - yueday))));
        } else {
            total = total.add(ye.multiply(DKLL.divide(new BigDecimal(1200), 10, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal(30), 10, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(30 - yueday + nowday))));
        }

        return total.setScale(6, BigDecimal.ROUND_HALF_UP);
    }


    public final static int currentQS(Date DKFSRQ, Date YDKKRQ) {
        if (DKFSRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款发放日期不能为空");
        if (YDKKRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "约定扣款日期不能为空");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DKFSRQ);
        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        calendar.setTime(YDKKRQ);
        int days = calendar.get(Calendar.DATE);
        int months = calendar.get(Calendar.MONTH) + 1;
        int years = calendar.get(Calendar.YEAR);
        int subyear = years - year;
        int submonth = months - month;
        int total = subyear * 12 + submonth;
        if (day >= days) {
            total = total - 1;
        }
        return total + 1;

    }

    /**
     * 等额本金：剩余利息 (正常还款)
     */
    @Deprecated
    public final static BigDecimal residualInterest(int dqqc, BigDecimal dkll, String dkhkfs, int dkqs, BigDecimal dkffe) {

        BigDecimal total = BigDecimal.ZERO;
        for (int i = dqqc; i <= dkqs; i++) {
            //期初余额
            BigDecimal dkye = loanBalanceBefore(i, dkll, dkhkfs, dkqs, dkffe);
            if (dqqc == 0) continue;
            total = total.add(dkye.multiply(new BigDecimal(dkll.doubleValue()).divide(new BigDecimal(1200), 10, BigDecimal.ROUND_HALF_UP)));
        }
        return total.setScale(6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 贷款余额，扣前余额（期初余额）
     */
    @Deprecated
    public final static BigDecimal loanBalanceBefore(int dqqc, BigDecimal dkll, String dkhkfs, int dkqs, BigDecimal dkffe) {
        if (dqqc == 0) return dkffe;
        BigDecimal dkye = new BigDecimal(dkffe.doubleValue());
        for (int i = 1; i <= dkqs; i++) {
            BigDecimal bjje = principalAmount(dkffe, dkqs, dkll, dkhkfs, i);
            if (dqqc == i) {
                return dkye;
            }
            dkye = dkye.subtract(bjje);
        }
        return dkye.setScale(6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 贷款余额，扣后余额（期末余额）
     */
    @Deprecated
    public final static BigDecimal loanBalanceAfter(int dqqc, BigDecimal dkll, String dkhkfs, int dkqs, BigDecimal dkffe) {
        if (dqqc == 0) return dkffe;
        BigDecimal dkye = new BigDecimal(dkffe.doubleValue());
        for (int i = 1; i <= dkqs; i++) {
            BigDecimal bjje = principalAmount(dkffe, dkqs, dkll, dkhkfs, i);
            if (dqqc == i) {
                dkye = dkye.subtract(bjje);
                if (dkye.doubleValue() <= BigDecimal.ZERO.doubleValue()) dkye = BigDecimal.ZERO;
                return dkye;
            }
            dkye = dkye.subtract(bjje);
        }
        return dkye.setScale(6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 新首月计息天数
     */
    @Deprecated
    public final static int firstMonthInterestDays(Date DKFFRQ, Date YDKKRQ) {
        if (DKFFRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款发放日期不能为空");
        if (YDKKRQ == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "约定扣款日期不能为空");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DKFFRQ);
        int day = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);//原来天数
        calendar.setTime(YDKKRQ);
        int days = calendar.get(Calendar.DATE) > 30 ? 30 : calendar.get(Calendar.DATE);//约定扣款日期
        int actday = 0;
        if (days > day) {
            actday = 30 - days + day;
        } else {
            actday = day - days;
        }
        return actday + 30;

    }

    /**
     * 节约利息, 根据之前代码的意思, 进行修改 <br />
     * 之前的调用方式对于现在的计算方式不友好
     *
     * @param BCHKJE
     * @param DKLL
     * @param DKHKFS
     * @param syqc
     * @return
     */
    public final static BigDecimal savingLX(BigDecimal BCHKJE, BigDecimal DKLL, String DKHKFS, int syqc) {
        BigDecimal lxze = residualInterestPrincipal(1, BCHKJE, DKLL, syqc, DKHKFS);
        return lxze;
    }

    /**
     * 等额本息：新月还款额
     */
    public final static BigDecimal newCrescentRepayment(BigDecimal sybj, BigDecimal dkll, int syqs) {
        return calBxFse(sybj, syqs, dkll);
    }

    /**
     * 每月本金金额(等额本息、等额本金)
     */
    public final static BigDecimal principalAmount(BigDecimal dkffe, int dkqs, BigDecimal dkll, String dkhkfs, int dqqc) {

        if (dkffe == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款发放额为空");
        if (dkll == null) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款利率为空");
        if (dkqs <= 0) throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款期数不小于0");
        if (!StringUtil.notEmpty(dkhkfs) || (dkhkfs.equals("02") && dkhkfs.equals("01")))
            throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款还款方式为空,或者02本金01本息");
        if (dqqc <= 0) return BigDecimal.ZERO;
        if (dqqc > dkqs) return BigDecimal.ZERO;
        try {
            // 由于之前计算利息的方式, 对于某些期数存在0.01的误差, 导致本金也存在0.01的误差 , 所以统一使用还款计划来获取值
            LinkedList<HousingfundAccountPlanGetInformation> housingfundAccountPlanGetInformations = repaymentPlan(dkffe, dkqs, dkhkfs, dkll, null);
            if (housingfundAccountPlanGetInformations.size() <= 0)
                return BigDecimal.ZERO;
            HousingfundAccountPlanGetInformation housingfundAccountPlanGetInformation = housingfundAccountPlanGetInformations.get(dqqc - 1);
            return new BigDecimal(housingfundAccountPlanGetInformation.getHKBJJE());
        } catch (Exception e) {
            throw new ErrorException(e);
        }
    }

    /**
     * 当前时间获取期数: 正常还款时间计算
     */
    public final static CurrentPeriodRange theTimePeriod(Date dkffrq, int dkqs, Date dqsj) {
        try {
            dqsj = sim.parse(sim.format(dqsj));
            CurrentPeriodRange currentPeriodRange = new CurrentPeriodRange();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dkffrq);
            int sourceDay = calendar.get(Calendar.DAY_OF_MONTH);
            for (int i = 1; ; i++) {
                long beforeTime = calendar.getTime().getTime();
                calendar = LoanRepaymentAlgorithm.calNextAmountMonth(sourceDay, calendar, 1);
                if (beforeTime < dqsj.getTime() && dqsj.getTime() <= calendar.getTime().getTime()) {
                    currentPeriodRange.setBeforeTime(new Date(beforeTime));
                    currentPeriodRange.setCurrentPeriod(i);
                    currentPeriodRange.setAfterTime(new Date(calendar.getTime().getTime()));
                    break;
                }
            }
            return currentPeriodRange;
        } catch (Exception e) {
            throw new ErrorException(e);
        }
    }

    /**
     * 还款计划（本金、本息）
     * over
     */
    /**
     * 还款计划
     *
     * @param DKFFE  贷款发放额
     * @param DKQS   贷款期数
     * @param DKHKFS 贷款还款方式
     * @param DKLL   贷款年利率(% , 百分数, 数据库中查出来的值)
     * @param DKFFRQ 贷款发放日期( 可为null或者"")
     * @return 还款计划
     * @throws ParseException
     */
    public final static LinkedList<HousingfundAccountPlanGetInformation> repaymentPlan(BigDecimal DKFFE, int DKQS, String DKHKFS, BigDecimal DKLL, String DKFFRQ) throws ParseException {
        if (!StringUtil.notEmpty(DKHKFS) || (!DKHKFS.equals("02") && !DKHKFS.equals("01")))
            throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款还款方式为空,或者02本金01本息");

        Calendar calendar = null;
        int sourceDay = 1;

        if (StringUtil.notEmpty(DKFFRQ)) {
            calendar = Calendar.getInstance();
            calendar.setTime(sim.parse(DKFFRQ));
            sourceDay = calendar.get(Calendar.DAY_OF_MONTH);
        }

        LinkedList<HousingfundAccountPlanGetInformation> information = new LinkedList<>();
        HousingfundAccountPlanGetInformation housingfundAccountPlanGetInformation = null;
        BigDecimal dkye = DKFFE;
        if (dkye.compareTo(BigDecimal.ZERO) <= 0)
            return information;
        BigDecimal hkbjje = BigDecimal.ZERO;
        BigDecimal hklxje = BigDecimal.ZERO;
        BigDecimal fse = BigDecimal.ZERO;
        // 本息还款方式 , 发生额一定
        BigDecimal bxFse = calBxFse(DKFFE, DKQS, DKLL);
        // 本金还款方式 , 月还本金一定
        BigDecimal bjHkbjje = calBjHkbjje(DKFFE, DKQS);
        for (int i = 1; i <= DKQS; i++) {
            housingfundAccountPlanGetInformation = new HousingfundAccountPlanGetInformation();

            hklxje = LoanRepaymentAlgorithm.calLxByDkye(dkye, DKLL);

            // 最后一期 , 贷款余额为该期本金
            if (i == DKQS) {
                hkbjje = dkye;
                fse = hkbjje.add(hklxje);
            } else {
                // 等额本息还款
                if ("01".equals(DKHKFS)) {
                    fse = bxFse;
                    hkbjje = fse.subtract(hklxje);
                }
                // 等额本金还款 (由于前面有验证还款方式)
                else {
                    hkbjje = bjHkbjje;
                    fse = hkbjje.add(hklxje);
                }
            }

            if (calendar != null) {
                calendar = LoanRepaymentAlgorithm.calNextAmountMonth(sourceDay, calendar, 1);
                housingfundAccountPlanGetInformation.setHKRQ(sim.format(calendar.getTime()) + "");
            }
            housingfundAccountPlanGetInformation.setHKQC(String.valueOf(i));//还款期次
            housingfundAccountPlanGetInformation.setDKYE(dkye.toString());//贷款余额 以前代码中,表示期初贷款余额
            housingfundAccountPlanGetInformation.setQMDKYE(dkye.subtract(hkbjje).toString()); // 期末贷款余额
            housingfundAccountPlanGetInformation.setHKBJJE(hkbjje.toString());//本金金额
            housingfundAccountPlanGetInformation.setHKLXJE(hklxje.toString());//利息
            housingfundAccountPlanGetInformation.setFSE(fse.toString());//本期本息

            // 下一期期初贷款余额为该期期末贷款余额
            dkye = dkye.subtract(hkbjje);

            information.add(housingfundAccountPlanGetInformation);
        }
        return information;
    }

    public final static Date periodOfafterTime(Date dkffrq, int dqqc) {
        try {
            dkffrq = sim.parse(sim.format(dkffrq));
            Calendar cal = Calendar.getInstance();
            cal.setTime(dkffrq);
            cal.add(Calendar.MONTH, dqqc);
            Date calafter = cal.getTime();
            return sim.parse(sim.format(calafter));
        } catch (Exception e) {
            throw new ErrorException(e);
        }

    }

    public final static Date settlementDate(Date dkffrq, int dkqs) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dkffrq);
        cal.add(Calendar.MONTH, dkqs);
        return cal.getTime();
    }

    @Deprecated
    public final static CurrentBeforeAfterime burrentBeforeAfterTime(Date dkffrq, int dqqc) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dkffrq);
            calendar.add(Calendar.MONTH, dqqc);
            long dqsj = DateUtil.dateStringtoStringDate(calendar.getTime()).getTime();
            long dqsj7 = dqsj + 604800000;
            calendar.add(Calendar.MONTH, -1);
            long sqsj7 = DateUtil.dateStringtoStringDate(calendar.getTime()).getTime() + 604800000;
            calendar.add(Calendar.MONTH, 2);
            long xqsj = DateUtil.dateStringtoStringDate(calendar.getTime()).getTime();
            return new CurrentBeforeAfterime(dqsj, dqsj7, sqsj7, xqsj);
        } catch (Exception e) {
            throw new ErrorException(e);
        }
    }

    @Deprecated
    public final static void currentRepamentTime(Date dkffrq, Date ydkkrq, int dqqc) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dkffrq);
            calendar.add(Calendar.MONTH, dqqc);
            long day = calendar.getTimeInMillis();
            calendar.add(Calendar.DATE, 7);
            long dayafter7 = calendar.getTimeInMillis();
            calendar.setTime(ydkkrq);
            long days = calendar.getTimeInMillis();
            if (days >= day && days <= dayafter7) {
                throw new ErrorException("正常还款时区不能办理提前还款业务，请" + ((dayafter7 - days) / 86400000 + 1) + "日后来办理此业务");
            }
        } catch (Exception e) {
            throw new ErrorException(e);
        }
    }

    @Deprecated
    public final static SettleAccounts settleAccounts(BigDecimal dkye, BigDecimal bqbj, BigDecimal dkll, BigDecimal bqbx, int jxts) {
        BigDecimal tqhbje = dkye.subtract(bqbj);
        BigDecimal tqhklx = tqhbje.multiply(dkll.divide(new BigDecimal(1200), 10, BigDecimal.ROUND_HALF_UP))
                .multiply(new BigDecimal(jxts)).divide(new BigDecimal(30), 10, BigDecimal.ROUND_HALF_UP);
        BigDecimal tqjqdkze = bqbx.add(tqhbje).add(tqhklx);
        return new SettleAccounts() {{
            this.setTQHBJE(tqhbje);
            this.setTQHKLX(tqhklx);
            this.setTQJQHKZE(tqjqdkze);
        }};
    }

    @Deprecated
    public final static SettlePartialRepayments settlePartialRepayments(BigDecimal bchkje, BigDecimal bqbx, BigDecimal dkll, int jxts) {
        BigDecimal tqhkje = new BigDecimal(bchkje.doubleValue()).subtract(new BigDecimal(bqbx.doubleValue())).setScale(6, BigDecimal.ROUND_HALF_UP);
        BigDecimal tqhbje = new BigDecimal(tqhkje.doubleValue()).divide(new BigDecimal(1).add(new BigDecimal(dkll.doubleValue()).divide(new BigDecimal(1200), 10, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(jxts)).divide(new BigDecimal(30), 10, BigDecimal.ROUND_HALF_UP)), 10, BigDecimal.ROUND_HALF_UP).setScale(6, BigDecimal.ROUND_HALF_UP);
        BigDecimal tqhklx = tqhkje.subtract(tqhbje);
        return new SettlePartialRepayments() {{
            this.setTQHKJE(tqhkje);
            this.setTQHBJE(tqhbje);
            this.setTQHKLX(tqhklx);
        }};
    }

    public final static BigDecimal lendingRate(BigDecimal dkll, BigDecimal llfdbl) {
        if (llfdbl.compareTo(BigDecimal.ZERO) == 0) {
            return dkll;
        } else {
            return dkll.multiply(llfdbl);
        }
    }

    @Deprecated
    public final static boolean checkTime(Date ydkkrq) {
        String[] split = sim.format(ydkkrq).split("-");
        Calendar instance = Calendar.getInstance();
        int month = instance.get(Calendar.MONTH) + 1;
        int days = instance.get(Calendar.DATE);
        int currentMonthDay = getCurrentMonthDay(instance);

        int currentMonthDay1 = Integer.parseInt(split[1]);
        int months = Integer.parseInt(split[2]);
        if (month == months) {
            if (days == currentMonthDay1 || currentMonthDay1 >= currentMonthDay) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public final static int getCurrentMonthDay(Calendar a) {
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    @Deprecated
    public final static int sea() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sim.parse("2017-10-31"));
            calendar.add(Calendar.MONTH, 1);
            System.out.println(sim.format(calendar.getTime()));
        } catch (ParseException e) {

        }
        return 1;
    }

    /**
     * 还款计划（本金、本息）时间范围搜索
     */
    public final static LinkedList<HousingfundAccountPlanGetInformation> repaymentPlanTimeFilter(BigDecimal DKFFE, int DKQS, String DKHKFS, BigDecimal DKLL, String DKFFRQ, String HKRQS, String HKRQE) throws ParseException {
        LinkedList<HousingfundAccountPlanGetInformation> information = new LinkedList<>();
        if (StringUtil.isEmpty(DKFFRQ))
            throw new ErrorException(ReturnEnumeration.Parameter_MISS, "贷款发放日期");
        LinkedList<HousingfundAccountPlanGetInformation> repayments = repaymentPlan(DKFFE, DKQS, DKHKFS, DKLL, DKFFRQ);
        if (StringUtil.isEmpty(HKRQS) && StringUtil.isEmpty(HKRQE)) {
            return repayments;
        }
        for (HousingfundAccountPlanGetInformation item : repayments) {
            String HKRQ = item.getHKRQ().substring(0, 7);
            if (StringUtil.notEmpty(HKRQS) && StringUtil.isEmpty(HKRQE)) {
                if (HKRQ.compareTo(HKRQS) >= 0)
                    information.add(item);
            } else if (StringUtil.isEmpty(HKRQS) && StringUtil.notEmpty(HKRQE)) {
                if (HKRQ.compareTo(HKRQE) <= 0)
                    information.add(item);
            } else if (StringUtil.notEmpty(HKRQS) && StringUtil.notEmpty(HKRQE)) {
                if (HKRQ.compareTo(HKRQS) >= 0 && HKRQ.compareTo(HKRQE) <= 0)
                    information.add(item);
            }
        }
        return information;
    }
}
