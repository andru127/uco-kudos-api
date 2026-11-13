package co.edu.uco.ucokudos.infrastructure.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(@NotBlank @Size(max = 80) String name, @Size(max = 300) String description) {
}
