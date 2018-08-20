package top.yinxiaokang.original.mains;

import com.sargeraswang.util.ExcelUtil.ExcelLogs;
import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import org.junit.Test;
import top.yinxiaokang.original.dto.AccountInformations;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.entity.excel.InitInformation;
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


        // 日志
        StringBuffer logs = new StringBuffer();


        logs.append("读取总条数: " + importExcel.size() + "\n");

        ArrayList<InitInformation> initHasOverdueList = new ArrayList<>();

        for (Map m : importExcel) {
            InitInformation initHasOverdue = new InitInformation();
            initHasOverdue.setDkzh((String) m.get("dkzh"));
            initHasOverdue.setCsye(new BigDecimal((String) m.get("csye")));
            initHasOverdue.setCsqs(new BigDecimal((String) m.get("csqs")));
            initHasOverdue.setCsyqbj(new BigDecimal((String) m.get("csyqbj")));
            initHasOverdueList.add(initHasOverdue);
        }

        List<AccountInformations> accountInformationsList = new ArrayList<>();
        for (InitInformation initInformation : initHasOverdueList) {
            AccountInformations accountInformations = toAccountInformations(accountCheck, initInformation);
            accountInformationsList.add(accountInformations);
        }

        Map<String, List<AccountInformations>> generateOrNotGenerateList = isGenerateOrNotGenerateList(accountInformationsList);
        List<AccountInformations> isGenerate = generateOrNotGenerateList.get("isGenerate");
        List<AccountInformations> notGenerate = generateOrNotGenerateList.get("notGenerate");


        int dealNum = 0;
        logs.append("==================================================start--已经产生业务==========================================\n");
        for (AccountInformations item : isGenerate) {
            logs.append("开始处理第: " + (++dealNum) + " 条 \n");
            analyze(item, logs);
            logs.append("结束处理第: " + dealNum + " 条 \n\n");

        }
        logs.append("==================================================end--已经产生业务==========================================\n");
        logs.append("==================================================start--没有产生业务==========================================\n");
        for (AccountInformations item : notGenerate) {
            logs.append("开始处理第: " + (++dealNum) + " 条 \n");
            analyze(item, logs);
            logs.append("结束处理第: " + dealNum + " 条 \n\n");

        }
        logs.append("==================================================end--没有产生业务==========================================\n");

        logs.append("读取总条数: " + importExcel.size() + "\n");


        System.out.println(logs.toString());

    }


    public void analyze(AccountInformations informations, StringBuffer logs) {

        logs.append("贷款账号: " + informations.getSthousingAccount().getDkzh() +
                " , 初始贷款余额 : " + informations.getInitInformation().getCsye() +
                " , 初始逾期本金 : " + informations.getInitInformation().getCsyqbj() +
                " , 初始期数: " + informations.getInitFirstQc() + " \n");
        List<SthousingDetail> details = informations.getDetails();
        for (SthousingDetail detail : details) {
            logs.append(detail + "\n");
        }
    }


    /**
     * 将每个账号有关的信息转换整理
     *
     * @param accountCheck
     * @param initInformation
     * @return
     */
    public AccountInformations toAccountInformations(AccountCheck accountCheck, InitInformation initInformation) {
        AccountInformations accountInformations = new AccountInformations();
        SthousingAccount account = accountCheck.getSthousingAccount(initInformation.getDkzh());
        List<CurrentPeriodRange> ranges = accountCheck.listHSRange(account, null);
        BigDecimal yhqs = accountCheck.yhqs(ranges);
        BigDecimal initFirstQc = yhqs.add(BigDecimal.ONE);
        // 该账号已入账的业务记录
        List<SthousingDetail> sthousingDetails = accountCheck.listDetails(account);
        Collections.sort(sthousingDetails, Comparator.comparing(SthousingDetail::getDqqc));
        // 还款计划
        List<RepaymentItem> repaymentItems = accountCheck.repaymentItems(account, ranges, initInformation.getCsye(), initInformation.getCsyqbj(), true);
        accountInformations.setSthousingAccount(account);
        accountInformations.setCurrentPeriodRanges(ranges);
        accountInformations.setYhqs(yhqs);
        accountInformations.setSyqs(accountCheck.syqs(yhqs, account));
        accountInformations.setInitFirstQc(initFirstQc);
        accountInformations.setDetails(sthousingDetails);
        accountInformations.setInitInformation(initInformation);
        accountInformations.setRepaymentItems(repaymentItems);
        return accountInformations;
    }


    public Map<String, List<AccountInformations>> isGenerateOrNotGenerateList(List<AccountInformations> list) {
        Map<String, List<AccountInformations>> result = new HashMap<>();
        List<AccountInformations> isGenerate = new ArrayList<>();
        List<AccountInformations> notGenerate = new ArrayList<>();

        for (AccountInformations item : list) {
            isGenerate(item);
            if (item.isGenerated()) {
                isGenerate.add(item);
            } else {
                notGenerate.add(item);
            }
        }
        result.put("isGenerate", isGenerate);
        result.put("notGenerate", notGenerate);

        return result;
    }

    public AccountInformations isGenerate(AccountInformations accountInformations) {

        // 该账号已入账的业务记录
        List<SthousingDetail> sthousingDetails = accountInformations.getDetails();

        // 是否连续
        boolean isContinuous = true;
        // 是否已产生入账的业务
        boolean isGenerated = false;
        BigDecimal qsqs = accountInformations.getInitFirstQc();
        for (SthousingDetail detail : sthousingDetails) {
            if (detail.getDqqc().compareTo(accountInformations.getInitFirstQc()) >= 0) {
                isGenerated = true;
                if (detail.getDqqc().compareTo(qsqs) != 0) {
                    isContinuous = false;
                }
                qsqs = qsqs.add(BigDecimal.ONE);

            }
        }
        accountInformations.setGenerated(isGenerated);
        accountInformations.setContinuous(isContinuous);
        return accountInformations;
    }
}
