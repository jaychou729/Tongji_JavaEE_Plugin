package com.example.test.trackcode.action;

import com.example.test.trackcode.datastruct.CodeVersion;
import com.example.test.trackcode.dialog.ShowDiffDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;

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
            CodeVersion[] versions ={
                    new CodeVersion("2024-10-14","10:00:00","first line\nsecond line\nthird line"),
                    new CodeVersion("2024-10-15","12:37:11","fff\nhhh\nccc")
            };
            ShowDiffDialog showDiffDialog = new ShowDiffDialog("Main.java",versions,code);
            showDiffDialog.show();
        });
    }
}