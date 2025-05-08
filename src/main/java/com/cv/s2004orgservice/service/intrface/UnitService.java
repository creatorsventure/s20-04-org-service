package com.cv.s2004orgservice.service.intrface;

import com.cv.s10coreservice.dto.ContextParamDto;
import com.cv.s10coreservice.dto.IdNameMapDto;
import com.cv.s10coreservice.service.intrface.generic.GenericService;
import com.cv.s2002orgservicepojo.dto.OptionsDto;
import com.cv.s2002orgservicepojo.dto.UnitDto;

public interface UnitService extends GenericService<UnitDto> {

    boolean signup(String id) throws Exception;

    ContextParamDto resolveUnitId(String code) throws Exception;

    OptionsDto resolveUnitOptions(String unitId) throws Exception;

    IdNameMapDto resolveUnitIdNameMaps(String unitId) throws Exception;
}
