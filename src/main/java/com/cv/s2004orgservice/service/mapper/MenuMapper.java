package com.cv.s2004orgservice.service.mapper;

import com.cv.s2002orgservicepojo.dto.MenuDto;
import com.cv.s2002orgservicepojo.entity.Menu;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    // @Mapping(source = "menu.module.id", target = "moduleId")
    MenuDto toDto(Menu menu);

    Menu toEntity(MenuDto menuDto);

}
