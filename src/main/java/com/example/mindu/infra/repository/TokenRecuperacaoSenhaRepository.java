package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.TokenRecuperacaoSenha;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRecuperacaoSenhaRepository extends JpaRepository<TokenRecuperacaoSenha, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TokenRecuperacaoSenha t join fetch t.usuario where t.hashToken = :hash")
    Optional<TokenRecuperacaoSenha> findByHashTokenForUpdate(@Param("hash") String hash);

    @Modifying
    @Transactional
    @Query("update TokenRecuperacaoSenha t set t.invalidadoEm = :agora " +
            "where t.usuario.id = :usuarioId and t.utilizadoEm is null and t.invalidadoEm is null")
    int invalidarPendentesDoUsuario(@Param("usuarioId") String usuarioId,
                                    @Param("agora") LocalDateTime agora);

    @Modifying
    @Transactional
    @Query("update TokenRecuperacaoSenha t set t.invalidadoEm = :agora " +
            "where t.usuario.id = :usuarioId and t.id <> :tokenAtual " +
            "and t.utilizadoEm is null and t.invalidadoEm is null")
    int invalidarOutrosPendentes(@Param("usuarioId") String usuarioId,
                                 @Param("tokenAtual") String tokenAtual,
                                 @Param("agora") LocalDateTime agora);

    @Modifying
    @Transactional
    @Query("update TokenRecuperacaoSenha t set t.invalidadoEm = :agora " +
            "where t.id = :tokenId and t.utilizadoEm is null and t.invalidadoEm is null")
    int invalidarSePendente(@Param("tokenId") String tokenId, @Param("agora") LocalDateTime agora);
}
