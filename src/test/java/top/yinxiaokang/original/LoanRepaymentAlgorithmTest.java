package top.yinxiaokang.original;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author yinxk
 * @date 2018/7/24 15:00
 */
public class LoanRepaymentAlgorithmTest {
    public static final BigDecimal DKLL = new BigDecimal("3.25");
    @Test
    public void calLxByDkye() throws Exception {
        BigDecimal dkye = new BigDecimal("237853.15");
        BigDecimal bigDecimal = LoanRepaymentAlgorithm.calLxByDkye(dkye, DKLL);
        System.out.println(bigDecimal);
    }

}