package com.cv.s2004orgservice.repository;

import com.cv.s10coreservice.repository.generic.GenericRepository;
import com.cv.s10coreservice.repository.generic.GenericSpecification;
import com.cv.s2002orgservicepojo.entity.MerchantCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantCategoryRepository extends GenericRepository, GenericSpecification<MerchantCategory>,
        JpaRepository<MerchantCategory, String>, JpaSpecificationExecutor<MerchantCategory> {
}
