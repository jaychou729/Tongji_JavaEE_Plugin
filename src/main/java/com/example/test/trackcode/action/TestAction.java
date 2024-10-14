package com.example.test.trackcode.action;

import com.example.test.trackcode.datastruct.Vension;
import com.example.test.trackcode.dialog.GitBondDialog;
import com.example.test.trackcode.dialog.ShowDiffDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;

public class TestAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        String code = "@Override\n" +
                "public void actionPerformed(AnActionEvent e) {\n" +
                "    ApplicationManager.getApplication().invokeLater(() -> {\n" +
                "        Vension[] versions ={\n" +
                "                new Vension(\"2024-10-14\",\"10:00:00\",\"first line\\nsecond line\\nthird line\"),\n" +
                "                new Vension(\"2024-10-15\",\"12:37:11\",\"fff\\nhhh\\nccc\")\n" +
                "        };\n" +
                "        ShowDiffDialog showDiffDialog = new ShowDiffDialog(\"Main.java\",versions,\"cur\\ncode\");\n" +
                "        showDiffDialog.show();\n" +
                "    });\n" +
                "}";

        ApplicationManager.getApplication().invokeLater(() -> {
            Vension[] versions ={
                    new Vension("2024-10-14","10:00:00","first line\nsecond line\nthird line"),
                    new Vension("2024-10-15","12:37:11","fff\nhhh\nccc")
            };
            ShowDiffDialog showDiffDialog = new ShowDiffDialog("Main.java",versions,code);
            showDiffDialog.show();
        });
    }
}