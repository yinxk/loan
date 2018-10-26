package top.yinxiaokang.original.entity;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CLoanHousingPersonInformationBasic {
    private String id;
    private String dkzh;
    private String dkzhzt;
    private String jkrgjjzh;
    private String jkrxm;
    private String jkrzjlx;
    private String jkrzjhm;
    private String sjhm;
    private BigDecimal yhqs;
    private String ywwd;
    private String coborrower;
    private String loancontract;
    private String personalaccount;

}
