package top.yinxiaokang.original.entity;

import lombok.Getter;
import lombok.Setter;
import top.yinxiaokang.util.Utils;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
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

    private String remark;

    @Override
    public String toString() {
        return "业务日期:" + (ywfsrq == null ? null : Utils.SDF_YEAR_MONTH_DAY.format(ywfsrq)) +
                ", 期次:" + dqqc +
                ", 类型:'" + dkywmxlx + '\'' +
                ", 本金:" + bjje +
                ", 利息:" + lxje +
                ", 罚息:" + fxje +
                ", 发生额:" + fse +
                ", 下期贷款余额:" + xqdkye;
    }
}
