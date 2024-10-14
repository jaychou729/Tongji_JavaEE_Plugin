package com.example.test.trackcode.dialog;


import com.example.test.trackcode.datastruct.CodeVersion;
import com.intellij.openapi.ui.DialogWrapper;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ShowDiffDialog extends DialogWrapper {
    String curCode;
    CodeVersion[] versionList;

    public ShowDiffDialog(String fn, List<CodeVersion> vl, String cc) {
        super(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int)(screenSize.width*0.8), (int)(screenSize.height*0.8));
        this.setTitle("versions of " + fn);
        curCode = cc;
        versionList = vl.toArray(new CodeVersion[0]);
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

        String[] data = new String[versionList.length];
        for (int i = 0; i < versionList.length; i++) {
            data[i] = versionList[i].getDate() + "," + versionList[i].getTime();
        }
        JList<String> list = new JList<>(data);  // 列表内容

        // 创建自定义的渲染器
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

                String[] parts = value.toString().split(",", 2);  // 2 表示分割为最多两部分
                String date = parts[0];  // 逗号前的部分
                String time = parts[1];   // 逗号后的部分

                // 创建一个 JPanel 用于承载两行字符串
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // 边框

                // 创建 JLabel 用于显示每行字符串
                JLabel line1 = new JLabel(date); // 第一行
                JLabel line2 = new JLabel(time); // 第二行
                line1.setFont(new Font("Arial", Font.BOLD, 16)); // 设置第一行字体
                line2.setFont(new Font("Arial", Font.PLAIN, 12)); // 设置第二行字体

                // 设置背景颜色，区分选中和未选中状态
                if (isSelected) {
                    panel.setBackground(Color.BLUE);
                } else {
                    panel.setBackground(Color.DARK_GRAY);
                }

                // 将两行添加到面板中
                panel.add(line1, BorderLayout.NORTH); // 第一行在顶部
                panel.add(line2, BorderLayout.SOUTH); // 第二行在底部

                return panel; // 返回自定义的面板
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
        textArea.setText(curCode);
        textArea.setEditable(false); // 设置为不可编辑
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // 根据文件类型设置语法样式
        textArea.setCodeFoldingEnabled(true); // 启用代码折叠
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 18)); // 设置字体
        textArea.setBackground(Color.LIGHT_GRAY);
        textArea.setHighlightCurrentLine(false);

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
        textArea2.setText(curCode);
        textArea2.setEditable(false); // 设置为不可编辑
        textArea2.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // 根据文件类型设置语法样式
        textArea2.setCodeFoldingEnabled(true); // 启用代码折叠
        textArea2.setFont(new Font("Monospaced", Font.PLAIN, 18)); // 设置字体
        textArea2.setBackground(Color.LIGHT_GRAY);
        textArea2.setHighlightCurrentLine(false);


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
