package top.yinxiaokang.original.service;

import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.loan.repayment.RepaymentItem;

/**
 * 检测器
 * @author yinxk
 * @date 2018/8/6 10:40
 */
public interface Detector {
    boolean check(SthousingDetail detail, RepaymentItem repaymentItem);
}
