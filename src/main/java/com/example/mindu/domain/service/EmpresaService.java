package com.example.mindu.domain.service;

import com.example.mindu.domain.entity.Empresa;
import com.example.mindu.domain.entity.Matricula;

import java.util.List;

public interface EmpresaService {
    Empresa cadastrar(Empresa empresa, String planoId); // RF05, RN02
    Empresa buscarPorId(String id);
    Matricula validarMatricula(String codigo);
    Empresa atualizarPerfil(String id, com.example.mindu.application.empresas.EmpresaPerfilDTO dto);
    List<Matricula> gerarMatriculas(String empresaId, int quantidade); // RF06, RN09
}