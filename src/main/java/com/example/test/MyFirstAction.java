package com.example.test;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;

public class MyFirstAction extends AnAction {

    private static final Logger logger = Logger.getInstance(MyFirstAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            // 记录当前项目名称
            logger.info("Current project: " + project.getName());
            System.out.println("Current project: " + project.getName());
        }
        MyApplicationComponent myapp = new MyApplicationComponent(project);
        myapp.initComponent();
    }
}
