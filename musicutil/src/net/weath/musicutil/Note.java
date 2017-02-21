package net.weath.musicutil;

import java.util.HashMap;

/**
 * Note names and enharmonic equivalences
 *
 * @author weath
 *
 */
public class Note {
    
    private static final HashMap<String, Note> map = new HashMap<>();
    
    private static final Note[] forOrdinal = new Note[35];

    protected int ordinal;

    private static int nextOrdinal = 0;

    private static final int[] offset = { // from Ab, in semitones
        11,  0,  1,  2,  3, // Abb, Ab, A, As, Ax
         1,  2,  3,  4,  5, // Bbb, Bb, B, Bs, Bx
         2,  3,  4,  5,  6, // Cbb, Cb, C, Cs, Cx
         4,  5,  6,  7,  8, // Dbb, Db, D, Ds, Dx
         6,  7,  8,  9, 10, // Ebb, Eb, E, Es, Ex
         7,  8,  9, 10, 11, // Fbb, Fb, F, Fs, Fx
         9, 10, 11,  0,  1  // Gbb, Gb, G, Gs, Gx
        };
    
    private static final String[] name = {
        "Abb", "Ab", "A", "A#", "Ax",
        "Bbb", "Bb", "B", "B#", "Bx",
        "Cbb", "Cb", "C", "C#", "Cx",
        "Dbb", "Db", "D", "D#", "Dx",
        "Ebb", "Eb", "E", "E#", "Ex",
        "Fbb", "Fb", "F", "F#", "Fx",
        "Gbb", "Gb", "G", "G#", "Gx"
    };
 
    public static final Note Abb = new Note();
    public static final Note Ab = new Note();
    public static final Note A = new Note();
    public static final Note As = new Note();
    public static final Note Ax = new Note();
    public static final Note Bbb = new Note();
    public static final Note Bb = new Note();
    public static final Note B = new Note();
    public static final Note Bs = new Note();
    public static final Note Bx = new Note();
    public static final Note Cbb = new Note();
    public static final Note Cb = new Note();
    public static final Note C = new Note();
    public static final Note Cs = new Note();
    public static final Note Cx = new Note();
    public static final Note Dbb = new Note();
    public static final Note Db = new Note();
    public static final Note D = new Note();
    public static final Note Ds = new Note();
    public static final Note Dx = new Note();
    public static final Note Ebb = new Note();
    public static final Note Eb = new Note();
    public static final Note E = new Note();
    public static final Note Es = new Note();
    public static final Note Ex = new Note();
    public static final Note Fbb = new Note();
    public static final Note Fb = new Note();
    public static final Note F = new Note();
    public static final Note Fs = new Note();
    public static final Note Fx = new Note();
    public static final Note Gbb = new Note();
    public static final Note Gb = new Note();
    public static final Note G = new Note();
    public static final Note Gs = new Note();
    public static final Note Gx = new Note();

    private static final int _Abb = 0;
    private static final int _Ab = 1;
    private static final int _A = 2;
    private static final int _As = 3;
    private static final int _Ax = 4;
    private static final int _Bbb = 5;
    private static final int _Bb = 6;
    private static final int _B = 7;
    private static final int _Bs = 8;
    private static final int _Bx = 9;
    private static final int _Cbb = 10;
    private static final int _Cb = 11;
    private static final int _C = 12;
    private static final int _Cs = 13;
    private static final int _Cx = 14;
    private static final int _Dbb = 15;
    private static final int _Db = 16;
    private static final int _D = 17;
    private static final int _Ds = 18;
    private static final int _Dx = 19;
    private static final int _Ebb = 20;
    private static final int _Eb = 21;
    private static final int _E = 22;
    private static final int _Es = 23;
    private static final int _Ex = 24;
    private static final int _Fbb = 25;
    private static final int _Fb = 26;
    private static final int _F = 27;
    private static final int _Fs = 28;
    private static final int _Fx = 29;
    private static final int _Gbb = 30;
    private static final int _Gb = 31;
    private static final int _G = 32;
    private static final int _Gs = 33;
    private static final int _Gx = 34;

    private static final Note[] all = {C, Db, D, Eb, E, F, Gb, G, Ab, A, Bb, B};

    /**
     * Constructor (protected); should be called only for the 35 final mtatic memebers and
     * the 35 final static PhantomNotes
     */
    protected Note() {
        this.ordinal = nextOrdinal++;
        if (ordinal < 35) {
            forOrdinal[ordinal] = this;
            map.put(name[ordinal], forOrdinal[ordinal]);
        }
    }
    
