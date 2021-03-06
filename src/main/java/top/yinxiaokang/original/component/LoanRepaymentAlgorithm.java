package top.yinxiaokang.original.component;

/**
 * @author yinxk
 * @date 2018/7/6 14:13
 */

import top.yinxiaokang.original.loan.repayment.RepaymentMonthRateScale;
import top.yinxiaokang.others.CurrentPeriodRange;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static top.yinxiaokang.util.Utils.SDF_YEAR_MONTH_DAY;

/**
 * @author yinxk
 * @date 2018/5/24 9:22
 * 贷款还款算法
 */
public class LoanRepaymentAlgorithm {
    /**
     * 一年以360天计算
     */
    public final static int YEAR_DAYS = 360;
    public final static int YEAR_MONTHS = 12;
    public final static int MONTH_DAYS = 30;


    /**
     * 小数舍入位数
     */
    public final static int SCALE_TWO = 2;
    public final static int SCALE_EIGHT = 8;
    public final static int SCALE_TEN = 10;
    public final static int SCALE_TWELVE = 12;

    /**
     * 计算提前还款本金
     *
     * @param tqhkje 提前还款金额
     * @param lx     利息
     * @return 提前还款本金
     */
    public static BigDecimal calEarlyRepaymentOfPrincipal(BigDecimal tqhkje, BigDecimal lx) {
        return tqhkje.subtract(lx).setScale(SCALE_TWO, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算提前部分还款或者结清的计息天数 <br />
     * 计息天数 = 实际还款日 - 还款日(正常扣款日) <br />
     * 计息日为实际还款日前一日<br />
     * 每个月按30天计算
     *
     * @param dkffrq 贷款发放日期  对应 正常扣款日
     * @param ydkkrq 约定扣款日期  对应 实际还款日
     * @return 计息天数 int
     */
    public final static int calInterestDays(Date dkffrq, Date ydkkrq) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dkffrq);
        // 每月还款日
        int normalDay = calendar.get(Calendar.DATE) > MONTH_DAYS ? MONTH_DAYS : calendar.get(Calendar.DATE);
        // 约定扣款日期即实际还款日
        calendar.setTime(ydkkrq);
        int actualDay = calendar.get(Calendar.DATE) > MONTH_DAYS ? MONTH_DAYS : calendar.get(Calendar.DATE);
        // 相差天数
        int subDays = 0;
        if (actualDay >= normalDay) {
            subDays = actualDay - normalDay;
        } else {
            subDays = MONTH_DAYS - normalDay + actualDay;
        }
        return subDays;

    }


