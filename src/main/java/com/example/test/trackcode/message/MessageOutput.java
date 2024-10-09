package com.example.test.trackcode.message;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.MessageDialogBuilder;

public class MessageOutput {
    public static void TakeMessage(String message) {
        ApplicationManager.getApplication().invokeLater(() -> {
            MessageDialogBuilder.yesNo("系统提示", message)
                .yesText("确定")  // "确定" 按钮
                .noText("关闭")  // "取消" 按钮
                .show();
        });
    }
}
