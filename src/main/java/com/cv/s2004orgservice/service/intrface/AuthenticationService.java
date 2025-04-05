package com.cv.s2004orgservice.service.intrface;


import com.cv.s10coreservice.dto.AuthInfoDto;

public interface AuthenticationService {

    AuthInfoDto login(AuthInfoDto dto) throws Exception;

    AuthInfoDto refreshToken(AuthInfoDto dto) throws Exception;

    boolean logout(AuthInfoDto dto) throws Exception;
}
