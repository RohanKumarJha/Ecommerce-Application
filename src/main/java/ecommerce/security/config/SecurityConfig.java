package ecommerce.security.config;

import ecommerce.security.jwt.AccessDeniedHandlerJwt;
import ecommerce.security.jwt.AuthEntryPointJwt;
import ecommerce.security.jwt.AuthTokenFilter;
import ecommerce.security.jwt.JwtUtils;
import ecommerce.security.services.UserDetailsServiceImpl;
import ecommerce.user.model.ENUM.AppRole;
import ecommerce.user.model.Role;
import ecommerce.user.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt authEntryPointJwt;
    private final JwtUtils jwtUtils;
    private final AccessDeniedHandlerJwt accessDeniedHandlerJwt;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          AuthEntryPointJwt authEntryPointJwt,
                          JwtUtils jwtUtils,
                          AccessDeniedHandlerJwt accessDeniedHandlerJwt) {
        this.userDetailsService = userDetailsService;
        this.authEntryPointJwt = authEntryPointJwt;
        this.jwtUtils = jwtUtils;
        this.accessDeniedHandlerJwt = accessDeniedHandlerJwt;
    }

    // ================= ROLE INITIALIZER =================
    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByRoleName(AppRole.ROLE_USER).isEmpty()) {
                roleRepository.save(new Role(AppRole.ROLE_USER));
            }
            if (roleRepository.findByRoleName(AppRole.ROLE_ADMIN).isEmpty()) {
                roleRepository.save(new Role(AppRole.ROLE_ADMIN));
            }
            if (roleRepository.findByRoleName(AppRole.ROLE_SELLER).isEmpty()) {
                roleRepository.save(new Role(AppRole.ROLE_SELLER));
            }
        };
    }

    // ================= SECURITY FILTER =================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> {}) // ✅ Enable CORS (configure globally if needed)

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPointJwt)   // 401
                        .accessDeniedHandler(accessDeniedHandlerJwt)   // 403
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )

                .authorizeHttpRequests(auth -> auth

                        // ✅ AUTH APIs
                        .requestMatchers("/api/auth/**").permitAll()

                        // ✅ SWAGGER + H2
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // ✅ PUBLIC READ APIs
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**",
                                "/api/categories/**"
                        ).permitAll()

                        // ✅ EVERYTHING ELSE PROTECTED
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ================= AUTH PROVIDER =================
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ================= PASSWORD ENCODER =================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ================= JWT FILTER =================
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    // ================= AUTH MANAGER =================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}