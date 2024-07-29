package e106.emissary_backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
@EqualsAndHashCode(callSuper=false)
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", unique = true, nullable = false, length = 50)
    private String email;

    @Column(name = "nickname", unique = false, nullable = false, length = 200)
    private String nickname;

    @Column(name = "password", /*nullable = false,*/ length = 200)
    private String password;

    @Column(name = "skin_img_url", length = 30)
    private String skinImgUrl; // img url 추가

    @Builder.Default
    @Column(name = "mafia_win_cnt")
    private Long mafiaWinCnt = 0L;

    @Builder.Default
    @Column(name = "mafia_play_cnt")
    private Long mafiaPlayCn = 0L;

    @Builder.Default
    @Column(name = "police_win_cnt")
    private Long policeWinCnt = 0L;

    @Builder.Default
    @Column(name = "police_play_cnt")
    private Long policePlayCnt = 0L;

    @Builder.Default
    @Column(name = "turncoat_game_cnt")
    private Long turncoatGameCnt = 0L;

    @Builder.Default
    @Column(name = "turncoat_win_cnt")
    private Long turncoatWinCnt = 0L;

    @Builder.Default
    @Column(name = "citizen_game_cnt")
    private Long citizenGameCnt = 0L;

    @Builder.Default
    @Column(name = "citizen_win_cnt")
    private Long citizenWinCnt = 0L;

    @Builder.Default
    @Column(name = "role", nullable = false, length = 45)
    private String role = "ROLE_USER";

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

}