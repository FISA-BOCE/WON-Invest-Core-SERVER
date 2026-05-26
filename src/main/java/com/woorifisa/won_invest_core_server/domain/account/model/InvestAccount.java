package com.woorifisa.won_invest_core_server.domain.account.model;

import com.woorifisa.won_invest_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invest_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestAccount extends BaseTimeEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID investAccountUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invest_user_uuid", nullable = false)
    private InvestCustomer investCustomer;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @Column(nullable = false)
    private String accountPasswordEnc;

    @Column(nullable = false, unique = true, length = 12)
    private String accountNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    @Column(nullable = false)
    private Instant openedAt;

    @Builder
    public InvestAccount(UUID investAccountUuid, InvestCustomer investCustomer, UUID userUuid,
                         String accountPasswordEnc, String accountNo,
                         AccountStatus accountStatus, Instant openedAt) {
        this.investAccountUuid = investAccountUuid;
        this.investCustomer = investCustomer;
        this.userUuid = userUuid;
        this.accountPasswordEnc = accountPasswordEnc;
        this.accountNo = accountNo;
        this.accountStatus = accountStatus;
        this.openedAt = openedAt;
    }
}