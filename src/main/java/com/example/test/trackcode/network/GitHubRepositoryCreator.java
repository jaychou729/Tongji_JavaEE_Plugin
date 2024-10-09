package com.example.test.trackcode.network;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/* 我的测试令牌 */
// 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

/*LMZ令牌*/
//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

public class GitHubRepositoryCreator {

    public static void createGitHubRepo(String repoName, String description, boolean isPrivate, String token) throws Exception {
        String url = "https://api.github.com/user/repos";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置请求方法为 POST
        con.setRequestMethod("POST");

        // 设置请求头
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Accept", "application/vnd.github.v3+json");
        con.setRequestProperty("Content-Type", "application/json");

        // 创建请求体
        String jsonInputString = "{ \"name\": \"" + repoName + "\", " +
                "\"description\": \"" + description + "\", " +
                "\"private\": " + isPrivate + " }";

        // 启用发送请求体
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 处理响应
        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            System.out.println("Repository created successfully.");
        } else {
            System.out.println("Failed to create repository.");
        }
    }
}

