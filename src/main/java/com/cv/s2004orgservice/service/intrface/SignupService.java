package com.cv.s2004orgservice.service.intrface;


import com.cv.s2002orgservicepojo.dto.SignupDto;

public interface SignupService {

    boolean signup(SignupDto signupDto) throws Exception;

}
