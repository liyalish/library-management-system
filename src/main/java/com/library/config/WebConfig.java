package com.library.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.Locale;

/**
 * Web (MVC) context configuration. Sets up component scanning for controllers, the
 * Thymeleaf template engine and view resolver, static resource handling, and
 * internationalization (locale resolver + language switch interceptor).
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.library.controller")
public class WebConfig implements WebMvcConfigurer {

    /**
     * Resolves Thymeleaf templates from /WEB-INF/views/ with an .html suffix.
     *
     * @return the template resolver
     */
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    /**
     * Builds the Thymeleaf template engine using the template resolver.
     *
     * @return the configured template engine
     */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.setEnableSpringELCompiler(true);
        engine.setTemplateEngineMessageSource(messageSource());
        return engine;
    }

    /**
     * Connects Thymeleaf to Spring MVC as the view resolver.
     *
     * @return the Thymeleaf view resolver
     */
    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    /**
     * Loads localized message bundles (messages_en.properties, messages_ru.properties).
     *
     * @return the message source
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    /**
     * Uses the message source for Bean Validation messages, so validation texts can be
     * localized and referenced as {validation.*} keys.
     *
     * @return a validator backed by the message source
     */
    @Override
    public org.springframework.validation.Validator getValidator() {
        org.springframework.validation.beanvalidation.LocalValidatorFactoryBean validator =
                new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource());
        return validator;
    }

    /**
     * Stores the chosen locale in the user session, defaulting to English.
     *
     * @return the locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }

    /**
     * Allows switching language via a {@code ?lang=} request parameter.
     *
     * @return the locale change interceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        // Protect role-specific areas; public pages (login, register, books, root) are open.
        registry.addInterceptor(new com.library.interceptor.AuthInterceptor())
                .addPathPatterns("/admin/**", "/librarian/**", "/requests/**", "/reader/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
    }
}