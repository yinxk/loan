package top.yinxiaokang.excel.util;

import com.sargeraswang.util.ExcelUtil.ExcelLogs;
import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public class ExcelUtilTest {

    @Test
    public void importExcelTest() throws FileNotFoundException {

        File f = new File("src/test/resources/初始有逾期.xlsx");
        //File f = new File("src/test/resources/包含所有的账号的初始余额和导入的逾期本金和.xlsx");
        InputStream inputStream = new FileInputStream(f);

        ExcelLogs logs = new ExcelLogs();
        Collection<Map> importExcel = ExcelUtil.importExcel(Map.class, inputStream, "yyyy/MM/dd HH:mm:ss", logs, 0);


        log.info("读取总条数: "+importExcel.size());

        for (Map m : importExcel) {

            //Set set = m.keySet();
            //Iterator iterator = set.iterator();
            //while (iterator.hasNext()) {
            //    Object next = iterator.next();
            //    log.info(next.getClass().getName() + "   ");
            //}
            //log.info();
            //Collection values = m.values();
            //Iterator iterator1 = values.iterator();
            //while (iterator1.hasNext()) {
            //    Object next = iterator1.next();
            //    log.info(next.getClass().getName() + "  ");
            //}
            log.info(m);
        }
        log.info("读取总条数: "+importExcel.size());
    }
}
