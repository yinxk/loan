package top.yinxiaokang.original.entity.excel;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author yinxk
 * @date 2018/8/20 16:44
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InitHasOverdue {

    private String dkzh;

    private BigDecimal csye;

    private BigDecimal csqs;

    private BigDecimal csyqbj;
}
