package top.yinxiaokang.original.component;

import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.entity.CLoanHousingPersonInformationBasic;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.util.BeanOrMapUtil;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.MilliSecond;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ActualAccountDkye {
    AccountCheck accountCheck;
    List<SthousingAccount> sthousingAccounts;
    List<CLoanHousingPersonInformationBasic> basicList;


    public ActualAccountDkye() {
        accountCheck = new AccountCheck();
    }

    private String initBaseAccountDkzhAppendedString() {
        List<InitInformation> initInformations = Common.listBaseAccountInformationByExcelUtil();
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (InitInformation information : initInformations) {
            isFirst = Common.appendDkzhToSqlCanRead(sb, isFirst, information.getDkzh(), false);
        }
        return sb.toString();
    }


    private void listSthousingAccountByDkzhs() {
        sthousingAccounts = accountCheck.listSthousingAccountByDkzhs(initBaseAccountDkzhAppendedString());
    }

    private void toExcel() {
        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("dkzh", "dkzh");
        keyMap.put("dkye", "dkye");
        keyMap.put("jkrxm", "jkrxm");
        List<Map<String, Object>> contentList = new ArrayList<>();
        for (SthousingAccount sthousingAccount : sthousingAccounts) {
            Map<String, Object> stringObjectMap = BeanOrMapUtil.transBean2Map(sthousingAccount);
            CLoanHousingPersonInformationBasic basicByDkzh = accountCheck.getBasicByDkzh(sthousingAccount.getDkzh(), false);
            stringObjectMap.put("jkrxm", basicByDkzh.getJkrxm());
            contentList.add(stringObjectMap);
        }
        try (OutputStream outputStream = new FileOutputStream(Constants.BASE_ACCOUNT_INFORMATION_SSDKYE)) {
            ExcelUtil.exportExcel(keyMap, contentList, outputStream);
            log.error("实时贷款余额文件更新写入完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void work() {
        listSthousingAccountByDkzhs();
        toExcel();
    }


    public static void main(String[] args) {
        while (true) {
            long sleepTime = MilliSecond.betweenNowAndTomorrow2301();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ActualAccountDkye actualAccountDkye = new ActualAccountDkye();
            actualAccountDkye.work();
        }
    }
}
