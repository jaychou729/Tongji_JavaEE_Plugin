package com.example.test.trackcode.action;

import com.example.test.trackcode.jgit.gitMethod;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.example.test.trackcode.dialog.GitBondDialog;
import com.example.test.trackcode.dialog.RepoCreateDialog;
import com.example.test.trackcode.message.MessageOutput;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

public class BondRepo extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {

    }
}
