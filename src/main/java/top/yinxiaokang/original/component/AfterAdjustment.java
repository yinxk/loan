package top.yinxiaokang.original.component;

import top.yinxiaokang.original.dto.AccountInformations;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.original.service.AccountCheck;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AfterAdjustment {

    private AccountCheck accountCheck ;
    private List<String> doneDkzhList;
    private List<AccountInformations> accountInformationList = new ArrayList<>();

    public AfterAdjustment() {
        accountCheck = new AccountCheck();
        List<InitInformation> initInformationList = Common.listBaseAccountInformationByExcelUtil();
        doneDkzhList = new ArrayList<>();
        List<Map<String, String>> maps = top.yinxiaokang.util.ExcelUtil.readStringExcel(Constants.TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH, 0, false, false);
        for (Map<String, String> map : maps) {
            doneDkzhList.add(map.get("贷款账号"));
        }

        for (InitInformation initInformation : initInformationList) {
            AccountInformations accountInformations = accountCheck.toAccountInformations(initInformation);
            if (accountInformations == null) {
                continue;
            }
            accountInformationList.add(accountInformations);
        }
    }


    private void doWork() {

    }


    public static void main(String[] args) {
        AfterAdjustment afterAdjustment = new AfterAdjustment();
        System.out.println("djdjd");
    }
}
