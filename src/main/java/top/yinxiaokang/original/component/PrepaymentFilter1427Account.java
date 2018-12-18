package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.util.*;

@SuppressWarnings("WeakerAccess")
@Slf4j
public class PrepaymentFilter1427Account {

    private Set<String> baseAccountSet = new HashSet<>();
    private List<String> inBaseAccountList = new ArrayList<>();
    List<InitInformation> initInformationList;

    public PrepaymentFilter1427Account() {
        initInformationList = Common.listBaseAccountInformationByExcelUtil();
        for (InitInformation information : initInformationList) {
            baseAccountSet.add(information.getDkzh());
        }
    }

    private boolean isInBaseAccount(String dkzh) {
        return baseAccountSet.contains(dkzh);
    }

    private boolean isInBaseAccountByInitList(String dkzh) {
        for (InitInformation initInformation : initInformationList) {
            if (initInformation.getDkzh().equals(dkzh)) {
                return true;
            }
        }
        return false;
    }

    public void work() {
        ExcelReadReturn excelReadReturn = ExcelUtil.readExcel(Constants.PRE_REPAYMENT_XLSX_PATH, 1, false, false);
        List<Map<String, Object>> content = excelReadReturn.getContent();

        List<Map<String, Object>> rContent = new ArrayList<>();
        for (Map<String, Object> map : content) {
            String dkzh = (String) map.get("贷款账号");
            if (StringUtils.isBlank(dkzh)) {
                continue;
            }
            if (isInBaseAccount(dkzh)) {
                inBaseAccountList.add(dkzh);
            } else {
                rContent.add(map);
            }
        }

        for (Map<String, Object> map : rContent) {
            String dkzh = (String) map.get("贷款账号");
            if (isInBaseAccountByInitList(dkzh)) {
                throw new RuntimeException("经过过滤还存在交集贷款账号 : " + dkzh);
            }
        }

        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("贷款账号", "贷款账号");
        keyMap.put("差额", "利息差额合计");

        ExcelUtil.writeToExcelByAll(Constants.PRE_REPAYMENT_PATH + "/转换版/待处理" + Constants.XLS, null, keyMap, rContent);

        for (String s : inBaseAccountList) {
            log.info("其中排除的贷款账号为: {} ", s);
        }
        log.info("数量: {}", inBaseAccountList.size());

    }


    public static void main(String[] args) {
        PrepaymentFilter1427Account filter1427Account = new PrepaymentFilter1427Account();
        filter1427Account.work();
    }
}
