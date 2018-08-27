package top.yinxiaokang.original.dto;

import lombok.*;
import top.yinxiaokang.original.entity.StOverdue;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.loan.repayment.RepaymentItem;
import top.yinxiaokang.others.CurrentPeriodRange;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccountInformations {
    private SthousingAccount sthousingAccount;

    private InitInformation initInformation;

    private List<SthousingDetail> details;

    private List<CurrentPeriodRange> currentPeriodRanges;

    private List<StOverdue> initOverdueList;

    private BigDecimal yhqs;

    private BigDecimal syqs;

    private BigDecimal initFirstQc;

    private List<RepaymentItem> repaymentItems;

    /**
     * 是否连续扣款
     */
    private boolean isContinuous;

    /**
     * 是否有已入账的业务
     */
    private boolean isGenerated;
    /**
     * 是否提前还款
     */
    private boolean isPrepayment;

    /**
     * 是否导入的时候存在逾期
     */
    private boolean isInitHasOverdue = false;


}
