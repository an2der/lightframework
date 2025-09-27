package com.lightframework.util.bean;

import com.lightframework.util.serialize.ProtostuffUtil;

public class BeanUtil {

    private BeanUtil(){}

    public static <T> T deepCopy(Object source) {
        return ProtostuffUtil.deserialize(ProtostuffUtil.serialize(source));
    }
}
