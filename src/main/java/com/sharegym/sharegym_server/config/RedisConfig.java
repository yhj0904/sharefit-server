package com.sharegym.sharegym_server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스
 * - SSE 연결 관리
 * - Pub/Sub 메시징 (FCM 알림 전달)
 * - 세션 관리
 */
@Configuration
@RequiredArgsConstructor
@Profile({"prod"})  // Only enable Redis in production
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.password:}")
    private String password;

    /**
     * Redis 연결 팩토리
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);
        if (!password.isEmpty()) {
            redisConfig.setPassword(password);
        }
        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * RedisTemplate 설정
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Jackson ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        // Serializer 설정
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Key-Value Serializer 설정
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis Message Listener Container
     * - Pub/Sub 메시지 수신을 위한 컨테이너
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter fcmNotificationAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // FCM 알림 채널 구독
        container.addMessageListener(fcmNotificationAdapter, fcmNotificationTopic());

        return container;
    }

    /**
     * FCM 알림 메시지 리스너 어댑터
     */
    @Bean
    public MessageListenerAdapter fcmNotificationAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "handleFcmNotification");
    }

    /**
     * FCM 알림 토픽
     */
    @Bean
    public ChannelTopic fcmNotificationTopic() {
        return new ChannelTopic("notification:fcm");
    }

    /**
     * SSE 이벤트 토픽 (운동 실시간 방송)
     */
    @Bean
    public ChannelTopic workoutEventTopic() {
        return new ChannelTopic("sse:workout");
    }

    /**
     * SSE 이벤트 토픽 (피드 알림)
     */
    @Bean
    public ChannelTopic feedEventTopic() {
        return new ChannelTopic("sse:feed");
    }

    /**
     * SSE 이벤트 토픽 (그룹 활동)
     */
    @Bean
    public ChannelTopic groupEventTopic() {
        return new ChannelTopic("sse:group");
    }
}