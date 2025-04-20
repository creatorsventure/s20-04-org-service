package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.MerchantCategoryDto;
import com.cv.s2002orgservicepojo.entity.MerchantCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MerchantCategoryMapper extends GenericMapper<MerchantCategoryDto, MerchantCategory> {
}
