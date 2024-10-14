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
import java.util.List;

public class TestAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        String code = "// 这是一行注释\n" + "@Override\n" +
                "public void actionPerformed(AnActionEvent e) {\n" +
                "    ApplicationManager.getApplication().invokeLater(() -> {\n" +
                "        Vension[] versions ={\n" +
                "                new Vension(\"2024-10-14\",\"10:00:00\",\"first line\\nsecond line\\nthird line\"),\n" +
                "                new Vension(\"2024-10-15\",\"12:37:11\",\"fff\\nhhh\\nccc\")\n" +
                "        };\n" +
                "        ShowDiffDialog showDiffDialog = new ShowDiffDialog(\"Main.java\",versions,\"curCode\");\n" +
                "        showDiffDialog.show();\n" +
                "    });\n" +
                "}";

        ApplicationManager.getApplication().invokeLater(() -> {
//            CodeVersion[] versions = {
//                    new CodeVersion("2024-10-14","10:00:00","first line\nsecond line\nthird line"),
//                    new CodeVersion("2024-10-15","12:37:11","fff\nhhh\nccc")
//            };

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
            ShowDiffDialog showDiffDialog = new ShowDiffDialog("Main.java",versions,code);
            showDiffDialog.show();
        });
    }
}