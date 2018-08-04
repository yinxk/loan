package top.yinxiaokang.original.loan.repayment;

import top.yinxiaokang.original.LoanRepaymentAlgorithm;
import top.yinxiaokang.others.ErrorException;
import top.yinxiaokang.others.ReturnEnumeration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RepaymentPlan {
    /**
     * 还款计划
     *
     * @param dkffe           贷款发放额
     * @param dkffrq          贷款发放日期
     * @param hkqs            还款期数
     * @param dknlv           贷款年利率
     * @param repaymentMethod 还款方式
     * @param qsqs            起始期数
     * @return 生成还款计划
     */
    public static List<RepaymentItem> listRepaymentPlan(BigDecimal dkffe, Date dkffrq, int hkqs, BigDecimal dknlv, RepaymentMethod repaymentMethod, Integer qsqs, RepaymentMonthRateScale repaymentMonthRateScale) {
        List<RepaymentItem> result = new ArrayList<>();
        if (dkffe == null || dkffe.compareTo(BigDecimal.ZERO) <= 0)
            return result;
        if (dkffrq == null)
            throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款发放日期");
        if (qsqs == null)
            qsqs = 0;
        if (dknlv == null || dknlv.compareTo(BigDecimal.ZERO) <= 0)
            throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "贷款年利率");
        if (repaymentMethod != RepaymentMethod.BX && repaymentMethod != RepaymentMethod.BJ)
            throw new ErrorException(ReturnEnumeration.Parameter_NOT_MATCH, "还款方式");

        if (repaymentMonthRateScale == null)
            repaymentMonthRateScale = RepaymentMonthRateScale.YES;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dkffrq);
        int sourceDay = calendar.get(Calendar.DAY_OF_MONTH);
        BigDecimal dkye = dkffe;
        BigDecimal hkbjje = BigDecimal.ZERO;
        BigDecimal hklxje = BigDecimal.ZERO;
        BigDecimal fse = BigDecimal.ZERO;
        // 本息还款方式 , 发生额一定
        BigDecimal bxFse = calBxFse(dkffe, hkqs, dknlv, repaymentMonthRateScale);
        // 本金还款方式 , 月还本金一定
        BigDecimal bjHkbjje = calBjHkbjje(dkffe, hkqs);

        for (int i = 0; i < hkqs; i++) {
            RepaymentItem repaymentItem = new RepaymentItem();

            hklxje = LoanRepaymentAlgorithm.calLxByDkye(dkye, dknlv);
            // 最后一期 , 贷款余额为该期本金
            if (i == hkqs - 1) {
                hkbjje = dkye;
                fse = hkbjje.add(hklxje);
            } else {
                if (repaymentMethod == RepaymentMethod.BX) {
                    fse = bxFse;
                    hkbjje = fse.subtract(hklxje);
                } else {
                    hkbjje = bjHkbjje;
                    fse = hkbjje.add(hklxje);
                }
            }

            calendar = LoanRepaymentAlgorithm.calNextAmountMonth(sourceDay, calendar, 1);
            repaymentItem.setHkqc(qsqs + i + 1);
            repaymentItem.setHkrq(calendar.getTime());
            repaymentItem.setQcdkye(dkye);
            repaymentItem.setQmdkye(dkye.subtract(hkbjje));
            repaymentItem.setHkbjje(hkbjje);
            repaymentItem.setHklxje(hklxje);
            repaymentItem.setFse(fse);
            // 下一期期初贷款余额为该期期末贷款余额
            dkye = repaymentItem.getQmdkye();
            result.add(repaymentItem);
        }
        return result;
    }


    /**
     * 等额本息计算月发生额计算
     *
     * @param dkffe                   贷款发放额
     * @param hkqs                    还款期数
     * @param dknlv                   贷款年利率
     * @param repaymentMonthRateScale 转换月利率是否处理小数位数
     * @return 月发生额
     */
    public static BigDecimal calBxFse(BigDecimal dkffe, int hkqs, BigDecimal dknlv, RepaymentMonthRateScale repaymentMonthRateScale) {
        BigDecimal fse = BigDecimal.ZERO;
        BigDecimal monthRate = LoanRepaymentAlgorithm.convertYearRateToMonthRate(dknlv, repaymentMonthRateScale);
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
    public static BigDecimal calBjHkbjje(BigDecimal dkffe, int hkqs) {
        BigDecimal bjje = BigDecimal.ZERO;
        bjje = dkffe.divide(new BigDecimal(hkqs), 2, BigDecimal.ROUND_HALF_UP);
        return bjje;
    }

}
