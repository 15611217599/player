package com.sun.player.webService.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.FileNotFoundException;

@Configuration
@Slf4j
public class myMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File path = null;
        try {
            path = new File(ResourceUtils.getURL("classpath:").getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String gitPath = path.getParentFile().getParentFile().getParent() + File.separator + "static" + File.separator + "imageFile";
        File file = new File(gitPath);

        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            //windows cmd获取后会多一个file:\前缀,奇怪

            if (!mkdirs) {
                int i = gitPath.indexOf(":");
                gitPath = gitPath.substring(i + 2);
                file = new File(gitPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
            }
        }

        //前面是访问路径   后面是磁盘路径  他俩是对应的
        registry.addResourceHandler("/images/**").addResourceLocations("file:" + gitPath + File.separator);
    }
}