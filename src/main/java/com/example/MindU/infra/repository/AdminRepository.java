package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.Admin;
import org.hibernate.internal.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {
    // Login — fallback do AuthController. RN11 diz que não há autocadastro público de
    // Admin (por isso não tem existsByEmail/cadastrar aqui), mas ele ainda precisa logar.
    Optional<Admin> findByEmail(String email);
}
