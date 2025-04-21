package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.OptionsDto;
import com.cv.s2002orgservicepojo.entity.Options;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OptionsMapper extends GenericMapper<OptionsDto, Options> {
}
