package kr.hs.dgsw.cns.schoolmealbacksetup.controller.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.auth.service.AuthDetailsService;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.entity.MenuRequest;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.entity.Vote;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.entity.VoteId;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.presentation.MenuController;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.presentation.dto.request.MenuCreationDto;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.presentation.dto.request.MenuStateDto;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.presentation.dto.response.MenuDto;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.presentation.dto.response.PlannerDto;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.repository.MenuRequestRepository;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.service.MenuService;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.type.MenuCategory;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.menu.type.MenuState;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.entity.AuthId;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.entity.User;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.repository.UserRepository;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.type.UserRole;
import kr.hs.dgsw.cns.schoolmealbacksetup.global.infra.neis.MealPlannerInfra;
import kr.hs.dgsw.cns.schoolmealbacksetup.global.security.JwtConfiguration;
import kr.hs.dgsw.cns.schoolmealbacksetup.global.security.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableAutoConfiguration
@EnableConfigurationProperties(value = JwtConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@ExtendWith(MockitoExtension.class)
@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean(name = "MenuServiceImpl")
    private MenuService menuService;
    
    @SpyBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserRepository userRepository;

    @SpyBean
    private AuthDetailsService authDetailsService;

    @Mock
    private MenuRequestRepository menuRequestRepository;
    
    private final LocalDateTime dateTime = LocalDateTime.now();

    private User user() {
        return user(UserRole.USER);
    }

    private User user(UserRole userRole) {
        return User.builder()
                .id(1L)
                .name("tester")
                .role(userRole)
                .openId("open_id")
                .build();
    }

    private MenuRequest toEntity(MenuCreationDto menuCreationDto) {
        return toEntity(
                menuCreationDto.getMenuName(),
                menuCreationDto.getDescription(),
                menuCreationDto.getKind(),
                new HashSet<>()
        );
    }

    private MenuRequest toEntity(String menuName,
                                 String description,
                                 MenuCategory menuCategory,
                                 Set<Vote> votes) {
        return MenuRequest.builder()
                .id(1L)
                .createAt(dateTime)
                .user(user())
                .menuName(menuName)
                .content(description)
                .menuCategory(menuCategory)
                .state(MenuState.STANDBY)
                .votes(votes)
                .build();
    }

    private String token() {
        return String.format("Bearer %s", jwtProvider.generateAccessToken("1"));
    }

    private List<String> list(String... args) {
        return List.of(args);
    }

    private Vote vote(MenuRequest menuRequest, User user) {
        return new Vote(new VoteId(new AuthId(user)), menuRequest);
    }

    @DisplayName("메뉴 추가 성공")
    @RepeatedTest(10)
    void addMenuSuccess() throws Exception {
        // given
        MenuCreationDto menuCreationDto = new MenuCreationDto(MenuCategory.KOREAN, "김밥", "참치 김밥");
        lenient().when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user()));
        lenient().when(menuService.addMenu(any(), any()))
                .thenReturn(new MenuDto(toEntity(menuCreationDto), user()));
        String content = objectMapper.writeValueAsString(menuCreationDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/menu")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token())
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.menu_name", "김밥").exists())
                .andExpect(jsonPath("$.description", "참치 김밥").exists())
                .andExpect(jsonPath("$.kind", MenuCategory.KOREAN).exists());
    }

    @DisplayName("메뉴 추가 실패")
    @Test
    void addMenuFailed() throws Exception {
        // given
        MenuCreationDto menuCreationDto = new MenuCreationDto(MenuCategory.KOREAN, "김밥", "참치 김밥");
        lenient().when(menuService.addMenu(any(), any()))
                .thenReturn(new MenuDto(toEntity(menuCreationDto), user()));
        String content = objectMapper.writeValueAsString(menuCreationDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .header("Authorization", token())
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("id로 메뉴 조회 성공")
    @Test
    void findByIdSuccess() throws Exception {
        // given
        MenuRequest menuRequest = toEntity(
                "우동",
                "맛있는 우동",
                MenuCategory.JAPANESE,
                new HashSet<>()
        );
        MenuDto menuDto = new MenuDto(menuRequest, user());
        lenient().when(menuService.findById(anyLong()))
                .thenReturn(menuDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/menu/1")
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menu_name", "우동").exists())
                .andExpect(jsonPath("$.description", "맛있는 우동").exists())
                .andExpect(jsonPath("$.kind", MenuCategory.JAPANESE).exists());
    }

    @DisplayName("메뉴 투표 성공")
    @Test
    void addVoteSuccess() throws Exception {
        // given
        MenuRequest menuRequest = toEntity(
                "쇠고기야채죽",
                "간 좀 맞춰주세요",
                MenuCategory.KOREAN,
                new HashSet<>()
        );
        menuRequestRepository.save(menuRequest);

        lenient().when(menuRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(menuRequest));
        lenient().when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user()));

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/menu/1/votes")
                        .header("Authorization", token())
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @DisplayName("메뉴 투표 실패")
    @Test
    void addVoteFailed() throws Exception {
        // given
        MenuRequest menuRequest = toEntity(
                "쇠고기야채죽",
                "간 좀 맞춰주세요",
                MenuCategory.KOREAN,
                new HashSet<>()
        );
        menuRequestRepository.save(menuRequest);
        lenient().when(menuRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(menuRequest));

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/menu/1/votes")
                        .header("Authorization", token())
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("중복된 메뉴 투표")
    @Test
    void voteOverlap() throws Exception {
        // given
        User user = user();
        MenuRequest menuRequest = toEntity(
                "쇠고기야채죽",
                "간 좀 맞춰주세요",
                MenuCategory.KOREAN,
                new HashSet<>()
        );
        Vote vote = vote(menuRequest, user);
        menuRequest.addVote(vote);

        lenient().when(menuRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(menuRequest));
        lenient().when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        lenient().doThrow(new Vote.AlreadyVoted())
                .when(menuService)
                .addVote(eq(user), anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/menu/1/votes")
                        .header("Authorization", token())
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @DisplayName("STANDBY 가 아닌 메뉴에 투표")
    @Test
    void voteAtNotStandby() throws Exception {
        MenuRequest notStandByMenu = toEntity(
                "맛있는 참치 뀨우",
                "",
                MenuCategory.KOREAN,
                new HashSet<>()
        );
        notStandByMenu.setMenuState(MenuState.ALLOWED);

        lenient().when(menuRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(notStandByMenu));
        lenient().when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user()));
        lenient().doThrow(new Vote.NotVotable())
                .when(menuService)
                .addVote(any(User.class), anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/menu/1/votes")
                        .header("Authorization", token())
                        .accept(MediaType.APPLICATION_JSON)
        );

        resultActions
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("메뉴 투표 취소 성공")
    @Test
    void cancelVoteSuccess() throws Exception {
        // given
        User user = user();
        MenuRequest menuRequest = toEntity(
                "차돌박이된장찌개",
                "차돌박이를 넣어 진한 된장찌개",
                MenuCategory.KOREAN,
                new HashSet<>()
        );
        Vote vote = vote(menuRequest, user);
        menuRequest.addVote(vote);

        lenient().when(menuRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(menuRequest));
        lenient().when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        lenient().doNothing()
                .when(menuService)
                .cancelVote(eq(user), anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/menu/1/votes")
                        .header("Authorization", token())
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @DisplayName("메뉴 투표 취소 실패")
    @Test
    void cancelVoteFailed() throws Exception {
        // given
        User user = user();

        lenient().when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        lenient().doThrow(new Vote.NeverVoted())
                .when(menuService)
                .cancelVote(eq(user), anyLong());

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/menu/1/votes")
                        .header("Authorization", token())
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @DisplayName("메뉴 상태 지정 성공")
    @Test
    void updateMenuStateSuccess() throws Exception {
        // given
        User user = user(UserRole.ADMIN);
        MenuRequest menuRequest = toEntity(
                "가지나물무침",
                ".",
                MenuCategory.KOREAN,
                new HashSet<>()
        );
        menuRequest.setMenuState(MenuState.DENIED);
        MenuDto menuDto = new MenuDto(menuRequest, user());

        lenient().when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        lenient().doReturn(menuDto)
                .when(menuService)
                .updateState(any(), anyLong(), any());
        String content = objectMapper.writeValueAsString(new MenuStateDto(false));

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/menu/1/state")
                        .content(content)
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.menuState", MenuState.DENIED).exists());
    }

    @DisplayName("메뉴 상태 지정 실패")
    @Test
    void updateMenuStateFailed() throws Exception {
        // given
        User user = user(UserRole.USER);
        MenuRequest menuRequest = toEntity(
                "가지나물무침",
                ".",
                MenuCategory.KOREAN,
                new HashSet<>()
        );
        menuRequest.setMenuState(MenuState.ALLOWED);
        MenuDto menuDto = new MenuDto(menuRequest, user());

        lenient().when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        lenient().doReturn(menuDto)
                .when(menuService)
                .updateState(any(), anyLong(), any());
        String content = objectMapper.writeValueAsString(new MenuStateDto(false));

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/menu/1/state")
                        .content(content)
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("식단표 조회")
    @Test
    void getMealPlanner() throws Exception {
        // given
        int year = 2022, month = 6, day = 16;
        String date = String.format("%02d%02d%02d", year, month, day);
        List<MealPlannerInfra.MealItem> mealItems = List.of(
                new MealPlannerInfra.MealItem(date, "조식", list("*기장밥", "새알심만두국", "숙주나물무침")),
                new MealPlannerInfra.MealItem(date, "중식", list("*기장밥", "단배추된장국", "오향장육")),
                new MealPlannerInfra.MealItem(date, "석식", list("매콤치킨마요덮밥", "미소된장국", "두부양념구이"))
        );
        lenient().when(menuService.getMenuPlanner(year, month, day)).thenReturn(new PlannerDto(mealItems));

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/menu/planner")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .param("day", String.valueOf(day))
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk());
    }

}
