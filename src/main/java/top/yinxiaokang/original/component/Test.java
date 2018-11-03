package top.yinxiaokang.original.component;

import top.yinxiaokang.original.dto.CellStyleAndContent;
import top.yinxiaokang.util.ExcelUtil;

import java.util.List;
import java.util.Map;

/**
 * @author yinxk
 * @date 2018/11/3 10:39
 */
public class Test {
    public static void main(String[] args) {
        String inFileName = "C:\\修账相关数据\\修账\\2018-10-18-业务推算和实际业务-凭证调整数据-修账-多少扣说明.xls";
        List<Map<String, CellStyleAndContent>> maps = ExcelUtil.readExcelCellStyleAndContent(inFileName, 1, null,false);
        System.out.println("etet");
    }
}
