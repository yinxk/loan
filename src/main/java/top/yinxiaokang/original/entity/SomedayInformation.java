package top.yinxiaokang.original.entity;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SomedayInformation {

    private String dkzh;

    private String dkffrq;

    private BigDecimal qc;

    private Date dkxffrq;

    private BigDecimal dqqc;

    private String nextkkrq;

    private String ffday;

    private String dkzhzt;

    private String jkrxm;

    private String sfwtkr;  // 是否委托扣款

    private String xffday;

    private String ffdaysfxd; // 发放日是否相等


}
