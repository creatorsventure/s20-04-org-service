package com.cv.s2004orgservice.controller;

import com.cv.s10coreservice.enumeration.APIResponseType;
import com.cv.s2002orgservicepojo.dto.PasswordDto;
import com.cv.s2004orgservice.constant.ORGConstant;
import com.cv.s2004orgservice.service.intrface.PasswordService;
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
@RequestMapping(ORGConstant.APP_NAVIGATION_API_PASSWORD)
@AllArgsConstructor
@Slf4j
public class PasswordController {

    private PasswordService service;

    @PostMapping
    public ResponseEntity<Object> changePassword(@RequestBody @Valid PasswordDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("PasswordController.changePassword {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }
            return StaticUtil.getSuccessResponse(service.changePassword(dto), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("PasswordController.changePassword {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

}
