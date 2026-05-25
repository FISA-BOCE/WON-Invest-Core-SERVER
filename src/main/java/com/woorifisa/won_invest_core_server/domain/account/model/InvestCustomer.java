package com.woorifisa.won_invest_core_server.domain.account.model;

import com.woorifisa.won_invest_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "invest_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestCustomer extends BaseTimeEntity {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private UUID investUserUuid;

    @Column(nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String telEnc;

    @Column(nullable = false)
    private String emailEnc;

    @Builder
    public InvestCustomer(UUID investUserUuid, UUID userUuid,
                          String customerName, String telEnc, String emailEnc) {
        this.investUserUuid = investUserUuid;
        this.userUuid = userUuid;
        this.customerName = customerName;
        this.telEnc = telEnc;
        this.emailEnc = emailEnc;
    }
}