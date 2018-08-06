package top.yinxiaokang.original.mains;

import org.junit.Test;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.others.CurrentPeriodRange;

import java.util.List;

/**
 * @author yinxk
 * @date 2018/8/6 11:45
 */
public class AccountCheckMain {
    @Test
    public void Test1(){
        AccountCheck accountCheck = new AccountCheck();
        String dkzh = "2406071098002745823";
        SthousingAccount sthousingAccount = accountCheck.getSthousingAccount(dkzh);
        List<CurrentPeriodRange> ranges = accountCheck.listHSRange(sthousingAccount);
        for (CurrentPeriodRange c : ranges) {
            System.out.println(c);
        }
        List<SthousingDetail> sthousingDetails = accountCheck.listDetails(sthousingAccount);
        for (SthousingDetail detail : sthousingDetails) {
            System.out.println(detail);
        }
    }
}
