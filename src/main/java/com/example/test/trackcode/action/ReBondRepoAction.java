package com.example.test.trackcode.action;

import com.example.test.trackcode.dialog.GitBondDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;

public class ReBondRepoAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ApplicationManager.getApplication().invokeLater(() -> {
            GitBondDialog dialog = new GitBondDialog();
            dialog.show();
        });
    }
}
