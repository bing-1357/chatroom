package com.yc.chatroot.web.controller;

import com.google.gson.Gson;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import javax.websocket.OnError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket")
public class webSocketServlet {
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> usernames = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    // 添加日志
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
            sendMessage(session, message);

            // 广播新用户加入消息
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
                broadcast(systemMessage);

                broadcastUserList();
            } else if ("message".equals(type)) {
                String content = (String) messageMap.get("content");
                String username = usernames.get(session.getId());

                Map<String, Object> chatMessage = new HashMap<>();
                chatMessage.put("type", "message");
                chatMessage.put("sender", username);
                chatMessage.put("content", content);
                broadcast(chatMessage);
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
            broadcast(message);
        }

        broadcastUserList();
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String sessionId = session.getId();
        log("Error in session " + sessionId + ": " + error.getMessage());
        error.printStackTrace();
    }

    private void broadcast(Map<String, Object> message) {
        String jsonMessage = gson.toJson(message);
        log("Broadcasting: " + jsonMessage);

        for (Session session : sessions.values()) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(jsonMessage);
                }
            } catch (IOException e) {
                log("Error broadcasting to session " + session.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(Session session, Map<String, Object> message) {
        try {
            session.getBasicRemote().sendText(gson.toJson(message));
        } catch (IOException e) {
            log("Error sending message to session " + session.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
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
        broadcast(message);
    }
}
