package kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.presentation;

import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.entity.VoteId;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.presentation.dto.request.MenuCreationDto;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.presentation.dto.response.MenuDto;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.presentation.dto.response.MenuListDto;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.service.MenuService;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.entity.AuthId;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/menu")
@RestController
public class MenuController {

    @Resource(name = "MenuServiceImpl")
    private final MenuService menuService;

    @GetMapping
    public MenuListDto findAllMenu(@RequestParam(defaultValue = "1") long page) {
        return menuService.findAllMenus(page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuDto addMenu(@RequestBody @Valid MenuCreationDto menuCreationDto,
                           Authentication authentication) {
        return menuService.addMenu((User) authentication.getPrincipal(), menuCreationDto);
    }

    @GetMapping("/{menu-id}")
    public MenuDto findById(@PathVariable(name = "menu-id") long menuId) {
        return menuService.findById(menuId);
    }

    @PostMapping("/{menu-id}/votes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addVote(@PathVariable(name = "menu-id") long menuId,
                        Authentication authentication) {
        VoteId voteId = new VoteId(
                new AuthId((User) authentication.getPrincipal())
        );
        menuService.addVote(voteId, menuId);
    }

    @DeleteMapping("/{menu-id}/votes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVote(@PathVariable(name = "menu-id") long menuId,
                           Authentication authentication) {
        VoteId voteId = new VoteId(
                new AuthId((User) authentication.getPrincipal())
        );
        menuService.cancelVote(voteId, menuId);
    }

}
