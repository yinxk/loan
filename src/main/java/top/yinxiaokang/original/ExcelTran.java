package top.yinxiaokang.original;

import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import top.yinxiaokang.util.ImportExcelUtilLessFour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinxk
 * @date 2018/10/30 11:45
 */
public class ExcelTran {

    private static String pathStr = "C:\\Users\\where\\Desktop\\修账相关数据\\修账\\";
    private static String fileStr = "2018-10-16-业务推算和实际业务-凭证调整数据-加说明";
    private static String xls = ".xls";

    public static void main(String[] args) {

        List<Map<String, Object>> list = ImportExcelUtilLessFour.read(pathStr + fileStr + xls, 1, false);
        Iterator<Map<String, Object>> iterator = list.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> next = iterator.next();
            Object 序号 = next.get("序号");
            if (!(序号 instanceof Double)) {
                iterator.remove();
            }

        }
        iterator = list.iterator();
        String regex = "账号：([\\s\\S]*)\n初始贷款余额";
        Pattern pattern = Pattern.compile(regex);


        int notMatchNumber = 0;
        while (iterator.hasNext()) {
            Map<String, Object> next = iterator.next();
            String 行号 = (String) next.get("行号");
            Matcher matcher = pattern.matcher(行号);
            if (matcher.find()) {
                String group = matcher.group(1);
                System.out.println("匹配得到的贷款账号: ====" + group + "====");
                next.put("dkzh", group);
            } else {
                System.out.println("存在没有匹配" + ++notMatchNumber);
            }
        }


        Map<String, String> keyMap = new LinkedHashMap<>();

        keyMap.put("dkzh", "dkzh");
        keyMap.put("发生额差额合计", "fsecehj");
        keyMap.put("本金差额合计", "bjcehj");
        keyMap.put("利息差额合计", "lxcehj");
        keyMap.put("备注", "bz");
        keyMap.put("行号", "hh");



        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(pathStr + "转换版/" + fileStr + "-转换版" + xls))) {
            ExcelUtil.exportExcel(keyMap, list, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("转换完成");

    }
}
