package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.BankUnitDto;
import com.cv.s2002orgservicepojo.entity.BankUnit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "Spring")
public interface BankUnitMapper extends GenericMapper<BankUnitDto, BankUnit> {
}
