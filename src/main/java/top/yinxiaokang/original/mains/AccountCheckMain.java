package top.yinxiaokang.original.mains;

import org.junit.Test;
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
        AccountCheck a = new AccountCheck();
        String dkzh = "";
        List<CurrentPeriodRange> ranges = a.listHSRange("2406071098002745823");
        for (CurrentPeriodRange c : ranges) {
            System.out.println(c);
        }
    }
}
