package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.IndexedColors;
import top.yinxiaokang.original.dto.CellStyleAndContent;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author yinxk
 * @date 2018/11/3 15:36
 */
@Slf4j
public class InitTakedAccounts {

    ExcelTransform excelTransform = new ExcelTransform();
    private static String pathStr = Constants.TAKE_ACCOUNT_PATH;


    public void work() {
        initTakeDoneAccounts();
    }

    public void initTakeDoneAccounts() {
        File diretory = new File(pathStr);
        if (!diretory.isDirectory()) {
            throw new RuntimeException("not directory");
        }
        File[] files = diretory.listFiles();
        List<Map<String, CellStyleAndContent>> all = new ArrayList<>();
        Map<String, String> dkzhsKey = new HashMap<>();
        assert files != null;
        for (File file : files) {
            if (file.isFile()) {
                List<Map<String, CellStyleAndContent>> excelAccounts = excelTransform.listExcelAccounts(file.getPath());
                for (Map<String, CellStyleAndContent> excelAccount : excelAccounts) {
                    String dkzh = excelAccount.get("dkzh").getContent().toString();
                    if (dkzhsKey.containsKey(dkzh)) {
                        String msg = "%s 和 %s 存在相同贷款账号: %s";
                        String formatMsg = String.format(msg, dkzhsKey.get(dkzh), file.getName(), dkzh);
                        throw new RuntimeException(formatMsg);
                    }
                    dkzhsKey.put(dkzh, file.getName());
                    all.add(excelAccount);
                }

            }
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss SSS");
        // 发现颜色为40 30的是已标记为处理过的数据
        List<Map<String, Object>> doneAccounts = new ArrayList<>();
        Map<Integer, Integer> colorMap = new HashMap<>();
        for (Map<String, CellStyleAndContent> contentMap : all) {
            CellStyleAndContent content = contentMap.get("dkzh");
            CellStyleAndContent style = contentMap.get("行号");
            String dkzh = content.getContent().toString();
            short color = style.getCellStyle().getFillForegroundColor();
            log.info("贷款账号: {}  颜色是: {}  对应颜色名: {}  文件: {} ", dkzh, color, IndexedColors.fromInt(color).name(), dkzhsKey.get(dkzh));
            Integer integer = colorMap.get((int) color);
            integer = integer == null ? 1 : integer + 1;
            colorMap.put((int) color, integer);
            if (color == 40 || color == 30) {
                Map<String, Object> data = new HashMap<>();
                data.put("dkzh", dkzh);
                data.put("colorValue", color + "");
                data.put("color", IndexedColors.fromInt(color).name());
                data.put("time", dateTimeFormatter.format(LocalDateTime.now()));
                data.put("isFlag", "是");
                data.put("zhMapFile", dkzhsKey.get(dkzh));
                doneAccounts.add(data);
            }
        }
        log.error("不同颜色对应的记录数量: {}", colorMap);
        Map<String, String> keyMap = new LinkedHashMap<>();
        keyMap.put("dkzh", "贷款账号");
        keyMap.put("colorValue", "颜色值");
        keyMap.put("color", "Excel对应颜色");
        keyMap.put("isFlag", "是否已标记");
        keyMap.put("time", "时间");
        keyMap.put("zhMapFile", "账号对应文件");
        ExcelUtil.writeToExcelByAll(Constants.TAKE_ACCOUNT_TAKED_ACCOUNTS_DATA_PATH, null, keyMap, doneAccounts);
    }

    //public static void main(String[] args) {
    //    new InitTakedAccounts().work();
    //}
}