    /**
     * Return the Note with the given name (using bb, b, #, and x)
     *
     * @param name the note name to look up
     * @return the named Note
     * @throws IllegalArgumentException if the name is not recognized
     */
    public static Note lookup(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(name.substring(0, 1).toUpperCase());
        sb.append(name.substring(1));
        name = sb.toString();
        Note n = map.get(name);
        if (n == null)
            throw new IllegalArgumentException("unrecognized note name: " + name);
        return n;
    }

    /**
     * Return true if this is not a PhantomNote
     * @return true if this is not a PhantomNote
     */
    public boolean isReal() {
        return true;
    }

    /**
     * @return the String representation of this Note, using bb, b, #, and x
     */
    @Override
    public String toString() {
        return name[ordinal];
    }

    /**
     * Return the Note with a simpler name, if any, or the original Note
     * @return the Note with a simpler name that equals this one, if any, or the original
     */
    public Note simplify() {
        Note n = this;
        switch (ordinal) {
            case _Abb:
                n = G;
                break;
            case _Ax:
                n = B;
                break;
            case _Bbb:
                n = A;
                break;
            case _Bs:
                n = C;
                break;
            case _Cb:
                n = B;
                break;
            case _Cx:
                n = D;
                break;
            case _Dbb:
                n = C;
                break;
            case _Dx:
                n = E;
                break;
            case _Ebb:
                n = D;
                break;
            case _Es:
                n = F;
                break;
            case _Fb:
                n = E;
                break;
            case _Fx:
                n = G;
                break;
            case _Gbb:
                n = F;
                break;
            case _Gx:
                n = A;
                break;
        }
        return n;
    }

    /**
     * Return true if the Note represents a white key on the keyboard
     * @return true if the Note falls on a white key 
     */
    public boolean isWhiteKey() {
        Note n = this.simplify();
        switch (n.ordinal) {
            case _A:
            case _B:
            case _C:
            case _D:
            case _E:
            case _F:
            case _G:
                return true;
            default:
                return false;
        }
    }

    /**
     * Return the Notes at the given offset (in semitones from Ab)
     *
     * @param n the number of semitones above Ab
     * @return array of 2 or 3 Notes ([G#, Ab] for n == 0)
     */
    public static Note[] forOffset(int n) {
        while (n < 0) {
            n += 12;
        }
        n %= 12;
        Note[] ret = null;
        switch (n) {
            case 0:
                ret = new Note[]{Gs, Ab};
                break;
            case 1:
                ret = new Note[]{Gx, A, Bbb};
                break;
            case 2:
                ret = new Note[]{As, Bb, Cbb};
                break;
            case 3:
                ret = new Note[]{Ax, B, Cb};
                break;
            case 4:
                ret = new Note[]{Bs, C, Dbb};
                break;
            case 5:
                ret = new Note[]{Bx, Cs, Db};
                break;
            case 6:
                ret = new Note[]{Cx, D, Ebb};
                break;
            case 7:
                ret = new Note[]{Ds, Eb, Fbb};
                break;
            case 8:
                ret = new Note[]{Dx, E, Fb};
                break;
            case 9:
                ret = new Note[]{Es, F, Gbb};
                break;
            case 10:
                ret = new Note[]{Ex, Fs, Gb};
                break;
            case 11:
                ret = new Note[]{Fx, G, Abb};
                break;
        }
        return ret;
    }

    /**
     * Return an array of all possible Notes
     * @return a Note[] with all the distinctly named Notes (35 total)
     */
    public static Note[] getAll() {
        return all;
    }

    /**
     * Return the number of sharps (positive) or flats (negative)
     *
     * @return an int from -2 to +2
     */
    public int modifier() {
        return (ordinal % 5) - 2;
    }
    
    /**
     * Return the Note one semitone sharper than this one, with the same note name
     *
     * @return the Note one semitone sharper than this one, with the same note
 name, or null if this is already a double sharp
     */
    public Note sharpen() {
        if (modifier() == 2)
            return null;
        return Note.forOrdinal[ordinal + 1];
    }
    
    /**
     * Return the Note one semitone flatter than this one, with the same note name
     *
     * @return the Note one semitone flatter, with the same note name, 
     * or null if this is already a double flat
     */
     public Note flatten() {
        if (modifier() == -2)
            return null;
        return Note.forOrdinal[ordinal - 1];
    }

    /**
     * Return the positive offset (in semitones) from Ab
     *
     * @return a positive integer, the number of semitones from Ab up to this
 Note
     */
    public int offset() {
        return offset[ordinal];
    }

