package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.RoleDto;
import com.cv.s2002orgservicepojo.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper extends GenericMapper<RoleDto, Role> {
}
