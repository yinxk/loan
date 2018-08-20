package top.yinxiaokang.original.mains;

import org.junit.Test;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.loan.repayment.RepaymentItem;
import top.yinxiaokang.original.loan.repayment.RepaymentMethod;
import top.yinxiaokang.original.loan.repayment.RepaymentMonthRateScale;
import top.yinxiaokang.original.loan.repayment.RepaymentPlan;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.others.CurrentPeriodRange;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author yinxk
 * @date 2018/8/6 11:45
 */
public class AccountCheckMain {
    @Test
    public void Test1() {
        AccountCheck accountCheck = new AccountCheck();

        String dkzh = "2406071098002745823";
        BigDecimal initDkye = new BigDecimal("234381.53");
        BigDecimal initOverdueBjje = new BigDecimal("");

        SthousingAccount account = accountCheck.getSthousingAccount(dkzh);
        List<CurrentPeriodRange> ranges = accountCheck.listHSRange(account, null);
        BigDecimal syqs = accountCheck.syqs(ranges, account);

        BigDecimal dkxffe = initDkye.subtract(initOverdueBjje);
        CurrentPeriodRange currentPeriodRange = null;
        if (!ranges.isEmpty()) {
            currentPeriodRange = ranges.get(0);
        }
        Date dkxffrq = currentPeriodRange == null ? null : currentPeriodRange.getBeforeTime();

        // 初始还款计划,如果后面发生提前还款 , 那么还款计划会发生改变
        List<RepaymentItem> repaymentItems = RepaymentPlan.listRepaymentPlan(dkxffe, dkxffrq, syqs.intValue(), account.getDkll(),
                RepaymentMethod.getRepaymentMethodByCode(account.getDkhkfs()), account.getDkqs().subtract(syqs).intValue(), RepaymentMonthRateScale.YES);

        for (RepaymentItem item : repaymentItems) {
            System.out.println(item);
        }

        List<SthousingDetail> sthousingDetails = accountCheck.listDetails(account);

        for (SthousingDetail detail : sthousingDetails) {
            System.out.println(detail+"\n\n\n\n\n");
        }
    }
}
