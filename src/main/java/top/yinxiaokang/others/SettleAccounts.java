package top.yinxiaokang.others;

import java.math.BigDecimal;

/**
 * @author yinxk
 * @date 2018/7/6 14:33
 */
public class SettleAccounts {
    private BigDecimal TQHBJE;
    private BigDecimal TQHKLX;
    private BigDecimal TQJQHKZE;

    public SettleAccounts() {
    }

    public SettleAccounts(BigDecimal TQHBJE, BigDecimal TQHKLX, BigDecimal TQJQHKZE) {
        this.TQHBJE = TQHBJE;
        this.TQHKLX = TQHKLX;
        this.TQJQHKZE = TQJQHKZE;
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

    public BigDecimal getTQJQHKZE() {
        return TQJQHKZE;
    }

    public void setTQJQHKZE(BigDecimal TQJQHKZE) {
        this.TQJQHKZE = TQJQHKZE;
    }

    @Override
    public String toString() {
        return "SettleAccounts{" +
                "TQBHJE=" + TQHBJE +
                ", TQHKLX=" + TQHKLX +
                ", TQJQHKZE=" + TQJQHKZE +
                '}';
    }
}

