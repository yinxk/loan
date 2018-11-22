package top.yinxiaokang.original.component;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplementRepayment {

    private List<Map<String, Object>> messages;

    public SupplementRepayment() {
        ExcelReadReturn excelReadReturn = ExcelUtil.readExcel(Constants.BASE_PATH + "/20181122补扣账号" + Constants.XLS, null, false, false);
        messages = excelReadReturn.getContent();
    }

    private Map<String, Object> getMsByDkzh(String dkzh) {
        for (Map<String, Object> message : messages) {
            if (message.get("dkzh").equals(dkzh)) {
                return message;
            }
        }
        return new HashMap<>();
    }

    private void work() {
        ExcelUtil.copyExcelAndUpdate(Constants.BASE_PATH + "/截止到11-06日需要进行补扣的Test" + Constants.XLSX, 0, false, null, (workbook, row, keyMap, keyMapReverse) -> {
            Cell dkzhCell = row.getCell(keyMapReverse.get("贷款账号"));
            if (dkzhCell == null) return;
            CellStyle cellStyle = dkzhCell.getCellStyle();
            if (cellStyle == null) return;
            String dkzh = ExcelUtil.getStringCellContent(dkzhCell);
            Map<String, Object> msByDkzh = getMsByDkzh(dkzh);
            Cell xmCell = row.createCell(keyMapReverse.get("姓名"));
            xmCell.setCellStyle(cellStyle);
            xmCell.setCellValue(msByDkzh.get("xm") == null ? "" : msByDkzh.get("xm").toString());
            Cell sjhmCell = row.createCell(keyMapReverse.get("手机号码"));
            sjhmCell.setCellValue(msByDkzh.get("sjhm") == null ? "" : msByDkzh.get("sjhm").toString());
            sjhmCell.setCellStyle(cellStyle);
            Cell kkfsCell = row.createCell(keyMapReverse.get("扣款方式"));
            kkfsCell.setCellValue(msByDkzh.get("kkfs") == null ? "" : msByDkzh.get("kkfs").toString());
            kkfsCell.setCellStyle(cellStyle);
        });
    }

    private void setCell(Cell cell) {

    }

    public static void main(String[] args) {
        new SupplementRepayment().work();
    }
}
