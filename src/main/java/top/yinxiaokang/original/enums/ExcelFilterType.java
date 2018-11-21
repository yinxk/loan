package top.yinxiaokang.original.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yinxk
 * @date 2018/11/3 12:52
 */
@Getter
@AllArgsConstructor
public enum ExcelFilterType {
    ALL("全部"),
    BX_REVERSE("本息颠倒"),
    MANY_FZF("多扣 未结清 负正负"),
    MANY_FFF_FFZ_FFL("多扣 未结清 负负负 | 负负正 | 负负零"),
    MANY_OUTSTANDING_FFF_FFZ_FFL("多扣 已结清"),
    LESS("少扣"),
    OTHER("其他"),
    CLOSED_ACCOUNT("已结清 类型不符合"),
    ;
    private String typeMessage;

}