    /**
     * 根据计息天数按天计算利息<br/>
     * 文档标准:一年以360天根据年利率计算 天利率<br />
     *
     * @param dkye 贷款余额
     * @param dkll 贷款年利率(根据之前的代码来看,传入的都是年利率,这里还是以年利率(百分数)作为参数,没有作小数处理的)
     * @param jxts 计息天数
     * @return 利息
     */
    public final static BigDecimal calInterestByInterestDays(BigDecimal dkye, BigDecimal dkll, int jxts) {
        BigDecimal dayRate = convertYearRateToDayRate(dkll, RepaymentMonthRateScale.NO);
        BigDecimal interest = dkye.multiply(dayRate)
                .multiply(new BigDecimal(jxts));
        return interest.setScale(SCALE_TWO, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 转换年利率为日利率
     *
     * @param yearRate 没有经过小数处理的年利率(百分数)
     * @return 日利率  根据文档需要精确到%表示的小数点后6位, 也就是小数表示的8位(根据拿到的资料是这么计算的) <br />
     * 网上资料显示精确到小数点后12位
     */
    public static BigDecimal convertYearRateToDayRate(BigDecimal yearRate, RepaymentMonthRateScale repaymentMonthRateScale) {
        BigDecimal dayRate = convertYearRateToMonthRate(yearRate, RepaymentMonthRateScale.NO).divide(new BigDecimal(MONTH_DAYS), SCALE_TEN, BigDecimal.ROUND_HALF_UP);
        if (repaymentMonthRateScale == RepaymentMonthRateScale.YES)
            dayRate = dayRate.setScale(SCALE_EIGHT, BigDecimal.ROUND_HALF_UP);
        return dayRate;
    }

    public static BigDecimal convertYearRateToDayRate(BigDecimal yearRate) {
        BigDecimal dayRate = convertYearRateToDayRate(yearRate, RepaymentMonthRateScale.NO);
        return dayRate;
    }

    /**
     * 转换年利率为月利率
     *
     * @param yearRate                没有经过小数处理的年利率(百分数)
     * @param repaymentMonthRateScale 转换月利率是否进行小数处理
     * @return 不理小数位数(最多12位)或者根据文档需要精确到%表示的小数点后6位, 也就是小数表示的8位(根据拿到的资料是这么计算的) <br />
     * 网上资料显示精确到小数点后12位
     */
    public static BigDecimal convertYearRateToMonthRate(BigDecimal yearRate, RepaymentMonthRateScale repaymentMonthRateScale) {
        BigDecimal monthRate = yearRate.divide(new BigDecimal(100), SCALE_TEN, BigDecimal.ROUND_HALF_UP)
                .divide(new BigDecimal(YEAR_MONTHS), SCALE_TEN, BigDecimal.ROUND_HALF_UP);
        if (repaymentMonthRateScale == RepaymentMonthRateScale.YES)
            monthRate = monthRate.setScale(SCALE_EIGHT, BigDecimal.ROUND_HALF_UP);
        return monthRate;
    }

    public static BigDecimal convertYearRateToMonthRate(BigDecimal yearRate) {
        return convertYearRateToMonthRate(yearRate, RepaymentMonthRateScale.NO);
    }

    /**
     * 只支持正向计算,即end>=start. 否则,结果为0
     * <p>计算开始日期和结束日期之间的天数,左闭右开区间,[2018-5-17,2018-5-18)</p>
     * <p>比如: 2018-5-17 和 2018-5-18 之间的计算结果为 1 </p>
     * <p>2018-5-17----> 2018-5-19  =  2 </p>
     * <p>按照每个月30天的计算,一年360天的计算方式</p>
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return 天数
     */
    public static int betweenTwoDateDays(Date start, Date end) {
        Calendar startC = Calendar.getInstance();
        startC.setTime(start);
        Calendar endC = Calendar.getInstance();
        endC.setTime(end);
        int startMonth = startC.get(Calendar.MONTH);
        int endMonth = endC.get(Calendar.MONTH) - 1;
        int startYear = startC.get(Calendar.YEAR);
        int endYear = endC.get(Calendar.YEAR);
        int startDay = startC.get(Calendar.DATE);
        startDay = startDay > MONTH_DAYS ? MONTH_DAYS : startDay;
        int endDay = endC.get(Calendar.DATE);
        endDay = endDay > MONTH_DAYS ? MONTH_DAYS : endDay;
        int subYears = endYear - startYear;
        int subMonths = endMonth - startMonth;
        // 比如日期是2017-1-20  -------->  2017-5-10
        // 相当于计算2017-1-20-----> 2017-4-20---->2017-5-10的天数
        // 相差   3*30 + (30-20)+10
        int subDays = MONTH_DAYS - startDay + endDay;
        int betweenDays = subYears * YEAR_MONTHS * MONTH_DAYS + subMonths * MONTH_DAYS + subDays;
        if (betweenDays < 0) {
            betweenDays = 0;
        }
        return betweenDays;
    }


    /**
     * <p>计算某一期的罚息, 逾期本金 ,逾期利息,逾期天数 是一个期次中的数据,不能一个是这个期次,一个是那个期次的.</p>
     *
     * @param yqbj   逾期本金
     * @param yqlx   逾期利息(等额本金，等额本息)
     * @param dknlv  贷款年利率(这个东西直接是:比如:3.25,数据库中存储的值以及之前代码都是使用该值进行传递)
     * @param ywfsrq 逾期业务对应的正常扣款的业务发生日期
     * @param ydkkrq 逾期业务实际的扣款时间
     * @return 本期的逾期罚息
     */
    public final static BigDecimal calOverdueFx(BigDecimal yqbj, BigDecimal yqlx, BigDecimal dknlv, Date ywfsrq, Date ydkkrq) {
        return calOverdueFx(yqbj, yqlx, dknlv, calYqts(ywfsrq, ydkkrq));
    }

    /**
     * <p>计算某一期的罚息, 逾期本金 ,逾期利息,逾期天数 是一个期次中的数据,不能一个是这个期次,一个是那个期次的.</p>
     * <p>逾期罚息：逾期本息×罚息日利率（贷款日利率*1.5）× 逾期天数(通过calculateYqts计算得到 )</p>
     *
     * @param yqbj  逾期本金
     * @param yqlx  逾期利息(等额本金，等额本息)
     * @param dknlv 贷款年利率(这个东西直接是:比如:3.25,数据库中存储的值以及之前代码都是使用该值进行传递)
     * @param yqts  逾期天数
     * @return 本期的逾期罚息
     */
    public final static BigDecimal calOverdueFx(BigDecimal yqbj, BigDecimal yqlx, BigDecimal dknlv, int yqts) {
        // 其中1.5是逾期罚息的倍率 , 根据以前代码来的
        return yqbj.add(yqlx)
                .multiply(
                        convertYearRateToDayRate(dknlv, RepaymentMonthRateScale.NO).multiply(new BigDecimal(1.5))
                )
                .multiply(new BigDecimal(yqts))
                .setScale(SCALE_TWO, BigDecimal.ROUND_HALF_UP);

    }


    /**
     * 计算逾期天数(每一次的暂停计息都会对暂停计息前的时刻的账户状态做一次统计) <br />
     * 在暂停计息结束的时候,第一期需要加入前面的利息和罚息<br />
     *
     * @param ywfsrq 业务发生日期,这个日期可以根据对应期次的正常还款的业务发生日期
     * @param ydkkrq 约定扣款日期
     * @return 逾期天数(也就是逾期中需要计算罚息的天数)
     */
    public static int calYqts(Date ywfsrq, Date ydkkrq) {
        int result = 0;
        result = betweenTwoDateDays(ywfsrq, ydkkrq);
        return result;
    }


    /**
     * 通过时间秒毫秒数计算两个时间的间隔天数
     *
     * @param start
     * @param end
     * @return
     */
    public static int differentDaysByMillisecond(Date start, Date end) {
        int days = (int) ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
        return days;
    }


    /**
     * 根据贷款发放日期和当前时间,计算当前时间属于的还款期间
     *
     * @param dkffrq
     * @param dqsj
     * @return
     * @throws ParseException
     */
    public static CurrentPeriodRange calCurrentPeriodRange(Date dkffrq, Date dqsj) throws ParseException {
        dqsj = SDF_YEAR_MONTH_DAY.parse(SDF_YEAR_MONTH_DAY.format(dqsj));
        dkffrq = SDF_YEAR_MONTH_DAY.parse(SDF_YEAR_MONTH_DAY.format(dkffrq));
        CurrentPeriodRange currentPeriodRange = new CurrentPeriodRange();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dkffrq);
        int sourceDay = calendar.get(Calendar.DAY_OF_MONTH);
        for (int i = 1; ; i++) {
            long beforeTime = calendar.getTime().getTime();
            calendar = calNextAmountMonth(sourceDay, calendar, 1);
            if (beforeTime < dqsj.getTime() && dqsj.getTime() <= calendar.getTime().getTime()) {
                currentPeriodRange.setBeforeTime(new Date(beforeTime));
                currentPeriodRange.setCurrentPeriod(i);
                currentPeriodRange.setAfterTime(new Date(calendar.getTime().getTime()));
                break;
            }
        }
        return currentPeriodRange;
    }

    /**
     * 根据贷款发放日期和当前时间,计算核算时间属于的还款期间
     *
     * @param dkffrq
     * @param hssj
     * @return
     * @throws ParseException
     */
    public static CurrentPeriodRange calHSRange(Date dkffrq, Date hssj, Date soutStartDate) throws ParseException {
        hssj = SDF_YEAR_MONTH_DAY.parse(SDF_YEAR_MONTH_DAY.format(hssj));
        dkffrq = SDF_YEAR_MONTH_DAY.parse(SDF_YEAR_MONTH_DAY.format(dkffrq));
        CurrentPeriodRange currentPeriodRange = new CurrentPeriodRange();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dkffrq);
        Calendar calendar1 = Calendar.getInstance();
        int sourceDay = calendar.get(Calendar.DAY_OF_MONTH);
        for (int i = 1; ; i++) {
            Date beforeTime = calendar.getTime();
            calendar = calNextAmountMonth(sourceDay, calendar, 1);

            calendar1.setTime(calendar.getTime());
            calendar1 = calNextAmountMonth(sourceDay, calendar1, 1);
            currentPeriodRange.setBeforeTime(beforeTime);
            currentPeriodRange.setCurrentPeriod(i);
            currentPeriodRange.setAfterTime(new Date(calendar.getTime().getTime()));
            if (calendar.getTime().getTime() > soutStartDate.getTime()) {
                System.out.println(currentPeriodRange);
            }
            if (calendar.getTime().getTime() <= hssj.getTime() && hssj.getTime() < calendar1.getTime().getTime()) {
                break;
            }
        }

        return currentPeriodRange;
    }

