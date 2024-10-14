package com.example.test.trackcode.jgit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.example.test.trackcode.datastruct.CodeVersion;
import com.example.test.trackcode.message.MessageOutput;
import com.example.test.trackcode.storage.PersistentStorage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
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

import static org.apache.commons.io.file.PathUtils.deleteFile;
import static org.apache.tools.ant.types.resources.MultiRootFileSet.SetType.file;

public class gitMethod {
    // 本地仓库初始化并初始化远程仓库
    public static void InitRepo() throws GitAPIException, IOException {
        try {
            // 获取当前文件夹位置
            String localPathStr = ProjectManager.getInstance().getOpenProjects()[0].getBasePath();

            // 定义本地项目路径
            File localPath = new File(localPathStr);

            // 初始化本地仓库
            Git git = Git.init().setDirectory(localPath).call();
            System.out.println("Initialized local repository.");

            // 添加文件并提交
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
            System.out.println("Files added and committed.");

            // 添加远程仓库地址
            git.remoteAdd().setName("origin").setUri(new URIish(PersistentStorage.getInstance().getUrl())).call();
            System.out.println("Remote repository added.");

            // 检查是否存在 main 分支，如果没有则创建
            if (git.getRepository().findRef("refs/heads/main") == null) {
                // 本地不存在 main 分支，创建分支
                git.branchCreate().setName("main").call();
                System.out.println("创建 main 分支");
            }

            // 推送到远程仓库
            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(PersistentStorage.getInstance().getUsername(), PersistentStorage.getInstance().getToken()))
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec("refs/heads/main:refs/heads/main"))
                    .call();
            System.out.println("Pushed to remote repository.");
        } catch (Exception e) {
            e.printStackTrace();
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

        // Step 1: 获取主分支的 SHA 值
        String getShaUrl = "https://api.github.com/repos/" + PersistentStorage.getInstance().getOwner() + "/" +
                PersistentStorage.getInstance().getRepoName() + "/git/refs/heads/main";  // 主分支

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

        // Step 2: 使用获取到的 SHA 在远程仓库创建新的分支
        String createBranchUrl = "https://api.github.com/repos/" + PersistentStorage.getInstance().getOwner() + "/" +
                PersistentStorage.getInstance().getRepoName() + "/git/refs";

        URL postUrl = new URL(createBranchUrl);
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Authorization", "token " + PersistentStorage.getInstance().getToken());
        postConn.setRequestProperty("Content-Type", "application/json");

        // 构建 JSON 请求体
        String payload = String.format("{\"ref\": \"refs/heads/%s\", \"sha\": \"%s\"}", "Version", sha);
        postConn.setDoOutput(true);
        try (OutputStream os = postConn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = postConn.getResponseCode();
        if (responseCode == 201) {
            System.out.println("New branch 'Version' created successfully.");
        } else {
            System.out.println("Failed to create branch. Response Code: " + responseCode);
        }

        postConn.disconnect();
        deleteAllFilesInBranch();
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
        String jsonPayload = String.format("{\"message\": \"%s\", \"content\": \"%s\", \"branch\": \"%s\"}", message, content, "Version");
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



    public static boolean isValidBase64(String base64) {
        String base64Pattern = "^[A-Za-z0-9+/=]+$";
        return base64.matches(base64Pattern);
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

        // 按照日期和时间进行排序
        Collections.sort(versions, new Comparator<CodeVersion>() {
            @Override
            public int compare(CodeVersion v1, CodeVersion v2) {
                String dateTime1 = v1.getDate() + " " + v1.getTime();
                String dateTime2 = v2.getDate() + " " + v2.getTime();
                return dateTime1.compareTo(dateTime2);  // 先按日期再按时间排序
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

}



/* git 操作流程 */
/*
* 若初始远程仓库为空，则任意文件都可以直接被推送到这个仓库，往后该文件就是本地仓库
*
* 若初始远程仓库不为空，则只有本地仓库可以进行提交操作
* 对于其他文件，只能先进行 clone 操作后才能成为本地仓库，获取执行提交的权限
*
*/






