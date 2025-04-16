package com.cv.s2004orgservice.service.mapper;

import com.cv.s10coreservice.service.mapper.generic.GenericMapper;
import com.cv.s2002orgservicepojo.dto.BinEntryDto;
import com.cv.s2002orgservicepojo.entity.BinEntry;
import com.cv.s2002orgservicepojo.entity.Scheme;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BinEntryMapper extends GenericMapper<BinEntryDto, BinEntry> {

    BinEntryDto toDto(BinEntry binEntry);

    BinEntry toEntity(BinEntryDto binEntryDTO);
    default Scheme map(String scheme) {
        if (scheme == null) return null;

        Scheme schemeobj = new Scheme();
        schemeobj.setId(scheme);
        return schemeobj;
    }
}
