package top.yinxiaokang.original.entity;

import com.sargeraswang.util.ExcelUtil.ExcelCell;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SomedayInformation {

    @ExcelCell(index = 1)
    private String dkzh;

    @ExcelCell(index = 2)
    private String dkffrq;

    @ExcelCell(index = 3)
    private String qc;

    private Date dkxffrq;

    private BigDecimal dqqc;

    @ExcelCell(index = 4)
    private String nextkkrq;

    private String ffday;

    @ExcelCell(index = 5)
    private String dkzhzt;

    private String jkrxm;

    private String sfwtkr;  // 是否委托扣款

    private String xffday;

    @ExcelCell(index = 6)
    private String ffdaysfxd; // 发放日是否相等


}
