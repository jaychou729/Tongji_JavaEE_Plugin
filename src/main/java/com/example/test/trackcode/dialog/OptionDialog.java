package com.example.test.trackcode.dialog;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;

import javax.swing.*;
import java.awt.*;

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
            // TODO 用本地项目初始化远程仓库、成功消息提示、Exception捕捉消息提示

            this.close(OK_EXIT_CODE);
        });

        panel.add(btnClone);
        panel.add(btnInit);
        return panel;
    }
}