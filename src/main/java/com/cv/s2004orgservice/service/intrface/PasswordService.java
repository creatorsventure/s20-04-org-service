package com.cv.s2004orgservice.service.intrface;


import com.cv.s2002orgservicepojo.dto.PasswordDto;

public interface PasswordService {

    PasswordDto changePassword(PasswordDto dto) throws Exception;

    boolean activateAccount(String id) throws Exception;

    boolean forgotPassword(String userId) throws Exception;

    boolean resetPassword(PasswordDto dto) throws Exception;

    boolean resendPasswordEmail(String id) throws Exception;

}
