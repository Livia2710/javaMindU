package com.example.mindu.application.empresas;

import com.example.mindu.domain.entity.Empresa;
import com.example.mindu.domain.entity.Matricula;
import com.example.mindu.domain.entity.Cliente;
import com.example.mindu.infra.repository.MatriculaRepository;
import com.example.mindu.infra.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmpresaMapper {

    // Precisa do MatriculaRepository só pra calcular matriculasUsadas/disponiveis
    // na hora de montar o EmpresaDTO — não guarda estado, só consulta.
    private final MatriculaRepository matriculaRepository;
    private final ClienteRepository clienteRepository;

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
                .email(empresa.getEmail()).telefone(empresa.getTelefone()).responsavel(empresa.getResponsavel())
                .cep(empresa.getCep()).logradouro(empresa.getLogradouro()).numero(empresa.getNumero())
                .complemento(empresa.getComplemento()).bairro(empresa.getBairro()).cidade(empresa.getCidade()).estado(empresa.getEstado())
                .plano(empresa.getPlano().getNome())
                .ativo(empresa.isAtivo())
                .matriculasUsadas((int) usadas)
                .matriculasDisponiveis(disponiveis)
                .precoMensalPorColaborador(empresa.getPlano().getPrecoMensalPorColaborador())
                .consultasSemLimite(empresa.getPlano().getConsultasSemLimite())
                .valorMensalEstimado(empresa.getPlano().getPrecoMensalPorColaborador()
                        .multiply(BigDecimal.valueOf(empresa.getPlano().getVagasTotais())))
                .inicioVigenciaPlano(empresa.getInicioVigenciaPlano())
                .fimVigenciaPlano(empresa.getFimVigenciaPlano())
                .diaPagamento(empresa.getDiaPagamento())
                .build();
    }

    public List<MatriculaDTO> toMatriculaDTOList(List<Matricula> matriculas) {
        if (matriculas.isEmpty()) return List.of();
        Map<String, Cliente> beneficiarios = clienteRepository.findByMatricula_IdIn(
                        matriculas.stream().map(Matricula::getId).toList())
                .stream().collect(Collectors.toMap(c -> c.getMatricula().getId(), Function.identity()));
        return matriculas.stream()
                .map(m -> {
                    Cliente beneficiario = beneficiarios.get(m.getId());
                    return MatriculaDTO.builder()
                            .id(m.getId())
                            .codigo(m.getCodigo())
                            .utilizada(m.isUtilizada())
                            .beneficiarioId(beneficiario == null ? null : beneficiario.getId())
                            .beneficiarioNome(beneficiario == null ? null : beneficiario.getNome())
                            .build();
                })
                .toList();
    }
}
