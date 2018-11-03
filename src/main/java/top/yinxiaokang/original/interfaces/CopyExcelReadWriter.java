package top.yinxiaokang.original.interfaces;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Map;

/**
 * @author yinxk
 * @date 2018/11/3 19:37
 */
@FunctionalInterface
public interface CopyExcelReadWriter {
    void process(Workbook workbook, Row row, Map<Integer, String> colIndexMapContent,
                 Map<String, Integer> contentMapColIndex, Map<Integer, Integer> proIndexMapColIndex);
}
