package top.yinxiaokang.original.entity;

import top.yinxiaokang.original.Utils;

import java.math.BigDecimal;
import java.util.Date;

public class SthousingDetail {

    private String id;

    private BigDecimal dqqc;

    private String dkywmxlx;

    private BigDecimal bjje;

    private BigDecimal lxje;

    private BigDecimal fxje;

    private BigDecimal fse;

    private Date ywfsrq;

    private BigDecimal xqdkye;

    private Date created_at;

    private String extenstion;

    public BigDecimal getDqqc() {
        return dqqc;
    }

    public void setDqqc(BigDecimal dqqc) {
        this.dqqc = dqqc;
    }

    public String getDkywmxlx() {
        return dkywmxlx;
    }

    public void setDkywmxlx(String dkywmxlx) {
        this.dkywmxlx = dkywmxlx;
    }

    public BigDecimal getBjje() {
        return bjje;
    }

    public void setBjje(BigDecimal bjje) {
        this.bjje = bjje;
    }

    public BigDecimal getLxje() {
        return lxje;
    }

    public void setLxje(BigDecimal lxje) {
        this.lxje = lxje;
    }

    public BigDecimal getFse() {
        return fse;
    }

    public void setFse(BigDecimal fse) {
        this.fse = fse;
    }

    public BigDecimal getXqdkye() {
        return xqdkye;
    }

    public void setXqdkye(BigDecimal xqdkye) {
        this.xqdkye = xqdkye;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getFxje() {
        return fxje;
    }

    public void setFxje(BigDecimal fxje) {
        this.fxje = fxje;
    }

    public Date getYwfsrq() {
        return ywfsrq;
    }

    public void setYwfsrq(Date ywfsrq) {
        this.ywfsrq = ywfsrq;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public String getExtenstion() {
        return extenstion;
    }

    public void setExtenstion(String extenstion) {
        this.extenstion = extenstion;
    }

    @Override
    public String toString() {
        return "SthousingDetail{" +
                " ywfsrq=" + (ywfsrq == null ? null : Utils.SDF_YEAR_MONTH_DAY.format(ywfsrq)) +
                ", dqqc=" + dqqc +
                ", dkywmxlx='" + dkywmxlx + '\'' +
                ", bjje=" + bjje +
                ", lxje=" + lxje +
                ", fxje=" + fxje +
                ", fse=" + fse +
                ", xqdkye=" + xqdkye +
                '}';
    }
}
