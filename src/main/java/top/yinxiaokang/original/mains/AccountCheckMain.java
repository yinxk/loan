package top.yinxiaokang.original.mains;

import com.sargeraswang.util.ExcelUtil.ExcelLogs;
import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import org.junit.Test;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.entity.excel.InitHasOverdue;
import top.yinxiaokang.original.loan.repayment.RepaymentItem;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.others.CurrentPeriodRange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/8/6 11:45
 */
public class AccountCheckMain {
    @Test
    public void Test1() {
        AccountCheck accountCheck = new AccountCheck();

        File f = new File("src/test/resources/初始有逾期.xlsx");
        Collection<Map> importExcel = new ArrayList<>();
        try {
            try (InputStream inputStream = new FileInputStream(f)) {
                ExcelLogs logs = new ExcelLogs();
                importExcel = ExcelUtil.importExcel(Map.class, inputStream, "yyyy/MM/dd HH:mm:ss", logs, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("读取总条数: " + importExcel.size());

        ArrayList<InitHasOverdue> initHasOverdueList = new ArrayList<>();

        for (Map m : importExcel) {
            InitHasOverdue initHasOverdue = new InitHasOverdue();
            initHasOverdue.setDkzh((String) m.get("dkzh"));
            initHasOverdue.setCsye((BigDecimal) m.get("csye"));
            initHasOverdue.setCsqs((BigDecimal) m.get("csqs"));
            initHasOverdue.setCsyqbj((BigDecimal) m.get("csyqbj"));
            initHasOverdueList.add(initHasOverdue);
        }

        for (InitHasOverdue item : initHasOverdueList) {
            byDkzh(accountCheck, item.getDkzh(), item.getCsye(), item.getCsyqbj());
        }

        System.out.println("读取总条数: " + importExcel.size());

    }


    public void byDkzh(AccountCheck accountCheck, String dkzh, BigDecimal initDkye, BigDecimal initOverdueBjje) {

        SthousingAccount account = accountCheck.getSthousingAccount(dkzh);
        List<CurrentPeriodRange> ranges = accountCheck.listHSRange(account, null);
        BigDecimal yhqs = accountCheck.yhqs(ranges);
        BigDecimal ourFirstQc = yhqs.add(BigDecimal.ONE);
        // 该账号已入账的业务记录
        List<SthousingDetail> sthousingDetails = accountCheck.listDetails(account);
        Collections.sort(sthousingDetails, Comparator.comparing(SthousingDetail::getDqqc));
        // 还款计划
        List<RepaymentItem> repaymentItems = accountCheck.repaymentItems(account, ranges, initDkye, initOverdueBjje, true);

        System.out.println("==============================开始======================================= " + dkzh + " ======================");
        System.out.printf("贷款账号: %s , 初始贷款余额 : %s , 初始逾期本金 : %s , 初始期数: %s \n", dkzh, initDkye, initOverdueBjje, ourFirstQc);

        for (RepaymentItem item : repaymentItems) {
//            if (item.getHkrq().getTime() <= System.currentTimeMillis())
//                System.out.println(item);
        }

        boolean isLianXu = true;
        boolean isKouKuan = false;
        BigDecimal qsqs = ourFirstQc;
        for (SthousingDetail detail : sthousingDetails) {
            if (detail.getDqqc().compareTo(ourFirstQc) >= 0) {
                isKouKuan = true;
                if (detail.getDqqc().compareTo(qsqs) != 0) {
                    isLianXu = false;
                }
                qsqs = qsqs.add(BigDecimal.ONE);
            }
            System.out.println(detail);
        }

        System.out.println("是否是连续的扣款期次: " + isLianXu);
        System.out.println("是否在生成的计划后扣款: " + isKouKuan);
        System.out.println("==============================结束======================================= " + dkzh + " ======================");

    }


}
