package top.yinxiaokang.original.component;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.dao.BaseDao;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.others.StringUtil;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.ExcelUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
@Slf4j
public class UpdateQcByExcel {
    private BaseDao baseDao;
    private int count;
    private List<String> doLog = new ArrayList<>();
    private List<UpdateModal> failedList = new ArrayList<>();
    private List<UpdateModal> successedList = new ArrayList<>();
    private final static String SQL_TO_FORMAT = "UPDATE st_housing_business_details de  " +
            "INNER JOIN c_housing_business_details_extension deex ON de.extenstion = deex.id  " +
            "SET de.DQQC = ? " +
            "WHERE  " +
            "  de.DKZH = ?  " +
            "  AND de.DQQC = ?  ";
    private Conn conn;
    private Connection connection;

    public UpdateQcByExcel() {
        conn = new Conn();
        connection = conn.getConnection();
        baseDao = new BaseDao(connection);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class UpdateModal {
        private String dkzh;
        private String errorQc;
        private String rightQc;
        private String ywlsh;
    }

    private void work(UpdateModal updateModal) {
        String sql = SQL_TO_FORMAT;
        if (StringUtil.notEmpty(updateModal.getYwlsh())) {
            sql += " AND de.ywlsh = ?";
        }
        try {
            count++;
            log.info("开始处理第 {} 条", count);
            connection.setAutoCommit(false);
            int i = 0;
            if (StringUtil.notEmpty(updateModal.getYwlsh())) {
                i = baseDao.updateCommon(sql, updateModal.getRightQc(), updateModal.getDkzh(), updateModal.getErrorQc(), updateModal.getYwlsh());
            } else {
                i = baseDao.updateCommon(sql, updateModal.getRightQc(), updateModal.getDkzh(), updateModal.getErrorQc());
            }
            if (i > 1) {
                log.info("更新数量大于1: 已进行回滚");
                connection.rollback();
                return;
            }
            log.info("处理更新了 {} 条", i);
            log.info("结束处理第 {} 条", count);
            connection.commit();
            successedList.add(updateModal);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                log.info("回滚失败!");
                e1.printStackTrace();
            }
            failedList.add(updateModal);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        ExcelReadReturn excelReadReturn = ExcelUtil.readExcel("C:\\修账相关数据\\提前还款\\期次问题\\提前还款期次错误-使用版.xls", 0, false, true);
        List<Map<String, Object>> content = excelReadReturn.getContent();

        List<UpdateModal> updateModalList = new ArrayList<>();
        for (Map<String, Object> map : content) {
            String dkzh = map.get("贷款账号").toString();
            String errorQc = map.get("错误期次").toString();
            String rightQc = map.get("正确期次").toString();
            BigDecimal rerrorQcDecimal = new BigDecimal(errorQc.toString());
            BigDecimal rightQcBigInteger = new BigDecimal(rightQc.toString());
            String ywlsh = map.get("业务流水号") == null ? "" : map.get("业务流水号").toString();
            UpdateModal updateModal = new UpdateModal();
            updateModal.setDkzh(dkzh);
            updateModal.setErrorQc(rerrorQcDecimal.toBigInteger().toString());
            updateModal.setRightQc(rightQcBigInteger.toBigInteger().toString());
            updateModal.setYwlsh(ywlsh);
            updateModalList.add(updateModal);
        }

        log.info("读取信息打印开头, 条数: {}", updateModalList.size());
        for (UpdateModal modal : updateModalList) {
            log.info("信息: {}", modal);
        }
        log.info("读取信息打印结尾, 条数: {}", updateModalList.size());
        UpdateQcByExcel updateQcByExcel = new UpdateQcByExcel();
        log.info("更新开始");
        for (UpdateModal updateModal : updateModalList) {
            try {
                updateQcByExcel.work(updateModal);
            } catch (Exception e) {
                log.error("{} {}", updateModal, e);
            }
        }
        log.info("更新结束");

    }
}