    /**
     * Return the Note which is the given Interval above this one
     *
     * @param i an Interval
     * @return the Note which is the given Interval above this one
     */
    public Note plus(Interval i) {
        int steps = i.semitones();
        Note[] choices = Note.forOffset(steps + this.offset());
        Note n = i.bestMatch(this, choices);
        if (n == null) // have to use enharmonic spelling
        {
            return pickOne(choices);
        }
        return n;
    }

    /**
     * Return the Note which is the given Interval below this one
     *
     * @param i an Interval
     * @return the Note which is the given Interval below this one
     */
    public Note minus(Interval i) {
        int steps = i.semitones();
        Note[] choices = Note.forOffset(this.offset() - steps);
        Note n = i.invert().bestMatch(this, choices);
        if (n == null) // have to use enharmonic spelling
        {
            return pickOne(choices);
        }
        return n;
    }

    /**
     * Return difference (in semitones), 0 or greater, between this and other Note
     *
     * @param other the other Note
     * @return number of semitones between this and other
     */
    public int minus(Note other) {
        int n = this.offset() - other.offset();
        while (n < 0) {
            n += 12;
        }
        return n;
    }

    /**
     * Pick a note when there's no good match; use the least modified one
     *
     * @param choices array of Notes to choose from
     * @return the least modified Note
     */
    private static Note pickOne(Note[] choices) {
        int[] mod = new int[choices.length];
        for (int i = 0; i < mod.length; i++) {
            mod[i] = choices[i].modifier();
        }
        int which = 0;
        int bestMod = 1000;
        for (int i = 0; i < mod.length; i++) {
            if (Math.abs(mod[i]) < bestMod) {
                bestMod = Math.abs(mod[i]);
                which = i;
            }
        }
        return choices[which];
    }

    /**
     * Distance (up) to another note, in note names
     *
     * @param other the upper note
     * @return distance in terms of note names, 1 == same note (unison)
     */
    public int nameDist(Note other) {
        Note naturalMe = this.natural();
        Note naturalHim = other.natural();
        int dist = 0;
        while (naturalMe.offset() != naturalHim.offset()) {
            dist++;
            naturalMe = naturalMe.nextByName();
        }
        return dist + 1;
    }

    /**
     * Return the natural note corresponding to this Note
     *
     * @return the Note with no sharps or flats
     */
    public Note natural() {
        Note n = this;
        switch (ordinal) {
            case _Abb:
            case _Ab:
            case _As:
            case _Ax:
                n = A;
                break;
            case _Bbb:
            case _Bb:
            case _Bs:
            case _Bx:
                n = B;
                break;
            case _Cbb:
            case _Cb:
            case _Cs:
            case _Cx:
                n = C;
                break;
            case _Dbb:
            case _Db:
            case _Ds:
            case _Dx:
                n = D;
                break;
            case _Ebb:
            case _Eb:
            case _Es:
            case _Ex:
                n = E;
                break;
            case _Fbb:
            case _Fb:
            case _Fs:
            case _Fx:
                n = F;
                break;
            case _Gbb:
            case _Gb:
            case _Gs:
            case _Gx:
                n = G;
                break;
        }
        return n;
    }
    
    /**
     * Return the next higher (natural) Note by note name (A follows G)
     *
     * @return the next higher Note by note name
     */
    public Note nextByName() {
        Note n = this.natural();
        return Note.forOrdinal[(n.ordinal + 5) % 35];
    }

    /**
     * Return the Note for the given MIDI number
     * @param number 0 to 127
     * @return the Note for that number
     */
    public static Note forMidiNumber(int number) {
        if (number < 0 || number > 127) {
            return null;
        }
        int n = number % 12;
        switch (n) {
            case 0:
                return C;
            case 1:
                return Db;
            case 2:
                return D;
            case 3:
                return Eb;
            case 4:
                return E;
            case 5:
                return F;
            case 6:
                return Gb;
            case 7:
                return G;
            case 8:
                return Ab;
            case 9:
                return A;
            case 10:
                return Bb;
            case 11:
                return B;
            default:
                return C; // can't happen
        }
    }

    /**
     * @return this Note's midi note number (in octave -1)
     */
    public int getNumber() {
        return this.minus(C);
    }

