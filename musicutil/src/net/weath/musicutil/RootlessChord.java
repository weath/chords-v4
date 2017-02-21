package net.weath.musicutil;

import java.util.ArrayList;

/**
 *
 * @author weath
 */
public class RootlessChord extends Chord {
    private Interval[] actualIntervals = null;

    private RootlessChord(String message) {
        super(message);
    }

    protected RootlessChord(String suffix, Interval[] n) {
        super(suffix, false, n);
    }

    protected RootlessChord(Chord c) {
        super(c.suffix + " (no root)", false, c.intervals);
        addComplexity(100_000_000);
    }

    public Interval getThird() {
        if (suffix.charAt(0) == 'm') {
            return Interval.m3;
        }
        return Interval.M3;
    }
    
    @Override
    public Interval[] getIntervals() {
        // Chord.intervals are relative to the third
        if (actualIntervals == null) {
            actualIntervals = new Interval[intervals.length + 1];
            actualIntervals[0] = getThird();
            for (int i = 0; i < intervals.length; i++) {
                actualIntervals[i+1] = intervals[i].plus(actualIntervals[0]);
            }
        }
        return actualIntervals;
    }

    /**
     * Get array of Pitches, given a bass Note and octave
     *
     * @param bass the first Note
     * @param octave the octave of the bass Pitch
     * @return array of Pitches
     */
    @Override
    public Pitch[] getPitches(Note bass, int octave) {
        ArrayList<Pitch> arr = new ArrayList<>();
        Pitch b = new Pitch(bass, octave);
        b = new PhantomPitch(b);
        // for rootless chord, intervals are relative to the third
        Pitch third = new Pitch(b, getThird());
        arr.add(b);
        arr.add(third);
        for (Interval n : intervals) {
            Pitch p = new Pitch(third, n);
            arr.add(p);
        }
        return arr.toArray(new Pitch[0]);
    }

    /**
     * Return the name, given a root note
     *
     * @param root
     * @return the name (a String);
     */
    @Override
    public String getName(Note root) {
        StringBuilder sb = new StringBuilder();
        sb.append(root);
        if (isInterval() || !root.isWhiteKey()) {
            sb.append(" ");
        }
        sb.append(toString());
        return sb.toString();
    }

    /**
     * Return PhantomPitch for root note
     * @param rootNote Note of the root
     * @param pitches Pitch[]; may not contain the root
     * @return a PhantomPitch corresponding to the root
     */
    @Override
    public Pitch getRootPitch(Note rootNote, Pitch[] pitches) {
        pitches = insertRootPitch(pitches);
        for (Pitch p : pitches) {
            if (p.getNote().equals(rootNote)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Can't happen");
    }

    public Pitch[] insertRootPitch(Pitch[] pitches) {
        // check whether we already have the root
        for (Pitch p : pitches) {
            if (!p.isReal()) {
                return pitches; // nothing to do
            }
        }
        // find the 3rd and ninth
        int third = -1;
        int ninth = -1;
        for (int i = 0; i < pitches.length - 1; i++) {
            Pitch p1 = pitches[i];
            for (int j = i + 1; j < pitches.length; j++) {
                Pitch p2 = pitches[j];
                Interval n = p2.minus(p1);
                if (n.isEquivalentTo(Interval.m7)
                        || n.isEquivalentTo(Interval.M7)
                        || n.isEquivalentTo(Interval.m2)
                        || n.isEquivalentTo(Interval.M2)) {
                    if (n.semitones() <= 2) { // m2 or M2
                        third = j;
                        ninth = i;
                    } else if (n.semitones() > 12 && n.semitones() < 15) { // M9
                        third = j;
                        ninth = i;
                    } else { // m7 or M7
                        third = i;
                        ninth = j;
                    }
                    break;
                }
            }
            if (third >= 0) {
                break;
            }
        }
        if (third < 0) {
            throw new IllegalArgumentException("Can't find the third");
        }
        PhantomPitch root = new PhantomPitch(pitches[third].minus(getThird()));
        while (root.getNumber() >= pitches[0].getNumber()) {
            root.minusOctave();
        }
        // now insert the root at the proper position
        Pitch[] newPitches = new Pitch[pitches.length + 1];
        // insert root in position 0
        newPitches[0] = root;
        System.arraycopy(pitches, 0, newPitches, 1, pitches.length);
        return newPitches;
    }
}
