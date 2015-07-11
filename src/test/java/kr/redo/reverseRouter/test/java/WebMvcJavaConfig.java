package kr.redo.reverseRouter.test.java;

import kr.redo.reverseRouter.ReverseRouter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@ComponentScan
@Configuration
@EnableWebMvc
public class WebMvcJavaConfig extends WebMvcConfigurerAdapter {
    @Bean
    public ReverseRouter router() {
        return new ReverseRouter();
    }

    @Bean
    public ApplicationListener<?> applicationListener() {
        return router();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(router());
        super.addInterceptors(registry);
    }
}
