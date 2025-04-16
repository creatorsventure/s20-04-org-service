package com.cv.s2004orgservice.service.implementation;

import com.cv.s0402notifyservicepojo.dto.RecipientDto;
import com.cv.s0402notifyservicepojo.helper.NotifiyHelper;
import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.dto.PaginationDto;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.component.HybridEncryptionComponent;
import com.cv.s10coreservice.service.function.StaticFunction;
import com.cv.s10coreservice.util.StaticUtil;
import com.cv.s2002orgservicepojo.dto.UserDetailDto;
import com.cv.s2002orgservicepojo.entity.Role;
import com.cv.s2002orgservicepojo.entity.UserDetail;
import com.cv.s2004orgservice.constant.ORGConstant;
import com.cv.s2004orgservice.repository.RoleRepository;
import com.cv.s2004orgservice.repository.UserDetailRepository;
import com.cv.s2004orgservice.service.component.KafkaProducer;
import com.cv.s2004orgservice.service.intrface.UserDetailService;
import com.cv.s2004orgservice.service.mapper.UserDetailMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = ORGConstant.APP_NAVIGATION_API_USER_DETAIL)
@Slf4j
@Transactional(rollbackOn = Exception.class)
public class UserDetailServiceImplementation implements UserDetailService {

    private final UserDetailRepository repository;
    private final RoleRepository roleRepository;
    private final UserDetailMapper mapper;
    private final ExceptionComponent exceptionComponent;
    private final KafkaProducer kafkaProducer;
    private final Environment environment;
    private final HybridEncryptionComponent encryptionComponent;

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public UserDetailDto create(UserDetailDto dto) throws Exception {
        var entity = mapper.toEntity(dto);
        entity.setRoleList(roleRepository.findAllByStatusTrueAndIdIn(
                dto.getSelectedRoleIds(),
                Role.class
        ).orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)));
        entity = repository.save(entity);
        kafkaProducer.notify(NotifiyHelper.notifyPasswordReset(
                RecipientDto.builder()
                        .name(entity.getName())
                        .email(entity.getEmail())
                        .mobileNumber(entity.getMobileNumber())
                        .countryCode(entity.getCountryCode())
                        .status(ApplicationConstant.APPLICATION_STATUS_ACTIVE)
                        .build(),
                Locale.ENGLISH,
                environment.getProperty("app.api-gateway.org-service.reset-password-url") + encryptionComponent.encrypt(entity.getId()),
                entity.getId()
        ));
        return mapper.toDto(entity);
    }

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public UserDetailDto update(UserDetailDto dto) throws Exception {
        return mapper.toDto(repository.findById(dto.getId()).map(entity -> {
            BeanUtils.copyProperties(dto, entity);
            entity.setRoleList(dto.getSelectedRoleIds().stream()
                    .map(roleId -> roleRepository.findByIdAndStatusTrue(
                                    roleId,
                                    Role.class)
                            .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)))
                    .collect(Collectors.toList()));
            repository.save(entity);
            return entity;
        }).orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)));
    }

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public Boolean updateStatus(String id, boolean status) throws Exception {
        return repository.findById(id).map(entity -> {
            entity.setStatus(status);
            repository.save(entity);
            return true;
        }).orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
    }

    @Cacheable(keyGenerator = "cacheKeyGenerator")
    @Override
    public UserDetailDto readOne(String id) throws Exception {
        return repository.findByIdAndStatusTrue(id, UserDetail.class)
                .map(entity -> {
                    var dto = mapper.toDto(entity);
                    dto.setSelectedRoleIds(entity.getRoleList().stream().map(Role::getId).toList());
                    return dto;
                })
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
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
        Page<UserDetail> page;
        if (StaticUtil.isSearchRequest(dto.getSearchField(), dto.getSearchValue())) {
            page = repository.findAll(repository.searchSpec(dto.getSearchField(), dto.getSearchValue()), StaticFunction.generatePageRequest.apply(dto));
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
        return repository.findAllByStatusTrue(
                        UserDetail.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true))
                .stream().collect(Collectors.toMap(UserDetail::getId, UserDetail::getName));
    }

    @Override
    public Long getCount() throws Exception {
        return repository.count();
    }

}
