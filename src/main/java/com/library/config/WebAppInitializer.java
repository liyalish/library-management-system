package com.library.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Programmatic replacement for {@code web.xml}. Bootstraps the Spring contexts and
 * registers the {@link org.springframework.web.servlet.DispatcherServlet}. Detected
 * automatically by the servlet container (Tomcat) at startup.
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * Configuration classes for the root (business) context.
     *
     * @return the root config classes
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{RootConfig.class};
    }

    /**
     * Configuration classes for the web (MVC) context.
     *
     * @return the web config classes
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class};
    }

    /**
     * Maps the DispatcherServlet to the application root.
     *
     * @return the servlet mappings
     */
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}