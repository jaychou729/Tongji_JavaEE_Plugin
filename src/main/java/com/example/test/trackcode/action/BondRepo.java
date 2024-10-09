package com.example.test.trackcode.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.example.test.trackcode.dialog.GitBondDialog;
import com.example.test.trackcode.dialog.RepoCreateDialog;
import com.example.test.trackcode.message.MessageOutput;

public class BondRepo extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        MessageOutput.TakeMessage("hhhhhhhhhhhh");
    }
}
