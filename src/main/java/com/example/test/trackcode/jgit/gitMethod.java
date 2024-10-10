package com.example.test.trackcode.jgit;

import com.example.test.trackcode.message.MessageOutput;
import com.example.test.trackcode.storage.PersistentStorage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

public class gitMethod {
    // 引入 IntelliJ 的日志系统
    private static final Logger logger = Logger.getInstance(gitMethod.class);

    public static void InitRepo() throws GitAPIException, IOException {
        try {
            String projectBasePath = ProjectManager.getInstance().getOpenProjects()[0].getBasePath();
            logger.info("Project base path: " + projectBasePath);

            String userName = PersistentStorage.getInstance().getUsername();
            String password = PersistentStorage.getInstance().getPassword();
            String token = PersistentStorage.getInstance().getToken();
            String url = PersistentStorage.getInstance().getUrl();
            logger.info("Git repository URL: " + url);

            Git git = null;
            git = Git.init().setDirectory(new File(projectBasePath)).call();
            logger.info("Initialized local Git repository");

            if (!git.getRepository().getBranch().equals("main")) {
                git.branchCreate().setName("main").call();
                git.checkout().setName("main").call();
            }


            StoredConfig config = git.getRepository().getConfig();
            config.setString("remote", "origin", "url", url);
            git.add().addFilepattern(".").call(); // 添加所有文件
            config.save();
            git.commit().setMessage("init").call();
            logger.info("Initial commit created");

            // 推送到远程仓库
            UsernamePasswordCredentialsProvider provider =
                    new UsernamePasswordCredentialsProvider(userName, token);
            git.push().setRemote("origin").setCredentialsProvider(provider)
                    .setRefSpecs(new RefSpec("refs/heads/main:refs/heads/main")).setForce(true).call();

            logger.info("Pushed to remote repository: " + url);
        } catch (Exception e) {
            logger.error("Failed to create Git repository", e);
            throw e;
        }
    }
}
