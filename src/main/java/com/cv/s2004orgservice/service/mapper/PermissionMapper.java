package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.PermissionDto;
import com.cv.s2002orgservicepojo.entity.Permission;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface PermissionMapper extends GenericMapper<PermissionDto, Permission> {
}
