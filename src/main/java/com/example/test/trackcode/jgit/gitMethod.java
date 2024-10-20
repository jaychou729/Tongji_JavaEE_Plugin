package com.example.test.trackcode.jgit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.example.test.trackcode.datastruct.CodeVersion;
import com.example.test.trackcode.message.MessageOutput;
import com.example.test.trackcode.storage.PersistentStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.ProjectManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import java.io.File;
import java.io.FileInputStream;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.net.URI;
import org.eclipse.jgit.api.Git;

import static org.apache.commons.io.file.PathUtils.deleteFile;
import static org.apache.tools.ant.types.resources.MultiRootFileSet.SetType.file;

public class gitMethod {
    // 本地仓库初始化并初始化远程仓库

    public static void InitRepo() throws GitAPIException, IOException {
        try {
            // 获取项目的基础路径
            String projectBasePath = ProjectManager.getInstance().getOpenProjects()[0].getBasePath();

            // 从持久化存储中获取用户名、密码和令牌
            String userName = PersistentStorage.getInstance().getUsername();
            String password = PersistentStorage.getInstance().getPassword();
            String token = PersistentStorage.getInstance().getToken();
            String url = PersistentStorage.getInstance().getUrl()+".git";

            // 初始化本地 Git 仓库
            Git git = Git.init().setDirectory(new File(projectBasePath)).call();
            System.out.println("Initialized local Git repository");

            // 创建或更新 .gitignore 文件
            File gitignoreFile = new File(projectBasePath, ".gitignore");
            if (!gitignoreFile.exists()) {
                gitignoreFile.createNewFile();
            }

            // 添加 PersistentStorage.xml 到 .gitignore 中
            List<String> lines = Files.readAllLines(gitignoreFile.toPath(), StandardCharsets.UTF_8);
            if (!lines.contains("PersistentStorage.xml")) {
                lines.add("PersistentStorage.xml");
                Files.write(gitignoreFile.toPath(), lines, StandardCharsets.UTF_8);
            }

            // 添加所有文件并提交，确保至少有一次提交
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
            System.out.println("Initial commit created.");

            // 检查并创建 main 分支
            if (git.getRepository().findRef("refs/heads/main") == null) {
                git.branchCreate().setName("main").call();
                System.out.println("Main branch created.");
            }

            // 切换到 main 分支
            git.checkout().setName("main").call();
            System.out.println("Switched to main branch.");

            // 删除本地 master 分支
            if (git.getRepository().findRef("refs/heads/master") != null) {
                git.branchDelete().setBranchNames("master").setForce(true).call();
                System.out.println("Deleted local master branch.");
            }


            // 添加远程仓库 URL
            StoredConfig config = git.getRepository().getConfig();
            config.setString("remote", "origin", "url", url);
            config.save();
            System.out.println("Remote repository set to: " + url);

            // 推送到远程仓库
            UsernamePasswordCredentialsProvider provider =
                    new UsernamePasswordCredentialsProvider(userName, token);
            git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(provider)
                    .setRefSpecs(new RefSpec("refs/heads/main:refs/heads/main")) // 推送到远程的 main 分支
                    .setForce(true)  // 强制推送
                    .call();
            System.out.println("Pushed to remote repository.");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;  // 将异常重新抛出以便调试
        }
    }

    public static void addGitignoreFile(String userName,String repoName, String token) throws Exception {

        String url = "https://api.github.com/repos/"+userName+"/" + repoName + "/contents/.gitignore";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置请求方法为 PUT
        con.setRequestMethod("PUT");

        // 设置请求头
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Accept", "application/vnd.github.v3+json");
        con.setRequestProperty("Content-Type", "application/json");

        // 自定义的 .gitignore 内容
        String gitignoreContent = "PersistentStorage.xml\n";

        // 将内容进行 base64 编码，因为 GitHub API 需要使用 base64 编码的内容
        String encodedContent = Base64.getEncoder().encodeToString(gitignoreContent.getBytes());

        // 创建请求体，提交到 GitHub
        String jsonInputString = "{ " +
                "\"message\": \"Add custom .gitignore\", " +
                "\"content\": \"" + encodedContent + "\"" +
                "}";

        // 启用发送请求体
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 处理响应
        int responseCode = con.getResponseCode();
        System.out.println("PUT Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            System.out.println(".gitignore file created successfully.");
        } else {
            System.out.println("Failed to create .gitignore file.");
        }
    }




