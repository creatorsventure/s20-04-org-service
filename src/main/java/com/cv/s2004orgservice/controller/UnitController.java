package com.cv.s2004orgservice.controller;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.controller.generic.GenericController;
import com.cv.s10coreservice.dto.PaginationDto;
import com.cv.s10coreservice.enumeration.APIResponseType;
import com.cv.s2002orgservicepojo.constant.ORGConstant;
import com.cv.s2002orgservicepojo.dto.UnitDto;
import com.cv.s2004orgservice.service.intrface.UnitService;
import com.cv.s2004orgservice.util.StaticUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ORGConstant.APP_NAVIGATION_API_UNIT)
@AllArgsConstructor
@Slf4j
public class UnitController implements GenericController<UnitDto> {

    private UnitService service;

    @PostMapping
    @Override
    public ResponseEntity<Object> create(@RequestBody @Valid UnitDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("UnitController.create {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }
            return StaticUtil.getSuccessResponse(service.create(dto), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("UnitController.create {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @PutMapping
    @Override
    public ResponseEntity<Object> update(@RequestBody @Valid UnitDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("UnitController.update {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }
            return StaticUtil.getSuccessResponse(service.update(dto), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("UnitController.update {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping(ApplicationConstant.APP_NAVIGATION_METHOD_UPDATE_STATUS)
    @Override
    public ResponseEntity<Object> updateStatus(@RequestParam String id, @RequestParam boolean status) {
        try {
            return StaticUtil.getSuccessResponse(service.updateStatus(id, status), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("UnitController.updateStatus {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping
    @Override
    public ResponseEntity<Object> readOne(@RequestParam String id) {
        try {
            log.info("UnitController.readOne {}", id);
            return StaticUtil.getSuccessResponse(service.readOne(id), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("UnitController.readOne {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @PostMapping(ApplicationConstant.APP_NAVIGATION_METHOD_READ_PAGE)
    @Override
    public ResponseEntity<Object> readPage(@RequestBody PaginationDto dto) {
        try {
            return StaticUtil.getSuccessResponse(service.readAll(dto), APIResponseType.OBJECT_LIST);
        } catch (Exception e) {
            log.error("UnitController.readPage {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping(ApplicationConstant.APP_NAVIGATION_METHOD_READ_ID_NAME_MAP)
    @Override
    public ResponseEntity<Object> readIdNameMapping() {
        try {
            return StaticUtil.getSuccessResponse(service.readIdAndNameMap(), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("UnitController.readIdNameMapping {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @DeleteMapping
    @Override
    public ResponseEntity<Object> delete(@RequestParam String id) {
        try {
            return StaticUtil.getSuccessResponse(service.delete(id), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("UnitController.delete {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping(ORGConstant.APP_NAVIGATION_API_UNIT_SIGNUP)
    public ResponseEntity<Object> signUp(@RequestParam String id) {
        try {
            return StaticUtil.getSuccessResponse(service.signup(id), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("UnitController.signUp {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping(ORGConstant.APP_NAVIGATION_API_UNIT_RESOLVE_ID)
    public ResponseEntity<Object> resolveUnitId(@RequestParam String code) {
        try {
            log.info("UnitController.resolveUnitId {}", code);
            return StaticUtil.getSuccessResponse(service.resolveUnitId(code), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("UnitController.resolveUnitId {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping
    public ResponseEntity<Object> resolveUnitOption(@RequestParam String id) {
        try {
            log.info("UnitController.resolveUnitOption {}", id);
            return StaticUtil.getSuccessResponse(service.resolveUnitOptions(id), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("UnitController.resolveUnitOption {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

}
