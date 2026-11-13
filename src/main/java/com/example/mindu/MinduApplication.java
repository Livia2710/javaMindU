package com.example.mindu;

import com.example.mindu.domain.entity.Plano;
import com.example.mindu.infra.repository.PlanoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class MinduApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinduApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedPlanos(PlanoRepository repo) {
		return args -> {
			criarOuAtualizarPlano(repo, "Plano Start", 30, "Pequeno", "129.00");
			criarOuAtualizarPlano(repo, "Plano Essencial", 100, "Médio", "109.00");
			criarOuAtualizarPlano(repo, "Plano Corporativo", 500, "Grande", "89.00");
		};
	}

	private void criarOuAtualizarPlano(PlanoRepository repo, String nome, int vagas,
									 String porte, String preco) {
		Plano plano = repo.findAll().stream()
				.filter(item -> item.getNome().equalsIgnoreCase(nome))
				.findFirst()
				.orElseGet(() -> Plano.builder().nome(nome).vagasTotais(vagas).porteIndicado(porte).build());
		plano.setVagasTotais(vagas);
		plano.setPorteIndicado(porte);
		plano.setPrecoMensalPorColaborador(new BigDecimal(preco));
		plano.setConsultasSemLimite(true);
		repo.save(plano);
	}






}
