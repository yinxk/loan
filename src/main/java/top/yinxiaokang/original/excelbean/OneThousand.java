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
    private Date dkffrq;
    @ExcelCell(index = 4)
    private BigDecimal dkqs;
    @ExcelCell(index = 5)
    private BigDecimal csqs;
    @ExcelCell(index = 6)
    private String csqszqx;

    @ExcelCell(index = 7)
    private String hklx;
    @ExcelCell(index = 8)
    private Date rq;
    @ExcelCell(index = 9)
    private Integer qc;
    @ExcelCell(index = 10)
    private BigDecimal fse;
    @ExcelCell(index = 11)
    private BigDecimal bj;
    @ExcelCell(index = 12)
    private BigDecimal lx;
    @ExcelCell(index = 13)
    private BigDecimal qmdkye;


    @ExcelCell(index = 14)
    private String sjhklx;
    @ExcelCell(index = 15)
    private Date sjrq;
    @ExcelCell(index = 16)
    private Integer sjqc;
    @ExcelCell(index = 17)
    private BigDecimal sjfse;
    @ExcelCell(index = 18)
    private BigDecimal sjbj;
    @ExcelCell(index = 19)
    private BigDecimal sjlx;
    @ExcelCell(index = 20)
    private BigDecimal sjqmdkye;

    @ExcelCell(index = 21)
    private BigDecimal subfse;
    @ExcelCell(index = 22)
    private BigDecimal subbj;
    @ExcelCell(index = 23)
    private BigDecimal sublx;
    @ExcelCell(index = 24)
    private BigDecimal subqmye;

    @ExcelCell(index = 25)
    private String bz;


//    @ExcelCell(index = 26)
//    private BigDecimal subFseTotal;
//    @ExcelCell(index = 26)
//    private BigDecimal subBjTotal;
//    @ExcelCell(index = 26)
//    private BigDecimal subLxTotal;
//    @ExcelCell(index = 26)
//    private BigDecimal subDkyeTotal;


    @ExcelCell(index = 26)
    private BigDecimal tsdkye;
    @ExcelCell(index = 27)
    private BigDecimal sjdkye;
    @ExcelCell(index = 28)
    private BigDecimal subDkye;

}
