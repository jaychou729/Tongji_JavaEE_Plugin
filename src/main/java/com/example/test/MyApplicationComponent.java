package com.example.test;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import java.awt.event.InputMethodListener;

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
    }

    @Override
    public void disposeComponent() {
        // 移除监听器
        for (Document document : FileDocumentManager.getInstance().getUnsavedDocuments()) {
            document.removeDocumentListener(documentListener);
        }
    }
}

