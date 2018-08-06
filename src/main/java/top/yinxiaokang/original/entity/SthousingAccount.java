package top.yinxiaokang.original.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yinxk
 * @date 2018/8/6 10:48
 */
public class SthousingAccount {
    private String id;

    private BigDecimal dkffe;

    private Date dkffrq;

    private BigDecimal dkll;

    private BigDecimal dkqs;

    private BigDecimal dkye;

    private String dkzh;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getDkffe() {
        return dkffe;
    }

    public void setDkffe(BigDecimal dkffe) {
        this.dkffe = dkffe;
    }

    public Date getDkffrq() {
        return dkffrq;
    }

    public void setDkffrq(Date dkffrq) {
        this.dkffrq = dkffrq;
    }

    public BigDecimal getDkll() {
        return dkll;
    }

    public void setDkll(BigDecimal dkll) {
        this.dkll = dkll;
    }

    public BigDecimal getDkqs() {
        return dkqs;
    }

    public void setDkqs(BigDecimal dkqs) {
        this.dkqs = dkqs;
    }

    public BigDecimal getDkye() {
        return dkye;
    }

    public void setDkye(BigDecimal dkye) {
        this.dkye = dkye;
    }

    public String getDkzh() {
        return dkzh;
    }

    public void setDkzh(String dkzh) {
        this.dkzh = dkzh;
    }

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
