package net.weath.chords;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import net.weath.musicutil.Chord;
import net.weath.musicutil.RootlessChord;
import net.weath.musicutil.Interval;
import net.weath.musicutil.KeyboardListener;
import net.weath.musicutil.KeyboardPanel;
import net.weath.musicutil.Note;
import net.weath.musicutil.PhantomNote;
import net.weath.musicutil.Pitch;
import net.weath.musicutil.RatioSet;

public class KeyboardAdapter implements KeyboardListener, ChordChangeListener {

    private final ChordModel model;

    private KeyboardPanel keyboardPanel;

    public KeyboardAdapter(ChordModel model) {
        this.model = model;
    }

    public void setKeyboardPanel(KeyboardPanel keyboardPanel) {
        this.keyboardPanel = keyboardPanel;
    }

    @Override
    public void keyPressed(int n, MouseEvent e) {
        Note note = Note.forMidiNumber(n);
        int octave = n / 12 - 1;
        Pitch[] pitches = model.getPitches();
        Pitch pitch = new Pitch(note, octave);
        int num = pitch.getNumber();
        if (e.isShiftDown()) {
            // clear the label
            keyboardPanel.setLabel(num, "");
            // clear the factor
            keyboardPanel.setFactor(num, 0);
            // clear the highlight
            keyboardPanel.setPressed(num, false);
            // delete note from chord
            for (int i = 0; i < pitches.length; i++) {
                Pitch p = pitches[i];
                num = p.getNumber();
                if (num == n) {
                    Pitch[] oldPitches = pitches;
                    pitches = new Pitch[oldPitches.length - 1];
                    if (i > 0) {
                        System.arraycopy(oldPitches, 0, pitches, 0, i);
                    }
                    if (i + 1 < oldPitches.length) {
                        System.arraycopy(oldPitches, i + 1, pitches, i, oldPitches.length - i - 1);
                    }
                    Arrays.sort(pitches);
                    break;
                }
            }
        } else { // add note to chord, or respell
            Note original = null;
            if (pitches != null) {
                for (Pitch p : pitches) {
                    if (p.getNumber() == n) {
                        original = p.getNote();
                        break;
                    }
                }
            }
            Note[] aliases = note.getAliases(); // possible names for the given midi note
            int i = 0;
            if (original != null && original.isReal()) {
                ArrayList<Pitch> list = new ArrayList<>();
                for (Pitch p : pitches) {
                    if (p.getNumber() == n) {
                        continue;
                    }
                    list.add(p);
                }
                pitches = list.toArray(new Pitch[0]);
                for (; i < aliases.length; i++) {
                    if (original.equals(aliases[i])) {
                        break;
                    }
                }
            }
            if (original != null && original.isReal()) {
                // get next alias
                i++;
                if (i >= aliases.length) {
                    i = 0;
                }
            }
            note = aliases[i];
            pitch = pitch.respell(note);
            // add note to chord
            if (pitches == null) {
                pitches = new Pitch[0];
            }
            Pitch[] oldPitches = pitches;
            pitches = new Pitch[oldPitches.length + 1];
            System.arraycopy(oldPitches, 0, pitches, 0, oldPitches.length);
            pitches[pitches.length - 1] = pitch;
            Arrays.sort(pitches);
        }
        Arrays.sort(pitches);
        model.setPitches(Origin.Keyboard, pitches);
        keyboardPanel.refresh();
    }

    @Override
    public void keyReleased(int n, MouseEvent e) {
        // ignore
    }

    @Override
    public void chordChanged(ChordChangeEvent event) {
        keyboardPanel.clear();
        // highlight the keys and set the labels
        Pitch[] pitches = event.getPitches();
        for (Pitch p : pitches) {
            if (p.isReal())
                keyboardPanel.setPressed(p.getNumber(), true);
        }
        Chord chord = event.getSelectedChord();
        Note root = event.getRoot();
        if (chord instanceof RootlessChord) {
            root = PhantomNote.lookup(root.toString());
        }
        if (chord != null && chord.getIntervals() != null && chord.getIntervals().length > 0) {
            Pitch rootPitch = null;
            for (Pitch p : pitches) {
                if (p.getNote().minus(root) == 0) {
                    rootPitch = p;
                    break;
                }
            }
            if (rootPitch == null) {
                rootPitch = new Pitch(root);
                rootPitch.setOctave(pitches[0].getOctave());
                if (rootPitch.distTo(pitches[0]) < 0) {
                    rootPitch.setOctave(rootPitch.getOctave() - 1);
                }
            }
            if (rootPitch.getNumber() > pitches[0].getNumber()) {
                rootPitch = new Pitch(rootPitch.getNote(), pitches[0].getOctave());
                if (rootPitch.getNumber() > pitches[0].getNumber()) {
                    rootPitch.setOctave(rootPitch.getOctave() - 1);
                }
            }
            for (Pitch p : pitches) {
                while (p.getNumber() < rootPitch.getNumber()) {
                    rootPitch.minusOctave();
                }
                int basicSize = rootPitch.distTo(p) + 1;
                int semis = p.getNumber() - rootPitch.getNumber();
                Interval interval = new Interval(basicSize, semis);
                if (interval.isEnharmonicWith(Interval.P1)) {
                    interval = Interval.P1;
                }
                if (interval.differsByOctaveWith(Interval.P1)) {
                    keyboardPanel.setLabel(p.getNumber(), "R");
                } else {
                    keyboardPanel.setLabel(p.getNumber(), interval.simplify().id());
                }
            }
        }
        RatioSet rs = event.getRatioSet();
        if (rs != null && model.isJust()) {
            if (pitches.length == rs.size()) {
                for (int i = 0; i < pitches.length; i++) {
                    keyboardPanel.setFactor(pitches[i].getNumber(), rs.value(i));
                }
            }
        }
        keyboardPanel.refresh();
    }

}
