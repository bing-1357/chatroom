# ChatRoom 在线聊天室

一个基于WebSocket的实时在线聊天室系统，支持多人同时在线聊天、用户状态显示等功能。

## 功能特点

- 用户注册和登录
- 实时消息发送和接收
- 在线用户列表显示
- 系统消息提醒（用户加入/退出）
- 响应式布局设计（支持PC和移动端）
- 自动重连机制
- 支持暗色模式

## 技术栈

### 前端
- Vue 3 (Composition API)
- Axios
- WebSocket
- 响应式CSS

### 后端
- Java EE
- WebSocket (Jakarta WebSocket)
- Gson
- Servlet

## 项目结构 
```
src/
├── main/
│ ├── java/
│ │ └── com/
│ │ └── yc/
│ │ └── chatroot/
│ │ └── web/
│ │ └── controller/
│ │ └── webSocketServlet.java
│ └── webapp/
│ ├── css/
│ │ ├── chat.css
│ │ ├── login.css
│ │ ├── register.css
│ │ └── style.css
│ ├── WEB-INF/
│ │ └── web.xml
│ ├── chat.html
│ ├── index.html
│ └── register.html
```

## 在线使用

-访问 `8.134.145.163/chat`
-注册账号
-登录
-开始聊天
