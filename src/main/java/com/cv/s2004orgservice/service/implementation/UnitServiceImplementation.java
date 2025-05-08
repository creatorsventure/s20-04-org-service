package com.cv.s2004orgservice.service.implementation;

import com.cv.s0402notifyservicepojo.dto.RecipientDto;
import com.cv.s0402notifyservicepojo.helper.NotifyHelper;
import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.dto.ContextParamDto;
import com.cv.s10coreservice.dto.IdNameMapDto;
import com.cv.s10coreservice.dto.PaginationDto;
import com.cv.s10coreservice.dto.VerifySignupDto;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.component.HybridEncryptionComponent;
import com.cv.s10coreservice.service.component.JsonComponent;
import com.cv.s10coreservice.service.function.StaticFunction;
import com.cv.s10coreservice.util.StaticUtil;
import com.cv.s2002orgservicepojo.constant.ORGConstant;
import com.cv.s2002orgservicepojo.dto.OptionsDto;
import com.cv.s2002orgservicepojo.dto.UnitDto;
import com.cv.s2002orgservicepojo.entity.*;
import com.cv.s2004orgservice.repository.*;
import com.cv.s2004orgservice.service.component.KafkaProducer;
import com.cv.s2004orgservice.service.intrface.UnitService;
import com.cv.s2004orgservice.service.mapper.OptionsMapper;
import com.cv.s2004orgservice.service.mapper.UnitMapper;
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

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = ORGConstant.APP_NAVIGATION_API_UNIT)
@Slf4j
@Transactional(rollbackOn = Exception.class)
public class UnitServiceImplementation implements UnitService {

    private final UnitRepository repository;
    private final UnitMapper mapper;
    private final ActionRepository actionRepository;
    private final CurrencyRepository currencyRepository;
    private final EngineRepository engineRepository;
    private final SchemeRepository schemeRepository;
    private final OptionsRepository optionsRepository;
    private final OptionsMapper optionsMapper;
    private final ExceptionComponent exceptionComponent;
    private final KafkaProducer kafkaProducer;
    private final Environment environment;
    private final HybridEncryptionComponent encryptionComponent;
    private final JsonComponent jsonComponent;

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public UnitDto create(UnitDto dto) throws Exception {
        var entity = mapper.toEntity(dto);
        constructUnitEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public UnitDto update(UnitDto dto) throws Exception {
        return mapper.toDto(repository.findById(dto.getId()).map(entity -> {
            BeanUtils.copyProperties(dto, entity);
            try {
                constructUnitEntity(dto, entity);
            } catch (Exception e) {
                log.error("Error while updating unit", e);
                throw new RuntimeException(e);
            }
            repository.save(entity);
            return entity;
        }).orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)));
    }

    private void constructUnitEntity(UnitDto dto, Unit entity) throws Exception {
        entity.setActionList(actionRepository.findAllByStatusTrueAndIdIn(dto.getSelectedActionIds(), Action.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)));
        entity.setCurrencyList(currencyRepository.findAllByStatusTrueAndIdIn(dto.getSelectedCurrencyIds(), Currency.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)));
        entity.setEngineList(engineRepository.findAllByStatusTrueAndIdIn(dto.getSelectedEngineIds(), Engine.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)));
        entity.setSchemeList(schemeRepository.findAllByStatusTrueAndIdIn(dto.getSelectedSchemeIds(), Scheme.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)));
        entity.setOptions(optionsRepository.findByIdAndStatusTrue(dto.getSelectedOptionsId(), Options.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)));
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
    public UnitDto readOne(String id) throws Exception {
        return repository.findByIdAndStatusTrue(id, Unit.class)
                .map(entity -> {
                    var dto = mapper.toDto(entity);
                    dto.setSelectedActionIds(entity.getActionList().stream().map(Action::getId).collect(Collectors.toList()));
                    dto.setSelectedCurrencyIds(entity.getCurrencyList().stream().map(Currency::getId).collect(Collectors.toList()));
                    dto.setSelectedEngineIds(entity.getEngineList().stream().map(Engine::getId).collect(Collectors.toList()));
                    dto.setSelectedSchemeIds(entity.getSchemeList().stream().map(Scheme::getId).collect(Collectors.toList()));
                    dto.setSelectedOptionsId(entity.getOptions().getId());
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
        Page<Unit> page;
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
                        Unit.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true))
                .stream().collect(Collectors.toMap(Unit::getId, Unit::getName));
    }

    @Override
    public boolean signup(String id) throws Exception {
        var entity = repository.findByIdAndStatusTrue(id, Unit.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));

        kafkaProducer.notify(NotifyHelper.notifyAccountSignUp(
                RecipientDto.builder()
                        .name(entity.getAdminName())
                        .email(entity.getAdminEmail())
                        .status(ApplicationConstant.APPLICATION_STATUS_ACTIVE)
                        .build(),
                Locale.ENGLISH,
                environment.getProperty("app.unit-service.activate-account-url")
                        + entity.getId() + "/"
                        + encryptionComponent.encrypt(
                        jsonComponent.toJson(VerifySignupDto.builder()
                                .adminEmail(entity.getAdminEmail())
                                .adminName(entity.getAdminName())
                                .adminUserId(entity.getAdminUserId())
                                .adminMobileNumber(entity.getAdminMobileNumber())
                                .adminCountryCode(entity.getAdminCountryCode())
                                .entityId(entity.getId())
                                .entityName(entity.getName())
                                .entityCode(entity.getUnitCode())
                                .otpRequired(entity.getOptions().isLoginOTP())
                                .createdAt(LocalDateTime.now())
                                .build())),
                entity.getId()
        ));
        return true;
    }

    @Cacheable(keyGenerator = "cacheKeyGenerator")
    @Override
    public ContextParamDto resolveUnitId(String code) throws Exception {
        return repository.findByUnitCodeIgnoreCaseAndStatusTrue(code)
                .map(unit -> ContextParamDto.builder()
                        .unitId(unit.getId())
                        .build())
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
    }

    @Override
    public OptionsDto resolveUnitOptions(String unitId) throws Exception {
        return repository.findById(unitId)
                .map(unit -> optionsMapper.toDto(unit.getOptions()))
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
    }

    @Override
    public IdNameMapDto resolveUnitIdNameMaps(String unitId) throws Exception {
        return repository.findById(unitId)
                .map(unit -> IdNameMapDto.builder().idNameMaps(Map.of(
                        "currency", unit.getCurrencyList().stream().collect(Collectors.toMap(Currency::getId, Currency::getName)),
                        "action", unit.getActionList().stream().collect(Collectors.toMap(Action::getId, Action::getName)),
                        "scheme", unit.getSchemeList().stream().collect(Collectors.toMap(Scheme::getId, Scheme::getName)),
                        "engine", unit.getEngineList().stream().collect(Collectors.toMap(Engine::getId, Engine::getName))
                )).build())
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
    }
}
