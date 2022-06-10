package kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.entity;

import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.type.UserRole;
import kr.hs.dgsw.cns.schoolmealbacksetup.global.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor @NoArgsConstructor
@Builder
@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Length(max = 14)
    private String name;

    @NotNull
    @ColumnDefault(value = "'USER'")
    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    @NotNull
    private String openId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.name()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static class FailedToAuthException extends BusinessException {
        public FailedToAuthException() {
            super(HttpStatus.UNAUTHORIZED, "OAuth 인증에 실패했습니다");
        }
    }

    public static class InvalidSchoolException extends BusinessException {
        public InvalidSchoolException() { super(HttpStatus.UNAUTHORIZED, "dgsw.hs.kr 이메일을 사용하는 유저만 접근할 수 있습니다"); }
    }

    public static class UnauthorizedException extends BusinessException {
        public UnauthorizedException() { super(HttpStatus.UNAUTHORIZED, "인증 실패"); }
    }
}
