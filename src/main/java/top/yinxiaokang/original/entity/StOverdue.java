package top.yinxiaokang.original.entity;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yinxk
 * @date 2018/8/27 13:35
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StOverdue {
    private String dkzh;

    private Date ssrq;

    private BigDecimal yqbj;

    private BigDecimal yqlx;

    private BigDecimal yqfx;

    private BigDecimal yqqc;

    private String ywzt;
}
