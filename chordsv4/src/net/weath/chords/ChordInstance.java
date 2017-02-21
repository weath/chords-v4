package net.weath.chords;

import java.util.ArrayList;
import net.weath.musicutil.Chord;
import net.weath.musicutil.Interval;
import net.weath.musicutil.Note;
import net.weath.musicutil.Pitch;
import net.weath.musicutil.RatioSet;
import net.weath.musicutil.RootlessChord;

/**
 * Represents a "concrete" chord: a Pitch array, with one distinguished as the
 * root, and a Chord (named set of intervals) which describes it.
 *
 * @author weath
 *
 */
public class ChordInstance {

    private Pitch[] pitches;
    private boolean hasUserBends;
    private int inversion;
    private final int rootIndex;
    private final Note root;
    private final Chord chord;
    private RatioSet ratioSet;

    public ChordInstance(Pitch[] pitches, Chord chord, boolean hasUserBends) {
        this.pitches = adjust(pitches, chord);
        this.hasUserBends = hasUserBends;
        this.chord = chord;
        this.rootIndex = figureRootIndex();
        if (rootIndex < 0) {
            root = null;
            throw new IllegalArgumentException("Can't find root");
        } else {
            root = this.pitches[rootIndex].getNote();
            inversion = figureInversion();
        }
        computeRatioSet();
    }

    private void computeRatioSet() {
        boolean failed = false;
        RatioSet rs = chord.getRatios();
        Pitch[] pArr = pitches.clone();
        if (chord instanceof RootlessChord) {
            RootlessChord rc = (RootlessChord) chord;
            pArr = rc.insertRootPitch(pArr);
        }
        pitches = pArr;
        int[] factor = new int[pArr.length];
        Note bass = pArr[0].getNote();
        Pitch[] chordPitches = chord.getPitches(root, pArr[0].getOctave());
        if (chord instanceof RootlessChord) {
            RootlessChord rc = (RootlessChord) chord;
            chordPitches = rc.insertRootPitch(chordPitches);
        }
        for (int i = 0; i < pArr.length; i++) {
            Pitch p = pArr[i];
            // find the right factor for this pitch
            for (Pitch cp : chordPitches) {
                if (cp.equalsET(p)) {
                    // easy case
                    int j = findIndex(cp, chordPitches);
                    factor[i] = rs.value(j);
                    break;
                }
            }
            int octdiff = 0;
            if (factor[i] == 0) {
                // no exact match; try octave
                for (Pitch cp : chordPitches) {
                    if (cp.getNote().equals(p.getNote())) {
                        octdiff = p.getOctave() - cp.getOctave();
                        int j = findIndex(cp, chordPitches);
                        if (octdiff > 0) {
                            factor[i] = (int) (rs.value(j) * Math.pow(2.0, octdiff));
                        } else {
                            // division truncates! multiply lower factors instead
                            factor[i] = rs.value(j);
                            for (int k = 0; k < i; k++) {
                                factor[k] = factor[k] * (int) Math.pow(2.0, -octdiff);
                            }
                        }
                        break;
                    }
                }
            }
            if (factor[i] == 0) {
                // hmm, maybe we need to try enharmonic spellings AND octave diff...
                for (Pitch cp : chordPitches) {
                    if (cp.getNote().getNumber() == p.getNote().getNumber()) {
                        octdiff = p.getOctave() - cp.getOctave();
                        int j = findIndex(cp, chordPitches);
                        factor[i] = (int) (rs.value(j) * Math.pow(2.0, octdiff));
                        break;
                    }
                }
            }
            if (factor[i] == 0) {
                System.err.println("Cannot compute factor for " + p);
                failed = true;
                continue;
            }
            if (factor[0] == 0) {
                return;
            }
            if (i > 0 && factor[i] <= factor[i - 1]) {
                while (factor[i] <= factor[i - 1]) {
                    factor[i] *= 2;
                }
            }
            double d = factor[i];
            double c = factor[0] * Math.pow(2.0, octdiff);
            double cents = 1200.0 * Math.log(d / c) / Math.log(2.0);
            if (i > 0) {
                cents -= p.minus(pArr[0]).semitones() * 100.0;
            }
            while (cents > 100.0) {
                cents -= 1200.0;
            }
            while (cents < -100.0) {
                cents += 1200.0;
            }
            if (Math.abs(cents) >= 200.0) {
                System.err.println("bad pitch bend: " + cents + " for " + p);
                cents = 0;
            }
            if (!hasUserBends)
                p.setBend((int) (cents * 40.96));
        }
        if (!failed) {
            ratioSet = new RatioSet(factor);
        }
    }

    public Pitch[] getPitches() {
        computeRatioSet();
        return pitches;
    }

    public int getRootIndex() {
        return rootIndex;
    }

    public Chord getChord() {
        return chord;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(chord.getName(getRoot()));
        if (inversion > 0) {
            sb.append(" (");
            sb.append(Chord.ordinal(inversion));
            sb.append(" inversion)");
        }
        return sb.toString();
    }

    public RatioSet getRatioSet() {
        return ratioSet;
    }

    private int findIndex(Pitch p, Pitch[] pitches) {
        for (int i = 0; i < pitches.length; i++) {
            if (pitches[i].equals(p)) {
                return i;
            }
        }
        return -1;
    }

    private Pitch[] adjust(Pitch[] pitches, Chord chord) {
        if (chord instanceof RootlessChord) {
            RootlessChord rc = (RootlessChord) chord;
            return rc.insertRootPitch(pitches);
        } else {
            ArrayList<Pitch> arr = new ArrayList<>();
            for (Pitch p : pitches) {
                if (p.isReal()) {
                    arr.add(p);
                }
            }
            return arr.toArray(new Pitch[0]);
        }
    }

    Note getRoot() {
        return root;
    }

    private int figureInversion() {
        Pitch rootPitch = pitches[rootIndex];
        Pitch lowest = pitches[0];
        for (Pitch p : pitches) {
            if (p.isReal()) {
                lowest = p;
                break;
            }
        }   
        while (lowest.getNumber() < rootPitch.getNumber()) {
            lowest = lowest.plus(Interval.P8);
        }
        Interval n = lowest.minus(rootPitch).simplify();
        if (n.equals(Interval.P1))
            return 0;
        Interval[] arr = chord.getIntervals();
        for (int i = 0; i < arr.length; i++) {
            if (n.isEquivalentTo(arr[i]))
                return i + 1;
        }
        throw new IllegalArgumentException("Can't determine inversion");
    }

    public int getInversion() {
        return inversion;
    }

    private int figureRootIndex() {
        Interval[] intervals = chord.getIntervals();
        Pitch[] pArr = pitches.clone();
        for (int i = 0; i < pArr.length; i++) {
            if (!pArr[i].isReal())
                pArr[i] = new Pitch(pArr[i]);
        }
        // find the root pitch
        for (int i = 0; i < pArr.length; i++) {
            Pitch p = pArr[i];
            boolean[] intFound = new boolean[intervals.length];
            while (p.getNumber() >= pArr[0].getNumber()) {
                p = p.minus(Interval.P8); // ensure this pitch is below all the others
            }
            int found = 0;
            for (Pitch p1 : pArr) {
                Interval n = p1.minus(p);
                for (int k = 0; k < intervals.length; k++) { // find all the intervals
                    if (intFound[k])
                        continue;
                    if (n.isEquivalentTo(intervals[k])) {
                        intFound[k] = true;
                        found++;
                        break;
                    }
                }
            }
            if (found == intervals.length) { // bingo!
                return i; // index of root pitch
            }
        }
        return -1; // failed!
    }
}