    // 项目克隆操作
    public static void CloneRepo() throws GitAPIException, IOException {
        // 当前项目路径
        String localPathStr = ProjectManager.getInstance().getOpenProjects()[0].getBasePath();

        // 使用异步任务来获取token、username、url，并在完成后执行后续操作
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // 获取 token, userName, url
            String token = PersistentStorage.getInstance().getToken();
            String userName = PersistentStorage.getInstance().getUsername();
            String url = PersistentStorage.getInstance().getUrl();

            // 打印调试信息
            System.out.println("Repository URL: " + url + ".git");
            System.out.println("Token: " + token);
            System.out.println("Username: " + userName);

            // 检查获取的信息是否为空
            if (url == null || token == null || userName == null) {
                throw new IllegalArgumentException("URL, token, or username is null. Cannot proceed.");
            }

            // 删除文件或执行其他操作
            try {
                deleteFiles(localPathStr);  // 例如你想在获取完信息后执行删除操作
            } catch (IOException e) {
                System.out.println("Failed to delete files.");
                e.printStackTrace();
            }

            // 指定克隆的仓库和分支
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(url + ".git")  // 确保 URL 是完整的 Git URL
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, token))
                    .setDirectory(new File(localPathStr))  // 目标克隆目录
                    .setCloneAllBranches(true);


            // 执行克隆操作
            try (Git git = cloneCommand.call()) {
                System.out.println("Successfully cloned repository.");
            } catch (Exception e) {
                System.out.println("Failed to clone repository.");
                e.printStackTrace();  // 打印异常信息
            }
        });

        future.thenRunAsync(() -> {
            // 这个任务将在克隆操作成功完成后异步执行
            System.out.println("Cloning finished, executing additional async task...");

            // 例如，执行某些额外的操作，比如更新UI或处理克隆完成后的数据
            try {
                PersistentStorage.getInstance().saveToFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 等待异步任务完成
        future.join();  // 主线程会等待异步任务完成后再继续
    }

    public static void deleteFiles(String localPathStr) throws IOException {
        Path directory = Paths.get(localPathStr);
        if (Files.exists(directory)) {
            try (Stream<Path> files = Files.walk(directory)) {
                files
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);  // 删除文件
            }
        }
    }

    public static void createBranch() throws IOException {
        String sha = null;
        String branchName = "Version";  // 目标分支名称
        isRepoExists(PersistentStorage.getInstance().getOwner(),
                PersistentStorage.getInstance().getRepoName(),
                PersistentStorage.getInstance().getToken());

        // Step 1: 获取主分支的 SHA 值
        String getShaUrl = "https://api.github.com/repos/" + PersistentStorage.getInstance().getOwner() + "/" +
                PersistentStorage.getInstance().getRepoName() + "/git/refs/heads/main";  // 主分支
        System.out.println(getShaUrl);
        URL url = new URL(getShaUrl);
        HttpURLConnection getConn = (HttpURLConnection) url.openConnection();
        getConn.setRequestMethod("GET");
        getConn.setRequestProperty("Authorization", "token " + PersistentStorage.getInstance().getToken());

        if (getConn.getResponseCode() == 200) {
            Scanner scanner = new Scanner(getConn.getInputStream());
            StringBuilder inline = new StringBuilder();
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();

            // 从响应中提取SHA
            sha = inline.toString().split("\"sha\":\"")[1].split("\"")[0];
        } else {
            throw new IOException("Error: " + getConn.getResponseCode() + " - Unable to get branch SHA");
        }
        getConn.disconnect();

        // Step 2: 检查目标分支是否已经存在
        String checkBranchUrl = "https://api.github.com/repos/" + PersistentStorage.getInstance().getOwner() + "/" +
                PersistentStorage.getInstance().getRepoName() + "/git/refs/heads/" + branchName;

        HttpURLConnection checkConn = (HttpURLConnection) new URL(checkBranchUrl).openConnection();
        checkConn.setRequestMethod("GET");
        checkConn.setRequestProperty("Authorization", "token " + PersistentStorage.getInstance().getToken());

        if (checkConn.getResponseCode() == 200) {
            // 分支已经存在，直接返回或执行其他操作
            System.out.println("Branch '" + branchName + "' already exists.");
            return;  // 如果不需要再操作，直接返回
        } else if (checkConn.getResponseCode() != 404) {
            // 如果不是 404（即分支不存在），抛出其他错误
            throw new IOException("Error: " + checkConn.getResponseCode() + " - Unable to check branch existence");
        }
        checkConn.disconnect();

        // Step 3: 使用获取到的 SHA 在远程仓库创建新的分支
        String createBranchUrl = "https://api.github.com/repos/" + PersistentStorage.getInstance().getOwner() + "/" +
                PersistentStorage.getInstance().getRepoName() + "/git/refs";

        URL postUrl = new URL(createBranchUrl);
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Authorization", "token " + PersistentStorage.getInstance().getToken());
        postConn.setRequestProperty("Content-Type", "application/json");

        // 构建 JSON 请求体
        String payload = String.format("{\"ref\": \"refs/heads/%s\", \"sha\": \"%s\"}", branchName, sha);
        postConn.setDoOutput(true);
        try (OutputStream os = postConn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = postConn.getResponseCode();
        if (responseCode == 201) {
            System.out.println("New branch '" + branchName + "' created successfully.");
        } else {
            System.out.println("Failed to create branch. Response Code: " + responseCode);
        }

        postConn.disconnect();

        // Step 4: 删除目标分支上的所有文件
        deleteAllFilesInBranch();
    }

    public static boolean isRepoExists(String owner, String repoName, String token) {
        try {
            // 构建 GitHub API URL
            String apiUrl = "https://api.github.com/repos/" + owner + "/" + repoName;

            // 创建 URL 对象
            URL url = new URL(apiUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 设置 GitHub Token 请求头
            connection.setRequestProperty("Authorization", "token " + token);

            // 获取响应码
            int responseCode = connection.getResponseCode();

            // 200 表示仓库存在
            if (responseCode == 200) {
                System.out.println("Repository exists.");
                return true;
            } else if (responseCode == 404) {
                System.out.println("Repository not found.");
                return false;
            } else {
                System.out.println("Failed to check repository. Response Code: " + responseCode);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void deleteAllFilesInBranch() throws IOException {
        String repoOwner = PersistentStorage.getInstance().getOwner();
        String repoName = PersistentStorage.getInstance().getRepoName();
        String branch = "Version";  // 修改为你要删除的分支名称
        String token = PersistentStorage.getInstance().getToken();

        // 获取远程分支上的文件列表
        String apiUrl = "https://api.github.com/repos/" + repoOwner + "/" + repoName + "/git/trees/" + branch + "?recursive=1";
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "token " + token);

        if (conn.getResponseCode() == 200) {
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder inline = new StringBuilder();
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();

            // 解析返回的JSON，获取文件路径列表
            String[] filePaths = inline.toString().split("\"path\":\"");
            for (int i = 1; i < filePaths.length; i++) {
                String filePath = filePaths[i].split("\"")[0];
                if (!filePath.equals(".gitignore")) { // 避免删除 .gitignore 文件
                    deleteFile(repoOwner, repoName, branch, filePath, token);
                }
            }
        } else {
            throw new IOException("Failed to list branch content: " + conn.getResponseCode());
        }
        conn.disconnect();
    }

    public static void deleteFile(String owner, String repo, String branch, String filePath, String token) throws IOException {
        // 获取文件的SHA
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + filePath;
        URL url = new URL(apiUrl);
        HttpURLConnection getConn = (HttpURLConnection) url.openConnection();
        getConn.setRequestMethod("GET");
        getConn.setRequestProperty("Authorization", "token " + token);

        String sha = null;
        if (getConn.getResponseCode() == 200) {
            Scanner scanner = new Scanner(getConn.getInputStream());
            StringBuilder inline = new StringBuilder();
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();

            // 从响应中提取文件的SHA值
            sha = inline.toString().split("\"sha\":\"")[1].split("\"")[0];
        } else {
            throw new IOException("Error getting file SHA: " + getConn.getResponseCode());
        }
        getConn.disconnect();

        // 删除文件
        String deleteUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + filePath;
        URL delUrl = new URL(deleteUrl);
        HttpURLConnection delConn = (HttpURLConnection) delUrl.openConnection();
        delConn.setRequestMethod("DELETE");
        delConn.setRequestProperty("Authorization", "token " + token);
        delConn.setRequestProperty("Content-Type", "application/json");

        String payload = String.format("{\"message\": \"Delete %s\", \"sha\": \"%s\", \"branch\": \"%s\"}", filePath, sha, branch);
        delConn.setDoOutput(true);
        try (OutputStream os = delConn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = delConn.getResponseCode();
        if (responseCode == 200 || responseCode == 204) {
            System.out.println("Deleted file: " + filePath);
        } else {
            System.out.println("Failed to delete file: " + filePath + " - Response Code: " + responseCode);
        }
        delConn.disconnect();
    }


    public static void createFolder(String FolderName) throws IOException {
        // 构造 API 请求 URL
        String apiUrl = "https://api.github.com/repos/" + PersistentStorage.getInstance().getOwner() + "/" + PersistentStorage.getInstance().getRepoName() + "/contents/" + FolderName + "/.gitkeep";
        URL url = new URL(apiUrl);

        // 创建连接
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "token " + PersistentStorage.getInstance().getToken());
        conn.setRequestProperty("Content-Type", "application/json");

        // 创建一个文件内容（空文件）
        String message = "Create empty .gitkeep in folder " + FolderName;
        String content = ""; // 空内容的 base64 编码结果是空字符串

        // 请求体，包含提交的文件路径、内容、提交消息和目标分支
        String jsonPayload = String.format("{\"message\": \"%s\", \"content\": \"%s\", \"branch\": \"%s\"}",
                message, content, "Version");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
        }

        // 处理响应
        int responseCode = conn.getResponseCode();
        if (responseCode == 201) { // HTTP 201 表示创建成功
            System.out.println("Folder created successfully in branch " + "Version" + " with .gitkeep file.");
        } else {
            System.out.println("Failed to create folder in branch " + "Version" + ". Response Code: " + responseCode);
        }
        conn.disconnect();

    }


    public static void commitFile(String FileName,String FolderPath,File file) throws IOException {
        // API URL to create or update a file in a repository
        String apiUrl = "https://api.github.com/repos/" + PersistentStorage.getInstance().getOwner() + "/" + PersistentStorage.getInstance().getRepoName() + "/contents/" + FolderPath + "/" + FileName;

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        fileInputStream.read(fileBytes);
        fileInputStream.close();
        String encodedContent = Base64.getEncoder().encodeToString(fileBytes);

        // Create JSON payload
        String payload = String.format("{\"message\":\"Add file %s\",\"content\":\"%s\",\"branch\":\"%s\"}",
                FileName, encodedContent, "Version");

        // Open connection to GitHub API
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "token " + PersistentStorage.getInstance().getToken());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Write the JSON payload to the output stream
        OutputStream os = conn.getOutputStream();
        os.write(payload.getBytes(StandardCharsets.UTF_8));
        os.close();

        // Read the response from GitHub
        int responseCode = conn.getResponseCode();
        if (responseCode == 201 || responseCode == 200) {
            System.out.println("File uploaded successfully.");
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                System.out.println(scanner.nextLine());
            }
            scanner.close();
        } else {
            System.out.println("Failed to upload file. Response Code: " + responseCode);
            Scanner scanner = new Scanner(conn.getErrorStream());
            while (scanner.hasNext()) {
                System.out.println(scanner.nextLine());
            }
            scanner.close();
        }

        conn.disconnect();
    }

    public static boolean isFolderPresent(String owner, String repo, String branch, String folderName, String token) {
        try {
            // 构建GitHub API的URL，用于获取仓库分支的内容
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", owner, repo, folderName, branch);

            // 创建URL对象
            URL url = new URL(apiUrl);

            // 建立HTTP连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "token " + token); // 添加GitHub token以进行身份验证

            // 获取响应码
            int responseCode = connection.getResponseCode();

            // 如果响应码是200，表示文件夹存在
            if (responseCode == 200) {
                return true;
            } else if (responseCode == 404) {
                return false;
            } else {
                System.out.println("Unexpected response code: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String fetchFileFromGitHub(String fileUrl) throws IOException {
        StringBuilder content = new StringBuilder();
        try {
            // Create a URL object
            URL url = new URL(fileUrl);

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Use BufferedReader to read the content
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            int charValue;
            while ((charValue = reader.read()) != -1) {
                content.append((char) charValue);  // Append each character, including whitespace and newlines
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content.toString();
    }



    public static List<CodeVersion> fetchFilesFromGitHubFolder(String owner, String repo, String branch, String folderPath, String token) throws IOException {
        // GitHub API URL，用于获取文件夹中的内容
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s?ref=%s", owner, repo, folderPath, branch);

        // 创建 HTTP 客户端
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);

        // 设置授权头部信息，使用 token 进行身份验证
        request.setHeader("Authorization", "Bearer " + token);
        request.setHeader("Accept", "application/vnd.github.v3+json");

        List<CodeVersion> versions = new ArrayList<>();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String jsonResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            // 使用 Jackson 解析 JSON 响应
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            // 遍历文件和文件夹列表
            for (JsonNode node : jsonNode) {
                if (node.get("type").asText().equals("file")) {
                    // 如果是文件，获取文件名
                    String fileName = node.get("name").asText();

                    if (fileName.endsWith(".txt")) {
                        System.out.println("Processing file: " + fileName);

                        Map<String, String> dateTime = fileNameExtractor(fileName);

                        // 拼接文件路径
                        String filePath = "https://raw.githubusercontent.com/"+owner+"/"+repo+"/Version/"+folderPath + "/" + fileName;

                        // 获取文件内容
                        String content = fetchFileFromGitHub(filePath);

                        // 添加到版本列表
                        versions.add(new CodeVersion(dateTime.get("date"), dateTime.get("time"), content));
                    } else {
                        System.out.println("Skipping non-txt file: " + fileName);
                    }
                }
            }
        }

        // 按照日期和时间从后往前进行排序
        Collections.sort(versions, new Comparator<CodeVersion>() {
            @Override
            public int compare(CodeVersion v1, CodeVersion v2) {
                String dateTime1 = v1.getDate() + " " + v1.getTime();
                String dateTime2 = v2.getDate() + " " + v2.getTime();
                // 改为反向排序，即让最近的时间点排在最前面
                return dateTime2.compareTo(dateTime1);  // 日期时间的反向比较
            }
        });
        return versions;
    }

    public static Map<String,String> fileNameExtractor(String fileName){
        System.out.println(fileName);
        String regex = ".*_(\\d{4}-\\d{2}-\\d{2})_(\\d{2}-\\d{2}-\\d{2})(\\.txt)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.matches()) {
            String date = matcher.group(1);  // 提取到的日期部分
            String time = matcher.group(2).replace("-", ":");  // 提取到的时间部分，并将 "-" 替换为 ":"
            System.out.println(date);
            // 将结果放入Map中
            Map<String, String> dateTime = new HashMap<>();
            dateTime.put("date", date);
            dateTime.put("time", time);
            System.out.println(dateTime);
            return dateTime;
        }
        return null;
    }

    public static void commitAndPushChanges() throws IOException {
        String projectBasePath = ProjectManager.getInstance().getOpenProjects()[0].getBasePath();
        File localRepoPath = new File(projectBasePath);
        String username = PersistentStorage.getInstance().getUsername();
        String token = PersistentStorage.getInstance().getToken();
        Git git = Git.open(localRepoPath);

        try {
            // 保存所有文档，确保没有未保存的更改
            FileDocumentManager.getInstance().saveAllDocuments();

            // 强制重置暂存区并重新添加文件
            git.reset().setMode(ResetCommand.ResetType.MIXED).call();
            git.add().addFilepattern(".").call();
            System.out.println("All changes added to the staging area.");

            // 提交更改，使用唯一的提交信息避免合并
            git.commit().setMessage("Update at " + System.currentTimeMillis()).call();

            // 推送到远程 main 分支
            UsernamePasswordCredentialsProvider credentialsProvider =
                    new UsernamePasswordCredentialsProvider(username, token);
            git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .setForce(true)  // 强制推送
                    .call();
            System.out.println("Changes pushed to the remote main branch.");

        } catch (GitAPIException e) {
            e.printStackTrace();
            System.err.println("Failed to commit or push changes.");
        }
    }



    public static JsonArray getFilesFromGitHub(String apiUrl, String token) throws IOException {
        HttpURLConnection connection = createConnection(apiUrl, "GET", token);
        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            String response = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();
            return JsonParser.parseString(response).getAsJsonArray();
        } else {
            System.err.println("Failed to fetch files: " + responseCode);
            return null;
        }
    }

    public static void renameFileOnGitHub(String owner, String repo, String branch,
                                          String oldPath, String newPath, String token) throws IOException {
        // Step 1: 获取旧文件的 SHA
        String getUrl = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                owner, repo, oldPath, branch
        );


        HttpURLConnection getConnection = createConnection(getUrl, "GET", token);
        int getResponseCode = getConnection.getResponseCode();

        if (getResponseCode != 200) {
            throw new IOException("Failed to get file: " + getResponseCode);
        }

        // 读取响应，提取 SHA 和文件内容
        JsonObject fileObject;
        try (InputStream is = getConnection.getInputStream();
             InputStreamReader reader = new InputStreamReader(is)) {
            fileObject = JsonParser.parseReader(reader).getAsJsonObject();
        }

        // 检查 SHA 是否存在，防止 NullPointerException
        if (!fileObject.has("sha")) {
            throw new IOException("SHA not found in response. Check if the file exists.");
        }

        String sha = fileObject.get("sha").getAsString();
        String content = fileObject.get("content").getAsString();

        // Step 2: 创建新文件（重命名为新路径）
        String createUrl = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s",
                owner, repo, newPath
        );

        JsonObject createRequest = new JsonObject();
        createRequest.addProperty("message", "Renamed " + oldPath + " to " + newPath);
        createRequest.addProperty("content", content);
        createRequest.addProperty("branch", branch);

        HttpURLConnection createConnection = createConnection(createUrl, "PUT", token);
        createConnection.setDoOutput(true);
        try (OutputStream os = createConnection.getOutputStream()) {
            byte[] input = createRequest.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int createResponseCode = createConnection.getResponseCode();
        if (createResponseCode != 201) {
            throw new IOException("Failed to create new file: " + createResponseCode);
        }

        // Step 3: 删除旧文件
        String deleteUrl = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s",
                owner, repo, oldPath
        );

        JsonObject deleteRequest = new JsonObject();
        deleteRequest.addProperty("message", "Deleted old file: " + oldPath);
        deleteRequest.addProperty("sha", sha);
        deleteRequest.addProperty("branch", branch);

        HttpURLConnection deleteConnection = createConnection(deleteUrl, "DELETE", token);
        deleteConnection.setDoOutput(true);
        try (OutputStream os = deleteConnection.getOutputStream()) {
            byte[] input = deleteRequest.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int deleteResponseCode = deleteConnection.getResponseCode();
        if (deleteResponseCode != 200) {
            throw new IOException("Failed to delete old file: " + deleteResponseCode);
        }

        System.out.println("File renamed from '" + oldPath + "' to '" + newPath + "' successfully.");
    }


    private static HttpURLConnection createConnection(String url, String method, String token) throws IOException {
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "token " + token);
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("Content-Type", "application/json");
        return connection;
    }

    public static void updateFileName(String folderPath, String newName, String oldName) throws IOException {
        String GITHUB_API = "https://api.github.com";
        String OWNER = PersistentStorage.getInstance().getUsername();
        String REPO = PersistentStorage.getInstance().getRepoName();
        String TOKEN = PersistentStorage.getInstance().getToken();

        String apiUrl = String.format(
                "%s/repos/%s/%s/contents/%s?ref=%s",
                GITHUB_API, OWNER, REPO, folderPath, "Version");

        JsonArray files = getFilesFromGitHub(apiUrl, TOKEN);
        if (files != null) {
            for (JsonElement element : files) {
                JsonObject fileObj = element.getAsJsonObject();
                String name = fileObj.get("name").getAsString();
                System.out.println(name);

                if (name.endsWith(".txt") && name.contains(oldName)) {
                    String renamedFile = name.replace(oldName, newName); // 替换 oldName 为 newName
                    System.out.println("Renaming: " + name + " to " + renamedFile);
                    String newFolderPath=folderPath.replace(oldName,newName);





                    CompletableFuture<Void> renameFilesFuture = CompletableFuture.runAsync(() -> {
                        try {
                            // 调用 renameFileOnGitHub 执行重命名
                            renameFileOnGitHub(OWNER, REPO, "Version",
                                    folderPath + "/" + name, folderPath + "/" + renamedFile,TOKEN);
                        } catch (IOException e) {
                            throw new RuntimeException("Error renaming files", e);
                        }
                    });

                    // 第二个任务：等第一个任务完成后再重命名文件夹
                    CompletableFuture<Void> renameFolderFuture = renameFilesFuture.thenRunAsync(() -> {
                        try {
                            renameFolderOnGitHub(OWNER, REPO, "Version",
                                    folderPath,newFolderPath,TOKEN);
                        } catch (IOException e) {
                            throw new RuntimeException("Error renaming folder", e);
                        }
                    });


                    System.out.println("All operations completed successfully.");
                }
            }
        }
    }

    public static void renameFolderOnGitHub(String owner, String repo, String branch,
                                            String oldFolderPath, String newFolderPath, String token) throws IOException {
        // Step 1: 获取旧文件夹中的所有文件
        String apiUrl = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                owner, repo, oldFolderPath, branch);

        JsonArray files = getFilesFromGitHub(apiUrl, token);

        if (files != null) {
            for (JsonElement element : files) {
                JsonObject fileObj = element.getAsJsonObject();
                String fileName = fileObj.get("name").getAsString();
                String oldFilePath = oldFolderPath + "/" + fileName;
                String newFilePath = newFolderPath + "/" + fileName;

                // Step 2: 重命名每个文件（移动到新文件夹路径）
                renameFileOnGitHub(owner, repo, branch, oldFilePath, newFilePath, token);
            }

            // Step 3: 确保旧文件夹中的文件都被移动之后，删除旧文件夹的内容
            for (JsonElement element : files) {
                JsonObject fileObj = element.getAsJsonObject();
                String fileName = fileObj.get("name").getAsString();
                String oldFilePath = oldFolderPath + "/" + fileName;



                if (isFileExists(owner, repo, branch, oldFilePath, token)) {
                    // 删除旧文件夹中的文件
                    deleteFileOnGitHub(owner, repo, branch, oldFilePath, fileObj.get("sha").getAsString(), token);
                } else {
                    System.out.println("File not found, skipping deletion: " + oldFilePath);
                }

            }

            System.out.println("Folder renamed from '" + oldFolderPath + "' to '" + newFolderPath + "' successfully.");
        } else {
            throw new IOException("Failed to fetch files from folder: " + oldFolderPath);
        }
    }

    public static void deleteFileOnGitHub(String owner, String repo, String branch,
                                          String filePath, String sha, String token) throws IOException {
        String deleteUrl = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s",
                owner, repo, filePath);

        // 打印删除请求的 URL 和文件的 SHA 值，便于调试
        System.out.println("API URL for deleting file: " + deleteUrl);
        System.out.println("SHA of the file to delete: " + sha);

        JsonObject deleteRequest = new JsonObject();
        deleteRequest.addProperty("message", "Deleted old file: " + filePath);
        deleteRequest.addProperty("sha", sha);
        deleteRequest.addProperty("branch", branch);

        HttpURLConnection deleteConnection = createConnection(deleteUrl, "DELETE", token);
        deleteConnection.setDoOutput(true);
        try (OutputStream os = deleteConnection.getOutputStream()) {
            byte[] input = deleteRequest.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int deleteResponseCode = deleteConnection.getResponseCode();
        if (deleteResponseCode != 200) {
            throw new IOException("Failed to delete file: " + deleteResponseCode + " - " + deleteConnection.getResponseMessage());
        }

        System.out.println("File deleted: " + filePath);
    }

    public static boolean isFileExists(String owner, String repo, String branch, String filePath, String token) throws IOException {
        String apiUrl = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                owner, repo, filePath, branch
        );

        HttpURLConnection connection = createConnection(apiUrl, "GET", token);
        int responseCode = connection.getResponseCode();
        return responseCode == 200;  // 返回 200 表示文件存在
    }

    public static void deleteFolderOnGitHub(String owner, String repo, String branch,
                                            String folderPath, String token) throws IOException {
        String apiUrl = String.format(
                "https://api.github.com/repos/%s/%s/contents/%s?ref=%s",
                owner, repo, folderPath, branch);

        JsonArray files = getFilesFromGitHub(apiUrl, token);
        if (files != null) {
            for (JsonElement element : files) {
                JsonObject fileObj = element.getAsJsonObject();
                String fileName = fileObj.get("name").getAsString();
                String filePath = folderPath + "/" + fileName;
                String sha = fileObj.get("sha").getAsString();

                // 如果是文件夹，递归删除
                if (fileObj.get("type").getAsString().equals("dir")) {
                    deleteFolderOnGitHub(owner, repo, branch, filePath, token);
                } else {
                    // 删除文件
                    deleteFileOnGitHub(owner, repo, branch, filePath, sha, token);
                }
            }

            // 删除文件夹中的 `.gitkeep` 或空文件
            System.out.println("Folder deleted: " + folderPath);
        } else {
            System.out.println("Folder is empty or not found: " + folderPath);
        }
    }


}







/* git 操作流程 */
/*
* 若初始远程仓库为空，则任意文件都可以直接被推送到这个仓库，往后该文件就是本地仓库
*
* 若初始远程仓库不为空，则只有本地仓库可以进行提交操作
* 对于其他文件，只能先进行 clone 操作后才能成为本地仓库，获取执行提交的权限
*
*/






