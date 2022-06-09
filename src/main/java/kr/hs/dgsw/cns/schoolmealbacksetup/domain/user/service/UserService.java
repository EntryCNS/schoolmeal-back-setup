package kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.service;

import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.presentation.dto.request.SignUpRequest;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.presentation.dto.response.SignUpResponse;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.presentation.dto.response.UserImageAcceptedDto;
import kr.hs.dgsw.cns.schoolmealbacksetup.domain.user.presentation.dto.response.UserResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends UserDetailsService {

    byte[] getUserImage(long userId);

    UserImageAcceptedDto setUserImage(long userId, MultipartFile profileImage);

    void resetUserImage(long userId);

    UserResponseDto getUser(Long userId);

}
