package com.cv.s2004orgservice.service.implementation;

import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.component.HybridEncryptionComponent;
import com.cv.s2002orgservicepojo.dto.PasswordDto;
import com.cv.s2004orgservice.constant.ORGConstant;
import com.cv.s2004orgservice.repository.PasswordRepository;
import com.cv.s2004orgservice.repository.UserDetailRepository;
import com.cv.s2004orgservice.service.intrface.PasswordService;
import com.cv.s2004orgservice.service.mapper.PasswordMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = ORGConstant.APP_NAVIGATION_API_PASSWORD)
@Transactional(rollbackOn = Exception.class)
public class PasswordServiceImplementation implements PasswordService {

    private final PasswordRepository repository;
    private final PasswordMapper mapper;
    private final ExceptionComponent exceptionComponent;
    private final UserDetailRepository userDetailRepository;
    private final PasswordEncoder passwordEncoder;
    private final HybridEncryptionComponent encryptionComponent;

    @CacheEvict(keyGenerator = "cacheKeyGenerator", allEntries = true)
    public PasswordDto changePassword(PasswordDto dto) throws Exception {
        var entity = mapper.toEntity(dto);
        var userEntity = userDetailRepository.findByUserIdAndStatusTrue(dto.getUserDetailId())
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        if (passwordEncoder.matches(dto.getPassword(), userEntity.getPassword().getHashPassword())) {
            throw exceptionComponent.expose("app.message.failure.same.password", true);
        }
        entity.setId(userEntity.getPassword().getId());
        entity.setHashPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setEncryptedPassword(encryptionComponent.encrypt(dto.getPassword()));
        entity.setUserDetail(userEntity);
        return mapper.toDto(repository.save(entity));
    }


}
