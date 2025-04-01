package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.PasswordDto;
import com.cv.s2002orgservicepojo.entity.Password;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PasswordMapper extends GenericMapper<PasswordDto, Password> {
}
