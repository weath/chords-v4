package net.weath.musicutil;

import java.util.HashMap;

/**
 * Pitch = a named frequency, possibly a Midi note number, a letter name,
 * possibly a flat or sharp, and an offset from the Equal Tempered note so
 * named.
 *
 * The octave numbers range from -1 to 9; Middle C is C4, A440 is A4, and the
 lowest Midi note is C-1 (number 0).

 Note that B#3 is the same pitch as C4, and Cb4 is the same pitch as B3.
 Likewise, Bx3 is the same pitch as C#4, and you can probably guess that Cbb4
 is the same pitch as Bb3.
 *
 * @author weath
 *
 */
public class Pitch implements Comparable<Pitch> {

    public static final double SEMITONE = Math.pow(2.0, 1.0 / 12.0);  // 12th root of 2
    public static final double QUARTERTONE = Math.pow(2.0, 1.0 / 24.0);  // 24th root of 2
    public static final double MINFREQ = 8.1757989156; // C-1's freq
    private static final double epsilon = 0.00001;

    private static final HashMap<Note, Double> pitch = new HashMap<>();

    static {
        double f = MINFREQ;
        for (Note n : Note.getAll()) {
            pitch.put(n, f);
            f *= SEMITONE;
        }
    }

    /**
     * middle C (C4)
     */
    public static Pitch middleC = new Pitch(Note.C, 4);

    /**
     * name of this pitch
     */
    Note note;

    /**
     * frequency (Hz)
     */
    private double freq;

    /**
     * nearest MIDI note number (within 50 cents +/-
     */
    private int number = -1;
    // May actually exceed the Midi spec; not limited to 0-127, but not negative.

    private Object voicePart; // null unless from MusicXML file (Object to avoid circular dependency)

    public Pitch(double freq) {
        this.freq = freq;
        figureNumber();
        figureNote();
    }

    private void figureNote() {
        note = Note.forMidiNumber(number);
    }

    public Pitch(int midiNumber) {
        this(Note.forMidiNumber(midiNumber), (midiNumber / 12) - 1);
    }

