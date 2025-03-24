package com.muje.capstone.config;

import com.muje.capstone.config.jwt.TokenProvider;
import com.muje.capstone.service.TokenService;
import com.muje.capstone.config.oauth.OAuth2SuccessHandler;
import com.muje.capstone.repository.RefreshTokenRepository;
import com.muje.capstone.repository.UserRepository;
import com.muje.capstone.service.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebOAuthSecurityConfig {

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    private final TokenProvider tokenProvider;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailService userDetailService;

    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toH2Console())
                .requestMatchers("/img/**", "/css/**", "/js/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 자체 로그인, 토큰 재발급, OAuth2 관련 엔드포인트는 permit
                        .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage(frontendBaseUrl + "/auth/login")
                        .successHandler(oAuth2SuccessHandler())
                )
                .logout(logout -> logout.logoutSuccessUrl(frontendBaseUrl + "/"))
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                )
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .cors(Customizer.withDefaults()); // 최신 방식으로 CORS 설정

        return http.build();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider,
                refreshTokenRepository,
                userDetailService
                );
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, tokenService);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 주소를 허용 (예: http://localhost:3000)
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        // 필요한 HTTP 메서드 설정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 모든 헤더 허용 (필요에 따라 조정)
        configuration.setAllowedHeaders(List.of("*"));
        // 인증 정보(쿠키 등) 전송 허용 여부 설정
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 엔드포인트에 대해 위 설정 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
