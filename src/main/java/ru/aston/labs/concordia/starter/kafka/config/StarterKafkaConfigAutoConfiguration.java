package ru.aston.labs.concordia.starter.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;



@Configuration
@EnableConfigurationProperties(StarterKafkaConfigProperties.class)
public class StarterKafkaConfigAutoConfiguration {


    private static final String earliest = "earliest" ;


    private final StarterKafkaConfigProperties starterKafkaConfigProperties;
    ;

    @Autowired
    public StarterKafkaConfigAutoConfiguration(StarterKafkaConfigProperties starterKafkaConfigProperties) {

        this.starterKafkaConfigProperties = starterKafkaConfigProperties;
    }


    // ******************************************* CONSUMER ******************************************





    @Bean
    public ConsumerFactory<Object, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, starterKafkaConfigProperties.getServer());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, starterKafkaConfigProperties.getGroup());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, earliest);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // Появилось несколько проблем при определении бина kafkaListenerContainerFactory
    // спринг ругался тк бин с таким именем уже есть, при переименовании, не запускался автоконфиг
    // @Primary не работает тоже. Внутри написанного спринг класса KafkaAnnotationDrivenConfiguration
    // нашёл строчку configurer.configure(factory, consumerFactory()), добавил с модификацией
    // получилось поднять свой бин с автоконфигом.
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> myKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        // строчка ниже нужна для автоконфига KafkaListener
        configurer.configure(factory, consumerFactory());

        return factory;
    }

    // ******************************************* PRODUCER ******************************************

    @Bean
    public ProducerFactory<String,String> producerFactoryString() {
        Map<String,Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, starterKafkaConfigProperties.getServer());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        return new DefaultKafkaProducerFactory<>(props);
    }



    @Bean(name = "kafkaTemplateProducer")
    KafkaTemplate<String, String> kafkaTemplateProducer() {
        return new KafkaTemplate<>(producerFactoryString());
    }

}
