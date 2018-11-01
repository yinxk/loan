package top.yinxiaokang.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * @author yinxk
 * @date 2018/10/9 10:39
 */
public class DateUtilTest {

    @Test
    public void date2LocalDate() {
        Date date = new Date();
        LocalDate localDate = DateUtil.date2LocalDate(date);
        log.info(localDate);
    }

    @Test
    public void localDate2Date() {
        LocalDate localDate = LocalDate.now();
        Date date = DateUtil.localDate2Date(localDate);
        log.info(Utils.SDF_YEAR_MONTH_DAY_HOUR_MIN_SEND.format(date));
    }

    @Test
    public void date2LocalDateTime() {
        Date date = new Date();
        LocalDateTime localDateTime = DateUtil.date2LocalDateTime(date);
        log.info(localDateTime);
    }

    @Test
    public void localDateTime2Date() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = DateUtil.localDateTime2Date(localDateTime);
        log.info(Utils.SDF_YEAR_MONTH_DAY_HOUR_MIN_SEND.format(date));
    }

    @Test
    public void date2LocalTime() {
        Date date = new Date();
        LocalTime localTime = DateUtil.date2LocalTime(date);
        log.info(localTime);
    }

    @Test
    public void localTime2Date() {
        LocalTime localTime = LocalTime.now();
        Date date = DateUtil.localTime2Date(localTime);
        log.info(Utils.SDF_YEAR_MONTH_DAY_HOUR_MIN_SEND.format(date));
    }
}