    /**
     * Return an array of Notes that name the same pitch as this Note
     * @return a Note[] containing the enharmonic spellings of this Note
     */
    public Note[] getAliases() {
        switch (ordinal) {
            case _Abb:
            case _G:
            case _Fx:
                return new Note[]{G, Abb, Fx};
            case _Ab:
            case _Gs:
                return new Note[]{Ab, Gs};
            case _Bbb:
            case _A:
            case _Gx:
                return new Note[]{A, Bbb, Gx};
            case _Cbb:
            case _Bb:
            case _As:
                return new Note[]{Bb, Cbb, As};
            case _Cb:
            case _B:
            case _Ax:
                return new Note[]{B, Cb, Ax};
            case _Dbb:
            case _C:
            case _Bs:
                return new Note[]{C, Dbb, Bs};
            case _Db:
            case _Cs:
            case _Bx:
                return new Note[]{Cs, Db, Bx};
            case _Ebb:
            case _D:
            case _Cx:
                return new Note[]{D, Ebb, Cx};
            case _Fbb:
            case _Eb:
            case _Ds:
                return new Note[]{Eb, Fbb, Ds};
            case _Fb:
            case _E:
            case _Dx:
                return new Note[]{E, Fb, Dx};
            case _Gbb:
            case _F:
            case _Es:
                return new Note[]{F, Gbb, Es};
            case _Gb:
            case _Fs:
            case _Ex:
                return new Note[]{Fs, Gb, Ex};
            default:
                return new Note[0]; // can't happen
        }
    }

    /**
     * Returns the Note with the alternative name of this Note, if any; else this Note
     * @param up a boolean indicating which direction; "true" if the alternate should be logically above the given Note
     * @return the Note which hase the alternative name of this note
     */
    public Note respell(boolean up) {
        Note n = this;
        if (up) {
            switch (ordinal) {
                case _A:
                    n = Bbb;
                    break;
                case _As:
                    n = Bb;
                    break;
                case _Ax:
                    n = B;
                    break;
                case _Bb:
                    n = Cbb;
                    break;
                case _B:
                    n = Cb;
                    break;
                case _Bs:
                    n = C;
                    break;
                case _Bx:
                    n = Cs;
                    break;
                case _C:
                    n = Dbb;
                    break;
                case _Cs:
                    n = Db;
                    break;
                case _Cx:
                    n = D;
                    break;
                case _D:
                    n = Ebb;
                    break;
                case _Ds:
                    n = Eb;
                    break;
                case _Dx:
                    n = E;
                    break;
                case _Eb:
                    n = Fbb;
                    break;
                case _E:
                    n = Fb;
                    break;
                case _Es:
                    n = F;
                    break;
                case _Ex:
                    n = Fs;
                    break;
                case _F:
                    n = Gbb;
                    break;
                case _Fs:
                    n = Gb;
                    break;
                case _Fx:
                    n = G;
                    break;
                case _G:
                    n = Abb;
                    break;
                case _Gs:
                    n = Ab;
                    break;
                case _Gx:
                    n = A;
                    break;
            }
        } else { // downward
            switch (ordinal) {
                case _Abb:
                    n = G;
                    break;
                case _Ab:
                    n = Gs;
                    break;
                case _A:
                    n = Gx;
                    break;
                case _Bbb:
                    n = A;
                    break;
                case _Bb:
                    n = As;
                    break;
                case _B:
                    n = Ax;
                    break;
                case _Cbb:
                    n = Bb;
                    break;
                case _Cb:
                    n = B;
                    break;
                case _C:
                    n = Bs;
                    break;
                case _Cs:
                    n = Bx;
                    break;
                case _Dbb:
                    n = C;
                    break;
                case _Db:
                    n = Cs;
                    break;
                case _D:
                    n = Cx;
                    break;
                case _Ebb:
                    n = D;
                    break;
                case _Eb:
                    n = Ds;
                    break;
                case _E:
                    n = Dx;
                    break;
                case _Fbb:
                    n = Eb;
                    break;
                case _Fb:
                    n = E;
                    break;
                case _F:
                    n = Es;
                    break;
                case _Fs:
                    n = Ex;
                    break;
                case _Gbb:
                    n = F;
                    break;
                case _Gb:
                    n = Fs;
                    break;
                case _G:
                    n = Fx;
                    break;
            }
        }
        return n;
    }

    /**
     * Return the AccidentalKind for this Note
     *
     * @return the AccidentalKind for this Note
     */
    public AccidentalKind getAccidentalKind() {
        int m = modifier();
        return AccidentalKind.forModifier(m);
    }

    /**
     * Return the basic Note name (without sharps or flats)
     *
     * @return the basic Note name
     */
    public String baseName() {
        return this.natural().toString();
    }

    /**
     * Apply the given AccidentalKind and return the resulting Note
     * @param ak an AccidentalKind
     * @return the resulting note
     */
    public Note apply(AccidentalKind ak) {
        Note n = this.natural();
        int delta = ak.modifier();
        while (delta < 0) {
            n = n.flatten();
            delta++;
        }
        while (delta > 0) {
            n = n.sharpen();
            delta--;
        }
        return n;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Note) {
            Note other = (Note)o;
            return this.ordinal == other.ordinal;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return name[ordinal].hashCode();
    }
}
