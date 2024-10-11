package com.example.test.trackcode.action;

import com.example.test.trackcode.dialog.GitBondDialog;
import com.example.test.trackcode.storage.PersistentStorage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageDialogBuilder;

public class ReBondRepo extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ApplicationManager.getApplication().invokeLater(() -> {
            GitBondDialog dialog = new GitBondDialog();
            dialog.show();
        });







    }
}
