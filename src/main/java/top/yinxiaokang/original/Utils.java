package top.yinxiaokang.original;

import java.text.SimpleDateFormat;

/**
 * Created by where on 2018/7/27.
 */
public class Utils {
    /**
     * 一年以360天计算
     */
    public final static int YEAR_DAYS = 360;
    public final static int YEAR_MONTHS = 12;
    public final static int MONTH_DAYS = 30;


    /**
     * 小数舍入位数
     */
    public final static int SCALE_TWO = 2;
    public final static int SCALE_EIGHT = 8;
    public final static int SCALE_TEN = 10;
    public final static int SCALE_TWELVE = 12;

    public final static SimpleDateFormat SDF_YEAR_MONTH_DAY = new SimpleDateFormat("yyyy-MM-dd");
}
