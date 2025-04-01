package com.cv.s2004orgservice.repository;

import com.cv.s10coreservice.repository.generic.GenericRepository;
import com.cv.s10coreservice.repository.generic.GenericSpecification;
import com.cv.s2002orgservicepojo.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends GenericRepository, GenericSpecification<Menu>,
        JpaRepository<Menu, String>, JpaSpecificationExecutor<Menu> {

    List<Menu> findAllByMenuTypeAndStatus(Integer menuType, Boolean status);

    Optional<List<Menu>> findAllByRootMenuIdAndMenuTypeAndStatus(String rootMenuId, Integer menuType, Boolean status);
}
