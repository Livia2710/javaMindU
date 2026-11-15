package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
    //RNO1 - Impede e-mail duplicado no cadastro do Cliente
    boolean existsByEmail(String email);

    //Login(AuthController) - tenta achar o e-mail nessa tabela primeiro
    Optional<Cliente> findByEmail(String email);

    List<Cliente> findByEmpresaId(String empresaId);

    List<Cliente> findByMatricula_IdIn(Collection<String> matriculaIds);
}
