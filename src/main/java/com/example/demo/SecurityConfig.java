package com.example.demo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private RequestMatcher ajaxRequestMatcher = new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest");

    @Autowired
    DataSource dataSource;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void configure(WebSecurity web) throws Exception {}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/secure/memberonly").hasRole("MEMBER")
                .antMatchers("/secure/adminonly").hasRole("ADMIN")
                .antMatchers("/secure/all").authenticated()
                .antMatchers("/secure/api").authenticated();

        http.formLogin()
                .loginProcessingUrl("/auth/login-handle")
                .loginPage("/auth/login")
                .defaultSuccessUrl("/secure/all")
                .usernameParameter("login_id")
                .passwordParameter("login_password")
                .failureUrl("/auth/login?fail")
                .permitAll();

        http.logout()
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll();

        http.exceptionHandling()
                .accessDeniedHandler(accessDeniedHandlerBean())
                .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/auth/login"), new NegatedRequestMatcher(ajaxRequestMatcher))
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), ajaxRequestMatcher);
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .passwordEncoder(passwordEncoder)
                .usersByUsernameQuery("SELECT user, password, enabled FROM users WHERE user=?")
                .authoritiesByUsernameQuery("SELECT user, role FROM authorities WHERE user=?");
    }

    @Bean
    AccessDeniedHandler accessDeniedHandlerBean() {
        return new AccessDeniedHandler() {
            AccessDeniedHandler handler = new AccessDeniedHandlerImpl();
            DefaultRedirectStrategy redirect = new DefaultRedirectStrategy();

            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                               AccessDeniedException accessDeniedException) throws IOException, ServletException {
                if (accessDeniedException instanceof CsrfException) {
                    if (ajaxRequestMatcher.matches(request)){
                        log.info("Token may be invalid or missing");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    } else if ("/auth/logout".equals(request.getRequestURI())){
                        redirect.sendRedirect(request, response, "/");
                    } else {
                        log.info("Token may be invalid or missing");
                        redirect.sendRedirect(request, response, "/auth/login");
                    }
                } else {
                    handler.handle(request, response, accessDeniedException);
                }
            }
        };
    }

}
