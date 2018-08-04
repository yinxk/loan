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

        File f = new File("src/test/resources/无标题1.xlsx");
        InputStream inputStream = new FileInputStream(f);

        ExcelLogs logs = new ExcelLogs();
        Collection<Map> importExcel = ExcelUtil.importExcel(Map.class, inputStream, "yyyy/MM/dd HH:mm:ss", logs, 0);


        System.out.println("读取总条数: "+importExcel.size());

        for (Map m : importExcel) {

            //Set set = m.keySet();
            //Iterator iterator = set.iterator();
            //while (iterator.hasNext()) {
            //    Object next = iterator.next();
            //    System.out.println(next.getClass().getName() + "   ");
            //}
            //System.out.println();
            //Collection values = m.values();
            //Iterator iterator1 = values.iterator();
            //while (iterator1.hasNext()) {
            //    Object next = iterator1.next();
            //    System.out.println(next.getClass().getName() + "  ");
            //}
            //System.out.println(m);
        }
    }
}
