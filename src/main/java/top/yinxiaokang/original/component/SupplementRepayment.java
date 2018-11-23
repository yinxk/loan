package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.util.Constants;
import top.yinxiaokang.util.ExcelUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
            String xh = ExcelUtil.getStringCellContent(row.getCell(keyMapReverse.get("序号")));
            if (StringUtils.isBlank(xh)) return;
            String dkzh = ExcelUtil.getStringCellContent(dkzhCell);
            Map<String, Object> msByDkzh = getMsByDkzh(dkzh);
            Cell xmCell = row.createCell(keyMapReverse.get("姓名"));
            xmCell.setCellStyle(cellStyle);
            xmCell.setCellValue(msByDkzh.get("xm") == null ? "" : msByDkzh.get("xm").toString());
            Cell sjhmCell = row.createCell(keyMapReverse.get("手机号码"));
            sjhmCell.setCellValue(msByDkzh.get("sjhm") == null ? "" : msByDkzh.get("sjhm").toString());
            sjhmCell.setCellStyle(cellStyle);
            Cell kkfsCell = row.createCell(keyMapReverse.get("扣款方式"));
            String ye = msByDkzh.get("ye").toString();
            String fse = ExcelUtil.getStringCellContent(row.getCell(keyMapReverse.get("发生额")));
            BigDecimal yeB = new BigDecimal(ye);
            BigDecimal fseB = new BigDecimal(fse);

            String kkfsStr = "";

            kkfsStr = yeB.compareTo(fseB) > 0 ? "公积金" : "银行卡";

            log.info("序号: {}  贷款账号:{}  发生额:{}  余额:{}  扣款方式:{}", xh, dkzh, fseB, yeB, kkfsStr);
            kkfsCell.setCellValue(kkfsStr);
            kkfsCell.setCellStyle(cellStyle);
        });
    }

    private void setCell(Cell cell) {

    }

    public static void main(String[] args) {
        new SupplementRepayment().work();
    }
}
