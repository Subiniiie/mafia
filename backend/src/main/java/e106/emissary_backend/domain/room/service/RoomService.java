package e106.emissary_backend.domain.room.service;

import e106.emissary_backend.domain.game.entity.Game;
import e106.emissary_backend.domain.game.enumType.GameState;
import e106.emissary_backend.domain.game.mapper.GameMapper;
import e106.emissary_backend.domain.game.model.GameDTO;
import e106.emissary_backend.domain.game.model.Player;
import e106.emissary_backend.domain.game.repository.RedisGameRepository;
import e106.emissary_backend.domain.room.dto.RoomOptionDto;
import e106.emissary_backend.domain.room.dto.RoomRequestDto;
import e106.emissary_backend.domain.room.entity.Room;
import e106.emissary_backend.domain.room.dto.RoomListDto;
import e106.emissary_backend.domain.room.enumType.RoomState;
import e106.emissary_backend.domain.room.repository.RoomRepository;
import e106.emissary_backend.domain.user.entity.User;
import e106.emissary_backend.domain.user.repository.UserRepository;
import e106.emissary_backend.domain.userInRoom.entity.UserInRoom;
import e106.emissary_backend.domain.userInRoom.repoistory.UserInRoomRepository;
import e106.emissary_backend.global.common.CommonResponseDto;
import e106.emissary_backend.global.error.CommonErrorCode;
import e106.emissary_backend.global.error.exception.GameFullException;
import e106.emissary_backend.global.error.exception.NotFoundGameException;
import e106.emissary_backend.global.error.exception.NotFoundRoomException;
import e106.emissary_backend.global.error.exception.NotFoundUserException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserInRoomRepository userInRoomRepository;
    private final UserRepository userRepository;
    private final RedisGameRepository redisGameRepository;
    private final RedisTemplate<Long, GameDTO> redisGameTemplate;

    public List<RoomListDto> getRooms(Pageable pageable) {
        Slice<Room> roomList = roomRepository.findAllBy(pageable).orElseThrow(()-> new NotFoundRoomException(CommonErrorCode.NOT_FOUND_ROOM_EXCEPTION));
        return roomList.stream().map(room -> RoomListDto.builder()
                        .title(room.getTitle())
                        .ownerName(userRepository.findNicknameByUserId(room.getOwnerId()).orElseThrow(
                                () -> new NotFoundUserException(CommonErrorCode.NOT_FOUND_USER_EXCEPTION)))
                        .maxPlayer(room.getMaxPlayer())
                        .nowPlayer(userInRoomRepository.countPeopleByRoomId(room.getRoomId()))
                        .build())
                .collect(Collectors.toList());
    }

    public RoomOptionDto getOption(Long roomId) {
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(() -> new NotFoundRoomException(CommonErrorCode.NOT_FOUND_ROOM_EXCEPTION));
        return RoomOptionDto.builder()
                .title(room.getTitle())
                .password(room.getPassword())
                .haveBetray(room.isHaveBetray())
                .maxPlayer(room.getMaxPlayer())
                .owner(userRepository.findNicknameByUserId(room.getOwnerId()).orElseThrow(
                        () -> new NotFoundUserException(CommonErrorCode.NOT_FOUND_USER_EXCEPTION)
                ))
                .build();
    }


    public CommonResponseDto updateOption(Long roomId, RoomRequestDto roomRequestDto) {
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(() -> new NotFoundRoomException(CommonErrorCode.NOT_FOUND_ROOM_EXCEPTION));
        room.update(roomRequestDto);
        return new CommonResponseDto("ok");
    }

    public CommonResponseDto deleteRoom(Long roomId) {
        roomRepository.findByRoomId(roomId).orElseThrow(() -> new NotFoundRoomException(CommonErrorCode.NOT_FOUND_ROOM_EXCEPTION));
        roomRepository.deleteById(roomId);
        return new CommonResponseDto("ok");
    }

    public CommonResponseDto deleteUser(Long roomId, Long userId) {
        roomRepository.findByRoomId(roomId).orElseThrow(() -> new NotFoundRoomException(CommonErrorCode.NOT_FOUND_ROOM_EXCEPTION));
        userInRoomRepository.findByPk_UserId(userId).orElseThrow(() -> new NotFoundUserException(CommonErrorCode.NOT_FOUND_USER_EXCEPTION));
        userInRoomRepository.deletePeopleByPk_UserIdAndRoomId(roomId, userId);

        return new CommonResponseDto("ok");
    }


    public RoomOptionDto makeRoom(long userId, RoomRequestDto roomRequestDto) {
        Room room = Room.builder()
                .title(roomRequestDto.getTitle())
                .password(roomRequestDto.getPassword())
                .haveBetray(roomRequestDto.isHaveBetray())
                .maxPlayer(roomRequestDto.getMaxPlayer())
                .ownerId(userId)
                .roomState(RoomState.WAIT)
                .build();
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new NotFoundUserException(CommonErrorCode.NOT_FOUND_USER_EXCEPTION));

        Room savedRoom = roomRepository.save(room);

        UserInRoom userInRoom = UserInRoom.builder()
                .pk(new UserInRoom.Pk(savedRoom.getRoomId(), userId))
                .room(savedRoom)
                .user(user)
                .isBlocked(false)
                .connectTime(LocalDateTime.now())
                .build();

        userInRoomRepository.save(userInRoom);
        // Redis 저장로직
        GameDTO gameDTO = GameDTO.builder()
                .gameId(savedRoom.getRoomId())
                .title(savedRoom.getTitle())
                .ownerName(user.getNickname())
                .maxPlayer(savedRoom.getMaxPlayer())
                .isHaveBetrayer(savedRoom.isHaveBetray())
                .gameState(GameState.WAIT)
                .build();
        Player player = Player.createPlayer(user.getUserId(), user.getNickname());
        gameDTO.addPlayer(player);

        Game game = GameMapper.INSTANCE.toGame(gameDTO);

        redisGameRepository.save(game);

        return RoomOptionDto.of(user.getNickname(), roomRequestDto);
    }// end of makeRoom

    // Todo : 분산 트랜잭션 처리 해줘야함.
    public CommonResponseDto enterRoom(Long roomId, long userId) {
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(() -> new NotFoundRoomException(CommonErrorCode.NOT_FOUND_ROOM_EXCEPTION));
        if(userInRoomRepository.countPeopleByRoomId(roomId) > room.getMaxPlayer()) {
            throw new GameFullException(CommonErrorCode.GAME_FULL_EXCEPTION);
        }

        User user = userRepository.findByUserId(userId).orElseThrow(() -> new NotFoundUserException(CommonErrorCode.NOT_FOUND_USER_EXCEPTION));

        UserInRoom userInRoom = UserInRoom.builder()
                .pk(new UserInRoom.Pk(roomId, userId))
                .room(room)
                .user(user)
                .isBlocked(false)
                .connectTime(LocalDateTime.now())
                .build();

        userInRoomRepository.save(userInRoom);

        // Redis 저장 로직
        Player player = Player.createPlayer(userId, user.getNickname());
        Game game = redisGameRepository.findByGameId(roomId).orElseThrow(
                () -> new NotFoundGameException(CommonErrorCode.NOT_FOUND_GAME_EXCEPTION));

        GameDTO gameDTO = GameMapper.INSTANCE.toGameDTO(game);
        gameDTO.addPlayer(player);

        Game updateGame = GameMapper.INSTANCE.toGame(gameDTO);
        redisGameRepository.save(updateGame);

        return new CommonResponseDto("ok");
    }

    public CommonResponseDto leaveRoom(Long roomId, long userId) {
        roomRepository.deleteById(roomId);
        userInRoomRepository.deletePeopleByPk_UserIdAndRoomId(roomId, userId);

        return new CommonResponseDto("ok");
    }
}
