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
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = ApplicationConstant.APP_NAVIGATION_API_PASSWORD)
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
    private final PasswordRepository passwordRepository;

    public PasswordDto changePassword(PasswordDto dto) throws Exception {
        var userEntity = userDetailRepository
                .findByUserIdIgnoreCaseAndStatusTrue(dto.getUserDetailId())
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        var passwordEntity = userEntity.getPassword();
        if (passwordEntity == null) {
            throw exceptionComponent.expose("app.message.failure.password.not.found", true);
        }
        constructPasswordEntity(dto, passwordEntity, userEntity);
        return mapper.toDto(repository.save(passwordEntity));
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
        var entity = userDetailRepository.findByUserIdIgnoreCaseAndStatusTrue(userId)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        entity.getPassword().setModifiedAt(LocalDateTime.now());
        entity.getPassword().setStatus(ApplicationConstant.APPLICATION_STATUS_INACTIVE);
        userDetailRepository.save(entity);
        return sendPasswordResetEmail(entity);
    }

    @Override
    public boolean resetPassword(PasswordDto dto) throws Exception {
        var actualId = encryptionComponent.decrypt(dto.getUserDetailId());
        var userEntity = userDetailRepository.findByIdAndStatusTrue(actualId, UserDetail.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        var passwordEntity = userEntity.getPassword();
        if (passwordEntity.isStatus() || passwordEntity.getModifiedAt().plusHours(1).isBefore(LocalDateTime.now())) {
            throw exceptionComponent.expose("app.message.failure.link.expired", true);
        }
        constructPasswordEntity(dto, passwordEntity, userEntity);
        repository.save(passwordEntity);
        return true;
    }


    @Override
    public boolean resendPasswordEmail(String id) throws Exception {
        var entity = userDetailRepository.findByIdAndStatusTrue(id, UserDetail.class)
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        if (Optional.ofNullable(entity.getPassword()).isPresent()) {
            entity.getPassword().setStatus(ApplicationConstant.APPLICATION_STATUS_INACTIVE);
            entity.setModifiedAt(LocalDateTime.now());
            userDetailRepository.save(entity);
        } else {
            String tempPassword = UUID.randomUUID().toString();
            passwordRepository.save(Password.builder()
                    .name(entity.getName())
                    .modifiedAt(LocalDateTime.now())
                    .status(ApplicationConstant.APPLICATION_STATUS_INACTIVE)
                    .encryptedPassword(encryptionComponent.encrypt(tempPassword))
                    .hashPassword(passwordEncoder.encode(tempPassword))
                    .userDetail(entity)
                    .build());
        }

        return sendPasswordResetEmail(entity);
    }

    private boolean sendPasswordResetEmail(UserDetail entity) throws Exception {
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

    private void constructPasswordEntity(PasswordDto dto, Password passwordEntity, UserDetail userEntity) throws Exception {
        if (passwordEncoder.matches(dto.getPassword(), passwordEntity.getHashPassword())) {
            throw exceptionComponent.expose("app.message.failure.same.password", true);
        }

        passwordEntity.setName(userEntity.getName());
        passwordEntity.setHashPassword(passwordEncoder.encode(dto.getPassword()));
        passwordEntity.setEncryptedPassword(encryptionComponent.encrypt(dto.getPassword()));
        passwordEntity.setStatus(ApplicationConstant.APPLICATION_STATUS_ACTIVE);
        passwordEntity.setModifiedAt(LocalDateTime.now());
        passwordEntity.setModifiedBy(userEntity.getName());
    }


}
