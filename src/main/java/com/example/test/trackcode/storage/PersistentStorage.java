package com.example.test.trackcode.storage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// 使用 @State 注解指定存储的位置
@State(
        name = "PersistentStorage", // 存储状态的唯一名称
        storages = {@Storage("PersistentStorage.xml")}  // 保存的位置
)
public class PersistentStorage implements PersistentStateComponent<PersistentStorage.State> {

    // 定义状态类，用于保存你想要持久化的数据
    public static class State {
        public String username;
        public String password;
        public String token;
        public String url;  // github url
    }

    private State myState = new State();

    // 获取持久化的状态
    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    // 加载持久化的状态
    @Override
    public void loadState(@NotNull State state) {
        this.myState = state;
    }

    // 提供对外的静态方法，方便其他类访问
    public static PersistentStorage getInstance() {
        return ServiceManager.getService(PersistentStorage.class);
    }


    /* 数据修改与调用 */
    public void setUsername(String value) {
        myState.username = value;
    }

    public String getUsername() {
        return myState.username;
    }

    public void setPassword(String value) {
        myState.password = value;
    }

    public String getPassword() {
        return myState.password;
    }

    public void setToken(String value) {
        myState.token = value;
    }

    public String getToken() {
        return myState.token;
    }

    public void setUrl(String value) {
        myState.url = value;
    }

    public String getUrl() {
        return myState.url;
    }
}

/* 数据调用方法 */
/*
 * PersistentStorage.getInstance().getUsername()    // 获取用户名
 * PersistentStorage.getInstance().getPassword()    // 获取密码
 * PersistentStorage.getInstance().getToken()       // 获取仓库令牌
 * PersistentStorage.getInstance().getUrl()         // 获取仓库地址
 */