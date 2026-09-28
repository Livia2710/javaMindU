package com.example.mindu;

import com.example.mindu.domain.entity.Plano;
import com.example.mindu.infra.repository.PlanoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MinduApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinduApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedPlanos(PlanoRepository repo) {
		return args -> {
			if (repo.count() == 0) {
				repo.save(Plano.builder().nome("Plano Start").vagasTotais(30).porteIndicado("Pequeno").build());
				repo.save(Plano.builder().nome("Plano Essencial").vagasTotais(100).porteIndicado("Médio").build());
				repo.save(Plano.builder().nome("Plano Corporativo").vagasTotais(500).porteIndicado("Grande").build());
			}
		};
	}






}
