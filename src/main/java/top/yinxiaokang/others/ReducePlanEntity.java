package top.yinxiaokang.others;

import java.math.BigDecimal;

/**
 * @author yinxk
 * @date 2018/7/6 15:19
 */
public class ReducePlanEntity {

    String id ;
    BigDecimal dkll ;
    BigDecimal llfdbl;
    BigDecimal dkgbjhqs ;
    BigDecimal dkgbjhye ;
    BigDecimal dqqc ;
    String dkhkfs ;

    @Override
    public String toString() {
        return "ReducePlanEntity{" +
                "id='" + id + '\'' +
                ", dkll=" + dkll +
                ", llfdbl=" + llfdbl +
                ", dkgbjhqs=" + dkgbjhqs +
                ", dkgbjhye=" + dkgbjhye +
                ", dqqc=" + dqqc +
                ", dkhkfs='" + dkhkfs + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getDkll() {
        return dkll;
    }

    public void setDkll(BigDecimal dkll) {
        this.dkll = dkll;
    }

    public BigDecimal getLlfdbl() {
        return llfdbl;
    }

    public void setLlfdbl(BigDecimal llfdbl) {
        this.llfdbl = llfdbl;
    }

    public BigDecimal getDkgbjhqs() {
        return dkgbjhqs;
    }

    public void setDkgbjhqs(BigDecimal dkgbjhqs) {
        this.dkgbjhqs = dkgbjhqs;
    }

    public BigDecimal getDkgbjhye() {
        return dkgbjhye;
    }

    public void setDkgbjhye(BigDecimal dkgbjhye) {
        this.dkgbjhye = dkgbjhye;
    }

    public BigDecimal getDqqc() {
        return dqqc;
    }

    public void setDqqc(BigDecimal dqqc) {
        this.dqqc = dqqc;
    }

    public String getDkhkfs() {
        return dkhkfs;
    }

    public void setDkhkfs(String dkhkfs) {
        this.dkhkfs = dkhkfs;
    }
}
