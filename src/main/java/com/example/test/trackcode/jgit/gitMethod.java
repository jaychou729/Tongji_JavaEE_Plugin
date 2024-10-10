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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

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

}



/* git 操作流程 */
/*
* 若初始远程仓库为空，则任意文件都可以直接被推送到这个仓库，往后该文件就是本地仓库
*
* 若初始远程仓库不为空，则只有本地仓库可以进行提交操作
* 对于其他文件，只能先进行 clone 操作后才能成为本地仓库，获取执行提交的权限
*
*/






