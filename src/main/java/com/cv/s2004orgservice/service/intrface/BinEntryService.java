package com.cv.s2004orgservice.service.intrface;

import com.cv.s10coreservice.service.intrface.generic.GenericService;
import com.cv.s2002orgservicepojo.dto.BinConfigFileUploadDto;
import com.cv.s2002orgservicepojo.dto.BinEntryDto;
import com.cv.s2002orgservicepojo.entity.BinEntry;

import java.util.List;


public interface BinEntryService extends GenericService<BinEntryDto> {

    public List<BinEntryDto> addBinRange(BinEntryDto binEntryDTO);
    public List<BinEntryDto> fileHandlingBinConfig(BinConfigFileUploadDto binConfigFileUploadDto);

}
