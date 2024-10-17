package com.example.test.trackcode.listener;

import com.example.test.trackcode.jgit.gitMethod;
import com.example.test.trackcode.storage.PersistentStorage;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MyApplicationComponent implements ApplicationComponent {

    private LocalHistoryDocumentListener documentListener;
    private final Project project;
    public MyApplicationComponent(Project project) {this.project = project;}

    @Override
    public void initComponent() {
        String projectBasePath = ProjectManager.getInstance().getOpenProjects()[0].getBasePath();
        String filePath = projectBasePath + "/.history/myfile_history.txt";
        documentListener = new LocalHistoryDocumentListener(project, filePath);

        // 获取所有已打开文档并注册监听器
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            Document document = editor.getDocument();
            document.addDocumentListener(documentListener);
            System.out.println("Listener added to document: " + document.toString());
        }


        // 监听新打开的文档并注册监听器
        EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
            @Override
            public void editorCreated(@NotNull EditorFactoryEvent event) {
                Document document = event.getEditor().getDocument();
                Editor editor = event.getEditor();

                document.addDocumentListener(documentListener);
                editor.putUserData(LocalHistoryDocumentListener.KEY, documentListener);
                System.out.println("Listener added to new document.");
            }

            @Override
            public void editorReleased(@NotNull EditorFactoryEvent event) {
                Editor editor = event.getEditor();
                Document document = editor.getDocument();

                // 获取并移除 DocumentListener
                LocalHistoryDocumentListener documentListener = editor.getUserData(LocalHistoryDocumentListener.KEY);
                if (documentListener != null) {
                    document.removeDocumentListener(documentListener);
                    editor.putUserData(LocalHistoryDocumentListener.KEY, null); // 清除存储的引用
                }
            }
        }, project);

        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                System.out.println("File created: " + event.getFile().getPath());
            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent event) {
                String path = event.getFile().getPath();
                String relativePath = path.replace(projectBasePath + "/", "");
                String folderName=relativePath.replaceAll("[/.]", "_");
                try {
                    gitMethod.deleteFolderOnGitHub(
                            PersistentStorage.getInstance().getOwner(),
                            PersistentStorage.getInstance().getRepoName(),
                            "Version",
                            folderName,
                            PersistentStorage.getInstance().getToken()
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("File deleted: " + event.getFile().getPath());
            }

            @Override
            public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
                // 检查是否是文件名的变更（重命名）
                if (VirtualFile.PROP_NAME.equals(event.getPropertyName())) {
                    String oldNameWithExt = (String) event.getOldValue();  // 原始名称（包含后缀）
                    String newNameWithExt = (String) event.getNewValue();  // 新名称（包含后缀）

                    // 移除文件后缀
                    String oldName = oldNameWithExt.replaceFirst("\\.[^.]+$", "");
                    String newName = newNameWithExt.replaceFirst("\\.[^.]+$", "");

                    // 获取旧路径和新路径
                    String oldPath = event.getFile().getParent().getPath() + "/" + oldNameWithExt;
                    String newPath = event.getFile().getPath();

                    System.out.println("File/Folder renamed:");
                    System.out.println("Old Name: " + oldName + ", New Name: " + newName);
                    System.out.println("Old Path: " + oldPath + ", New Path: " + newPath);

                    // 获取相对路径
                    String relativeOldPath = oldPath.replace(projectBasePath + "/", "");
                    String relativeNewPath = newPath.replace(projectBasePath + "/", "");

                    // 将路径转换为用于存储的格式
                    String oldFolderName = relativeOldPath.replaceAll("[/.]", "_");

                    // 调用 gitMethod 更新文件名
                    try {
                        gitMethod.updateFileName(oldFolderName, newName, oldName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            @Override
            public void fileMoved(@NotNull VirtualFileMoveEvent event) {
                // 移动前的名字和路径
                String oldName = event.getFile().getName();  // 文件或文件夹名在移动过程中不变
                String oldPath = event.getOldParent().getPath() + "/" + oldName;

                // 移动后的名字和路径
                String newName = event.getFile().getName();  // 名字保持不变，但路径发生改变
                String newPath = event.getFile().getPath();

                System.out.println("File/Folder moved:");
                System.out.println("Name: " + oldName);
                System.out.println("Old Path: " + oldPath + ", New Path: " + newPath);

                String relativeOldPath = oldPath.replace(projectBasePath + "/", "");
                String relativeNewPath = newPath.replace(projectBasePath + "/", "");

                String oldFolderName = relativeOldPath.replaceAll("[/.]", "_");
                String newFolderName = relativeNewPath.replaceAll("[/.]", "_");

                try {
                    gitMethod.renameFolderOnGitHub(
                            PersistentStorage.getInstance().getOwner(),
                            PersistentStorage.getInstance().getRepoName(),
                            "Version",
                            oldFolderName,
                            newFolderName,
                            PersistentStorage.getInstance().getToken()
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }, project);


    }

    @Override
    public void disposeComponent() {
        // 移除监听器
        for (Document document : FileDocumentManager.getInstance().getUnsavedDocuments()) {
            document.removeDocumentListener(documentListener);
        }
    }
}

