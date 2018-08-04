package top.yinxiaokang.original.loan.repayment;

import org.junit.Test;
import top.yinxiaokang.original.Utils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
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
        //for (RepaymentItem item  : repaymentItems1) {
        //    if (oddBx != null)
        //        System.out.printf("本金差额: %s  ",item.getHkbjje().subtract(oddBx.getHkbjje()));
        //    //System.out.println(item);
        //    oddBx = item;
        //}
        List<RepaymentItem> repaymentItems2 = RepaymentPlan.listRepaymentPlan(dkffe, dkffrq, hkqs, dknlv, RepaymentMethod.BX, null, RepaymentMonthRateScale.NO);


        for ( int i = 0 ; i < repaymentItems1.size() ; i++) {
            RepaymentItem repaymentItem1 = repaymentItems1.get(i);
            RepaymentItem repaymentItem2 = repaymentItems2.get(i);
            System.out.printf("%s\t%s\t 本金差额: %s \t 利息差额: %s \t 发生额差额: %s \t 期初贷款余额差额: %s \t 期末贷款余额差额: %s \n",repaymentItem1,repaymentItem2,
                    repaymentItem1.getHkbjje().subtract(repaymentItem2.getHkbjje()),repaymentItem1.getHklxje().subtract(repaymentItem2.getHklxje()),
                    repaymentItem1.getFse().subtract(repaymentItem2.getFse()),repaymentItem1.getQcdkye().subtract(repaymentItem2.getQcdkye()),repaymentItem1.getQmdkye().subtract(repaymentItem2.getQmdkye()));
            //System.out.print( repaymentItem1 + "\t" + repaymentItem2 + "\t 本金差额 : "+repaymentItem1.getHkbjje().subtract(repaymentItem2.getHkbjje()) + "\n");
        }
        // 经过上面的比对,计算的月利息 , 月利率保留10位和保留12位计算的结果一致
        // 保留8位和保留12位有些期数会相差0.01
        // 那么保留8位和保留10也是有些期数会相差0.01
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

        //LinkedList<HousingfundAccountPlanGetInformation> housingfundAccountPlanGetInformations = CommLoanAlgorithm.repaymentPlan(dkffe, hkqs, RepaymentMethod.BX.getCode(), dknlv, Utils.SDF_YEAR_MONTH_DAY.format(dkffrq));
        //HousingfundAccountPlanGetInformation oddBx2 = null;
        //for (HousingfundAccountPlanGetInformation item : housingfundAccountPlanGetInformations) {
        //    if (oddBx2 != null)
        //        System.out.printf("本金差额: %s  ",new BigDecimal(item.getHKBJJE()).subtract(new BigDecimal(oddBx2.getHKBJJE())));
        //    System.out.println(item);
        //    oddBx2 = item;
        //}


        System.out.println("=========================");
    }

}