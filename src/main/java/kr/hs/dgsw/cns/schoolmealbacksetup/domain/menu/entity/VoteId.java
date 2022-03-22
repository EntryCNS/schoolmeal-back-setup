package kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.entity;

import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor @NoArgsConstructor
@Getter
public class VoteId implements Serializable {
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "menu_id")
    private MenuRequest menu;
}
