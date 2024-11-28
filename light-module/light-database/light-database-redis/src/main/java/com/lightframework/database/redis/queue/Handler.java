package com.lightframework.database.redis.queue;

/**
 * @Description Key监听接口，实现该接口即可
 * @Author hua
 * @Date 2022/6/14
 **/
public interface Handler {

    void doHandle(String pattern, String channel, String message);
}
