package br.com.coworking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada (cadastro e atualizacao). Nao possui "id": o servidor gera.
 */
public record SalaInput(
        @NotBlank(message = "O nome da sala é obrigatório e não pode ser vazio.")
        String nome,

        @NotNull(message = "A capacidade da sala é obrigatória.")
        @Positive(message = "A capacidade da sala deve ser maior que zero.")
        Integer capacidadePessoas
) {
}
