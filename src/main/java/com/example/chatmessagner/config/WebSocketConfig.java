package com.example.chatmessagner.config;

import com.example.chatmessagner.config.interceptors.LoggingInterceptor;
import com.example.chatmessagner.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.session.Session;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.socket.server.SessionRepositoryMessageInterceptor;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.util.EnumSet;
import java.util.List;

import static org.springframework.messaging.simp.SimpMessageType.*;


@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final long[] HEARTBEAT = {10_000, 10_000};
    public static final int MAX_TEXT_MESSAGE_SIZE = 10 * 1024 * 1024;
    public static final int MAX_WORKERS_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors());
    public static final int TASK_QUEUE_SIZE = 10_000;
    public static final String[] BROKER_PREFIXES = new String[]{"/topic"};
    public static final String[] APP_PREFIXES = new String[]{"/topic/", "/user", "/app"};

    @Lazy
    private SimpMessagingTemplate messagingTemplate;
    @Lazy
    private final SessionService sessionService;
    @Lazy
    private final SessionRepository<? extends Session> sessionRepository;

    @Lazy
    private final ObjectMapper objectMapper;

    @Autowired
    public WebSocketConfig(SessionService sessionService,
                           SessionRepository<? extends Session> sessionRepository, ObjectMapper objectMapper) {
        this.sessionService = sessionService;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOrigins("*")
                .withSockJS();

        registry.setErrorHandler(new StompSubProtocolErrorHandler());
    }

//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.setApplicationDestinationPrefixes(APP_PREFIXES);
//        registry.enableSimpleBroker(BROKER_PREFIXES);
//    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry
                .setPreservePublishOrder(true)
                .setApplicationDestinationPrefixes(APP_PREFIXES)
                .enableSimpleBroker(BROKER_PREFIXES)
                .setHeartbeatValue(HEARTBEAT)
                .setTaskScheduler(getHeartbeatScheduler());

        registry.configureBrokerChannel()
                .interceptors(new LoggingInterceptor(), sessionRepositoryInterceptor())
                .taskExecutor().corePoolSize(1).maxPoolSize(MAX_WORKERS_COUNT).queueCapacity(TASK_QUEUE_SIZE);
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration
                .interceptors(new LoggingInterceptor(), sessionRepositoryInterceptor())
                .taskExecutor().corePoolSize(1).maxPoolSize(MAX_WORKERS_COUNT).queueCapacity(TASK_QUEUE_SIZE);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration
                .interceptors(new LoggingInterceptor(), sessionRepositoryInterceptor())
                .taskExecutor().corePoolSize(1).maxPoolSize(MAX_WORKERS_COUNT).queueCapacity(TASK_QUEUE_SIZE);
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        MappingJackson2MessageConverter jsonConverter = new MappingJackson2MessageConverter();
        jsonConverter.setObjectMapper(objectMapper);

        messageConverters.add(new StringMessageConverter());
        messageConverters.add(jsonConverter);

        return false;
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry
                .setMessageSizeLimit(MAX_TEXT_MESSAGE_SIZE)
                .addDecoratorFactory(handler -> new WebSocketHandlerConnectionConfig(handler, messagingTemplate, sessionService)
        );
    }

    @Bean
    public SessionRepositoryMessageInterceptor<? extends Session> sessionRepositoryInterceptor() {
        SessionRepositoryMessageInterceptor<? extends Session> interceptor = new SessionRepositoryMessageInterceptor<>(sessionRepository);
        interceptor.setMatchingMessageTypes(EnumSet.of(CONNECT, MESSAGE, SUBSCRIBE, UNSUBSCRIBE, SimpMessageType.HEARTBEAT));

        return interceptor;
    }

    @Bean
    public TaskScheduler getHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.initialize();

        return scheduler;
    }
}