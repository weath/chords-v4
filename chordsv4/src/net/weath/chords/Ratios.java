package net.weath.chords;

import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.weath.musicutil.Chord;
import net.weath.musicutil.Pitch;
import net.weath.musicutil.RatioSet;

public class Ratios extends JPanel implements IChordController, IChordViewer {

    private static final long serialVersionUID = 1L;

    private final ChordModel model;
    private ErrMsg errMsg;
    private final JTextField field;
    private final JLabel label;
    private RatioSet ratioSet;

    private Chord chord;

    public Ratios(ChordModel model, JLabel label, ErrMsg errMsg) {
        this.model = model;
        this.label = label;
        this.errMsg = errMsg;
        field = new JTextField(15);
        field.setFont(Chords.getDefaultFont());
        add(field);
        field.addActionListener((ActionEvent arg0) -> {
            Ratios.this.errMsg.setErrMsg(null);
            ratioSet = null;
            setRatios();
        });
        field.setToolTipText("Enter a sequence of integers describing the ratio: 4:5:6:7 (only applies if in JI mode)");
        field.setEnabled(false);
        label.setEnabled(false);
        model.addListener(this);
    }

    private void setRatios() {
        String[] arr = field.getText().trim().split(":");
        if (arr.length == 0 || chord == null) {
            return;
        }
        if (arr.length == 1 && arr[0].equals("")) {
            ratioSet = null;
            model.setRatios(null, chord, null); // null origin so we don't ignore event
            return;
        }
        Pitch[] pitches = model.getPitches();
        if (arr.length != pitches.length) {
            errMsg.setErrMsg("Number of ratio components does not match number of pitches!");
            return;
        }
        int[] intArr = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            try {
                intArr[i] = Integer.parseInt(arr[i].trim());
            } catch (NumberFormatException ex) {
                errMsg.setErrMsg("Cannot parse integer: " + arr[i]);
                return;
            }
            if (intArr[i] <= 0) {
                errMsg.setErrMsg("Ratio components must be greater than zero: " + arr[i]);
                return;
            }
        }
        ratioSet = new RatioSet(intArr);
        model.setRatios(getOrigin(), chord, ratioSet);
    }

    @Override
    public void chordChanged(ChordChangeEvent event) {
        if (event.getOrigin() == getOrigin()) {
            return;
        }
        if (model.isJust()) {
            Chord[] chords = event.getChords();
            if (chords.length == 0) {
                chord = null;
                field.setText("");
                field.setEnabled(false);
                label.setEnabled(false);
            } else {
                field.setEnabled(true);
                label.setEnabled(true);
                int selection = event.getSelection();
                chord = chords[selection];
                ratioSet = event.getRatioSet();
                field.setText(ratioSet.toString());
            }
        } else {
            field.setText("");
            field.setEnabled(false);
            label.setEnabled(false);
        }
    }

    @Override
    public Origin getOrigin() {
        return Origin.Ratios;
    }
}
