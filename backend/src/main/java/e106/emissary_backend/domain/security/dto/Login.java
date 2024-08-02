package e106.emissary_backend.domain.security.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Login {
    private String email;
    private String password;
}