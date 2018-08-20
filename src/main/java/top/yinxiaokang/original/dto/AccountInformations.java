package top.yinxiaokang.original.dto;

import lombok.*;
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

    private BigDecimal yhqs;

    private BigDecimal syqs;

    private BigDecimal initFirstQc;

    private List<RepaymentItem> repaymentItems;

    private boolean isContinuous;

    private boolean isGenerated;


}
