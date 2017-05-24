package net.weath.musicutil;

/**
 * Represents a key signature at a given position in a sequence
 *
 * @author weath
 *
 */
public class KeySignature {

    /**
     * the MIDI tick value of this key change
     */
    private final int tick;

    /**
     * 0 == no sharps/flats, -1 = one flat, +1 = one sharp, etc.
     */
    private final int midiKey;

    /**
     * true if it's a minor key
     */
    private final boolean isMinor;

    /**
     * the key of C major
     */
    public static KeySignature Cmajor = new KeySignature(0, 0, false);

    /**
     * Construct a new KeySignature object
     *
     * @param tick the MIDI tick
     * @param midiKey an int (0 = no sharps/flats, -1 = one flat, etc.)
     * @param isMinor true if its a minor key
     */
    public KeySignature(int tick, int midiKey, boolean isMinor) {
        this.tick = tick;
        this.midiKey = midiKey;
        this.isMinor = isMinor;
    }

    /**
     * Return the MIDI representation of this key
     *
     * @return an int (0 = no sharps/flats, -1 = 1 flat, etc.)
     */
    public int getMidiKey() {
        return midiKey;
    }

    /**
     * Is this KeySignature for a minor key?
     *
     * @return true if it's minor
     */
    public boolean isMinor() {
        return isMinor;
    }

    /**
     * Return the key Note for this KeySignature
     *
     * @return Note.Bb for midiKey = -2, e.g.
     */
    public Note getNote() {
        if (!isMinor) {
            switch (midiKey) {
                case -7:
                    return Note.Cb;
                case -6:
                    return Note.Gb;
                case -5:
                    return Note.Db;
                case -4:
                    return Note.Ab;
                case -3:
                    return Note.Eb;
                case -2:
                    return Note.Bb;
                case -1:
                    return Note.F;
                case 0:
                    return Note.C;
                case 1:
                    return Note.G;
                case 2:
                    return Note.D;
                case 3:
                    return Note.A;
                case 4:
                    return Note.E;
                case 5:
                    return Note.B;
                case 6:
                    return Note.Fs;
                case 7:
                    return Note.Cs;
                default:
                    throw new IllegalArgumentException("MIDI key out of range: "
                            + midiKey);
            }
        } else {
            switch (midiKey) {
                // case -10:
                // return Note.Cb;
                // case -9:
                // return Note.Gb;
                // case -8:
                // return Note.Db;
                case -7:
                    return Note.Ab;
                case -6:
                    return Note.Eb;
                case -5:
                    return Note.Bb;
                case -4:
                    return Note.F;
                case -3:
                    return Note.C;
                case -2:
                    return Note.G;
                case -1:
                    return Note.D;
                case 0:
                    return Note.A;
                case 1:
                    return Note.E;
                case 2:
                    return Note.B;
                case 3:
                    return Note.Fs;
                case 4:
                    return Note.Cs;
                case 5:
                    return Note.Gs;
                case 6:
                    return Note.Ds;
                case 7:
                    return Note.As;
                default:
                    throw new IllegalArgumentException("MIDI key out of range: "
                            + midiKey);
            }
        }
    }

    /**
     * @return the String representation of this KeySignature ("Bb major")
     */
    @Override
    public String toString() {
        return getNote().toString() + " " + ((isMinor) ? "minor" : "major");
    }

    /**
     * Return the MIDI tick for this KeySignature
     *
     * @return an int (the MIDI tick value)
     */
    public int getTick() {
        return tick;
    }

    /**
     * Return true if the given Note is in this KeySignature
     *
     * @param n a Note
     * @return true if it's in this KeySignature, or is 7# of a minor key
     */
    public boolean inKey(Note n) {
        if (n.getAccidentalKind() == AccidentalKind.doubleFlat
                || n.getAccidentalKind() == AccidentalKind.doubleSharp) {
            return false;
        }
        char name = n.baseName().charAt(0);
        if (null == n.getAccidentalKind()) {
            // natural
            if (getMidiKey() < 0) { // a flat key
                if (isMinor) {
                    Note k = getNote();
                    if (k.minus(n) == 1) {
                        return true; // 7 natural
                    }
                }
                return "BEADGCF".indexOf(name) >= -getMidiKey();
            } else if (getMidiKey() > 0) { // a sharp key
                return "FCGDAEB".indexOf(name) >= getMidiKey();
            } else {
                return true; // key of C
            }
        } else {
            switch (n.getAccidentalKind()) {
                case sharp:
                    if (isMinor) {
                        Note k = getNote();
                        if (k.minus(n) == 1) {
                            return true; // 7 sharp
                        }
                    }
                    return "FCGDAEB".indexOf(name) < getMidiKey();
                case flat:
                    return "BEADGCF".indexOf(name) < -getMidiKey();
                default:
                    // natural
                    if (getMidiKey() < 0) { // a flat key
                        if (isMinor) {
                            Note k = getNote();
                            if (k.minus(n) == 1) {
                                return true; // 7 natural
                            }
                        }
                        return "BEADGCF".indexOf(name) >= -getMidiKey();
                    } else if (getMidiKey() > 0) { // a sharp key
                        return "FCGDAEB".indexOf(name) >= getMidiKey();
                    } else {
                        return true; // key of C
                    }
            }
        }
    }
}
