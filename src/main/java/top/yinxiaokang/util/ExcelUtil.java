package top.yinxiaokang.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import top.yinxiaokang.original.interfaces.ExcelProcess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;

@SuppressWarnings("Duplicates")
@Slf4j
public class ExcelUtil {

    private static File init(String fileName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL resource = classLoader.getResource(fileName);
        Objects.requireNonNull(resource);
        String fileNameTransform = null;
        try {
            fileNameTransform = java.net.URLDecoder.decode(resource.getPath(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert fileNameTransform != null;
        return new File(fileNameTransform);
    }

    /**
     * 复制excel, 更新其中一些值写出
     *
     * @param inFilename    输入文件名
     * @param updateSheetAt 需要更新的sheet序号
     * @param isClassPath
     * @param outFileName   输出文件名
     */
    public static void copyExcelAndUpdate(String inFilename, Integer updateSheetAt, boolean isClassPath, String outFileName, ExcelProcess process) {
        try (Workbook wb = WorkbookFactory.create(isClassPath ? init(inFilename) : new File(inFilename))) {
            updateSheetAt = Optional.ofNullable(updateSheetAt).orElse(0);
            Sheet sheetAt = wb.getSheetAt(updateSheetAt);
            Iterator<Row> rowIterator = sheetAt.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                process.doUpdate(row);
            }

            if (StringUtils.isBlank(outFileName)) {
                String[] split = inFilename.split("\\.");
                outFileName = split[0];
            }
            wb.write(new FileOutputStream(outFileName + "复制更新版.xls"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        log.info("读取 " + inFilename + "结束!");
    }
}
