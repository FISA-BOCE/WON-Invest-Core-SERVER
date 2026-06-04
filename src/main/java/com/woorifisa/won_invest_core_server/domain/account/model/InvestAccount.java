package com.woorifisa.won_invest_core_server.domain.account.model;

import com.woorifisa.won_invest_core_server.domain.account.model.enums.AccountStatus;
import com.woorifisa.won_invest_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Pattern;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "invest_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invest_account_user_uuid", columnNames = {"user_uuid"}),
                @UniqueConstraint(name = "uk_invest_account_account_no", columnNames = {"account_no"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestAccount extends BaseTimeEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "invest_account_uuid", columnDefinition = "CHAR(36)")
    private UUID investAccountUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invest_user_uuid", nullable = false)
    private InvestUser investUser;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @Column(name = "account_password_enc", nullable = false)
    private String accountPasswordEnc;

    @Pattern(regexp = "\\d{12}")
    @Column(name = "account_no", nullable = false, length = 12)
    private String accountNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "krw_balance_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal krwBalanceAmount = BigDecimal.ZERO;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    public void depositKrw(BigDecimal amount) {
        if (amount != null && amount.signum() > 0) {
            this.krwBalanceAmount = this.krwBalanceAmount.add(amount);
        }
    }

    @Builder
    public InvestAccount(
            UUID investAccountUuid,
            InvestUser investUser,
            UUID userUuid,
            String accountPasswordEnc,
            String accountNo,
            AccountStatus accountStatus,
            BigDecimal krwBalanceAmount,
            LocalDateTime openedAt
    ) {
        if (!investUser.getUserUuid().equals(userUuid)) {
            throw new IllegalArgumentException(
                    "investUser.userUuid and userUuid must match, but got: "
                            + investUser.getUserUuid() + " vs " + userUuid
            );
        }

        this.investAccountUuid = investAccountUuid;
        this.investUser = investUser;
        this.userUuid = userUuid;
        this.accountPasswordEnc = accountPasswordEnc;
        this.accountNo = accountNo;
        this.accountStatus = accountStatus;
        this.krwBalanceAmount = krwBalanceAmount != null ? krwBalanceAmount : BigDecimal.ZERO;
        this.openedAt = openedAt;
    }
}
