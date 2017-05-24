package net.weath.chords;

import net.weath.musicutil.Chord;
import net.weath.musicutil.Note;
import net.weath.musicutil.Pitch;
import net.weath.musicutil.RatioSet;

public class ChordChangeEvent {

    private Pitch[] pitches;
    private Pitch[] rootlessPitches;
    private final ChordInstance[] instances;
    private final Chord[] chords;
    private final int selection;
    private final Note root;
    private final Note bass;
    private final RatioSet ratioSet;
    private final Origin origin;
    private final boolean just;
    private final boolean hasUserBends;

    public ChordChangeEvent(Pitch[] pitches, ChordInstance[] instances, int selection, Note root,
            Note bass, RatioSet ratioSet, boolean just, boolean ub, Origin origin) {
        this.pitches = pitches.clone();
        this.instances = instances.clone();
        this.selection = selection;
        this.root = root;
        this.bass = bass;
        this.ratioSet = ratioSet;
        this.just = just;
        this.hasUserBends = ub;
        this.origin = origin;
        chords = new Chord[instances.length];
        for (int i = 0; i < chords.length; i++) {
            if (instances[i] != null)
                chords[i] = instances[i].getChord();
        }
    }

    public Pitch[] getPitches() {
//        if (selection >= 0 && selection < instances.length) {
//            pitches = instances[selection].getPitches();
//        }
        return pitches;
    }

    public ChordInstance[] getChordInstances() {
        return instances;
    }

    public Chord[] getChords() {
        return chords;
    }

    public Origin getOrigin() {
        return origin;
    }

    public int getSelection() {
        return selection;
    }

    public Note getRoot() {
        return root;
    }

    public Chord getSelectedChord() {
        if (selection < 0 || selection >= instances.length) {
            return null;
        }
        return chords[selection];
    }

    public Note getBass() {
        return bass;
    }

    public RatioSet getRatioSet() {
        if (selection >= 0 && selection < instances.length) {
            return instances[selection].getRatioSet();
        }
        return ratioSet;
    }

    public int getInversion() {
        if (selection >= 0 && selection < instances.length) {
            return instances[selection].getInversion();
        }
        return 0;
    }

    public boolean isJust() {
        return just;
    }
    
    public boolean hasUserBends() {
        return hasUserBends;
    }
}
