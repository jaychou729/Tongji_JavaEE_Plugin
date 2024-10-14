package com.example.test;

import com.example.test.trackcode.jgit.gitMethod;
import com.example.test.trackcode.storage.PersistentStorage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;

import com.intellij.openapi.util.Key;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;

import org.eclipse.jgit.api.Git;

import com.github.difflib.DiffUtils;
import java.util.Arrays;
import java.util.List;


public class LocalHistoryDocumentListener implements DocumentListener {


    private final Project project;
    private static final Logger logger = Logger.getInstance(LocalHistoryDocumentListener.class);
    private static final long SAVE_DELAY = 5000; // 延迟1秒保存，减少输入法中间状态的捕获
    private Timer timer;
    public static final Key<LocalHistoryDocumentListener> KEY = new Key<>("LocalHistoryInputMethodListener");

    public LocalHistoryDocumentListener(Project project, String filePath) {
        this.project = project;
        timer = new Timer();
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {

        Document document = event.getDocument();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);


        if (psiFile != null) {
            // 取消之前的计时器任务，避免频繁保存
            resetSaveTimer();

            System.out.println("documentChanged executed");

        }
    }

    // 重置计时器，延迟处理
    private void resetSaveTimer() {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    saveEditorContent();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, SAVE_DELAY);
    }



    public void saveEditorContent() throws IOException {
        // 获取当前打开的编辑器
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        if (editor != null) {
            Document document = editor.getDocument();
            String content = document.getText();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String timestamp = LocalDateTime.now().format(formatter);

            // 获取当前文件名或路径（根据需求修改保存的路径）
            VirtualFile file = FileEditorManager.getInstance(project).getSelectedFiles()[0];
            String fileName = file.getNameWithoutExtension();

            // 定义保存的路径（如：项目根目录下的一个 txt 文件）
            String filePath = project.getBasePath() + "/.history/" + fileName + "_record_"+timestamp+".txt";

            // 使用 OutputStreamWriter 指定编码为 UTF-8 来避免乱码
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8")) {
                writer.write(content);
                System.out.println("Saved to: " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String FileName=fileName + "_record_"+timestamp+".txt";
            commitToGithub(filePath,FileName);
        } else {
            System.out.println("No opened editor");
        }
    }

    public void commitToGithub(String filePath,String FileName) throws IOException {
        File file = new File(filePath);

        String FolderName=getRelativePath(this.project);

        if(gitMethod.isFolderPresent(
                PersistentStorage.getInstance().getOwner(),
                PersistentStorage.getInstance().getRepoName(),
                "Version",
                FolderName,
                PersistentStorage.getInstance().getToken()
                )){

            gitMethod.commitFile(FileName,FolderName,file);

        }
        else{
            gitMethod.createFolder(FolderName);
            gitMethod.commitFile(FileName,FolderName,file);
        }

    }

    public static String getRelativePath(Project project) {
        // 获取项目的根目录
        VirtualFile projectBaseDir = project.getBaseDir();

        // 获取当前选中的文件
        VirtualFile selectedFile = FileEditorManager.getInstance(project).getSelectedFiles()[0];

        // 获取项目根目录的路径和文件的路径
        String projectBasePath = projectBaseDir.getPath();
        String filePath = selectedFile.getPath();

        // 返回文件相对于项目根目录的相对路径
        if (filePath.startsWith(projectBasePath)) {
            String original=filePath.substring(projectBasePath.length() + 1); // +1 是为了去掉开头的 "/"
            return original.replaceAll("[/.]", "_");
        } else {
            return null; // 如果文件不在项目根目录下
        }
    }




}
