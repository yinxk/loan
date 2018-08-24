package top.yinxiaokang.original.excelbean;

import com.sargeraswang.util.ExcelUtil.ExcelCell;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yinxk
 * @date 2018/8/24 10:51
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OneThousand {
    @ExcelCell(index = 0)
    private String dkzh;
    @ExcelCell(index = 1)
    private BigDecimal csdkye;
    @ExcelCell(index = 2)
    private BigDecimal csyqbj;
    @ExcelCell(index = 3)
    private String hklx;
    @ExcelCell(index = 4)
    private Date rq;
    @ExcelCell(index = 5)
    private Integer qc;
    @ExcelCell(index = 6)
    private BigDecimal fse;
    @ExcelCell(index = 7)
    private BigDecimal bj;
    @ExcelCell(index = 8)
    private BigDecimal lx;
    @ExcelCell(index = 9)
    private BigDecimal qmdkye;

}
