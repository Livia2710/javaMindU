package com.example.mindu.domain.service;

import com.example.mindu.application.profissionais.DisponibilidadeDTO;
import com.example.mindu.application.profissionais.LocalAtendimentoDTO;
import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.enums.Modalidade;

import java.util.List;

public interface ProfissionalService {

    Profissional cadastrar(Profissional profissional); // RF07, RF10

    Profissional aprovar(String id, boolean aprovado); // RF16

    Profissional buscarPorId(String id);

    LocalAtendimentoDTO adicionarLocal(String profissionalId, LocalAtendimentoDTO dto); // RF08

    List<DisponibilidadeDTO> definirDisponibilidade(String profissionalId, List<DisponibilidadeDTO> dtos); // RF09

    Profissional atualizarPerfil(String id, byte[] foto, byte[] fotoBanner); // editável depois do cadastro

    List<Profissional> pesquisar(String nome, String especialidade, Modalidade modalidade); // RF13
}