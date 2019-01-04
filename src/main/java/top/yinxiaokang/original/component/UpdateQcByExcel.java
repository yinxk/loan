package top.yinxiaokang.original.component;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.others.StringUtil;
import top.yinxiaokang.util.ExcelUtil;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
@Slf4j
public class UpdateQcByExcel {
    private DoSql doSql;
    private int count;
    private List<String> doLog;
    private List<UpdateModal> failedList;
    private List<UpdateModal> successedList;
    private final static String SQL_TO_FORMAT = "UPDATE st_housing_business_details de  " +
            "INNER JOIN c_housing_business_details_extension deex ON de.extenstion = deex.id  " +
            "SET de.DQQC = %s " +
            "WHERE  " +
            "  de.DKZH = %s  " +
            "  AND de.DQQC = %s  ";
    private Conn conn;
    private Connection connection;

    public UpdateQcByExcel() {
        conn = new Conn();
        connection = conn.getConnection();
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
        String sql = String.format(SQL_TO_FORMAT, updateModal.getRightQc(), updateModal.getDkzh(), updateModal.getErrorQc());
        if (StringUtil.notEmpty(updateModal.getYwlsh())) {
            sql += " AND de.ywlsh = '" + updateModal.getYwlsh() + "'";
        }
        try {
            count++;
            log.info("开始处理第 {} 条", count);
            connection.setAutoCommit(false);
            int i = doSql.doUpdate(connection, sql);
            log.info("结束处理第 {} 条", count );
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
        } finally {
            Conn.closeResource(connection, null, null);
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
            BigInteger rightQcBigInteger = new BigInteger(rightQc.toString());
            String ywlsh = map.get("业务流水号") == null ? "" : map.get("业务流水号").toString();
            UpdateModal updateModal = new UpdateModal();
            updateModal.setDkzh(dkzh);
            updateModal.setErrorQc(errorQc);
            updateModal.setRightQc(rightQcBigInteger.toString());
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
            //updateQcByExcel.work(updateModal);
        }
        log.info("更新结束");

    }
}
