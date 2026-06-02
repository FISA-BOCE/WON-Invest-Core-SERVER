package com.woorifisa.won_invest_core_server.domain.autoinvest.model;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Entity
@Table(name = "invest_account_etf_ledger")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestAccountEtfLedger extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "etf_ledger_id")
    private Long etfLedgerId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "invest_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID investUserUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invest_account_uuid", nullable = false)
    private InvestAccount investAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etf_id", nullable = false)
    private InvestEtfProduct etf;

    public static InvestAccountEtfLedger buy(
            InvestAccount account,
            InvestEtfProduct etf
    ) {
        InvestAccountEtfLedger ledger = new InvestAccountEtfLedger();
        ledger.userUuid = account.getUserUuid();
        ledger.investUserUuid = account.getInvestUser().getInvestUserUuid();
        ledger.investAccount = account;
        ledger.etf = etf;
        return ledger;
    }
}
