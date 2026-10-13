package com.example.mindu.application.planos;

import com.example.mindu.infra.repository.PlanoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoRepository repository;

    @GetMapping
    public ResponseEntity<List<PlanoDTO>> listar() {
        List<PlanoDTO> planos = repository.findAll().stream()
                .map(plano -> new PlanoDTO(plano.getId(), plano.getNome(),
                        plano.getVagasTotais(), plano.getPorteIndicado()))
                .toList();
        return ResponseEntity.ok(planos);
    }
}
