package com.mycellium.mycellium.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name", System.getenv("dvv8whlgg"),
                        "api_key", System.getenv("958271169621716"),
                        "api_secret", System.getenv("E5u3bG_1GqCXsryUkiuMNnIeD_A")
                )
        );
    }
}