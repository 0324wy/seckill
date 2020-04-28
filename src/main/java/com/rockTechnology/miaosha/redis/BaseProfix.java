package com.rockTechnology.miaosha.redis;

public abstract class BaseProfix implements KeyProfix {

    private int expireSeconds;
    private String profix;

    public BaseProfix(String profix) {
        this.profix = profix;
    }

    public BaseProfix(int expireSeconds, String profix) {
        this.expireSeconds = expireSeconds;
        this.profix = profix;
    }

    @Override
    public int expireSeconds() {
        return this.expireSeconds;
    }

    @Override
    public String getProfix() {
        String className = getClass().getName();
        return className + ":" + profix + ":";
    }
}
