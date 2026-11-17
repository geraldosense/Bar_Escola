package pt.escola.barescola.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Pedido de criação de conta de aluno (POST /api/registo). */
public record RegistoDTO(
        @NotBlank(message = "Indica o teu nome.") @Size(max = 100) String nome,
        @NotNull(message = "Indica o número de aluno.") @Positive(message = "Número de aluno inválido.") Integer numero,
        @NotBlank(message = "Indica a turma.")
        @Pattern(regexp = "\\d{1,2}\\s?[A-Za-z]{1,3}", message = "Turma inválida (ex.: 10A).") String turma,
        @NotBlank(message = "Indica o email.") @Email(message = "Email inválido.")
        @Pattern(regexp = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", message = "Email inválido.") String email,
        @NotBlank(message = "Escolhe uma palavra-passe.")
        @Size(min = 8, max = 64, message = "A palavra-passe tem de ter entre 8 e 64 caracteres.") String palavraPasse)
        implements Serializable {
}
