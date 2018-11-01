package top.yinxiaokang.others;

import top.yinxiaokang.util.Utils;

import java.util.Date;

/**
 * @author yinxk
 * @date 2018/7/6 14:14
 */
public class CurrentPeriodRange {
    private Date beforeTime;
    private Date  afterTime;
    private int currentPeriod;

    public CurrentPeriodRange() {
    }

    public Date getBeforeTime() {
        return beforeTime;
    }

    public void setBeforeTime(Date beforeTime) {
        this.beforeTime = beforeTime;
    }

    public Date getAfterTime() {
        return afterTime;
    }

    public void setAfterTime(Date afterTime) {
        this.afterTime = afterTime;
    }

    public int getCurrentPeriod() {
        return currentPeriod;
    }

    public void setCurrentPeriod(int currentPeriod) {
        this.currentPeriod = currentPeriod;
    }

    @Override
    public String toString() {
        return "CurrentPeriodRange{" +
                "beforeTime=" + Utils.SDF_YEAR_MONTH_DAY.format(beforeTime) +
                ", afterTime=" + Utils.SDF_YEAR_MONTH_DAY.format(afterTime) +
                ", currentPeriod=" + currentPeriod +
                '}';
    }
}