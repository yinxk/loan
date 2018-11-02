package top.yinxiaokang.original.component;

import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.entity.SthousingAccount;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.util.BeanOrMapUtil;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ActualAccountDkye {
    AccountCheck accountCheck = new AccountCheck();
    List<SthousingAccount> sthousingAccounts;


    private String initBaseAccountDkzhAppendedString() {
        List<InitInformation> initInformations = Common.listBaseAccountInformationByExcelUtil();
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (InitInformation information : initInformations) {
            isFirst = Common.appendDkzhToSqlCanRead(sb, isFirst, information.getDkzh());
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
        List<Map<String, Object>> contentList = new ArrayList<>();
        for (SthousingAccount sthousingAccount : sthousingAccounts) {
            Map<String, Object> stringObjectMap = BeanOrMapUtil.transBean2Map(sthousingAccount);
            contentList.add(stringObjectMap);
        }
        try (OutputStream outputStream = new FileOutputStream(Constants.BASE_ACCOUNT_INFORMATION_SSDKYE)) {
            ExcelUtil.exportExcel(keyMap, contentList, outputStream);
            log.error("实时贷款余额文件更新写入完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void work(){
        listSthousingAccountByDkzhs();
        toExcel();
    }

    public static void main(String[] args) {
        ActualAccountDkye actualAccountDkye = new ActualAccountDkye();
        actualAccountDkye.work();
    }
}
