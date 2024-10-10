package com.example.test.trackcode.dialog;


import com.example.test.trackcode.jgit.gitMethod;
import com.example.test.trackcode.message.MessageOutput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import org.eclipse.jgit.api.errors.GitAPIException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

// TODO 将这个弹窗的关闭按钮去掉
public class OptionDialog extends DialogWrapper {
    public OptionDialog() {
        super(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel(){
        JPanel panel = new JPanel();
        return panel;
    }

    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JButton btnClone = new JButton("克隆到本地项目");

        btnClone.addActionListener(e -> {
            try {
                gitMethod.CloneRepo();
                MessageOutput.TakeMessage("克隆已完成");
                this.close(OK_EXIT_CODE);
            } catch (GitAPIException | IOException ex) {
                MessageOutput.TakeMessage("克隆失败，稍后重试");
            }

            try{
                gitMethod.createBranch();
                MessageOutput.TakeMessage("新建分支");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        panel.add(btnClone);
        return panel;
    }
}