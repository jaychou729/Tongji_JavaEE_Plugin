package com.example.test.trackcode.action;

import com.example.test.trackcode.listener.LocalHistoryDocumentListener;
import com.example.test.trackcode.datastruct.CodeVersion;
import com.example.test.trackcode.dialog.ShowDiffDialog;
import com.example.test.trackcode.jgit.gitMethod;
import com.example.test.trackcode.storage.PersistentStorage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.List;

public class ShowDiffAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();


        ApplicationManager.getApplication().invokeLater(() -> {
//            List<CodeVersion> versions = new ArrayList<>();
//            versions.add(new CodeVersion("111","111","111"));
//            versions.add(new CodeVersion("222","222","222"));
//            versions.add(new CodeVersion("333","333","333"));


            String owner= PersistentStorage.getInstance().getOwner();
            String repo=PersistentStorage.getInstance().getRepoName();
            String branch="Version";
            String folderPath= LocalHistoryDocumentListener.getRelativePath(e.getProject());
            String token=PersistentStorage.getInstance().getToken();
            VirtualFile currentFile = FileEditorManager.getInstance(project).getSelectedEditor().getFile();
            String fileName = currentFile.getName();
            List<CodeVersion> versions= null;
            try {
                versions = gitMethod.fetchFilesFromGitHubFolder(owner,repo,branch,folderPath,token);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            ShowDiffDialog showDiffDialog = new ShowDiffDialog(project,fileName,versions,versions.get(0).getCode());
            showDiffDialog.show();
        });
    }
}