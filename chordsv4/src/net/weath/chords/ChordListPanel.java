package net.weath.chords;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import net.weath.musicutil.Chord;

public class ChordListPanel extends JPanel implements ChordChangeListener, IChordController {

    private static final long serialVersionUID = 1L;
    private final ChordModel model;
    private Chord[] chords;
//    private Note[] roots;
    private ChordInstance[] instances;

    public ChordListPanel(ChordModel model) {
        this.model = model;
        model.addListener(this);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(400, 400));
        setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    @Override
    public Origin getOrigin() {
        return Origin.ChordList;
    }

    @Override
    public void chordChanged(ChordChangeEvent event) {
        chords = event.getChords();
        instances = event.getChordInstances();
        update();
    }

    private void update() {
        removeAll();
        for (int i = 0; i < chords.length; i++) {
            ChordName item = new ChordName(this, i, instances[i]);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            add(item, gbc);
            if (i == model.getSelection()) {
                item.setSelected(true);
            }
        }
        JPanel fill = new JPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = chords.length;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        add(fill, gbc);
        validate();
        repaint();
    }

    public void itemSelected(int n) {
        model.setSelection(getOrigin(), n);
        repaint();
    }
}
