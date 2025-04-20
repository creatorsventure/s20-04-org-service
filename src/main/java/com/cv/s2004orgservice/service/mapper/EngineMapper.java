package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.EngineDto;
import com.cv.s2002orgservicepojo.entity.Engine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EngineMapper extends GenericMapper<EngineDto, Engine> {
}
