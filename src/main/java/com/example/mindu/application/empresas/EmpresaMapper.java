package com.example.mindu.application.empresas;

import com.example.mindu.domain.entity.Empresa;
import com.example.mindu.domain.entity.Matricula;
import com.example.mindu.infra.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmpresaMapper {

    // Precisa do MatriculaRepository só pra calcular matriculasUsadas/disponiveis
    // na hora de montar o EmpresaDTO — não guarda estado, só consulta.
    private final MatriculaRepository matriculaRepository;

    // DTO de entrada → Entidade. Note que NÃO seta o Plano aqui — o Service
    // é quem busca o Plano pelo planoId e associa, porque o mapper não deveria
    // depender do PlanoRepository (ele só monta o objeto, não valida nada).
    public Empresa toEntity(EmpresaCadastroDTO dto) {
        return Empresa.builder()
                .razaoSocial(dto.getRazaoSocial())
                .nomeFantasia(dto.getNomeFantasia())
                .inscricaoEstadual(dto.getInscricaoEstadual())
                .cnpj(dto.getCnpj())
                .responsavel(dto.getResponsavel())
                .cep(dto.getCep())
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .complemento(dto.getComplemento())
                .bairro(dto.getBairro())
                .cidade(dto.getCidade())
                .estado(dto.getEstado())
                .nome(dto.getRazaoSocial()) // Usuario.nome — reaproveita a razão social
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                .senha(dto.getSenha()) // ainda em texto puro aqui — Service faz o encode
                .build();
    }

    // Entidade → DTO de saída
    public EmpresaDTO toDTO(Empresa empresa) {
        long usadas = matriculaRepository.countByEmpresaId(empresa.getId()); // RN09
        int disponiveis = empresa.getPlano().getVagasTotais() - (int) usadas;

        return EmpresaDTO.builder()
                .id(empresa.getId())
                .razaoSocial(empresa.getRazaoSocial())
                .nomeFantasia(empresa.getNomeFantasia())
                .cnpj(empresa.getCnpj())
                .plano(empresa.getPlano().getNome())
                .matriculasUsadas((int) usadas)
                .matriculasDisponiveis(disponiveis)
                .build();
    }

    public List<MatriculaDTO> toMatriculaDTOList(List<Matricula> matriculas) {
        return matriculas.stream()
                .map(m -> MatriculaDTO.builder()
                        .id(m.getId())
                        .codigo(m.getCodigo())
                        .utilizada(m.isUtilizada())
                        .build())
                .toList();
    }
}