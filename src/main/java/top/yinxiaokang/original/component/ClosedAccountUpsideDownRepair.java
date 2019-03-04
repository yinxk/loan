package top.yinxiaokang.original.component;

import lombok.extern.slf4j.Slf4j;
import top.yinxiaokang.original.dao.SthousingDetailDao;
import top.yinxiaokang.original.dto.ExcelReadReturn;
import top.yinxiaokang.original.entity.SthousingDetail;
import top.yinxiaokang.original.entity.excel.InitInformation;
import top.yinxiaokang.others.StringUtil;
import top.yinxiaokang.util.Common;
import top.yinxiaokang.util.ExcelUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
@Slf4j
public class ClosedAccountUpsideDownRepair {

    private Map<String, InitInformation> initInformationMap = new HashMap<>();


    private SthousingDetailDao sthousingDetailDao;

    public ClosedAccountUpsideDownRepair() {
        List<InitInformation> initInformations = Common.listBaseAccountInformationByExcelUtil();
        for (InitInformation initInformation : initInformations) {
            initInformationMap.put(initInformation.getDkzh(), initInformation);
        }
        sthousingDetailDao = new SthousingDetailDao(new Conn().getConnection());
    }


    public List<SthousingDetail> listDetailByDkzh(String dkzh) {
        try {
            return sthousingDetailDao.listByDkzh(dkzh, false);
        } catch (IllegalAccessException | SQLException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int doUpdate(String id, BigDecimal value) {
        String sql = "UPDATE\n" +
                "\tst_housing_business_details de\n" +
                "\tINNER JOIN c_housing_business_details_extension deex ON de.extenstion = deex.id \n" +
                "SET deex.XQDKYE = ?\n" +
                "WHERE\n" +
                "\tde.id = ?";
        try {
            int i = sthousingDetailDao.updateCommon(sql, value, id);
            return i;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int doUpdateQc(String id, BigDecimal dqqc) {
        String sql = "UPDATE\n" +
                "\tst_housing_business_details de\n" +
                "\tINNER JOIN c_housing_business_details_extension deex ON de.extenstion = deex.id \n" +
                "SET de.DQQC = ?\n" +
                "WHERE\n" +
                "\tde.id = ?";
        try {
            int i = sthousingDetailDao.updateCommon(sql, dqqc, id);
            return i;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void work() {
        ExcelReadReturn excelReadReturn = ExcelUtil.readExcel("C:\\修账相关数据\\修账\\转换版\\剩余结清的 - 本金利息颠倒 - 副本-转换版.xls", 0, false, true);
        List<Map<String, Object>> content = excelReadReturn.getContent();
        for (Map<String, Object> map : content) {
            workForOne(map.get("dkzh").toString());
        }
    }

    public void workForOne(String dkzh) {
        log.info("处理贷款账号: {} 开始", dkzh);
        InitInformation initInformation = initInformationMap.get(dkzh);
        List<SthousingDetail> sthousingDetails = listDetailByDkzh(dkzh);
        SthousingDetail repaired = null;
        for (SthousingDetail sthousingDetail : sthousingDetails) {
            if (StringUtil.notEmpty(sthousingDetail.getRemark())) {
                repaired = sthousingDetail;
            }
        }

        sthousingDetails.removeIf(sthousingDetail -> !StringUtil.isEmpty(sthousingDetail.getRemark()));
        sthousingDetails.sort(Comparator.comparing(SthousingDetail::getDqqc));
        log.info("初始信息: {} ", initInformation);
        log.info("业务信息: {}", sthousingDetails);
        SthousingDetail last = null;
        BigDecimal ye = initInformation.getCsye();
        for (SthousingDetail sthousingDetail : sthousingDetails) {
            last = sthousingDetail;
            ye = ye.subtract(sthousingDetail.getBjje());
            int i = doUpdate(sthousingDetail.getId(), ye);
            log.info("更新期次:{},贷款余额:{}", sthousingDetail.getDqqc(), ye);
        }

        doUpdateQc(repaired.getId(), last.getDqqc());

        log.info("处理贷款账号: {} 结束", dkzh);
    }


    public static void main(String[] args) {
        ClosedAccountUpsideDownRepair closedAccountUpsideDownRepair = new ClosedAccountUpsideDownRepair();
        closedAccountUpsideDownRepair.work();
    }
}
