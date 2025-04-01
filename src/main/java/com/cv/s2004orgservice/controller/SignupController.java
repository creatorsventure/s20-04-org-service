package com.cv.s2004orgservice.controller;

import com.cv.s10coreservice.enumeration.APIResponseType;
import com.cv.s2002orgservicepojo.dto.SignupDto;
import com.cv.s2004orgservice.constant.UAMConstant;
import com.cv.s2004orgservice.service.intrface.SignupService;
import com.cv.s2004orgservice.util.StaticUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UAMConstant.APP_NAVIGATION_API_SIGNUP)
@AllArgsConstructor
@Slf4j
public class SignupController {

    private SignupService service;

    @PostMapping
    public ResponseEntity<Object> signup(@RequestBody @Valid SignupDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("SignupController.signup {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }
            return StaticUtil.getSuccessResponse(service.signup(dto), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("SignupController.signup {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }


}
