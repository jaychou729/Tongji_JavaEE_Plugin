package com.example.test.trackcode.dialog;

import com.example.test.trackcode.listener.MyApplicationComponent;
import com.example.test.trackcode.jgit.gitMethod;
import com.example.test.trackcode.message.MessageOutput;
import com.example.test.trackcode.network.GitHubRepositoryCreator;
import com.example.test.trackcode.storage.PersistentStorage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


// TODO 将这个弹窗的关闭按钮去掉
public class RepoCreateDialog extends DialogWrapper {

    private JTextField tfUserName;
    private JTextField tfPassWord;
    private JTextField tfToken;
    private JTextField tfRepoName;
    private JTextField tfDescription;
    private JRadioButton btnIsPrivate;
    private final Project project;

    public RepoCreateDialog() {
        super(true); // 模态对话框
        init();
        setTitle("新建远程仓库");
        setSize(600,300);
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        this.project = openProjects.length > 0 ? openProjects[0] : null; // 选择第一个打开的项目
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
        panel.add(new JLabel("用户令牌："), gbc);

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
            // 保存数据
            if(tfUserName.getText()==null||tfPassWord.getText()==null||tfToken.getText()==null||tfRepoName.getText()==null||tfDescription.getText()==null){
                MessageOutput.TakeMessage("输入不能为空");
            }
            else{
                if("".equals(tfUserName.getText()) || "".equals(tfPassWord.getText()) || "".equals(tfToken.getText())||"".equals(tfRepoName.getText())||"".equals(tfDescription.getText())){
                    MessageOutput.TakeMessage("输入不能为空");
                }
                else{
                    PersistentStorage.getInstance().setUsername(tfUserName.getText());
                    PersistentStorage.getInstance().setPassword(tfPassWord.getText());
                    PersistentStorage.getInstance().setToken(tfToken.getText());
                    PersistentStorage.getInstance().setUrl("https://github.com/"+tfUserName.getText()+"/"+tfRepoName.getText());
                    PersistentStorage.getInstance().setRepoName_Owner("https://github.com/"+tfUserName.getText()+"/"+tfRepoName.getText());
                    try {
                        // 保存数据到文件
                        PersistentStorage.getInstance().saveToFile();
                        MessageOutput.TakeMessage("数据存储成功");

                        // 创建新的远程仓库
                        try {
                            GitHubRepositoryCreator.createGitHubRepo(tfRepoName.getText(), tfDescription.getText(), btnIsPrivate.isSelected(), tfToken.getText());

                            // 初始化仓库并推送
                            try {
                                gitMethod.InitRepo();
                                MessageOutput.TakeMessage("推送远程仓库成功");
                                this.close(OK_EXIT_CODE);

                                // 在仓库推送成功后，异步创建分支
                                CompletableFuture.runAsync(() -> {
                                    try {
                                        gitMethod.createBranch();
                                        MessageOutput.TakeMessage("分支创建成功");
                                    } catch (IOException ex) {
                                        MessageOutput.TakeMessage("分支创建失败");
                                        ex.printStackTrace();
                                    }
                                });
                            } catch (GitAPIException | IOException ex) {
                                System.out.println("error");
                                MessageOutput.TakeMessage("仓库已创建，请尝试推送");
                                this.close(OK_EXIT_CODE);
                                GitBondDialog gitBondDialog = new GitBondDialog();
                                gitBondDialog.show();
                            }
                        } catch (Exception ex) {
                            MessageOutput.TakeMessage("仓库创建失败");
                            throw new RuntimeException(ex);
                        }
                    } catch (IOException E) {
                        E.printStackTrace();
                        MessageOutput.TakeMessage("数据存储失败，请重新输入");

                    }
                    MyApplicationComponent myapp = new MyApplicationComponent(project);
                    myapp.initComponent();
                }
            }
        });

        panel.add(button);
        return panel;
    }
}
