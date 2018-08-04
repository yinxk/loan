package top.yinxiaokang.original;

import org.junit.Assert;
import org.junit.Test;
import top.yinxiaokang.others.CurrentPeriodRange;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yinxk
 * @date 2018/7/24 15:00
 */
public class LoanRepaymentAlgorithmTest {
    public static final BigDecimal DKLL = new BigDecimal("3.25");
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    public void calLxByDkye() throws Exception {
        BigDecimal dkye = new BigDecimal("237853.15");
        BigDecimal bigDecimal = LoanRepaymentAlgorithm.calLxByDkye(dkye, DKLL);
        System.out.println(bigDecimal);
    }

    @Test
    public void calHSRange() throws Exception {

        Date dkffrq = SIMPLE_DATE_FORMAT.parse("2017-06-10");
        Date hssj = SIMPLE_DATE_FORMAT.parse("2017-12-31");
        Date soutStartDate = Utils.SDF_YEAR_MONTH_DAY.parse("2017-12-01");
        CurrentPeriodRange currentPeriodRange = LoanRepaymentAlgorithm.calHSRange(dkffrq, hssj, soutStartDate);
        Assert.assertEquals(6, currentPeriodRange.getCurrentPeriod());
        System.out.println(currentPeriodRange);

        dkffrq = SIMPLE_DATE_FORMAT.parse("2015-03-20");
        currentPeriodRange = LoanRepaymentAlgorithm.calHSRange(dkffrq, hssj, soutStartDate);
        Assert.assertEquals(33, currentPeriodRange.getCurrentPeriod());
        System.out.println(currentPeriodRange);
    }

    @Test
    public void betweenTwoDateDays() throws Exception {
        Date start = SIMPLE_DATE_FORMAT.parse("2017-02-27");
        Date end = SIMPLE_DATE_FORMAT.parse("2017-3-10");
        int days = LoanRepaymentAlgorithm.betweenTwoDateDays(start, end);
        System.out.printf("日期 %s 到 %s 之间的按每月30天计算的天数: %s \n", Utils.SDF_YEAR_MONTH_DAY.format(start), Utils.SDF_YEAR_MONTH_DAY.format(end), days);
        days = LoanRepaymentAlgorithm.differentDaysByMillisecond(start, end);
        System.out.printf("日期 %s 到 %s 之间的按实际天数计算的天数: %s \n", Utils.SDF_YEAR_MONTH_DAY.format(start), Utils.SDF_YEAR_MONTH_DAY.format(end), days);
    }
}