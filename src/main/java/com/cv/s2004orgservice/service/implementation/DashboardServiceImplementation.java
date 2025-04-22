package com.cv.s2004orgservice.service.implementation;

import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.component.RepositoryRegistry;
import com.cv.s2002orgservicepojo.dto.CountDto;
import com.cv.s2002orgservicepojo.dto.DashboardDto;
import com.cv.s2002orgservicepojo.entity.Menu;
import com.cv.s2004orgservice.constant.ORGConstant;
import com.cv.s2004orgservice.repository.MenuRepository;
import com.cv.s2004orgservice.service.intrface.DashboardService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = ORGConstant.APP_NAVIGATION_API_DASHBOARD)
@Transactional(rollbackOn = Exception.class)
public class DashboardServiceImplementation implements DashboardService {

    private final MenuRepository menuRepository;
    private final RepositoryRegistry repositoryRegistry;
    private final ExceptionComponent exceptionComponent;

    @Cacheable(keyGenerator = "cacheKeyGenerator")
    @Override
    public DashboardDto getCount() {
        return DashboardDto.builder()
                .countDtoList(menuRepository.findAllByStatusTrue(Menu.class)
                        .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true))
                        .stream()
                        .filter(Menu::isDashboardCountCard)
                        .map(menu -> {
                            long count = repositoryRegistry.getByCode(menu.getDescription())
                                    .map(CrudRepository::count)
                                    .orElse(0L);
                            return CountDto.builder()
                                    .title(menu.getName())
                                    .icon(menu.getIcon())
                                    .count(count)
                                    .build();
                        }).collect(Collectors.toList())).build();
    }
}
