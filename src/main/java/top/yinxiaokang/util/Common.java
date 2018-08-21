package top.yinxiaokang.util;

import top.yinxiaokang.original.loan.repayment.RepaymentItem;

import java.util.List;

/**
 * @author yinxk
 * @date 2018/8/21 10:05
 */
public class Common {
    /**
     * 根据期次获取对应还款计划的某一期
     *
     * @param list
     * @param dqqc
     * @return
     */
    public static RepaymentItem getRepaymentItemByDqqc(List<RepaymentItem> list, Integer dqqc) {
        for (RepaymentItem item : list) {
            if (dqqc == item.getHkqc()) {
                return item;
            }
        }
        return null;
    }
}
