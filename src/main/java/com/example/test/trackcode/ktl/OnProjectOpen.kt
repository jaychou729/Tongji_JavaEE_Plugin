package com.example.test.trackcode.ktl

import com.example.test.trackcode.listener.MyApplicationComponent
import com.example.test.trackcode.dialog.GitBondDialog
import com.example.test.trackcode.storage.PersistentStorage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity


// TODO 添加检测逻辑，当该项目的信息已经保存好后，不再显示弹窗
class OnProjectOpen : ProjectActivity {
    override suspend fun execute(project: Project) {

        val username = PersistentStorage.getInstance().username
        val password = PersistentStorage.getInstance().password
        val token = PersistentStorage.getInstance().token
        val url = PersistentStorage.getInstance().url

        if (username == null || password == null || token == null || url == null) {
            ApplicationManager.getApplication().invokeLater {
                val dialog = GitBondDialog()
                dialog.show()
            }
        } else if ("" == username || "" == password || "" == token || "" == url){
                ApplicationManager.getApplication().invokeLater {
                    val dialog = GitBondDialog()
                    dialog.show()
                }
        }
        else{
            val myapp = MyApplicationComponent(project)
            myapp.initComponent()
        }
    }
}