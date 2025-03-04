package e106.emissary_backend.global.error.exception;

import e106.emissary_backend.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NoUserInRoomException extends RuntimeException{
    final ErrorCode errorCode;
}
