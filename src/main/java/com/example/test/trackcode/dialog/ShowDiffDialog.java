package com.example.test.trackcode.dialog;


import com.example.test.trackcode.datastruct.Vension;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class ShowDiffDialog extends DialogWrapper {

    String fileName = "Main.java";
    String curCode;
    Vension[] vesionList;

    // TODO 通过构造函数获取参数列表
    public ShowDiffDialog() {
        super(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int)(screenSize.width*0.8), (int)(screenSize.height*0.8));
        this.setTitle("versions of " + fileName);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        // 承载整个界面的面板
        JPanel backgroundPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 版本 list 菜单栏
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        BorderLayout layout1 = new BorderLayout();
        layout1.setVgap(20);
        JPanel panel1 = new JPanel(layout1);  // 列表面板
        panel1.add(new JLabel("版本列表"),BorderLayout.NORTH);  // 添加小标题
        JScrollPane scrollPane = new JScrollPane();  // 滚轮面板
        JList<String> list = new JList<>(new String[] {"Version1","Version2","Version3"});  // 列表内容

        // 创建自定义的渲染器
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                // 创建一个 JPanel 作为容器
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());

                // 设置边框
                panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2)); // 设置灰色边框，厚度为 2

                // 创建 JLabel 并设置字体样式和内容
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Arial", Font.BOLD, 16)); // 设置字体为 Arial，粗体，大小为 16
                label.setPreferredSize(new Dimension(200, 30)); // 设置每一项的大小

                // 将 JLabel 添加到 JPanel 中
                panel.add(label, BorderLayout.CENTER);

                // 选中时改变背景颜色
                if (isSelected) {
                    panel.setBackground(Color.LIGHT_GRAY); // 选中时设置背景颜色
                } else {
                    panel.setBackground(Color.WHITE); // 默认背景颜色
                }

                // 返回带边框的面板
                return panel;
            }
        };

        // 应用自定义渲染器
        list.setCellRenderer(renderer);

        scrollPane.setViewportView(list);   // 添加列表内容
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        panel1.add(scrollPane, BorderLayout.CENTER);  // 添加滚轮面板
        backgroundPanel.add(panel1,gbc);


        // 对应版本的代码展示栏
        gbc.gridx = 1;
        gbc.weightx = 3;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        BorderLayout layout2 = new BorderLayout();
        layout2.setVgap(20);
        JPanel panel2 = new JPanel(layout2);
        panel2.add(new JLabel("版本代码展示区"),BorderLayout.NORTH);  // 添加小标题
        JPanel vensionCodePanel = new JPanel(new BorderLayout());
        vensionCodePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // 设置边框
        // TODO 放置代码
        // 创建 RSyntaxTextArea
        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setText("System.out.println(\"hello world!!\");");
        textArea.setEditable(false); // 设置为不可编辑
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // 根据文件类型设置语法样式
        textArea.setCodeFoldingEnabled(true); // 启用代码折叠
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); // 设置字体
        textArea.setBackground(Color.DARK_GRAY);

        // 创建带滚动条的面板
        RTextScrollPane testPanel = new RTextScrollPane(textArea);
        vensionCodePanel.add(testPanel, BorderLayout.CENTER);



        panel2.add(vensionCodePanel,BorderLayout.CENTER);
        backgroundPanel.add(panel2,gbc);


        // 当前版本的代码展示栏
        gbc.gridx = 2;
        gbc.weightx = 3;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        BorderLayout layout3 = new BorderLayout();
        layout3.setVgap(20);
        JPanel panel3 = new JPanel(layout3);
        panel3.add(new JLabel("当前代码展示区"),BorderLayout.NORTH);  // 添加小标题
        JPanel curCodePanel = new JPanel(new BorderLayout());
        curCodePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        // TODO 放置代码
        RSyntaxTextArea textArea2 = new RSyntaxTextArea();
        textArea2.setText("System.out.println(\"hello world!!\");");
        textArea2.setEditable(false); // 设置为不可编辑
        textArea2.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // 根据文件类型设置语法样式
        textArea2.setCodeFoldingEnabled(true); // 启用代码折叠
        textArea2.setFont(new Font("Monospaced", Font.PLAIN, 12)); // 设置字体
        textArea2.setBackground(Color.DARK_GRAY);

        // 创建带滚动条的面板
        RTextScrollPane testPanel2 = new RTextScrollPane(textArea2);
        curCodePanel.add(testPanel2, BorderLayout.CENTER);


        panel3.add(curCodePanel,BorderLayout.CENTER);
        backgroundPanel.add(panel3,gbc);


        // 返回整个面板
        return backgroundPanel;
    }


    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        JButton btnClose = new JButton("关闭");
        JButton btnRollBack = new JButton("回退到当前版本");

        btnClose.addActionListener(e -> {
            // 关闭当前窗口
            this.close(OK_EXIT_CODE);
        });

        btnRollBack.addActionListener(e -> {
            // TODO 回退到当前选择的版本
        });

        panel.add(btnRollBack);
        panel.add(btnClose);
        return panel;
    }
}
