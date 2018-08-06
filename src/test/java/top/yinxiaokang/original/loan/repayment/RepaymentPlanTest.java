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
        //for (RepaymentItem item  : repaymentItems1) {
        //    if (oddBx != null)
        //        System.out.printf("本金差额: %s  ",item.getHkbjje().subtract(oddBx.getHkbjje()));
        //    //System.out.println(item);
        //    oddBx = item;
        //}
        List<RepaymentItem> repaymentItems2 = RepaymentPlan.listRepaymentPlan(dkffe, dkffrq, hkqs, dknlv, RepaymentMethod.BX, null, RepaymentMonthRateScale.NO);
        BigDecimal sumlx1 = BigDecimal.ZERO;
        BigDecimal sumlx2 = BigDecimal.ZERO;

        for ( int i = 0 ; i < repaymentItems1.size() ; i++) {
            RepaymentItem repaymentItem1 = repaymentItems1.get(i);
            RepaymentItem repaymentItem2 = repaymentItems2.get(i);
            sumlx1 = sumlx1.add(repaymentItem1.getHklxje());
            sumlx2 = sumlx2.add(repaymentItem2.getHklxje());
            System.out.printf("%s\t%s\t 本金差额: %s \t 利息差额: %s \t 发生额差额: %s \t 期初贷款余额差额: %s \t 期末贷款余额差额: %s \n",repaymentItem1,repaymentItem2,
                    repaymentItem1.getHkbjje().subtract(repaymentItem2.getHkbjje()),repaymentItem1.getHklxje().subtract(repaymentItem2.getHklxje()),
                    repaymentItem1.getFse().subtract(repaymentItem2.getFse()),repaymentItem1.getQcdkye().subtract(repaymentItem2.getQcdkye()),repaymentItem1.getQmdkye().subtract(repaymentItem2.getQmdkye()));
            //System.out.print( repaymentItem1 + "\t" + repaymentItem2 + "\t 本金差额 : "+repaymentItem1.getHkbjje().subtract(repaymentItem2.getHkbjje()) + "\n");
        }
        System.out.printf("利息1总额: %s , 利息2总额: %s , 利息总额差额: %s",sumlx1,sumlx2,sumlx1.subtract(sumlx2));
        System.out.println("=========================");
        // 经过上面的比对,计算的月利息 , 月利率保留10位和保留12位计算的结果一致
        // 保留8位和保留12位有些期数会相差0.01
        // 那么保留8位和保留10也是有些期数会相差0.01

        //repaymentItems1 = repaymentItems2;

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
        int i= 0 ;
        sumlx1 = BigDecimal.ZERO;
        sumlx2 = BigDecimal.ZERO;
        for (HousingfundAccountPlanGetInformation item : housingfundAccountPlanGetInformations) {
            RepaymentItem repaymentItem1 = repaymentItems1.get(i++);
            sumlx1 = sumlx1.add(repaymentItem1.getHklxje());
            sumlx2 = sumlx2.add(new BigDecimal(item.getHKLXJE()));
            System.out.printf("%s\t%s\t 本金差额: %s \t 利息差额: %s \t 发生额差额: %s \t 期初贷款余额差额: %s \t 期末贷款余额差额: %s \n",repaymentItem1,item,
                    repaymentItem1.getHkbjje().subtract(new BigDecimal(item.getHKBJJE())),repaymentItem1.getHklxje().subtract(new BigDecimal(item.getHKLXJE())),
                    repaymentItem1.getFse().subtract(new BigDecimal(item.getFSE())),repaymentItem1.getQcdkye().subtract(new BigDecimal(item.getDKYE())),
                    repaymentItem1.getQmdkye().subtract(new BigDecimal(item.getQMDKYE())));
        }
        System.out.printf("利息1总额: %s , 利息2总额: %s , 利息总额差额: %s",sumlx1,sumlx2,sumlx1.subtract(sumlx2));
        System.out.println("=========================");
    }

}