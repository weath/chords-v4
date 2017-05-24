package net.weath.chords;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class HelpWindow extends JFrame {

    private static final long serialVersionUID = 1L;
    private final JEditorPane editorPane;
    private final JScrollPane scrollPane;

    public HelpWindow(String title, String contents) {
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        scrollPane = new JScrollPane(editorPane);
        add(scrollPane, BorderLayout.CENTER);
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; font-size:16pt }");
        styleSheet.addRule("h1 {color: blue;}");
        styleSheet.addRule("h2 {color: #ff0000;}");
        styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");
        editorPane.setEditorKit(kit);
        Document doc = kit.createDefaultDocument();
        editorPane.setDocument(doc);
        editorPane.setText(contents);
        setTitle(title);
    }

    public void scrollToTop() {
        SwingUtilities.invokeLater(() -> {
            scrollPane.getVerticalScrollBar().setValue(0);
        });
    }

    public static final void main(String[] args) {
        String s = readHelpFile(args[0]);
//	s = "<h2>Header</h2><br><p>Paragraph text.</p>";
        HelpWindow w = new HelpWindow("Testing", s);
        w.setSize(new Dimension(300, 200));
        w.setVisible(true);
    }

    private static String readHelpFile(String filename) {
        StringBuffer sb = new StringBuffer();
        FileReader rdr = null;
        try {
            rdr = new FileReader(filename);
            char[] buf = new char[1024];
            while (true) {
                int result = rdr.read(buf);
                if (result <= 0) {
                    break;
                }
                sb.append(buf, 0, result);
            }
        } catch (IOException e) {
            System.err.println("Cannot open " + filename + ": " + e.getMessage());
            return e.getMessage();
        } finally {
            if (rdr != null) {
                try {
                    rdr.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return sb.toString();
    }
}
