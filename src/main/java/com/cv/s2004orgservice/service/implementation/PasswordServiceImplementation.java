package com.cv.s2004orgservice.service.implementation;

import com.cv.s0402notifyservicepojo.dto.RecipientDto;
import com.cv.s0402notifyservicepojo.helper.NotifiyHelper;
import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.component.HybridEncryptionComponent;
import com.cv.s2002orgservicepojo.dto.PasswordDto;
import com.cv.s2002orgservicepojo.entity.Password;
import com.cv.s2002orgservicepojo.entity.UserDetail;
import com.cv.s2004orgservice.constant.ORGConstant;
import com.cv.s2004orgservice.repository.PasswordRepository;
import com.cv.s2004orgservice.repository.UserDetailRepository;
import com.cv.s2004orgservice.service.component.KafkaProducer;
import com.cv.s2004orgservice.service.intrface.PasswordService;
import com.cv.s2004orgservice.service.mapper.PasswordMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

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
    private final KafkaProducer kafkaProducer;
    private final Environment environment;

    public PasswordDto changePassword(PasswordDto dto) throws Exception {
        var entity = mapper.toEntity(dto);
        var userEntity = userDetailRepository.findByUserIdAndStatusTrue(dto.getUserDetailId())
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        constructEntity(dto, entity, userEntity);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public boolean activateAccount(String id) throws Exception {
        var actualId = encryptionComponent.decrypt(id);
        var entity = userDetailRepository.findById(actualId)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        if (entity.isStatus()) {
            throw exceptionComponent.expose("app.message.failure.user.already.activated", true);
        } else if (entity.getCreatedAt().plusHours(1).isBefore(LocalDateTime.now())) {
            throw exceptionComponent.expose("app.message.failure.link.expired", true);
        } else {
            entity.setStatus(ApplicationConstant.APPLICATION_STATUS_ACTIVE);
            userDetailRepository.save(entity);
            return true;
        }
    }

    @Override
    public boolean forgotPassword(String userId) throws Exception {
        var entity = userDetailRepository.findByUserIdAndStatusTrue(userId)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        entity.getPassword().setModifiedAt(LocalDateTime.now());
        entity.getPassword().setStatus(ApplicationConstant.APPLICATION_STATUS_INACTIVE);
        userDetailRepository.save(entity);
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
        return true;
    }

    @Override
    public boolean resetPassword(PasswordDto dto) throws Exception {
        var actualId = encryptionComponent.decrypt(dto.getUserDetailId());
        var entity = mapper.toEntity(dto);
        var userEntity = userDetailRepository.findByIdAndStatusTrue(actualId, UserDetail.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        if (userEntity.getPassword().isStatus() || userEntity.getPassword().getModifiedAt().plusHours(1).isBefore(LocalDateTime.now())) {
            throw exceptionComponent.expose("app.message.failure.link.expired", true);
        } else {
            constructEntity(dto, entity, userEntity);
            repository.save(entity);
            return true;
        }
    }

    private void constructEntity(PasswordDto dto, Password entity, UserDetail userEntity) throws Exception {
        if (passwordEncoder.matches(dto.getPassword(), userEntity.getPassword().getHashPassword())) {
            throw exceptionComponent.expose("app.message.failure.same.password", true);
        }
        entity.setId(userEntity.getPassword().getId());
        entity.setName(userEntity.getName());
        entity.setHashPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setEncryptedPassword(encryptionComponent.encrypt(dto.getPassword()));
        entity.setStatus(ApplicationConstant.APPLICATION_STATUS_ACTIVE);
        entity.setUserDetail(userEntity);
    }

}
