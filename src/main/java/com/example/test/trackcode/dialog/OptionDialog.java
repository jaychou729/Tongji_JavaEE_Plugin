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
        JButton btnInit = new JButton("初始化远程仓库");

        btnClone.addActionListener(e -> {
            // TODO 从远程仓库克隆项目到本地、成功消息提示、Exception捕捉消息提示

            this.close(OK_EXIT_CODE);
        });

        btnInit.addActionListener(e -> {
            try{
                gitMethod.InitRepo();
                MessageOutput.TakeMessage("推送远程仓库成功");
                this.close(OK_EXIT_CODE);
            }catch (GitAPIException | IOException ex) {
                System.out.println("error");
                MessageOutput.TakeMessage("推送远程仓库失败,稍后重试");
            }
        });

        panel.add(btnClone);
        panel.add(btnInit);
        return panel;
    }
}