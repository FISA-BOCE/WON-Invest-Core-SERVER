package com.woorifisa.won_invest_core_server.domain.account.model;

import com.woorifisa.won_invest_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "invest_chn_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestChnAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private UUID investAccountUuid;

    @Column(nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @Column(nullable = false)
    private String accountNoDisplay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    @Builder
    public InvestChnAccount(UUID investAccountUuid, UUID userUuid,
                            String accountNoDisplay, AccountStatus accountStatus) {
        this.investAccountUuid = investAccountUuid;
        this.userUuid = userUuid;
        this.accountNoDisplay = accountNoDisplay;
        this.accountStatus = accountStatus;
    }
}