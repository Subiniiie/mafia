package e106.emissary_backend.domain.game.service.subscriber.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class StartConfirmMessage {
    private long gameId;
}