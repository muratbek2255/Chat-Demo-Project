package com.example.chatmessagner.config.interceptors;


import com.example.chatmessagner.Utils;
import com.example.chatmessagner.entity.User;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.ExecutorSubscribableChannel;

import static org.springframework.messaging.simp.SimpMessageType.MESSAGE;
import static org.springframework.messaging.simp.SimpMessageType.SUBSCRIBE;

public class LoggingInterceptor implements ExecutorChannelInterceptor {

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        ExecutorSubscribableChannel ch = (ExecutorSubscribableChannel) channel;
        User user = Utils.user(accessor.getUser());

        if (accessor.getMessageType() == MESSAGE || accessor.getMessageType() == SUBSCRIBE) {
            System.out.printf("BEFORE [%s]: %s, %s, %s, %s%n",
                    ch.getBeanName(),
                    accessor.getMessageType(),
                    handler.getClass().getSimpleName(),
                    accessor.getDestination(),
                    user == null ? null : user.getUsername()
            );
        }

        return message;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        ExecutorSubscribableChannel ch = (ExecutorSubscribableChannel) channel;
        User user = Utils.user(accessor.getUser());

        if (accessor.getMessageType() == MESSAGE || accessor.getMessageType() == SUBSCRIBE) {
            System.out.printf("AFTER [%s]: %s, %s, %s, %s%n",
                    ch.getBeanName(),
                    accessor.getMessageType(),
                    handler.getClass().getSimpleName(),
                    accessor.getDestination(),
                    user == null ? null : user.getUsername()
            );
        }

    }
}