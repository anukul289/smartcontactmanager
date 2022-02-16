package com.smart;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class SmartcontactmanagerApplication implements WebMvcConfigurer{
	
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Register resource handler for images
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/static/","classpath:/img/")
                .setCacheControl(CacheControl.maxAge(2, TimeUnit.HOURS).cachePublic());
    }

	public static void main(String[] args) {
		SpringApplication.run(SmartcontactmanagerApplication.class, args);
	}

}
