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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "comm_user_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMapping extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID cardUserUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "CHAR(36)")
    private UUID investUserUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvstConnectedStatus invstConnectedStatus;

    public static UserMapping createNew(UUID userUuid) {
        Objects.requireNonNull(userUuid, "userUuid must not be null");
        UserMapping mapping = new UserMapping();
        mapping.userUuid = userUuid;
        mapping.invstConnectedStatus = InvstConnectedStatus.NOT_CONNECTED;
        return mapping;
    }

    public void connect(UUID investUserUuid) {
        Objects.requireNonNull(investUserUuid, "investUserUuid must not be null");
        this.invstConnectedStatus = InvstConnectedStatus.CONNECTED;
        this.investUserUuid = investUserUuid;
    }
}
