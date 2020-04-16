package authentication.config;

import authentication.filter.JWTAuthenticationFilter;
import authentication.filter.JWTLoginFilter;
import authentication.filter.RegisterFilter;
import authentication.filter.TokenAuthenticationService;
import authentication.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final TokenAuthenticationService tokenService;

    private final RedisService redisService;

    public SecurityConfiguration(TokenAuthenticationService tokenService, RedisService redisService) {
        this.tokenService = tokenService;
        this.redisService = redisService;
    }

    @Override
    protected AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(new AuthenticationProviderImpl(redisService)));
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().cacheControl();

        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .httpBasic().disable()
                .addFilterBefore(new RegisterFilter("/register", redisService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTLoginFilter("/login", authenticationManager(), tokenService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTAuthenticationFilter(tokenService), UsernamePasswordAuthenticationFilter.class);

    }

}