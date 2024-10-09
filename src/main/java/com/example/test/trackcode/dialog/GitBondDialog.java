package com.example.test.trackcode.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

// TODO 将这个弹窗的关闭按钮去掉
public class GitBondDialog extends DialogWrapper {

    private JTextField tfUserName;
    private JTextField tfPassWord;
    private JTextField tfToken;
    private JTextField tfURL;

    public GitBondDialog() {
        super(true); // 模态对话框
        init();
        setTitle("绑定远程仓库");
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
        panel.add(new JLabel("仓库地址："), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tfURL = new JTextField(30);
        panel.add(tfURL, gbc);


        return panel;
    }


    // 承载创建并初始化的按钮
    protected JComponent createSouthPanel(){
        JPanel panel = new JPanel(new FlowLayout());
        JButton btnNoRepo = new JButton("暂无远程仓库");
        JButton btnBondRepo = new JButton("绑定远程仓库");

        btnNoRepo.addActionListener(e -> {
            this.close(OK_EXIT_CODE);
            RepoCreateDialog dialog = new RepoCreateDialog();
            dialog.show();
        });

        btnBondRepo.addActionListener(e -> {
            this.close(OK_EXIT_CODE);
            // TODO 保存信息、弹出选择克隆还是初始化的弹窗
        });

        panel.add(btnNoRepo);
        panel.add(btnBondRepo);
        return panel;
    }
}
