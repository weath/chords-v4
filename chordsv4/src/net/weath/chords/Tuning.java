package net.weath.chords;

import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.weath.musicutil.Chord;

public class Tuning extends JPanel implements IChordController, IChordViewer {

    private static final long serialVersionUID = 1L;

    private ChordModel model;

    private JTextField textField;

    private final JLabel label;

    private final JLabel ofLabel;

    private int which = 0;

    private int tunings = -1;

    private Chord chord;

    private int selection = -1;

    public Tuning(ChordModel model) {
        this.model = model;

        label = new JLabel("  Tuning:");
        label.setFont(Chords.getDefaultFont());
        add(label);
        textField = new JTextField(2);
        textField.setFont(Chords.getDefaultFont());
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setText("?");
        add(textField);
        textField.setToolTipText("Enter number of desired tuning, if there are multiple");
        ofLabel = new JLabel("of ?");
        ofLabel.setFont(Chords.getDefaultFont());
        add(ofLabel);
        ofLabel.setToolTipText("Displays number of known tunings for the current chord");
        JLabel spacer = new JLabel("  ");
        spacer.setFont(Chords.getDefaultFont());
        add(spacer);
        setEnabled(false);
        model.addListener(this);
        textField.addActionListener((ActionEvent e) -> {
            int n = -1;
            try {
                n = Integer.parseInt(textField.getText());
            } catch (NumberFormatException e1) {
            }
            if (n > 0 && n <= tunings) {
                which = n - 1;
            } else {
                which = 1;
                textField.setText("1");
            }
            chord.selectTuning(which);
            Tuning.this.model.setSelection(Origin.Tuning, selection);
        });
    }

    @Override
    public Origin getOrigin() {
        return Origin.Tuning;
    }

    @Override
    public void chordChanged(ChordChangeEvent event) {
        if (event.getOrigin() == getOrigin()) {
            return;
        }
        if (model.isJust()) {
            Chord[] chords = event.getChords();
            if (chords.length == 0) {
                return;
            }
            selection = event.getSelection();
            chord = chords[selection];
            tunings = chord.getTotalTunings();
            which = chord.getSelectedTuning();
            ofLabel.setText("of " + Integer.toString(tunings));
            textField.setText(Integer.toString(which + 1));
            setEnabled(true);
        } else {
            ofLabel.setText("of ?");
            textField.setText("?");
            setEnabled(false);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        label.setEnabled(enabled);
        textField.setEnabled(enabled);
        ofLabel.setEnabled(enabled);
        super.setEnabled(enabled);
    }
}
