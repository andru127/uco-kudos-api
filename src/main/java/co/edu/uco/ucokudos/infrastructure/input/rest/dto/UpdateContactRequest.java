package co.edu.uco.ucokudos.infrastructure.input.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateContactRequest(@NotBlank @Email @Size(max = 150) String email,
                                   @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$",
                                           message = "Usa formato internacional, por ejemplo +573001234567")
                                   String phone) {
}
