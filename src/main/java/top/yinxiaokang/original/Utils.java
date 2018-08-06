package top.yinxiaokang.original;

import java.text.SimpleDateFormat;

/**
 * Created by where on 2018/7/27.
 */
public class Utils {


    public final static SimpleDateFormat SDF_YEAR_MONTH_DAY = new SimpleDateFormat("yyyy-MM-dd");

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
