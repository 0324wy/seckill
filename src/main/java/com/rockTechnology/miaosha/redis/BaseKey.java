package com.rockTechnology.miaosha.redis;

public abstract class BaseKey implements KeyProfix {

    private int expireSeconds = 0;
    private String profix;

    public BaseKey(String profix) {
        this.profix = profix;
    }

    public BaseKey(int expireSeconds, String profix) {
        this.expireSeconds = expireSeconds;
        this.profix = profix;
    }

    @Override
    public int expireSeconds() {
        return 0;
    }

    @Override
    public String getProfix() {
        String className = getClass().getName();
        return className + ":" + profix + ":";
    }
}
