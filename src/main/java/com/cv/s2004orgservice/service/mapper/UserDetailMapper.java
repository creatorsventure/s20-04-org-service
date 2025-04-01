package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.UserDetailDto;
import com.cv.s2002orgservicepojo.entity.UserDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDetailMapper extends GenericMapper<UserDetailDto, UserDetail> {
}
