package com.example.mindu.application.contas;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/conta")
@RequiredArgsConstructor
public class ContaController {
    private final SolicitacaoExclusaoService service;

    @PostMapping("/solicitacao-exclusao")
    public ResponseEntity<SolicitacaoExclusaoDTO> solicitarExclusao(@AuthenticationPrincipal String email) {
        return ResponseEntity.accepted().body(service.solicitar(email));
    }
}
