package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.UnitDto;
import com.cv.s2002orgservicepojo.entity.Unit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "Spring")
public interface UnitMapper extends GenericMapper<UnitDto, Unit> {
}
