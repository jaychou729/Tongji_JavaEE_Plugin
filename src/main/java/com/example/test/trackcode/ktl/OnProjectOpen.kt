package com.example.test.trackcode.ktl

import com.example.test.trackcode.dialog.GitBondDialog
import com.example.test.trackcode.jgit.gitMethod
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.startup.ProjectActivity
import org.eclipse.jgit.api.errors.GitAPIException
import java.io.IOException


// TODO 添加检测逻辑，当该项目的信息已经保存好后，不再显示弹窗
class OnProjectOpen : ProjectActivity {
    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            // 创建并显示对话框
            val dialog = GitBondDialog()
            dialog.show()  // 显示对话框
        }
    }
}