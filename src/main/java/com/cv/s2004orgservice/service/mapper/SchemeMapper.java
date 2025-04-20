package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.SchemeDto;
import com.cv.s2002orgservicepojo.entity.Scheme;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SchemeMapper extends GenericMapper<SchemeDto, Scheme> {

}
