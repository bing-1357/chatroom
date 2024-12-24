package com.yc.chatroot.utils;

import com.google.gson.Gson;
import com.yc.chatroot.web.controller.webSocketServlet;
import jakarta.websocket.Session;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

public class MessageQueueManager {
    private static final BlockingQueue<QueueMessage> messageQueue = new LinkedBlockingQueue<>();
    private static final ExecutorService messageProcessor = Executors.newSingleThreadExecutor();
    private static final Gson gson = new Gson();

    // 消息类型枚举
    public enum MessageType {
        CHAT, SYSTEM, USER_LIST
    }

    // 消息实体类
    public static class QueueMessage {
        private final MessageType type;
        private final Map<String, Object> content;
        private final Session targetSession;

        public QueueMessage(MessageType type, Map<String, Object> content, Session targetSession) {
            this.type = type;
            this.content = content;
            this.targetSession = targetSession;
        }
    }

    static {
        // 启动消息处理线程
        messageProcessor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    QueueMessage message = messageQueue.take();
                    processMessage(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    // 添加消息到队列
    public static void enqueueMessage(QueueMessage message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 处理消息
    private static void processMessage(QueueMessage message) {
        String jsonMessage = gson.toJson(message.content);

        try {
            if (message.targetSession != null) {
                // 发送给特定会话
                if (message.targetSession.isOpen()) {
                    message.targetSession.getBasicRemote().sendText(jsonMessage);
                }
            } else {
                // 广播消息
                for (Session session : webSocketServlet.getSessions().values()) {
                    if (session.isOpen()) {
                        session.getBasicRemote().sendText(jsonMessage);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 关闭消息处理器
    public static void shutdown() {
        messageProcessor.shutdown();
        try {
            if (!messageProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageProcessor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}