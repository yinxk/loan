package top.yinxiaokang.original.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yinxk
 * @date 2018/8/6 10:48
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SthousingAccount {
    private String id;

    private BigDecimal dkffe;

    private Date dkffrq;

    private BigDecimal dkll;

    private BigDecimal dkqs;

    private BigDecimal dkye;

    private String dkzh;

    private String dkhkfs;

    private BigDecimal dkgbjhqs;

    private BigDecimal dkgbjhye;

    private Date dkxffrq;

    @Override
    public String toString() {
        return "SthousingAccount{" +
                "dkzh=" + dkzh +
                ", dkffrq=" + dkffrq +
                ", dkll=" + dkll +
                ", dkqs=" + dkqs +
                ", dkffe=" + dkffe +
                ", dkye='" + dkye + '\'' +
                '}';
    }
}
