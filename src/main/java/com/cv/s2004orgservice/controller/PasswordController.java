package com.cv.s2004orgservice.controller;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.controller.generic.GenericController;
import com.cv.s10coreservice.dto.PaginationDto;
import com.cv.s10coreservice.enumeration.APIResponseType;
import com.cv.s2002orgservicepojo.dto.PasswordDto;
import com.cv.s2004orgservice.constant.UAMConstant;
import com.cv.s2004orgservice.service.intrface.PasswordService;
import com.cv.s2004orgservice.util.StaticUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UAMConstant.APP_NAVIGATION_API_PASSWORD)
@AllArgsConstructor
@Slf4j
public class PasswordController implements GenericController<PasswordDto> {

    private PasswordService service;

    @PostMapping
    @Override
    public ResponseEntity<Object> create(@RequestBody @Valid PasswordDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("PasswordController.create {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }
            return StaticUtil.getSuccessResponse(service.create(dto), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("PasswordController.create {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @PutMapping
    @Override
    public ResponseEntity<Object> update(@RequestBody @Valid PasswordDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("PasswordController.update {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }
            return StaticUtil.getSuccessResponse(service.update(dto), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("PasswordController.update {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping(ApplicationConstant.APP_NAVIGATION_METHOD_UPDATE_STATUS)
    @Override
    public ResponseEntity<Object> updateStatus(@RequestParam String id, @RequestParam boolean status) {
        try {
            return StaticUtil.getSuccessResponse(service.updateStatus(id, status), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("PasswordController.updateStatus {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }


    @GetMapping
    @Override
    public ResponseEntity<Object> readOne(@RequestParam String id) {
        try {
            log.info("PasswordController.readOne {}", id);
            return StaticUtil.getSuccessResponse(service.readOne(id), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("PasswordController.readOne {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @PostMapping(ApplicationConstant.APP_NAVIGATION_METHOD_READ_PAGE)
    @Override
    public ResponseEntity<Object> readPage(@RequestBody PaginationDto dto) {
        try {
            return StaticUtil.getSuccessResponse(service.readAll(dto), APIResponseType.OBJECT_LIST);
        } catch (Exception e) {
            log.error("PasswordController.readPage {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping(ApplicationConstant.APP_NAVIGATION_METHOD_READ_ID_NAME_MAP)
    @Override
    public ResponseEntity<Object> readIdNameMapping() {
        try {
            return StaticUtil.getSuccessResponse(service.readIdAndNameMap(), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("PasswordController.readIdNameMapping {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @DeleteMapping
    @Override
    public ResponseEntity<Object> delete(@RequestParam String id) {
        try {
            return StaticUtil.getSuccessResponse(service.delete(id), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("PasswordController.delete {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

}
