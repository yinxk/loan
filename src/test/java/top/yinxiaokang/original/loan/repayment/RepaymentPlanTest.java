package top.yinxiaokang.original.loan.repayment;

import org.junit.Test;
import top.yinxiaokang.original.CommLoanAlgorithm;
import top.yinxiaokang.original.Utils;
import top.yinxiaokang.others.HousingfundAccountPlanGetInformation;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RepaymentPlanTest {

    private static final BigDecimal dknlv = new BigDecimal("3.25");

    @Test
    public void repaymentPlan() throws ParseException {
        BigDecimal dkffe = new BigDecimal("330000");
        Date dkffrq = Utils.SDF_YEAR_MONTH_DAY.parse("2017-10-31");
        int hkqs = 12 * 10;
        System.out.println("=========================");
        List<RepaymentItem> repaymentItems1 = RepaymentPlan.listRepaymentPlan(dkffe, dkffrq, hkqs, dknlv, RepaymentMethod.BX, 0, RepaymentMonthRateScale.YES);
        RepaymentItem oddBx = null;
        for (RepaymentItem item  : repaymentItems1) {
            if (oddBx != null)
                System.out.printf("本金差额: %s  ",item.getHkbjje().subtract(oddBx.getHkbjje()));
            System.out.println(item);
            oddBx = item;
        }
        //List<RepaymentItem> repaymentItems2 = RepaymentPlan.listRepaymentPlan(dkffe, dkffrq, hkqs, dknlv, RepaymentMethod.BX, null, RepaymentMonthRateScale.NO);
        //for (RepaymentItem item  : repaymentItems2) {
        //    //System.out.println(item);
        //}
        //List<RepaymentItem> repaymentItems3 = RepaymentPlan.listRepaymentPlan(dkffe, dkffrq, hkqs, dknlv, RepaymentMethod.BJ, null, RepaymentMonthRateScale.YES);
        //RepaymentItem oddBj = null;
        //for (RepaymentItem item  : repaymentItems3) {
        //    if (oddBj != null)
        //        System.out.printf("月还款额差额: %s   " ,oddBj.getFse().subtract(item.getFse()));
        //    System.out.println(item);
        //    oddBj = item;
        //}
        //List<RepaymentItem> repaymentItems4 = RepaymentPlan.listRepaymentPlan(dkffe, dkffrq, hkqs, dknlv, RepaymentMethod.BJ, null, RepaymentMonthRateScale.NO);
        //for (RepaymentItem item  : repaymentItems4) {
        //    System.out.println(item);
        //}

        LinkedList<HousingfundAccountPlanGetInformation> housingfundAccountPlanGetInformations = CommLoanAlgorithm.repaymentPlan(dkffe, hkqs, RepaymentMethod.BX.getCode(), dknlv, Utils.SDF_YEAR_MONTH_DAY.format(dkffrq));
        HousingfundAccountPlanGetInformation oddBx2 = null;
        for (HousingfundAccountPlanGetInformation item : housingfundAccountPlanGetInformations) {
            if (oddBx2 != null)
                System.out.printf("本金差额: %s  ",new BigDecimal(item.getHKBJJE()).subtract(new BigDecimal(oddBx2.getHKBJJE())));
            System.out.println(item);
            oddBx2 = item;
        }


        System.out.println("=========================");
    }

}