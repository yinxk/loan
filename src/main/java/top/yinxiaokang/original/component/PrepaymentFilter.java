package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("WeakerAccess")
@Slf4j
public class PrepaymentFilter {

    private Set<String> baseAccountSet = new HashSet<>();
    private List<String> inBaseAccountList = new ArrayList<>();
    List<InitInformation> initInformationList;
    private Map<String, Integer> doneAccountMap = new HashMap<>();

    public PrepaymentFilter() {
        initInformationList = Common.listBaseAccountInformationByExcelUtil();
        for (InitInformation information : initInformationList) {
            baseAccountSet.add(information.getDkzh());
        }
        ExcelReadReturn excelReadReturn = ExcelUtil.readExcel(Constants.PRE_REPAYMENT_DONE_XLSX_PATH, 0, false, false);
        List<Map<String, Object>> content = excelReadReturn.getContent();
        for (Map<String, Object> map : content) {
            String dkzh = (String) map.get("贷款账号");
            if (doneAccountMap.containsKey(dkzh)) {
                doneAccountMap.put(dkzh, doneAccountMap.get(dkzh) + 1);
            } else {
                doneAccountMap.put(dkzh, 1);
            }
        }
    }

    private Integer getDoneAccountNumber(String dkzh) {
        return doneAccountMap.get(dkzh);
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
        Map<String, Integer> accountMap = new HashMap<>();
        for (Map<String, Object> map : content) {
            String dkzh = (String) map.get("贷款账号");
            if (StringUtils.isBlank(dkzh)) {
                continue;
            }
            if (accountMap.containsKey(dkzh)) {
                accountMap.put(dkzh, accountMap.get(dkzh) + 1);
            } else {
                accountMap.put(dkzh, 1);
            }
            Double ce = (Double) map.get("差额");
            BigDecimal ceBig = new BigDecimal(ce.toString());
            int scale = ceBig.scale();
            if (scale > 2) {
                throw new RuntimeException(dkzh + " 差额位数太多: " + ceBig.toPlainString());
            }
            map.put("差额", ceBig.negate());

            Integer doneAccountNumber = getDoneAccountNumber(dkzh);
            if (doneAccountNumber != null && accountMap.get(dkzh).compareTo(doneAccountNumber) <= 0) {
                log.error(" 贷款账号: {}  处理次数: {}  当前计数序号:{}", dkzh, doneAccountNumber, accountMap.get(dkzh));
                continue;
            }

            if (isInBaseAccount(dkzh)) {
                inBaseAccountList.add(dkzh);
            } else {
                rContent.add(map);
            }
        }


        Map<String, Integer> dkzhMap = new HashMap<>();

        for (Map<String, Object> map : rContent) {
            String dkzh = (String) map.get("贷款账号");
            if (StringUtils.isBlank(dkzh)) {
                throw new RuntimeException("验证阶段,存在空贷款账号");
            }
            if (isInBaseAccountByInitList(dkzh)) {
                throw new RuntimeException("经过过滤还存在交集贷款账号 : " + dkzh);
            }

            Integer number = dkzhMap.get(dkzh);

            dkzhMap.put(dkzh, number == null ? 1 : number + 1);

        }


        StringBuilder sb = new StringBuilder();
        boolean first = true;
        int checkNum = 0;
        for (String s : dkzhMap.keySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("'");
            sb.append(s);
            sb.append("'");
            checkNum++;
        }

        log.error("总共条数: {}", rContent.size());
        String dkzhs = sb.toString();
        log.error(dkzhs);
        log.error("方式一验证账号的数量: {}", checkNum);
        log.error("方式二验证账号的数量: {}", dkzhs.split(",").length);

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
        PrepaymentFilter filter1427Account = new PrepaymentFilter();
        filter1427Account.work();
    }
}
