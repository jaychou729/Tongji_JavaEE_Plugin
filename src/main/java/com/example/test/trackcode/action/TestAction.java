package com.example.test.trackcode.action;

import com.example.test.LocalHistoryDocumentListener;
import com.example.test.trackcode.datastruct.CodeVersion;
import com.example.test.trackcode.dialog.ShowDiffDialog;
import com.example.test.trackcode.jgit.gitMethod;
import com.example.test.trackcode.storage.PersistentStorage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {


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
            List<CodeVersion> versions= null;
            try {
                versions = gitMethod.fetchFilesFromGitHubFolder(owner,repo,branch,folderPath,token);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            ShowDiffDialog showDiffDialog = new ShowDiffDialog("Main.java",versions,versions.get(0).getCode());
            showDiffDialog.show();
        });
    }
}