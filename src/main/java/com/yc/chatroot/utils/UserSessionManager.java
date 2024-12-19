package com.yc.chatroot.utils;

import jakarta.servlet.http.HttpSession;

import java.util.concurrent.ConcurrentHashMap;

public class UserSessionManager {
    private static final ConcurrentHashMap<String, HttpSession> activeSessions = new ConcurrentHashMap<>();

    public static void addSession(String userId, HttpSession session) {
        // Remove any existing session for this user
        removeSession(userId);
        activeSessions.put(userId, session);
    }

    public static void removeSession(String userId) {
        HttpSession oldSession = activeSessions.remove(userId);
        if (oldSession != null) {
            oldSession.invalidate();
        }
    }

    public static HttpSession getSession(String userId) {
        return activeSessions.get(userId);
    }
}