    public static List<CurrentPeriodRange> listHSRange(Date dkffrq, Date hssj, Date soutStartDate) throws ParseException {
        List<CurrentPeriodRange> result = new ArrayList<>();
        if (dkffrq == null) return result;
        hssj = SDF_YEAR_MONTH_DAY.parse(SDF_YEAR_MONTH_DAY.format(hssj));
        dkffrq = SDF_YEAR_MONTH_DAY.parse(SDF_YEAR_MONTH_DAY.format(dkffrq));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dkffrq);
        Calendar calendar1 = Calendar.getInstance();
        int sourceDay = calendar.get(Calendar.DAY_OF_MONTH);
        for (int i = 1; ; i++) {
            CurrentPeriodRange currentPeriodRange = new CurrentPeriodRange();
            Date beforeTime = calendar.getTime();
            calendar = calNextAmountMonth(sourceDay, calendar, 1);

            calendar1.setTime(calendar.getTime());
            calendar1 = calNextAmountMonth(sourceDay, calendar1, 1);
            currentPeriodRange.setBeforeTime(beforeTime);
            currentPeriodRange.setCurrentPeriod(i);
            currentPeriodRange.setAfterTime(new Date(calendar.getTime().getTime()));
            if (calendar.getTime().getTime() > soutStartDate.getTime()) {
                result.add(currentPeriodRange);
            }
            if (calendar.getTime().getTime() <= hssj.getTime() && hssj.getTime() < calendar1.getTime().getTime()) {
                break;
            }
        }

