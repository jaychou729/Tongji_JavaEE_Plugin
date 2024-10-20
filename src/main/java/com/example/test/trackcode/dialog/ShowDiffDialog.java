package com.example.test.trackcode.dialog;


import com.example.test.trackcode.datastruct.CodeVersion;
import com.example.test.trackcode.message.MessageOutput;
import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShowDiffDialog extends DialogWrapper {
    String curCode;
    CodeVersion[] versionList;
    int curSelectedIndex = -1;      // 初始情况下 -1 表示没有任何选择
    Project project;

    public ShowDiffDialog(Project pj, String fn, List<CodeVersion> vl, String cc) {
        super(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int)(screenSize.width*0.8), (int)(screenSize.height*0.8));
        this.setTitle("versions of " + fn);
        curCode = cc;
        versionList = vl.toArray(new CodeVersion[0]);
        project = pj;
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
                line1.setFont(new Font("Arial", Font.BOLD, 20)); // 设置第一行字体
                line2.setFont(new Font("Arial", Font.PLAIN, 16)); // 设置第二行字体

                // 设置背景颜色，区分选中和未选中状态
                if (isSelected) {
                    panel.setBackground(Color.BLUE);
                } else {
                    panel.setBackground(Color.DARK_GRAY);
                }

                // 将两行添加到面板中
                panel.add(line1, BorderLayout.NORTH); // 第一行在顶部
                panel.add(line2, BorderLayout.SOUTH); // 第二行在底部
                panel.setPreferredSize(new Dimension(100,50));
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
        // 创建 RSyntaxTextArea
        RSyntaxTextArea textArea = new RSyntaxTextArea();

        // 加载 IntelliJ IDEA 样式主题
        try {
            InputStream themeStream = getClass().getResourceAsStream("/themes/idea.xml");
            Theme theme = Theme.load(themeStream);
            theme.apply(textArea);  // 应用主题
        } catch (IOException e) {
            e.printStackTrace();
        }

        textArea.setText("");    // 初始没有任何选择，字符串为空
        textArea.setEditable(false); // 设置为不可编辑
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // 根据文件类型设置语法样式
        textArea.setCodeFoldingEnabled(true); // 启用代码折叠
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 18)); // 设置字体
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setHighlightCurrentLine(false);

        // 创建带滚动条的面板
        RTextScrollPane testPanel = new RTextScrollPane(textArea);
        vensionCodePanel.add(testPanel, BorderLayout.CENTER);


        panel2.add(vensionCodePanel,BorderLayout.CENTER);
        panel2.setPreferredSize(new Dimension(500,500));
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
        RSyntaxTextArea textArea2 = new RSyntaxTextArea();

        // 加载 IntelliJ IDEA 样式主题
        try {
            InputStream themeStream = getClass().getResourceAsStream("/themes/idea.xml");
            Theme theme = Theme.load(themeStream);
            theme.apply(textArea2);  // 应用主题
        } catch (IOException e) {
            e.printStackTrace();
        }

        textArea2.setText(curCode);
        textArea2.setEditable(false); // 设置为不可编辑
        textArea2.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); // 根据文件类型设置语法样式
        textArea2.setCodeFoldingEnabled(true); // 启用代码折叠
        textArea2.setFont(new Font("Monospaced", Font.PLAIN, 18)); // 设置字体
        textArea2.setBackground(Color.DARK_GRAY);
        textArea2.setHighlightCurrentLine(false);


        // 创建带滚动条的面板
        RTextScrollPane testPanel2 = new RTextScrollPane(textArea2);
        curCodePanel.add(testPanel2, BorderLayout.CENTER);


        panel3.add(curCodePanel,BorderLayout.CENTER);
        panel3.setPreferredSize(new Dimension(500,500));
        backgroundPanel.add(panel3,gbc);


        list.addListSelectionListener(e -> {
            curSelectedIndex = list.getSelectedIndex();
            String versionCode = versionList[curSelectedIndex].getCode();
            textArea.setText(versionCode);

            // 消除之前的高亮
            Highlighter versionHighlighter = textArea.getHighlighter();
            Highlighter curHighlighter = textArea2.getHighlighter();
            versionHighlighter.removeAllHighlights();
            curHighlighter.removeAllHighlights();

            // diff获取差别
            // 将字符串按行分割为 List<String>
            List<String> original = Arrays.asList(versionCode.split("\n"));
            List<String> revised = Arrays.asList(curCode.split("\n"));

            // 使用 DiffUtils 来生成代码差异的补丁
            Patch<String> patch = null;
            try {
                patch = DiffUtils.diff(original, revised);
            } catch (DiffException ex) {
                throw new RuntimeException(ex);
            }

            // 第一个数组：发生 CHANGE 类型改变的行在 original 代码中的行号
            List<Integer> changeLinesInOriginal = new ArrayList<>();
            // 第二个数组：发生 CHANGE 类型改变的行在 revised 代码中的行号
            List<Integer> changeLinesInRevised = new ArrayList<>();
            // 第三个数组：original 代码中被删除的行的行号
            List<Integer> deletedLinesInOriginal = new ArrayList<>();
            // 第四个数组：revised 代码中新插入的行的行号
            List<Integer> insertedLinesInRevised = new ArrayList<>();

            // 遍历所有的 Delta
            for (AbstractDelta<String> delta : patch.getDeltas()) {
                DeltaType deltaType = delta.getType();

                // 处理 CHANGE 类型
                if (deltaType == DeltaType.CHANGE) {
                    // 获取 original 中的行号 (Source)
                    int originalPosition = delta.getSource().getPosition();
                    int originalSize = delta.getSource().size();
                    for (int i = 0; i < originalSize; i++) {
                        changeLinesInOriginal.add(originalPosition + i);
                    }

                    // 获取 revised 中的行号 (Target)
                    int revisedPosition = delta.getTarget().getPosition();
                    int revisedSize = delta.getTarget().size();
                    for (int i = 0; i < revisedSize; i++) {
                        changeLinesInRevised.add(revisedPosition + i);
                    }
                }

                // 处理 DELETE 类型（对应 original 中的删除行）
                if (deltaType == DeltaType.DELETE) {
                    int originalPosition = delta.getSource().getPosition();
                    int originalSize = delta.getSource().size();
                    for (int i = 0; i < originalSize; i++) {
                        deletedLinesInOriginal.add(originalPosition + i);
                    }
                }

                // 处理 INSERT 类型（对应 revised 中的新增行）
                if (deltaType == DeltaType.INSERT) {
                    int revisedPosition = delta.getTarget().getPosition();
                    int revisedSize = delta.getTarget().size();
                    for (int i = 0; i < revisedSize; i++) {
                        insertedLinesInRevised.add(revisedPosition + i);
                    }
                }
            }

            // 输出结果
            System.out.println("CHANGE 类型的行在 original 代码中的行号: " + changeLinesInOriginal);
            System.out.println("CHANGE 类型的行在 revised 代码中的行号: " + changeLinesInRevised);
            System.out.println("original 代码中被删除的行的行号: " + deletedLinesInOriginal);
            System.out.println("revised 代码中新插入的行的行号: " + insertedLinesInRevised);

            // 标亮颜色
            Color blueColor = new Color(0, 34, 252, 100);
            Color orangeColor = new Color(252, 67, 0, 100);
            Highlighter.HighlightPainter bluePainter = new DefaultHighlighter.DefaultHighlightPainter(blueColor);
            Highlighter.HighlightPainter orangePainter = new DefaultHighlighter.DefaultHighlightPainter(orangeColor);

            // 遍历行号数组，并为每一行添加高亮
            for (int line : changeLinesInOriginal) {
                try {
                    // 获取每行的起始和结束偏移量
                    int start = textArea.getLineStartOffset(line);  // 获取行的开始偏移量
                    int end = textArea.getLineEndOffset(line);      // 获取行的结束偏移量

                    // 为指定行添加高亮
                    versionHighlighter.addHighlight(start, end, orangePainter);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
            for (int line : deletedLinesInOriginal) {
                try {
                    // 获取每行的起始和结束偏移量
                    int start = textArea.getLineStartOffset(line);  // 获取行的开始偏移量
                    int end = textArea.getLineEndOffset(line);      // 获取行的结束偏移量

                    // 为指定行添加高亮
                    versionHighlighter.addHighlight(start, end, orangePainter);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
            for (int line : changeLinesInRevised) {
                try {
                    // 获取每行的起始和结束偏移量
                    int start = textArea2.getLineStartOffset(line);  // 获取行的开始偏移量
                    int end = textArea2.getLineEndOffset(line);      // 获取行的结束偏移量

                    // 为指定行添加高亮
                    curHighlighter.addHighlight(start, end, bluePainter);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
            for (int line : insertedLinesInRevised) {
                try {
                    // 获取每行的起始和结束偏移量
                    int start = textArea2.getLineStartOffset(line);  // 获取行的开始偏移量
                    int end = textArea2.getLineEndOffset(line);      // 获取行的结束偏移量

                    // 为指定行添加高亮
                    curHighlighter.addHighlight(start, end, bluePainter);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }

        });


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
            if (curSelectedIndex != -1) {
                // TODO 回退到当前选择的版本
                String newCode = versionList[curSelectedIndex].getCode();
                // 获取当前打开的文件
                VirtualFile currentFile = FileEditorManager.getInstance(project).getSelectedFiles()[0];
                if (currentFile != null) {
                    Document document = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();

                    // 启动写入操作，覆盖文件内容
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        document.setText(newCode);
                    });
                } else {
                    System.out.println("No file is currently selected.");
                }
                this.close(OK_EXIT_CODE);
            }
            else{
                MessageOutput.TakeMessage("当前还未选择回退版本");
            }
        });

        panel.add(btnRollBack);
        panel.add(btnClose);
        return panel;
    }
}
