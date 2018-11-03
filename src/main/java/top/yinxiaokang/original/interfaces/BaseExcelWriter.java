package top.yinxiaokang.original.interfaces;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;
@FunctionalInterface
public interface BaseExcelWriter<T> {
    void process(Workbook workbook, Map<String, String> keyMap, List<Map<String, T>> contents);
}