        return result;
    }


    /**
     * 根据对应的正常还款的业务发生日期(如果没有对应的业务发生日期(数据异常),则使用以前的计算罚息的方式进行计算) <br/>
     * 为了方便在以前调用罚息的地方修改为新的计算罚息的方式,参数类型和名称都保持一致
     *
     * @param yqbj   逾期本金
     * @param yqlx   逾期利息
     * @param dknlv  贷款年利率(没有经过小数处理)
     * @param dkffrq 贷款发放日期
     * @param yqqc   逾期期次
     * @param ydkkrq 约定扣款日期
     * @param ywfsrq 对应的逾期期次的正常还款记录的业务发生日期
     *               (有些逾期记录查询不到对应的正常还款记录(数据问题),就使用老实的罚息计算方法,(这种方法对于没有暂停计息的计算是准确的,对于存在暂停计息并且恢复计息之后, 逾期的记录的罚息的计算不对))
     * @return
     */
    public static BigDecimal calOverdueFxByYwfsrqOrOddAlgorithm(BigDecimal yqbj, BigDecimal yqlx, BigDecimal dknlv, Date dkffrq, int yqqc, Date ydkkrq, Date ywfsrq) {
        BigDecimal result = BigDecimal.ZERO;
        if (ywfsrq == null) {
            result = CommLoanAlgorithm.overdueFX(yqbj, yqlx, dknlv, dkffrq, yqqc, ydkkrq);
        } else {
            result = LoanRepaymentAlgorithm.calOverdueFx(yqbj, yqlx, dknlv, ywfsrq, ydkkrq);
        }
        return result;
    }

    /**
     * 针对于还款计划的每月还款日的计算<br/>
     * 比如: 传入日期 2017-12-30 , 以月份步进1<br/>
     * 则得到的日期会是这样的: <br/>
     * <p>
     * 2018-01-30<br/>
     * 2018-02-28<br/>
     * 2018-03-28<br/>
     * 2018-04-28<br/>
     * 2018-05-28<br/>
     * 2018-06-28<br/>
     * 2018-07-28<br/>
     * 2018-08-28<br/>
     * 2018-09-28<br/>
     * 2018-10-28<br/>
     * 还款日就存在小问题 , 这种情况只会在传入日期 大于28日才会出现<br/>
     * 针对这种情况 <br/>
     * 通过该方法 , 可以得到:<br/>
     * 2018-01-30<br/>
     * 2018-02-28<br/>
     * 2018-03-30<br/>
     * 2018-04-30<br/>
     * 2018-05-30<br/>
     * 2018-06-30<br/>
     * 2018-07-30<br/>
     * 2018-08-30<br/>
     * 2018-09-30<br/>
     * 2018-10-30<br/>
     * 2018-11-30<br/>
     * 2018-12-30<br/>
     * 2019-01-30<br/>
     * 2019-02-28<br/>
     * 2019-03-30<br/>
     * 2019-04-30<br/>
     *
     * @param sourceDay 固定日
     * @param instance  传入的Calendar实例
     * @param amount    步进值
     * @return 修改后实例
     */
    public static Calendar calNextAmountMonth(int sourceDay, Calendar instance, Integer amount) {
        int maxMonthDay = 0;
        int nowDay = 0;
        if (amount == null) {
            amount = 1;
        }
        instance.add(Calendar.MONTH, amount.intValue());
        nowDay = instance.get(Calendar.DAY_OF_MONTH);
        maxMonthDay = instance.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (sourceDay > nowDay) {
            if (maxMonthDay >= sourceDay) {
                instance.add(Calendar.DAY_OF_MONTH, sourceDay - nowDay);
            }
        }
        return instance;
    }

    /**
     * 根据贷款余额和贷款利率计算利息
     *
     * @param dkye 贷款余额
     * @param dkll 贷款利率
     * @return 利息
     */
    public static BigDecimal calLxByDkye(BigDecimal dkye, BigDecimal dkll) {
        BigDecimal lx = dkye.multiply(convertYearRateToMonthRate(dkll, RepaymentMonthRateScale.NO));
        return lx.setScale(SCALE_TWO, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 根据贷款余额和贷款利率计算利息
     *
     * @param dkye                    贷款余额
     * @param dkll                    贷款利率
     * @param repaymentMonthRateScale 是否处理小数
     * @return 利息
     */
    public static BigDecimal calLxByDkye(BigDecimal dkye, BigDecimal dkll, RepaymentMonthRateScale repaymentMonthRateScale) {
        BigDecimal lx = dkye.multiply(convertYearRateToMonthRate(dkll, repaymentMonthRateScale));
        return lx.setScale(SCALE_TWO, BigDecimal.ROUND_HALF_UP);
    }
}
