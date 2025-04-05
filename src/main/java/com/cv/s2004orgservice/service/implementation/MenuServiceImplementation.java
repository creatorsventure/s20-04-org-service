package com.cv.s2004orgservice.service.implementation;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.dto.PaginationDto;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.function.StaticFunction;
import com.cv.s10coreservice.util.StaticUtil;
import com.cv.s2002orgservicepojo.dto.MenuDto;
import com.cv.s2002orgservicepojo.dto.MenuTreeDto;
import com.cv.s2002orgservicepojo.entity.Menu;
import com.cv.s2004orgservice.constant.ORGConstant;
import com.cv.s2004orgservice.repository.MenuRepository;
import com.cv.s2004orgservice.service.intrface.MenuService;
import com.cv.s2004orgservice.service.mapper.MenuMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = ORGConstant.APP_NAVIGATION_API_MENU)
@Transactional(rollbackOn = Exception.class)
public class MenuServiceImplementation implements MenuService {

    private final MenuRepository repository;
    private final MenuMapper mapper;
    private final ExceptionComponent exceptionComponent;

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public MenuDto create(MenuDto dto) throws Exception {
        Menu entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public MenuDto update(MenuDto dto) throws Exception {
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
    public MenuDto readOne(String id) throws Exception {
        return mapper.toDto(repository.findByIdAndStatus(
                        id, ApplicationConstant.APPLICATION_STATUS_ACTIVE, Menu.class)
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
        Page<Menu> page;
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
                        Menu.class)
                .orElseThrow(() -> exceptionComponent.expose("app.code.004", true))
                .stream().collect(Collectors.toMap(Menu::getId, Menu::getName));
    }


    @Override
    public List<MenuTreeDto> readMenuAsTree() throws Exception {
        List<Menu> parents = repository.findAllByMenuTypeAndStatus(
                ORGConstant.MENU_TYPE_PARENT, ApplicationConstant.APPLICATION_STATUS_ACTIVE);

        List<Menu> children = repository.findAllByMenuTypeAndStatus(
                ORGConstant.MENU_TYPE_CHILD, ApplicationConstant.APPLICATION_STATUS_ACTIVE);

        // Group child menus by rootMenuId for faster lookup
        Map<String, List<Menu>> childrenByRootMenuId = children.stream()
                .collect(Collectors.groupingBy(Menu::getRootMenuId));

        return parents.stream()
                .map(parent -> MenuTreeDto.builder()
                        .key(parent.getId())
                        .title(parent.getPath())
                        .expanded(true)
                        .children(
                                childrenByRootMenuId.getOrDefault(parent.getId(), Collections.emptyList())
                                        .stream()
                                        .map(child -> MenuTreeDto.builder()
                                                .key(child.getId())
                                                .title(child.getPath())
                                                .isLeaf(true)
                                                .build())
                                        .collect(Collectors.toList())
                        )
                        .build())
                .collect(Collectors.toList());
    }
}
