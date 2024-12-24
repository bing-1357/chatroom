package com.yc.chatroot.web.controller;

import com.google.gson.Gson;
import com.yc.chatroot.utils.MessageQueueManager;
import com.yc.chatroot.utils.MessageQueueManager.MessageType;
import com.yc.chatroot.utils.MessageQueueManager.QueueMessage;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket")
public class webSocketServlet {
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> usernames = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    // 获取所有会话
    public static Map<String, Session> getSessions() {
        return sessions;
    }

    private static void log(String message) {
        System.out.println("[WebSocket] " + message);
    }

    @OnOpen
    public void onOpen(Session session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log("New connection opened: " + sessionId);

        try {
            // 发送欢迎消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", "system");
            message.put("content", "欢迎加入聊天室！");

            // 使用消息队列发送欢迎消息
            MessageQueueManager.enqueueMessage(
                    new QueueMessage(MessageType.SYSTEM, message, session));

            broadcastUserList();
        } catch (Exception e) {
            log("Error in onOpen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log("Received message from " + session.getId() + ": " + message);

        try {
            Map<String, Object> messageMap = gson.fromJson(message, Map.class);
            String type = (String) messageMap.get("type");

            if ("init".equals(type)) {
                String username = (String) messageMap.get("username");
                log("Initializing user: " + username);
                usernames.put(session.getId(), username);

                Map<String, Object> systemMessage = new HashMap<>();
                systemMessage.put("type", "system");
                systemMessage.put("content", username + " 加入了聊天室");

                // 广播用户加入消息
                MessageQueueManager.enqueueMessage(
                        new QueueMessage(MessageType.SYSTEM, systemMessage, null));

                broadcastUserList();
            } else if ("message".equals(type)) {
                String content = (String) messageMap.get("content");
                String username = usernames.get(session.getId());

                Map<String, Object> chatMessage = new HashMap<>();
                chatMessage.put("type", "message");
                chatMessage.put("sender", username);
                chatMessage.put("content", content);

                // 广播聊天消息
                MessageQueueManager.enqueueMessage(
                        new QueueMessage(MessageType.CHAT, chatMessage, null));
            }
        } catch (Exception e) {
            log("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        log("Connection closed: " + sessionId);

        String username = usernames.get(sessionId);
        sessions.remove(sessionId);
        usernames.remove(sessionId);

        if (username != null) {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "system");
            message.put("content", username + " 离开了聊天室");

            // 广播用户离开消息
            MessageQueueManager.enqueueMessage(
                    new QueueMessage(MessageType.SYSTEM, message, null));
        }

        broadcastUserList();
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String sessionId = session.getId();
        log("Error in session " + sessionId + ": " + error.getMessage());
        error.printStackTrace();
    }

    private void broadcastUserList() {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "userList");

        List<Map<String, Object>> users = new ArrayList<>();
        for (Map.Entry<String, String> entry : usernames.entrySet()) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", entry.getKey());
            user.put("name", entry.getValue());
            user.put("status", "active");
            users.add(user);
        }

        message.put("users", users);

        // 广播用户列表
        MessageQueueManager.enqueueMessage(
                new QueueMessage(MessageType.USER_LIST, message, null));
    }
}
