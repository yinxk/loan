package top.yinxiaokang.original;

import java.text.SimpleDateFormat;

/**
 * Created by where on 2018/7/27.
 */
public class Utils {


    /**
     * 非多线程情况, 无所谓
     */
    public final static SimpleDateFormat SDF_YEAR_MONTH_DAY = new SimpleDateFormat("yyyy-MM-dd");
    public final static SimpleDateFormat SDF_YEAR_MONTH_DAY_HOUR_MIN_SEND = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String toLowerCase(String s) {
        char[] chars = s.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (char c : chars) {
            if (c >= 'A' && c <= 'Z')
                c = (char) (c + 32);
            sb.append(c);
        }
        return sb.toString();
    }
}
