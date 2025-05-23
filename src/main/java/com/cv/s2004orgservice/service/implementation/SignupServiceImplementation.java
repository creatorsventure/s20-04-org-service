package com.cv.s2004orgservice.service.implementation;

import com.cv.core.s09coresecurity.component.HybridEncryptionComponent;
import com.cv.s0402notifyservicepojo.dto.RecipientDto;
import com.cv.s0402notifyservicepojo.helper.NotifyHelper;
import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s2002orgservicepojo.dto.SignupDto;
import com.cv.s2002orgservicepojo.entity.*;
import com.cv.s2004orgservice.repository.*;
import com.cv.s2004orgservice.service.component.KafkaProducer;
import com.cv.s2004orgservice.service.intrface.SignupService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(rollbackOn = Exception.class)
public class SignupServiceImplementation implements SignupService {

    private final OrganizationRepository organizationRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PasswordRepository passwordRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailRepository userDetailRepository;
    private final ExceptionComponent exceptionComponent;
    private final HybridEncryptionComponent encryptionComponent;
    private final KafkaProducer kafkaProducer;
    private final Environment environment;


    @Override
    public boolean signup(SignupDto signupDto) throws Exception {
        if (userDetailRepository.count() == 0) {
            var organizationEntity = Organization.builder()
                    .name(signupDto.getOrganizationName())
                    .description(signupDto.getOrganizationName())
                    .organizationCode(signupDto.getOrganizationCode())
                    .status(ApplicationConstant.APPLICATION_STATUS_ACTIVE)
                    .build();
            organizationEntity = organizationRepository.save(organizationEntity);

            var roleEntity = Role.builder()
                    .name(signupDto.getOrganizationCode() + "_role")
                    .description(signupDto.getOrganizationCode() + "_role")
                    .status(ApplicationConstant.APPLICATION_STATUS_ACTIVE)
                    .organization(organizationEntity)
                    .permissionList(permissionRepository.findAllByStatusTrue(
                            Permission.class
                    ).orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)))
                    .menuList(menuRepository.findAllByStatusTrue(
                            Menu.class
                    ).orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true)))
                    .build();
            roleEntity = roleRepository.save(roleEntity);

            var userDetail = userDetailRepository.save(
                    UserDetail.builder()
                            .name(signupDto.getName())
                            .userId(signupDto.getUserId())
                            .countryCode(signupDto.getCountryCode())
                            .mobileNumber(signupDto.getMobileNumber())
                            .email(signupDto.getEmail())
                            .status(ApplicationConstant.APPLICATION_STATUS_INACTIVE)
                            .role(roleEntity)
                            .build());

            // log.info("Hash Password {}", passwordEncoder.encode(signupDto.getPassword()));
            // log.info("Encrypted Password {}", encryptionComponent.encrypt(signupDto.getPassword()));
            passwordRepository.save(Password.builder()
                    .name(signupDto.getName())
                    .encryptedPassword(encryptionComponent.encrypt(signupDto.getPassword()))
                    .hashPassword(passwordEncoder.encode(signupDto.getPassword()))
                    .userDetail(userDetail)
                    .build());
            kafkaProducer.notify(NotifyHelper.notifyActivateAccount(
                    RecipientDto.builder()
                            .name(signupDto.getName())
                            .email(signupDto.getEmail())
                            .mobileNumber(signupDto.getMobileNumber())
                            .countryCode(signupDto.getCountryCode())
                            .status(ApplicationConstant.APPLICATION_STATUS_ACTIVE)
                            .build(),
                    Locale.ENGLISH,
                    environment.getProperty("app.org-service.activate-account-url") + encryptionComponent.encrypt(userDetail.getId()),
                    userDetail.getId()
            ));
            return true;
        } else {
            throw new Exception(exceptionComponent.expose("app.message.failure.user.exists", true));
        }
    }

}
