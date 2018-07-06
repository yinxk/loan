package top.yinxiaokang.others;

/**
 * @author yinxk
 * @date 2018/7/6 14:24
 */

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@XmlRootElement(name = "LoanPlanItem")
@XmlAccessorType(XmlAccessType.FIELD)
public class LoanPlanItem implements Serializable {

    private BigDecimal hkbjje;  //还款本金金额

    private BigDecimal fse;  //发生额

    private BigDecimal dkye;  //贷款余额

    private BigDecimal hklxje;  //还款利息金额

    private Date hkrq;  //还款日期

    private Integer hkqc; //还款期次

    public BigDecimal getHkbjje() {
        return hkbjje;
    }

    public void setHkbjje(BigDecimal hkbjje) {
        this.hkbjje = hkbjje;
    }

    public BigDecimal getFse() {
        return fse;
    }

    public void setFse(BigDecimal fse) {
        this.fse = fse;
    }

    public BigDecimal getDkye() {
        return dkye;
    }

    public void setDkye(BigDecimal dkye) {
        this.dkye = dkye;
    }

    public BigDecimal getHklxje() {
        return hklxje;
    }

    public void setHklxje(BigDecimal hklxje) {
        this.hklxje = hklxje;
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
    public String toString() {
        return "LoanPlanItem{" +
                "hkbjje=" + hkbjje +
                ", fse=" + fse +
                ", dkye=" + dkye +
                ", hklxje=" + hklxje +
                ", hkrq=" + hkrq +
                ", hkqc=" + hkqc +
                '}';
    }
}