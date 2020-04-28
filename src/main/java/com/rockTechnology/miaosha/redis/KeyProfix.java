package com.rockTechnology.miaosha.redis;

public interface KeyProfix {
    public int expireSeconds();
    public String getProfix();
}
