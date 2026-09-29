package com.example.mindu.infra.repository;

import com.example.mindu.domain.entity.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoRepository extends JpaRepository<Plano, String> {
    // Sem métodos derivados — o CommandLineRunner usa save(), e o cadastro de Empresa
    // usa o findById() que já vem de JpaRepository. Nenhum RF pede busca por nome/porte.
}