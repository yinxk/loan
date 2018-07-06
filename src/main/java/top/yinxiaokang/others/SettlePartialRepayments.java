package top.yinxiaokang.others;

import java.math.BigDecimal;

/**
 * @author yinxk
 * @date 2018/7/6 14:33
 */
public class SettlePartialRepayments {
    private BigDecimal TQHKJE;
    private BigDecimal TQHBJE;
    private BigDecimal TQHKLX;

    public SettlePartialRepayments() {
    }

    public SettlePartialRepayments(BigDecimal TQHKJE, BigDecimal TQHBJE, BigDecimal TQHKLX) {
        this.TQHKJE = TQHKJE;
        this.TQHBJE = TQHBJE;
        this.TQHKLX = TQHKLX;
    }

    public BigDecimal getTQHKJE() {
        return TQHKJE;
    }

    public void setTQHKJE(BigDecimal TQHKJE) {
        this.TQHKJE = TQHKJE;
    }

    public BigDecimal getTQHBJE() {
        return TQHBJE;
    }

    public void setTQHBJE(BigDecimal TQHBJE) {
        this.TQHBJE = TQHBJE;
    }

    public BigDecimal getTQHKLX() {
        return TQHKLX;
    }

    public void setTQHKLX(BigDecimal TQHKLX) {
        this.TQHKLX = TQHKLX;
    }
}