    public Pitch(Note note, int octave) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }
        freq = pitch.get(Note.forMidiNumber(note.getNumber())); // note in octave "-1"
        number = note.minus(Note.C);
        for (int oct = -1; oct < octave; oct++) {
            number += 12;
        }
        // MIDI number may need adjusting near octave boundary
        switch (note.toString()) {
            case "B#":
            case "Bx":
                number += 12;
                freq *= 2;
                break;
            case "Cb":
            case "Cbb":
                number -= 12;
                freq /= 2;
                break;
        }
        this.note = note;
        for (int oct = -1; oct < octave; oct++) {
            freq *= 2;
        }
    }

    public Pitch(Note note, int octave, int bend) {
        this(note, octave);
        double f = freq;
        f = f * Math.pow(2.0, (double) bend / (12 * 4096.0) * Math.log(2.0));
        freq = f;
    }

    /**
     * Create a new Pitch at the given Interval above the root
     *
     * @param rootPitch the root Pitch
     * @param n the Interval
     */
    public Pitch(Pitch rootPitch, Interval n) {
        this.note = rootPitch.note.plus(n);
        int targetNumber = rootPitch.number + n.semitones();
        this.freq = rootPitch.freq * (Math.pow(SEMITONE, n.semitones()));
        figureNumber();
        while (this.number < targetNumber) {
            plusOctave();
        }
    }

    public Pitch(Note n) {
        this(n, -1);
    }

    public Pitch(Pitch p) {
        this(p.getFrequency());
    }

    private void figureNumber() {
        // based on A440 = MIDI pitch 69
        int n = 69;
        double a = 440;

        if (freq < MINFREQ) {
            return; // illegal freq -- no MIDI number!
        }
        while (a + epsilon < freq) {
            a *= 2.0;
            n += 12;
        }
        while (a - epsilon > freq) {
            a /= 2.0;
            n -= 12;
        }

        // a is now the frequency of the A which is below the
        // given pitch, but not more than 1 octave below it, and
        // n is the MIDI note number for that A
        double f = a;
        double f1 = f * SEMITONE;

        while (f1 < freq) {
            n++;
            f = f1;
            f1 = f * SEMITONE;
        }

        // f is now one semitone or less below freq, and
        // f1 is one semitone above f; n is f's number
        // if f is more than 0.5 semitone low, use f1 instead
        if (freq / f > QUARTERTONE) {
            number = n + 1;
        } else {
            number = n;
        }
    }

    public final void plusOctave() {
        number += 12;
        freq *= 2.0;
    }

    public void minusOctave() {
        number -= 12;
        freq /= 2.0;
    }

    @Override
    public String toString() {
        if (number < 0) {
            return "<none>";
        }
        double cents = getCents();
        char sign = '+';
        if (cents < 0.0) {
            cents *= -1.0;
            sign = '-';
        }
        int octave = getOctave();
        return String.format("%s%c%5.3f(%4.3fHz)(n=%d,o=%d)",
                note, sign, cents, freq, number, octave);
    }

    public double getCents() {
        double d = freq;
        double c = getFreq(number);
        double cents = 1200.0 * Math.log(d / c) / Math.log(2.0);
        // suppress very small values
        if (Math.abs(cents) < 0.0001) {
            cents = 0.0;
        }
        return cents;
    }

    /**
     * Return frequency for MIDI note number "num"
     *
     * @param num the MIDI note number, 0 - 127
     * @return the frequency in Hz as a double
     */
    private static double getFreq(int num) {
        double f = MINFREQ;
        int n = 0;

        while (n < num - 11) {
            f *= 2.0;
            n += 12;
        }
        // n is now at or below num, but less than 12 below, and
        // f is the frequency of that note (a C)

        while (n < num) {
            f *= SEMITONE;
            n++;
        }

        return f;
    }

    public Note getNote() {
        return note;
    }

    public int getNumber() {
        return number;
    }

    public double getFrequency() {
        return freq;
    }

    public double getETFrequency() {
        return getFreq(number);
    }

    public int getOctave() {
        int octave = (number / 12) - 1;
        // adjust near octave boundary
        switch (note.toString()) {
            case "B#":
            case "Bx":
                octave--;
                break;
            case "Cb":
            case "Cbb":
                octave++;
                break;
        }
        return octave;
    }

    public int getPitchBend() {
        double cents = getCents();
        return (int) (cents * 40.96);
    }

    /**
     * The "line" for the note. This is an integer, 0 - 74, that corresponds to
     * the line or space of the staff that this pitch would be drawn on. 0 ==
     * C0, 35 == C4 (middle C). Spaces are even numbers.
     *
     * @return an int corresponding to the line or space of the staff
     */
    private int line() {
        int n = Note.C.nameDist(note) - 1;
        n += (getOctave() + 1) * 7;
        return n;
    }

    /**
     * Upward distance to another Pitch, in note names; unison = 0
     *
     * @param p another Pitch
     * @return distance in note names
     */
    public int distTo(Pitch p) {
        int n = this.line();
        int m = p.line();
        n = m - n;
        return n;
    }

    /**
     * Return the distance from a root Pitch up to this one, as an Interval
     *
     * @param pitch the root Pitch
     * @return the Interval between the other Pitch and this one
     */
    public Interval minus(Pitch pitch) {
        Interval[] v = Interval.analyze(new Pitch[]{pitch, this}, false);
        return v[0];
    }

    public void setOctave(int newOct) {
        int oldOct = getOctave();
        while (oldOct < newOct) {
            plusOctave();
            oldOct++;
        }
        while (oldOct > newOct) {
            minusOctave();
            oldOct--;
        }
    }

    public void setCents(double cents) {
        double oldCents = getCents();
        double newFreq = freq;
//		Note oldNote = note;
        newFreq = newFreq * Math.pow(2.0, (cents - oldCents) / 1200.0);
        freq = newFreq;
//		int oldNum = getNumber();
//		figureNumber();
//		note = oldNote;
    }

    public void setBend(int bend) {
        setCents((double) bend / 40.96);
    }

    public boolean respell(Chord chord, Pitch p) {
        return false;
    }

    public Pitch plus(Interval n) {
        Pitch p = new Pitch(getFrequency() * n.getETratio());
        return p;
    }

    public Pitch minus(Interval n) {
        Note newNote = note.minus(n);
        Pitch p = new Pitch(getFrequency() / n.getETratio());
        p.respell(newNote);
        return p;
    }

    public void addBend(int delta) {
        int bend = getPitchBend();
        setBend(bend + delta);
    }

    public static void main(String[] args) {
        Pitch a = new Pitch(440.0);
        System.out.format("A 440Hz = %s\n", a);
        Pitch cm1 = new Pitch(Note.C, -1);
        System.out.format("C-1 = %s\n", cm1);
        System.out.format("middle C = %s\n", middleC);
        Pitch e5 = new Pitch(660.0);
        System.out.format("E 660Hz = %s\n", e5);
        e5.setCents(0);
        System.out.format("E5 (ET) = %s\n", e5);
        e5 = new Pitch(76);
        System.out.format("E5 (midi 76) = %s\n", e5);
        e5.setCents(1.955);
        System.out.format("E5 + 1.955\u00a2 = %s\n", e5);
        Pitch g = new Pitch(770.0);
        System.out.format("G 770Hz = %s\n", g);
        Pitch p = new Pitch(Note.A, 5);
        p.setBend(4096);
        System.out.format("A5 plus 4096pbu = %s\n", p);
        p = p.plus(Interval.P5);
        System.out.format("... plus P5 = %s\n", p);
        p = new Pitch(Note.Bs, 3);
        System.out.format("B#3 = %s; line = %d\n", p, p.line());
        p = new Pitch(Note.Bx, 3);
        System.out.format("Bx3 = %s; line = %d\n", p, p.line());
        p = new Pitch(Note.Cb, 4);
        System.out.format("Cb4 = %s; line = %d\n", p, p.line());
        p = new Pitch(Note.Cbb, 4);
        System.out.format("Cbb4 = %s; line = %d\n", p, p.line());
    }

    @Override
    public int compareTo(Pitch other) {
        if (this.getNumber() == other.getNumber()) {
            if (this.note.equals(other.note)) {
                return 0;
            }
            return (this.note.nameDist(other.note) < 3) ? -1 : 1;
        }
        return this.getNumber() - other.getNumber();
    }

    /**
     * Respell -- change name without changing frequency
     *
     * @param n new Note name
     * @return this
     */
    public Pitch respell(Note n) {
        if (n.equals(note)) {
            return this; // no-op
        }
        Note[] aliases = note.getAliases();
        boolean found = false;
        for (Note nn : aliases) {
            if (nn.equals(n)) {
                found = true;
                break;
            }
        }
//        if (!found) {
//            throw new IllegalArgumentException("New name is not an alias of the old name: " + n + " != " + note);
//        }
        note = n;
        return this;
    }

    public Object getVoicePart() {
        return voicePart;
    }

    public void setVoicePart(Object voicePart) {
        this.voicePart = voicePart;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(freq);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((note == null) ? 0 : note.hashCode());
        result = prime * result + number;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Pitch other = (Pitch) obj;
        if (Double.doubleToLongBits(freq) != Double
                .doubleToLongBits(other.freq)) {
            return false;
        }
        if (note != other.note) {
            return false;
        }
        if (number != other.number) {
            return false;
        }
        return true;
    }

    /**
     * compare for equality, ignoring pitch bend
     *
     * @param p a Pitch
     * @return true if same note and same octave
     */
    public boolean equalsET(Pitch p) {
        return p.getNote().equals(getNote()) && p.getOctave() == getOctave();
    }

    /**
     * Change the note name enharmonically
     *
     * @param up true if new basic name should be higher (F# -> Gb)
     */
    public void enharmonicallyRespell(boolean up) {
        note = note.respell(up);
    }

    public boolean isReal() {
        return note.isReal();
    }
    
    public Pitch asET() {
        Pitch p = new Pitch(this);
        p.setBend(0);
        return p;
    }
}
