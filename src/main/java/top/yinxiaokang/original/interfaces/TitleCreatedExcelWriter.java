package top.yinxiaokang.original.interfaces;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface TitleCreatedExcelWriter<T> {
    void process(Sheet sheet, Map<String, String> keyMap, List<Map<String, T>> contents);
}
