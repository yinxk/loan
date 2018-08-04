package top.yinxiaokang.original.loan.repayment;

public enum RepaymentMethod {
    /**
     * 等额本息
     */
    BX("01"),
    /**
     * 等额本金
     */
    BJ("02");

    String code;

    RepaymentMethod(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public RepaymentMethod getRepaymentMethodByCode(String code) {
        RepaymentMethod[] values = values();
        for (RepaymentMethod repaymentMethod : values) {
            if (repaymentMethod.getCode().equals(code)) {
                return repaymentMethod;
            }
        }
        return null;
    }
}
