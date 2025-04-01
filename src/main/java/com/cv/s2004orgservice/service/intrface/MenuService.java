package com.cv.s2004orgservice.service.intrface;


import com.cv.s10coreservice.service.intrface.generic.GenericService;
import com.cv.s2002orgservicepojo.dto.MenuDto;
import com.cv.s2002orgservicepojo.dto.MenuTreeDto;

import java.util.List;

public interface MenuService extends GenericService<MenuDto> {

    List<MenuTreeDto> readMenuAsTree() throws Exception;
}
