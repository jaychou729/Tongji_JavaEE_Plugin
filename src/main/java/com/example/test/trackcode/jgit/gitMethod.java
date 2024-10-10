package com.example.test.trackcode.jgit;

import com.example.test.trackcode.message.MessageOutput;
import com.example.test.trackcode.storage.PersistentStorage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.Stream;
import java.net.URI;

import static org.apache.commons.io.file.PathUtils.deleteFile;

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

        // 删除当前项目的内容
        Path directory = Paths.get(localPathStr);
        if (Files.exists(directory)) {
            try (Stream<Path> files = Files.walk(directory)) {
                files.sorted(Comparator.reverseOrder())  // 先删除子文件和子文件夹
                        .map(Path::toFile)
                        .forEach(File::delete);  // 删除文件
            }
        }

        // 指定克隆的仓库和分支
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(PersistentStorage.getInstance().getUrl())
                .setDirectory(new File(localPathStr))
                .setCloneAllBranches(true);

        // 执行克隆操作
        try (Git git = cloneCommand.call()) {
            System.out.println("克隆成功");
        }catch (Exception e) {
            System.out.println("克隆失败");
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



    public static void commitFile(String FileName){

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






