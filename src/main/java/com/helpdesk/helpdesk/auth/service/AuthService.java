package com.helpdesk.helpdesk.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.helpdesk.helpdesk.auth.dto.RegisterRequest;
import com.helpdesk.helpdesk.user.entity.User;
import com.helpdesk.helpdesk.user.enums.UserRole;
import com.helpdesk.helpdesk.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.helpdesk.helpdesk.auth.dto.RegisterResponse;
import com.helpdesk.helpdesk.security.service.JwtService;
import com.helpdesk.helpdesk.auth.dto.LoginResponse;
import com.helpdesk.helpdesk.security.user.HelpDeskUserDetails;
import com.helpdesk.helpdesk.refreshToken.service.RefreshTokenService;
import com.helpdesk.helpdesk.auth.dto.RefreshResponse;
import com.helpdesk.helpdesk.refreshToken.entity.RefreshToken;
import com.helpdesk.helpdesk.auth.dto.RefreshResponse;
import com.helpdesk.helpdesk.refreshToken.service.RefreshTokenRotationResult;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public Authentication authenticate(String email, String password) {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(email, password);

        return authenticationManager.authenticate(authentication);
    }

    public LoginResponse login(String email, String password) {

        Authentication authentication = authenticate(email, password);

        HelpDeskUserDetails userDetails =
                (HelpDeskUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(
                userDetails.getId(),
                userDetails.getRole()
        );

        String refreshToken = refreshTokenService.createRefreshToken(
                userDetails.getId()
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds(),
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getUsername(),
                userDetails.getRole(),
                userDetails.getCreatedAt()
        );
    }

    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {

        String email = registerRequest.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();

        user.setName(registerRequest.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
    }

    @Transactional
    public RefreshResponse refresh(String token) {
        RefreshTokenRotationResult rotationResult =
                refreshTokenService.rotateRefreshToken(token);

        String newAccessToken = jwtService.generateAccessToken(
                rotationResult.userId(),
                rotationResult.role()
        );

        return new RefreshResponse(
                newAccessToken,
                rotationResult.refreshToken(),
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds()
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    @Transactional
    public int logoutFromAllDevices(String refreshToken) {
        return refreshTokenService.logoutFromAllDevices(refreshToken);
    }

}