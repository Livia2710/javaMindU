package com.example.mindu.domain.entity;

import com.example.mindu.application.contas.SolicitacaoExclusaoStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_exclusao_conta")
@Data
@NoArgsConstructor
public class SolicitacaoExclusaoConta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String usuarioId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String tipoConta;

    @Column(nullable = false)
    private LocalDateTime solicitadaEm = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SolicitacaoExclusaoStatus status = SolicitacaoExclusaoStatus.PENDENTE;
}
