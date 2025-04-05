package com.cv.s2004orgservice.service.intrface;

import com.cv.s10coreservice.service.intrface.generic.GenericService;
import com.cv.s2002orgservicepojo.dto.RoleDto;
import com.cv.s2002orgservicepojo.dto.SideNaveDto;

import java.util.List;

public interface RoleService extends GenericService<RoleDto> {

    List<SideNaveDto> loadRoleMenu(List<String> roleIds) throws Exception;
}
