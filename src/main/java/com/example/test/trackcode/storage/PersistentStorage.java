package com.example.test.trackcode.storage;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// 使用 @State 注解标注持久化组件
@State(
        name = "PersistentStorage",
        storages = {@Storage("PersistentStorage.xml")}  // 使用默认配置，稍后会手动管理路径
)
public class PersistentStorage implements PersistentStateComponent<PersistentStorage.State> {

    // 定义状态类
    public static class State {
        public String username;
        public String password;
        public String token;
        public String url;  // github url
        public String owner;
        public String repoName;
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

    // 手动保存状态到项目根目录
    public void saveToFile() throws IOException {
        String projectBasePath = ProjectManager.getInstance().getOpenProjects()[0].getBasePath();
        if (projectBasePath == null) {
            throw new IOException("Unable to find project base path");
        }
        String storagePath = projectBasePath + "/PersistentStorage.xml";
        File storageFile = new File(storagePath);
        if (!storageFile.exists()) {
            storageFile.createNewFile();
        }

        // 简单保存内容到文件
        try (FileWriter writer = new FileWriter(storageFile)) {
            writer.write("<PersistentStorage>\n");
            writer.write("\t<username>" + myState.username + "</username>\n");
            writer.write("\t<password>" + myState.password + "</password>\n");
            writer.write("\t<token>" + myState.token + "</token>\n");
            writer.write("\t<url>" + myState.url + "</url>\n");
            writer.write("\t<url>" + myState.owner + "</url>\n");
            writer.write("\t<url>" + myState.repoName + "</url>\n");
            writer.write("</PersistentStorage>");
        }
    }

    // 手动从文件加载状态
    public void loadFromFile() throws IOException {
        // 在这里，你可以编写代码来从 PersistentStorage.xml 文件中加载数据
        // 比如用 XML 解析器来读取数据并填充到 myState 中
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

    public void setRepoName_Owner(String value) {

        try {
            URI uri = new URI(value);
            String[] pathSegments = uri.getPath().split("/");

            if (pathSegments.length >= 3) {
                myState.owner = pathSegments[1];  // 获取owner部分
                myState.repoName = pathSegments[2];   // 获取repo部分

            } else {
                System.out.println("Invalid GitHub URL format.");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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

    public String getRepoName() {
        return myState.repoName;
    }

    public String getOwner() {
        return myState.owner;
    }
}


/* 数据调用方法 */
/*
 * PersistentStorage.getInstance().getUsername()    // 获取用户名
 * PersistentStorage.getInstance().getPassword()    // 获取密码
 * PersistentStorage.getInstance().getToken()       // 获取仓库令牌
 * PersistentStorage.getInstance().getUrl()         // 获取仓库地址
 */