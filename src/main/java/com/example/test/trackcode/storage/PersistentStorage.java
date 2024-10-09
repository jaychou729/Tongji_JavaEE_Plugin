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
        public String githubURL = "";  // github url
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

    // 设置字符串值并保存
    public void setGithubURL(String value) {
        myState.githubURL = value;
    }

    // 获取字符串值
    public String getGithubURL() {
        return myState.githubURL;
    }
}

