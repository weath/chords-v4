package net.weath.chords;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * An entry in the ChordListPanel
 *
 * @author weath
 */
public class ChordName extends JPanel {

    private static final long serialVersionUID = 1L;
    private final JLabel label;
    private boolean selected = false;
    private int n;
    private final Color normalBackground;

    /**
     * Create a ChordName object
     *
     * @param panel the parent panel
     * @param n the index
     * @param instance a ChordInstance
     */
    public ChordName(final ChordListPanel panel, int n, ChordInstance instance) {
        this.n = n;
        this.setAlignmentX(0.0F);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel(instance.toString());
        label.setFont(Chords.getDefaultFont());
        label.setHorizontalAlignment(SwingConstants.LEFT);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent ev) {
                panel.itemSelected(ChordName.this.n);
                setSelected(!selected);
            }
        });
        this.add(label);
        normalBackground = panel.getBackground();
        setSelected(selected);
    }

    public void setSelected(boolean b) {
        Color color = (b) ? Color.cyan : normalBackground;
        this.setBackground(color);
        selected = b;
    }
}
