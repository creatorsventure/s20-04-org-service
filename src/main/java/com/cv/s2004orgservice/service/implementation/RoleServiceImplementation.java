package com.cv.s2004orgservice.service.implementation;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.dto.PaginationDto;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.function.StaticFunction;
import com.cv.s10coreservice.util.StaticUtil;
import com.cv.s2002orgservicepojo.dto.RoleDto;
import com.cv.s2002orgservicepojo.entity.Menu;
import com.cv.s2002orgservicepojo.entity.Organization;
import com.cv.s2002orgservicepojo.entity.Permission;
import com.cv.s2002orgservicepojo.entity.Role;
import com.cv.s2004orgservice.constant.UAMConstant;
import com.cv.s2004orgservice.repository.MenuRepository;
import com.cv.s2004orgservice.repository.OrganizationRepository;
import com.cv.s2004orgservice.repository.PermissionRepository;
import com.cv.s2004orgservice.repository.RoleRepository;
import com.cv.s2004orgservice.service.intrface.RoleService;
import com.cv.s2004orgservice.service.mapper.RoleMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = UAMConstant.APP_NAVIGATION_API_ROLE)
@Transactional(rollbackOn = Exception.class)
public class RoleServiceImplementation implements RoleService {
    private final RoleRepository repository;
    private final RoleMapper mapper;
    private final ExceptionComponent exceptionComponent;

    private final OrganizationRepository organizationRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public RoleDto create(RoleDto dto) throws Exception {
        var entity = mapper.toEntity(dto);
        createRoleEntity(dto, entity);
        return mapper.toDto(repository.save(entity));
    }

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    @Override
    public RoleDto update(RoleDto dto) throws Exception {
        return mapper.toDto(repository.findById(dto.getId()).map(entity -> {
            BeanUtils.copyProperties(dto, entity);
            createRoleEntity(dto, entity);
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
    public RoleDto readOne(String id) throws Exception {
        return repository.findByIdAndStatus(id, ApplicationConstant.APPLICATION_STATUS_ACTIVE, Role.class)
                .map(entity -> {
                    RoleDto dto = mapper.toDto(entity);
                    dto.setSelectedOrganizationIds(entity.getOrganizationList().stream().map(Organization::getId).toList());
                    dto.setSelectedPermissionIds(entity.getPermissionList().stream().map(Permission::getId).toList());
                    dto.setSelectedMenuIds(entity.getMenuList().stream().map(Menu::getId).toList());
                    return dto;
                })
                .orElseThrow(() -> exceptionComponent.expose("app.code.004", true));
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
        Page<Role> page;
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
        return repository.findAllByStatus(
                        ApplicationConstant.APPLICATION_STATUS_ACTIVE,
                        Role.class
                )
                .orElseThrow(() -> exceptionComponent.expose("app.code.004", true))
                .stream().collect(Collectors.toMap(Role::getId, Role::getName));
    }

    private void createRoleEntity(RoleDto dto, Role entity) {
        // Set Organization
        var organizations = organizationRepository.findAllByStatusAndIdIn(
                ApplicationConstant.APPLICATION_STATUS_ACTIVE,
                dto.getSelectedOrganizationIds(),
                Organization.class
        ).orElseThrow(() -> exceptionComponent.expose("app.code.004", true));
        entity.setOrganizationList(organizations);

        // Set Permissions
        var permissions = permissionRepository.findAllByStatusAndIdIn(
                ApplicationConstant.APPLICATION_STATUS_ACTIVE,
                dto.getSelectedPermissionIds()
        ).orElseThrow(() -> exceptionComponent.expose("app.code.004", true));
        entity.setPermissionList(permissions);

        // Process and Set Menus
        List<Menu> menuList = new ArrayList<>();
        for (String menuId : dto.getSelectedMenuIds()) {
            var menu = menuRepository.findByIdAndStatus(menuId, ApplicationConstant.APPLICATION_STATUS_ACTIVE, Menu.class)
                    .orElseThrow(() -> exceptionComponent.expose("app.code.004", true));

            menuList.add(menu);

            // If it's a parent menu, fetch and add its children
            if (UAMConstant.MENU_TYPE_PARENT == menu.getMenuType()) {
                var children = menuRepository.findAllByRootMenuIdAndMenuTypeAndStatus(
                        menu.getId(),
                        UAMConstant.MENU_TYPE_CHILD,
                        ApplicationConstant.APPLICATION_STATUS_ACTIVE
                ).orElseThrow(() -> exceptionComponent.expose("app.code.004", true));

                menuList.addAll(children);
            }
        }
        entity.setMenuList(menuList);

    }
}
