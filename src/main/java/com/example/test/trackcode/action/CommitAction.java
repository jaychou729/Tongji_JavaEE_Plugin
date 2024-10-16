package com.example.test.trackcode.action;

import com.example.test.trackcode.jgit.gitMethod;
import com.example.test.trackcode.message.MessageOutput;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;

public class CommitAction extends AnAction {
    private static final Logger logger = Logger.getInstance(CommitAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            gitMethod.commitAndPushChanges();
            MessageOutput.TakeMessage("提交成功");
        } catch (IOException ex) {
            MessageOutput.TakeMessage("提交失败");
            throw new RuntimeException(ex);
        }
        
    }
}
