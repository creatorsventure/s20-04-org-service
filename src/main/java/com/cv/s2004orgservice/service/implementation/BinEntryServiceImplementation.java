package com.cv.s2004orgservice.service.implementation;

import com.cv.s10coreservice.dto.PaginationDto;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.function.StaticFunction;
import com.cv.s10coreservice.util.StaticUtil;
import com.cv.s2002orgservicepojo.dto.BinConfigFileUploadDto;
import com.cv.s2002orgservicepojo.dto.BinEntryDto;
import com.cv.s2002orgservicepojo.entity.BinEntry;
import com.cv.s2002orgservicepojo.entity.Scheme;
import com.cv.s2004orgservice.constant.ORGConstant;
import com.cv.s2004orgservice.repository.BinEntryRepository;
import com.cv.s2004orgservice.service.intrface.BinEntryService;
import com.cv.s2004orgservice.service.mapper.BinEntryMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = ORGConstant.APP_NAVIGATION_API_BINCONFIG)
@Transactional(rollbackOn = Exception.class)
public class BinEntryServiceImplementation implements BinEntryService {

    private final BinEntryRepository repository;
    private final BinEntryMapper mapper;
    private final ExceptionComponent exceptionComponent;

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public BinEntryDto create(BinEntryDto dto) throws Exception {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public BinEntryDto update(BinEntryDto dto) throws Exception {
        return mapper.toDto(repository.findById(dto.getId()).map(entity -> {
            BeanUtils.copyProperties(dto, entity);
            repository.save(entity);
            return entity;
        }).orElseThrow(() -> exceptionComponent.expose("app.code.004", true)));
    }


    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public Boolean updateStatus(String id, boolean status) throws Exception {
        return repository.findById(id).map(entity -> {
            entity.setStatus(status);
            repository.save(entity);
            return true;
        }).orElseThrow(() -> exceptionComponent.expose("app.code.004", true));
    }

    @Override
    public BinEntryDto readOne(String id) throws Exception {
        return mapper.toDto(repository.findByIdAndStatusTrue(id, BinEntry.class).orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)));
    }


    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public Boolean delete(String id) throws Exception {
        repository.deleteById(id);
        return true;
    }

    @Cacheable(keyGenerator = "cacheKeyGenerator")
    @Override
    public PaginationDto readAll(PaginationDto dto) throws Exception {
        Page<BinEntry> page;
        if (StaticUtil.isSearchRequest(dto.getSearchField(), dto.getSearchValue())) {
            page = repository.findAll(repository.searchSpec(dto.getSearchField(), dto.getSearchValue()), StaticFunction.generatePageRequest.apply(dto));
        } else {
            page = repository.findAll(StaticFunction.generatePageRequest.apply(dto));
        }
        dto.setTotal(page.getTotalElements());
        dto.setResult(page.stream().map(mapper::toDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    public Map<String, String> readIdAndNameMap() throws Exception {
        return Map.of();
    }


    public List<BinEntryDto> addBinRange(BinEntryDto binEntryDTO) {
        try {
            List<BinEntryDto> binEntryDtoList = new ArrayList<>();
            List<BinEntry> binEntryList = new ArrayList<>();
            if (binEntryDTO.getBinRangeFrom().equalsIgnoreCase(binEntryDTO.getBinRangeTo())) {
                binEntryDtoList.add(create(binEntryDTO));
                return binEntryDtoList;
            } else {
                //This code is removed onec scheme select taken from deepak
                Scheme scheme = new Scheme();
                int binFrom = Integer.parseInt(binEntryDTO.getBinRangeFrom());
                int binTo = Integer.parseInt(binEntryDTO.getBinRangeTo());
                scheme.setId("3243243223432");
                scheme.setName("Visa");
                scheme.setSchemeCode("VISA0");

                binEntryList = IntStream.rangeClosed(binFrom, binTo)
                        .mapToObj(binNumber -> {
                            BinEntry entry = new BinEntry();
                            entry.setName(binEntryDTO.getName());
                            entry.setBinRangeFrom(binNumber + "");
                            entry.setBinRangeTo(binNumber + "");
                            entry.setInstrument(binEntryDTO.getInstrument());
                            entry.setScheme(scheme);
                            entry.setStatus(true);
                            return entry;
                        })
                        .collect(Collectors.toList());

            }
            binEntryList = insertAllBin(binEntryList);
            binEntryDtoList=null;

            return binEntryList.stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {


        }
    }

    //Insert all bin
    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    public List<BinEntry> insertAllBin(List<BinEntry> binEntryList) {
        try {
            List<BinEntry> entryList = repository.saveAll(binEntryList);
            return entryList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    //Read file and insert binconfig and bin entry table

    public List<BinEntryDto> fileHandlingBinConfig(BinConfigFileUploadDto binConfigFileUploadDto) {
        List<BinEntry> binEntryList;
        binEntryList = parseCSVtoBinConfigDtoList(binConfigFileUploadDto);
        return insertAllBin(binEntryList).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    //Parse CSV to List of DTO

    public List<BinEntry> parseCSVtoBinConfigDtoList(BinConfigFileUploadDto dto) {
        List<BinEntry> binEntryList = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("Bin_Upload.csv");

        Path path = Path.of(dto.getBinFilePath());
        //This code will be replace once scheme select code taken
        Scheme scheme = new Scheme();
        scheme.setId("3243243223432");
        scheme.setName("Visa");
        scheme.setSchemeCode("VISA0");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // skip header
                }
                String[] parts = line.split(",", -1); // -1 includes trailing empty values
                if (parts.length >= 2) {
                    String bin = parts[0].trim();
                    String instrument = parts[1].trim();

                    BinEntry binEntry = new BinEntry();
                    binEntry.setName(dto.getName());
                    binEntry.setBinRangeFrom(bin);
                    binEntry.setBinRangeTo(bin);
                    binEntry.setInstrument(instrument);
                    binEntry.setScheme(scheme);
                    binEntryList.add(binEntry);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV", e);
        }finally {
            resource=null;
            scheme=null;
        }

        return binEntryList;
    }

}
