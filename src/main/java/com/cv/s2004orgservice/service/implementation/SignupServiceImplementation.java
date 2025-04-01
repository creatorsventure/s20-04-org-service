package com.cv.s2004orgservice.service.implementation;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s2002orgservicepojo.dto.SignupDto;
import com.cv.s2002orgservicepojo.entity.*;
import com.cv.s2004orgservice.repository.*;
import com.cv.s2004orgservice.service.intrface.SignupService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(rollbackOn = Exception.class)
public class SignupServiceImplementation implements SignupService {

    private final OrganizationRepository organizationRepository;
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PasswordRepository passwordRepository;
    private final UserDetailRepository userDetailRepository;
    private final ExceptionComponent exceptionComponent;


    @Override
    public boolean signup(SignupDto signupDto) throws Exception {
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
                .organizationList(List.of(organizationEntity))
                .permissionList(permissionRepository.findAllByStatus(
                        ApplicationConstant.APPLICATION_STATUS_ACTIVE,
                        Permission.class
                ).orElseThrow(() -> exceptionComponent.expose("app.code.004", true)))
                .menuList(menuRepository.findAllByStatus(
                        ApplicationConstant.APPLICATION_STATUS_ACTIVE,
                        Menu.class
                ).orElseThrow(() -> exceptionComponent.expose("app.code.004", true)))
                .build();
        roleEntity = roleRepository.save(roleEntity);

        var passwordEntity = Password.builder()
                .name(signupDto.getName())
                .encryptedPassword("")
                .hashPassword("")
                .build();
        passwordEntity = passwordRepository.save(passwordEntity);

        userDetailRepository.save(
                UserDetail.builder()
                        .name(signupDto.getName())
                        .userId(signupDto.getUserId())
                        .countryCode(signupDto.getCountryCode())
                        .mobileNumber(signupDto.getMobileNumber())
                        .email(signupDto.getEmail())
                        .status(ApplicationConstant.APPLICATION_STATUS_ACTIVE)
                        .roleList(List.of(roleEntity))
                        .password(passwordEntity)
                        .build());
        return true;
    }
}
