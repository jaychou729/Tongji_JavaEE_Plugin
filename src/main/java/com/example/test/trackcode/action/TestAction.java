package com.example.test.trackcode.action;

import com.example.test.trackcode.dialog.GitBondDialog;
import com.example.test.trackcode.dialog.ShowDiffDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;

public class TestAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ShowDiffDialog showDiffDialog = new ShowDiffDialog();
            showDiffDialog.show();
        });
    }
}
