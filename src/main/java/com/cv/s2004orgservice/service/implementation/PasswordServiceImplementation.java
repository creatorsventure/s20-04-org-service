package com.cv.s2004orgservice.service.implementation;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.dto.PaginationDto;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.function.StaticFunction;
import com.cv.s10coreservice.util.StaticUtil;
import com.cv.s2002orgservicepojo.dto.PasswordDto;
import com.cv.s2002orgservicepojo.entity.Password;
import com.cv.s2004orgservice.constant.ORGConstant;
import com.cv.s2004orgservice.repository.PasswordRepository;
import com.cv.s2004orgservice.service.intrface.PasswordService;
import com.cv.s2004orgservice.service.mapper.PasswordMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = ORGConstant.APP_NAVIGATION_API_PASSWORD)
@Transactional(rollbackOn = Exception.class)
public class PasswordServiceImplementation implements PasswordService {

    private final PasswordRepository repository;
    private final PasswordMapper mapper;
    private final ExceptionComponent exceptionComponent;

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public PasswordDto create(PasswordDto dto) throws Exception {
        return mapper.toDto(repository.save(mapper.toEntity(dto)));
    }

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public PasswordDto update(PasswordDto dto) throws Exception {
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

    @Cacheable(keyGenerator = "cacheKeyGenerator")
    @Override
    public PasswordDto readOne(String id) throws Exception {
        return mapper.toDto(repository.findByIdAndStatus(id, ApplicationConstant.APPLICATION_STATUS_ACTIVE, Password.class)
                .orElseThrow(() -> exceptionComponent.expose("app.code.004", true)));
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
        Page<Password> page;
        if (StaticUtil.isSearchRequest(dto.getSearchField(), dto.getSearchValue())) {
            page = repository.findAll(
                    repository.searchSpec(dto.getSearchField(), dto.getSearchValue()),
                    StaticFunction.generatePageRequest.apply(dto));
        } else {
            page = repository.findAll(StaticFunction.generatePageRequest.apply(dto));
        }
        dto.setTotal(page.getTotalElements());
        dto.setResult(page.stream().map(mapper::toDto).collect(Collectors.toList()));
        return dto;
    }

    @Cacheable(keyGenerator = "cacheKeyGenerator")
    @Override
    public Map<String, String> readIdAndNameMap() throws Exception {
        return repository.findAllByStatus(
                        ApplicationConstant.APPLICATION_STATUS_ACTIVE,
                        Password.class)
                .orElseThrow(() -> exceptionComponent.expose("app.code.004", true))
                .stream().collect(Collectors.toMap(Password::getId, Password::getName));
    }


}
