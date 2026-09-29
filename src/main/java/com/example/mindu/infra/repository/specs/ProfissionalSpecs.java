package com.example.mindu.infra.repository.specs;

import com.example.mindu.domain.entity.LocalAtendimento;
import com.example.mindu.domain.entity.Profissional;
import com.example.mindu.domain.enums.Modalidade;
import com.example.mindu.domain.enums.StatusVerificacao;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class ProfissionalSpecs {

    private ProfissionalSpecs() {} // classe utilitária, não deve ser instanciada

    public static Specification<Profissional> comNome(String nome) {
        return (root, query, cb) -> cb.like(cb.upper(root.get("nome")), "%" + nome.toUpperCase() + "%");
    }

    public static Specification<Profissional> comEspecialidade(String tag) {
        return (root, query, cb) -> {
            Join<Profissional, String> tags = root.join("tags");
            return cb.like(cb.upper(tags), "%" + tag.toUpperCase() + "%");
        };
    }

    // Diferente das outras — modalidade não é campo direto do Profissional, é
    // campo de LocalAtendimento, então precisa de um JOIN entre as duas tabelas.
    public static Specification<Profissional> comModalidade(Modalidade modalidade) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Profissional, LocalAtendimento> locais = root.join("locaisAtendimento");
            return cb.equal(locais.get("modalidade"), modalidade);
        };
    }

    // RN06 — Cliente NUNCA deve ver Profissional não aprovado, mesmo sem filtro nenhum
    public static Specification<Profissional> aprovado() {
        return (root, query, cb) -> cb.equal(root.get("status"), StatusVerificacao.APROVADO);
    }
}