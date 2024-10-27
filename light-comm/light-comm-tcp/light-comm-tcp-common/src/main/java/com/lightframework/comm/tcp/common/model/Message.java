package com.lightframework.comm.tcp.common.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message<T> {
    private long seq;//序列号，请求和响应需要一致

    private int type;

    private T payload;
}
