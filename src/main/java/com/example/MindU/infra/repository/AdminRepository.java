package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {
    // Login — fallback do AuthController. RN11 diz que não há autocadastro público de
    // Admin (por isso não tem existsByEmail/cadastrar aqui), mas ele ainda precisa logar.
    Optional<Admin> findByEmail(String email); // "me dá o Admin com esse e-mail" — pode não achar, daí o Optional

    boolean existsByEmail(String email);       // "existe um Admin com esse e-mail?" — só sim ou não
}
