package com.cv.s2004orgservice.service.intrface;


import com.cv.s2002orgservicepojo.dto.PasswordDto;

public interface PasswordService {

    PasswordDto changePassword(PasswordDto dto) throws Exception;

}
