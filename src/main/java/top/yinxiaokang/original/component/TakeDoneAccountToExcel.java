package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.SimpleExcelUtilLessFour;

import java.util.*;

@Slf4j
public class TakeDoneAccountToExcel {

    private List<Map<String, Object>> doneAccounts;
    private String inOutFileName = Constants.TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH;
    private Map<String, String> keyMap = new LinkedHashMap<>();

    public List<Map<String, Object>> getDoneAccounts() {
        return doneAccounts;
    }

    public TakeDoneAccountToExcel() {
        doneAccounts = SimpleExcelUtilLessFour.read(inOutFileName, 0, false);
        Set<String> strings = doneAccounts.get(0).keySet();
        for (String string : strings) {
            keyMap.put(string, string);
        }
    }

    public void canAppendToExcel(String dkzh) {
        for (Map map : doneAccounts) {
            if (map.get("贷款账号").equals(dkzh)) {
                throw new RuntimeException("已经存在处理过的贷款账号:" + dkzh);
            }
        }
    }


    public void addToDoneAccounts(String dkzh) {
        canAppendToExcel(dkzh);
        Map addDone = new HashMap();
        System.out.printf("添加已处理贷款账号 %s 到数据文件\n", dkzh);
        addDone.put("贷款账号", dkzh);
        addDone.put("是否已标记", "否");
        doneAccounts.add(addDone);
    }

    public void doToExcel() {
        log.error("准备写出到 {} , 不要关闭程序", inOutFileName);
        SimpleExcelUtilLessFour.writeToExcel(inOutFileName, keyMap, doneAccounts);
    }

}
