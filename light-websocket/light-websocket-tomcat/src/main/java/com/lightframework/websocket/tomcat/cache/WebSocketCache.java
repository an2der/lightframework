package com.lightframework.websocket.tomcat.cache;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;

/*** 
 * @author yg
 * @date 2024/6/3 14:30
 * @version 1.0
 */
public class WebSocketCache {
    public static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();
}
