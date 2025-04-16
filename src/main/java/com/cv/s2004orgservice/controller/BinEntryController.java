package com.cv.s2004orgservice.controller;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.controller.generic.GenericController;
import com.cv.s10coreservice.dto.PaginationDto;
import com.cv.s10coreservice.enumeration.APIResponseType;
import com.cv.s2002orgservicepojo.dto.BinConfigFileUploadDto;
import com.cv.s2002orgservicepojo.dto.BinEntryDto;
import com.cv.s2004orgservice.constant.ORGConstant;
import com.cv.s2004orgservice.service.intrface.BinEntryService;
import com.cv.s2004orgservice.util.StaticUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ORGConstant.APP_NAVIGATION_API_BINCONFIG)
@AllArgsConstructor
@Slf4j
public class BinEntryController implements GenericController<BinEntryDto> {
    private BinEntryService service;

    @PostMapping
    @Override
    public ResponseEntity<Object> create(@RequestBody @Valid BinEntryDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("BinconfigController.create {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }
            return StaticUtil.getSuccessResponse(service.create(dto), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("BinconfigController.create {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @PutMapping
    @Override
    public ResponseEntity<Object> update(@RequestBody @Valid BinEntryDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("BinconfigController.update {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }
            return StaticUtil.getSuccessResponse(service.update(dto), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("BinconfigController.update {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }


    @GetMapping(ApplicationConstant.APP_NAVIGATION_METHOD_UPDATE_STATUS)
    @Override
    public ResponseEntity<Object> updateStatus(@RequestParam String id, @RequestParam boolean status) {
        try {
            return StaticUtil.getSuccessResponse(service.updateStatus(id, status), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("BinconfigController.updateStatus {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping
    @Override
    public ResponseEntity<Object> readOne(@RequestParam String id) {
        try {
            log.info("BinconfigController.readOne {}", id);
            return StaticUtil.getSuccessResponse(service.readOne(id), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("BinconfigController.readOne {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @PostMapping(ApplicationConstant.APP_NAVIGATION_METHOD_READ_PAGE)
    @Override
    public ResponseEntity<Object> readPage(@RequestBody PaginationDto dto) {
        try {
            return StaticUtil.getSuccessResponse(service.readAll(dto), APIResponseType.OBJECT_LIST);
        } catch (Exception e) {
            log.error("BinconfigController.readPage {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @GetMapping(ApplicationConstant.APP_NAVIGATION_METHOD_READ_ID_NAME_MAP)
    @Override
    public ResponseEntity<Object> readIdNameMapping() {
        try {
            return StaticUtil.getSuccessResponse(service.readIdAndNameMap(), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("BinconfigController.readIdNameMapping {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    @DeleteMapping
    @Override
    public ResponseEntity<Object> delete(@RequestParam String id) {
        try {
            return StaticUtil.getSuccessResponse(service.delete(id), APIResponseType.OBJECT_ONE);
        } catch (Exception e) {
            log.error("BinconfigController.delete {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

    //Add BinRange addtion handing
    @PostMapping(ORGConstant.APP_NAVIGATION_API_BINCONFIG_INSERT_ALL)
    public ResponseEntity<Object> addBinRange(@RequestBody @Valid BinEntryDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("BinconfigController.create {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }

            return StaticUtil.getSuccessResponse(service.addBinRange(dto), APIResponseType.OBJECT_ONE);

        } catch (Exception e) {
            log.error("BinconfigController.create {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }


    //Add all file upload BinConfig handing
    @PostMapping(ORGConstant.APP_NAVIGATION_API_BINCONFIG_FILE_PROCESS)
    public ResponseEntity<Object> binFileUpload(@RequestBody @Valid BinConfigFileUploadDto dto, BindingResult result) {
        try {
            if (result.hasErrors()) {
                log.info("BinconfigController.create {}", result.getAllErrors());
                return StaticUtil.getFailureResponse(result);
            }


            return StaticUtil.getSuccessResponse(service.fileHandlingBinConfig(dto), APIResponseType.OBJECT_ONE);

        } catch (Exception e) {
            log.error("BinconfigController.create {}", ExceptionUtils.getStackTrace(e));
            return StaticUtil.getFailureResponse(e);
        }
    }

}
