package top.yinxiaokang.original.excelbean;

import com.sargeraswang.util.ExcelUtil.ExcelCell;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yinxk
 * @date 2018/8/24 15:07
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AllAccountDkye {
    @ExcelCell(index = 0)
    private String dkzh;

    @ExcelCell(index = 1)
    private Date dkffrq;

    @ExcelCell(index = 2)
    private BigDecimal dkqs;

    @ExcelCell(index = 3)
    private BigDecimal csdkye;

    @ExcelCell(index = 4)
    private BigDecimal csyqbj;

    @ExcelCell(index = 5)
    private Integer csqs;

    @ExcelCell(index = 6)
    private BigDecimal tsdkye;

    @ExcelCell(index = 7)
    private BigDecimal sjdkye;

    @ExcelCell(index = 8)
    private BigDecimal subdkye;

}
