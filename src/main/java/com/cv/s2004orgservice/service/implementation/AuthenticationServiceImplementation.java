package com.cv.s2004orgservice.service.implementation;

import com.cv.s10coreservice.config.props.CoreSecurityProperties;
import com.cv.s10coreservice.dto.AuthInfoDto;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.cv.s10coreservice.service.component.JWTComponent;
import com.cv.s10coreservice.service.component.Sha256HashComponent;
import com.cv.s2002orgservicepojo.entity.Organization;
import com.cv.s2002orgservicepojo.entity.Role;
import com.cv.s2002orgservicepojo.entity.Token;
import com.cv.s2002orgservicepojo.entity.UserDetail;
import com.cv.s2004orgservice.repository.TokenRepository;
import com.cv.s2004orgservice.repository.UserDetailRepository;
import com.cv.s2004orgservice.service.intrface.AuthenticationService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(rollbackOn = Exception.class)
public class AuthenticationServiceImplementation implements AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final Sha256HashComponent sha256HashComponent;
    private final UserDetailRepository userDetailRepository;
    private final ExceptionComponent exceptionComponent;
    private final JWTComponent jwtComponent;
    private final CoreSecurityProperties coreSecurityProperties;
    private final TokenRepository tokenRepository;

    @Override
    public AuthInfoDto login(AuthInfoDto dto) throws Exception {
        var userEntity = userDetailRepository.findByUserIdIgnoreCaseAndStatusTrue(dto.getUserId())
                .filter(entity -> passwordEncoder.matches(dto.getPassword(), entity.getPassword().getHashPassword()))
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.credential.invalid", true));
        userEntity.setLastLogin(LocalDateTime.now());
        userEntity = userDetailRepository.save(userEntity);
        return createAuthInfoDto(userEntity);
    }

    @Override
    public AuthInfoDto refreshToken(AuthInfoDto dto) throws Exception {
        if (!jwtComponent.isTokenValid(dto.getRefreshToken())) {
            throw exceptionComponent.expose("app.message.failure.object.unavailable", true);
        }
        var storedToken = tokenRepository.findByTokenHashAndRevokedFalse(
                        sha256HashComponent.hash(dto.getRefreshToken()))
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw exceptionComponent.expose("app.message.failure.token.expired", true);
        }
        // Revoke old token (rotation)
        storedToken.setRevoked(true);
        tokenRepository.save(storedToken);
        var userEntity = userDetailRepository.findByUserIdIgnoreCaseAndStatusTrue(dto.getUserId())
                .orElseThrow(() -> exceptionComponent.expose("app.message.failure.object.unavailable", true));
        return createAuthInfoDto(userEntity);
    }

    @Override
    public boolean logout(AuthInfoDto dto) throws Exception {
        tokenRepository.findByTokenHashAndRevokedFalse(
                        sha256HashComponent.hash(dto.getRefreshToken()))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    tokenRepository.save(token);
                });
        return true;
    }

    private AuthInfoDto createAuthInfoDto(UserDetail userDetail) throws Exception {
        return AuthInfoDto.builder()
                .userId(userDetail.getUserId())
                .name(userDetail.getName())
                .email(userDetail.getEmail())
                .roleIds(userDetail.getRoleList().stream().map(Role::getId).collect(Collectors.toList()))
                .organizationIds(
                        userDetail.getRoleList().stream()
                                .flatMap(role -> role.getOrganizationList().stream()
                                        .map(Organization::getId))
                                .collect(Collectors.toList()))
                .token(createAccessToken(userDetail))
                .refreshToken(createAndStoreRefreshToken(userDetail))
                .build();
    }

    private String createAccessToken(UserDetail userDetail) throws Exception {
        return jwtComponent.generateAccessToken(
                userDetail.getUserId(),
                Map.ofEntries(
                        Map.entry("id", userDetail.getId()),
                        Map.entry("name", userDetail.getName())));
    }

    private String createAndStoreRefreshToken(UserDetail userDetail) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(coreSecurityProperties.getJWTRefreshTokenExpirationMins());
        String rawToken = jwtComponent.generateRefreshToken(userDetail.getUserId());
        String tokenHash = sha256HashComponent.hash(rawToken);
        tokenRepository.findByUserIdAndRevokedFalse(userDetail.getUserId()).ifPresent(tokenRepository::delete);
        tokenRepository.save(Token.builder()
                .userId(userDetail.getUserId())
                .issuedAt(now)
                .expiresAt(expiry)
                .revoked(false)
                .tokenHash(tokenHash)
                .userDetail(userDetail)
                .build());
        return rawToken;
    }

}
