package com.sun.player.webService.util;

import com.sun.player.syncDto.VideoCategory;
import com.sun.player.syncDto.VideoList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowedOrigins(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("*"));
        config.setMaxAge(86400L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    /**
     * @Author：LJ
     * @Description：为了解决Spring Data Rest不暴露ID字段的问题。 参考：http://tommyziegler.com/how-to-expose-the-resourceid-with-spring-data-rest/
     * @Date: 2018/3/21
     * @Modified By:
     */
    @Configuration
    public class SpringDataRestConfig {
        @Bean
        public RepositoryRestConfigurer repositoryRestConfigurer() {
            return new RepositoryRestConfigurerAdapter() {
                @Override
                public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
                    config.exposeIdsFor(VideoCategory.class, VideoList.class);
                }
            };
        }
    }
}