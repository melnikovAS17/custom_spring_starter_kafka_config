package ru.aston.labs.concordia.starter.kafka.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
//@Component // TODO не работает автоконифг без указания пропертей в yaml
public class EnvPostProcessor implements EnvironmentPostProcessor {

    private final YamlPropertySourceLoader propertySourceLoader;

    public EnvPostProcessor(YamlPropertySourceLoader propertySourceLoader) {
        this.propertySourceLoader = propertySourceLoader;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        var resource = new ClassPathResource("default.yaml"); // определяем default.yaml как локальный ресурс
        PropertySource<?> propertySource = null;
        try {
            // и просим Yaml...Loader зачитать настройки из файла
            for (int i = 0; i < 4; i++) {
                propertySource = propertySourceLoader.load("kafka-config-service", resource).get(i);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // прочитанные настройки проставляются в настройки окружения Spring'а
        environment.getPropertySources().addLast(propertySource);
    }
}
