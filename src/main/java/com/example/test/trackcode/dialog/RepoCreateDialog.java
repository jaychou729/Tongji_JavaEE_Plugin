package com.example.test.trackcode.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;


// TODO 将这个弹窗的关闭按钮去掉
public class RepoCreateDialog extends DialogWrapper {

    private JTextField tfUserName;
    private JTextField tfPassWord;
    private JTextField tfToken;
    private JTextField tfRepoName;
    private JTextField tfDescription;
    private JRadioButton btnIsPrivate;

    public RepoCreateDialog() {
        super(true); // 模态对话框
        init();
        setTitle("新建远程仓库");
        setSize(600,300);
    }

    @Nullable
    @Override
    // 承载输入框
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); // 使用 GridBagLayout 布局
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 设置内边距

        // 第一行输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("用户名："), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tfUserName = new JTextField(30);
        panel.add(tfUserName, gbc);

        // 第二行输入
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("密码："), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tfPassWord = new JTextField(30);
        panel.add(tfPassWord, gbc);

        // 第三行输入
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("仓库令牌："), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tfToken = new JTextField(30);
        panel.add(tfToken, gbc);

        // 第四行输入
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("仓库名："), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tfRepoName = new JTextField(30);
        panel.add(tfRepoName, gbc);

        // 第五行输入
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("仓库描述："), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tfDescription = new JTextField(30);
        panel.add(tfDescription, gbc);

        // 单选框
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;  // 占据两列
        gbc.anchor = GridBagConstraints.WEST;
        btnIsPrivate = new JRadioButton("仓库私有");
        panel.add(btnIsPrivate, gbc);

        return panel;
    }


    // 承载创建并初始化的按钮
    protected JComponent createSouthPanel(){
        JPanel panel = new JPanel();
        JButton button = new JButton("创建并初始化远程仓库");

        button.addActionListener(e -> {
            // TODO 存储信息、创建并初始化仓库、成功消息提示、Exception捕捉消息提示

            this.close(OK_EXIT_CODE);
        });

        panel.add(button);
        return panel;
    }
}
