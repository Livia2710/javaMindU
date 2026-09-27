package com.example.mindu.application.profissionais;

import com.example.mindu.domain.entity.Disponibilidade;
import com.example.mindu.domain.entity.LocalAtendimento;
import com.example.mindu.domain.entity.Profissional;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class ProfissionalMapper {

    // fotoCarteirinha é o único arquivo que entra no cadastro — foto/fotoBanner
    // ficam null aqui de propósito, só são preenchidos depois via atualizarPerfil.
    public Profissional toEntity(ProfissionalCadastroDTO dto, MultipartFile fotoCarteirinha) throws IOException {
        return Profissional.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(dto.getSenha())
                .tipo(dto.getTipo())
                .tags(dto.getTags())
                .fotoCarterinha(fotoCarteirinha.getBytes())
                .build();
    }

    public ProfissionalDTO toDTO(Profissional profissional) {
        return ProfissionalDTO.builder()
                .id(profissional.getId())
                .nome(profissional.getNome())
                .email(profissional.getEmail())
                .tipo(profissional.getTipo())
                .tags(profissional.getTags())
                .status(profissional.getStatus())
                .build();
    }

    public LocalAtendimento toEntity(LocalAtendimentoDTO dto, Profissional profissional) {
        return LocalAtendimento.builder()
                .profissional(profissional)
                .modalidade(dto.getModalidade())
                .cep(dto.getCep())
                .logradouro(dto.getLogradouro())
                .numero(dto.getNumero())
                .complemento(dto.getComplemento())
                .bairro(dto.getBairro())
                .cidade(dto.getCidade())
                .estado(dto.getEstado())
                .build();
    }

    public LocalAtendimentoDTO toDTO(LocalAtendimento local) {
        LocalAtendimentoDTO dto = new LocalAtendimentoDTO();
        dto.setModalidade(local.getModalidade());
        dto.setCep(local.getCep());
        dto.setLogradouro(local.getLogradouro());
        dto.setNumero(local.getNumero());
        dto.setComplemento(local.getComplemento());
        dto.setBairro(local.getBairro());
        dto.setCidade(local.getCidade());
        dto.setEstado(local.getEstado());
        return dto;
    }

    public Disponibilidade toEntity(DisponibilidadeDTO dto, Profissional profissional) {
        return Disponibilidade.builder()
                .profissional(profissional)
                .diaSemana(dto.getDiaSemana())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .build();
    }

    public DisponibilidadeDTO toDTO(Disponibilidade d) {
        DisponibilidadeDTO dto = new DisponibilidadeDTO();
        dto.setDiaSemana(d.getDiaSemana());
        dto.setHoraInicio(d.getHoraInicio());
        dto.setHoraFim(d.getHoraFim());
        return dto;
    }

    public List<ProfissionalDTO> toDTOList(List<Profissional> profissionais) {
        return profissionais.stream().map(this::toDTO).toList();
    }
}