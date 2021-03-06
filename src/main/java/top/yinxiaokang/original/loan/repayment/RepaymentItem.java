package top.yinxiaokang.original.loan.repayment;

import top.yinxiaokang.util.Utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;


@XmlRootElement(name = "LoanPlanItem")
@XmlAccessorType(XmlAccessType.FIELD)
public class RepaymentItem implements Serializable {
    /**
     * 还款本金金额
     */
    private BigDecimal hkbjje;
    /**
     * 还款利息金额
     */
    private BigDecimal hklxje;
    /**
     * 发生额
     */
    private BigDecimal fse;
    /**
     * 期初贷款余额
     */
    private BigDecimal qcdkye;
    /**
     * 期末贷款余额
     */
    private BigDecimal qmdkye;
    /**
     * 还款日期
     */
    private Date hkrq;
    /**
     * 还款期次
     */
    private Integer hkqc;

    public BigDecimal getHkbjje() {
        return hkbjje;
    }

    public void setHkbjje(BigDecimal hkbjje) {
        this.hkbjje = hkbjje;
    }

    public BigDecimal getHklxje() {
        return hklxje;
    }

    public void setHklxje(BigDecimal hklxje) {
        this.hklxje = hklxje;
    }

    public BigDecimal getFse() {
        return fse;
    }

    public void setFse(BigDecimal fse) {
        this.fse = fse;
    }

    public BigDecimal getQcdkye() {
        return qcdkye;
    }

    public void setQcdkye(BigDecimal qcdkye) {
        this.qcdkye = qcdkye;
    }

    public BigDecimal getQmdkye() {
        return qmdkye;
    }

    public void setQmdkye(BigDecimal qmdkye) {
        this.qmdkye = qmdkye;
    }

    public Date getHkrq() {
        return hkrq;
    }

    public void setHkrq(Date hkrq) {
        this.hkrq = hkrq;
    }

    public Integer getHkqc() {
        return hkqc;
    }

    public void setHkqc(Integer hkqc) {
        this.hkqc = hkqc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepaymentItem that = (RepaymentItem) o;
        return Objects.equals(hkbjje, that.hkbjje) &&
                Objects.equals(hklxje, that.hklxje) &&
                Objects.equals(fse, that.fse) &&
                Objects.equals(qcdkye, that.qcdkye) &&
                Objects.equals(qmdkye, that.qmdkye) &&
                Objects.equals(hkrq, that.hkrq) &&
                Objects.equals(hkqc, that.hkqc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hkbjje, hklxje, fse, qcdkye, qmdkye, hkrq, hkqc);
    }

    @Override
    public String toString() {
        return "RepaymentItem{" +
                "hkbjje=" + hkbjje +
                ", hklxje=" + hklxje +
                ", fse=" + fse +
                ", qcdkye=" + qcdkye +
                ", qmdkye=" + qmdkye +
                ", hkrq=" + Utils.SDF_YEAR_MONTH_DAY.format(hkrq) +
                ", hkqc=" + hkqc +
                '}';
    }
}