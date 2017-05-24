package net.weath.musicutil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents the distance between two pitches
 *
 * @author weath
 *
 */
public class Interval implements Comparable<Object> {

    /**
     * the number of note names spanned (1 = unison, 8 = octave)
     */
    private int basicSize;

    /**
     * the delta in terms of semitones (0 = unison, 12 = octave)
     */
    private int semis;

    /*
	 * Array of intervals in a major scale; indexed by basicSize%7
     */
    private static final Interval basicInterval[] = new Interval[7];

    /**
     * map an interval name to an Interval
     */
    private static final HashMap<String, Interval> nameMap = new HashMap<>();

    /**
     * map an Interval to its Quality
     */
    private static final HashMap<Interval, Quality> qualityMap = new HashMap<>();

    /**
     * the known Intervals, up through a perfect 15th
     */
    public static final Interval P1 = new Interval(1, 0);
    public static final Interval m2 = new Interval(2, 1);
    public static final Interval M2 = new Interval(2, 2);
    public static final Interval m3 = new Interval(3, 3);
    public static final Interval M3 = new Interval(3, 4);
    public static final Interval P4 = new Interval(4, 5);
    public static final Interval A4 = new Interval(4, 6);
    public static final Interval d5 = new Interval(5, 6);
    public static final Interval P5 = new Interval(5, 7);
    public static final Interval A5 = new Interval(5, 8);
    public static final Interval m6 = new Interval(6, 8);
    public static final Interval M6 = new Interval(6, 9);
    public static final Interval d7 = new Interval(7, 9);
    public static final Interval m7 = new Interval(7, 10);
    public static final Interval M7 = new Interval(7, 11);
    public static final Interval A7 = new Interval(7, 12);
    public static final Interval P8 = new Interval(8, 12);
    public static final Interval m9 = new Interval(9, 13);
    public static final Interval M9 = new Interval(9, 14);
    public static final Interval A9 = new Interval(9, 15);
    public static final Interval m10 = new Interval(10, 15);
    public static final Interval M10 = new Interval(10, 16);
    public static final Interval P11 = new Interval(11, 17);
    public static final Interval A11 = new Interval(11, 18);
    public static final Interval P12 = new Interval(12, 19);
    public static final Interval m13 = new Interval(13, 20);
    public static final Interval M13 = new Interval(13, 21);
    public static final Interval m14 = new Interval(14, 22);
    public static final Interval M14 = new Interval(14, 23);
    public static final Interval P15 = new Interval(15, 24);

    static {
        // populate the basicInterval array
        basicInterval[1] = P1;
        basicInterval[2] = M2;
        basicInterval[3] = M3;
        basicInterval[4] = P4;
        basicInterval[5] = P5;
        basicInterval[6] = M6;
        basicInterval[0] = M7;

        // populate the qualityMap
        qualityMap.put(P1, Quality.Perfect);
        qualityMap.put(m2, Quality.Minor);
        qualityMap.put(M2, Quality.Major);
        qualityMap.put(m3, Quality.Minor);
        qualityMap.put(M3, Quality.Major);
        qualityMap.put(P4, Quality.Perfect);
        qualityMap.put(A4, Quality.Augmented);
        qualityMap.put(d5, Quality.Diminished);
        qualityMap.put(P5, Quality.Perfect);
        qualityMap.put(A5, Quality.Augmented);
        qualityMap.put(m6, Quality.Minor);
        qualityMap.put(M6, Quality.Major);
        qualityMap.put(d7, Quality.Diminished);
        qualityMap.put(m7, Quality.Minor);
        qualityMap.put(M7, Quality.Major);
        qualityMap.put(P8, Quality.Perfect);
        qualityMap.put(m9, Quality.Minor);
        qualityMap.put(M9, Quality.Major);
        qualityMap.put(A9, Quality.Augmented);
        qualityMap.put(m10, Quality.Minor);
        qualityMap.put(M10, Quality.Major);
        qualityMap.put(P11, Quality.Perfect);
        qualityMap.put(A11, Quality.Augmented);
        qualityMap.put(P12, Quality.Perfect);
        qualityMap.put(m13, Quality.Minor);
        qualityMap.put(M13, Quality.Major);
        qualityMap.put(m14, Quality.Minor);
        qualityMap.put(M14, Quality.Major);
        qualityMap.put(P15, Quality.Perfect);

        // populate the nameMap
        nameMap.put("P1", P1);
        nameMap.put("m2", m2);
        nameMap.put("M2", M2);
        nameMap.put("m3", m3);
        nameMap.put("M3", M3);
        nameMap.put("P4", P4);
        nameMap.put("A4", A4);
        nameMap.put("d5", d5);
        nameMap.put("P5", P5);
        nameMap.put("A5", A5);
        nameMap.put("m6", m6);
        nameMap.put("M6", M6);
        nameMap.put("d7", d7);
        nameMap.put("m7", m7);
        nameMap.put("M7", M7);
        nameMap.put("P8", P8);
        nameMap.put("m9", m9);
        nameMap.put("M9", M9);
        nameMap.put("A9", A9);
        nameMap.put("m10", m10);
        nameMap.put("M10", M10);
        nameMap.put("P11", P11);
        nameMap.put("P12", P12);
        nameMap.put("A11", A11);
        nameMap.put("m13", m13);
        nameMap.put("M13", M13);
        nameMap.put("m14", m14);
        nameMap.put("M14", M14);
        nameMap.put("P15", P15);
    }

    /**
     * Enter all the basic intervals into the chord map.
     */
    public static void createChordsFromIntervals() {
        for (Interval n : nameMap.values().toArray(new Interval[0])) {
            if (!n.isBasicKind()) {
                continue;
            }
            Chord.create(n);
            for (int i = 1; i <= 5; i++) {
                if (n.semis >= i && n.getQuality() != Quality.Major) {
                    Interval n2 = new Interval(n.basicSize, n.semis - i);
                    Chord.create(n2);
                }
                if (n.getQuality() != Quality.Minor) {
                    Interval n2 = new Interval(n.basicSize, n.semis + i);
                    Chord.create(n2);
                }
            }
        }
    }

    /**
     * Look up an Interval by name; if not found, add it
     *
     * @param name the name, like P11
     * @return the named Interval
     */
    public static Interval lookup(String name) {
        Interval n = nameMap.get(name);
        if (n != null) {
            return n;
        } else {
            // parse the name
            if (name.startsWith("P")) {
                int bs = Integer.parseInt(name.substring(1));
                int mod = bs % 7;
                if (mod == 1 || mod == 4 || mod == 5) {
                    // P1 P8 P15 P22 ... P4 P11 P18 ... P5 P12 P19 ...
                    int s;
                    switch (mod) {
                        case 1:
                            s = ((bs - 1) / 7) * 12;
                            break;
                        case 4:
                            s = ((bs - 4) / 7) * 12 + 5;
                            break;
                        default:
                            s = ((bs - 5) / 7) * 12 + 7;
                            break;
                    }
                    n = new Interval(bs, s);
                }
            } else if (name.startsWith("M")) {
                int bs = Integer.parseInt(name.substring(1));
                int mod = bs % 7;
                if (mod == 2 || mod == 3 || mod == 6 || mod == 0) {
                    // M2 M9 M16 ... M3 M10 M17 ... M6 M13 M20 ... M7 M14 M21
                    // ...
                    int s;
                    switch (mod) {
                        case 2:
                            s = ((bs - 2) / 7) * 12 + 2;
                            break;
                        case 3:
                            s = ((bs - 3) / 7) * 12 + 4;
                            break;
                        case 6:
                            s = ((bs - 6) / 7) * 12 + 9;
                            break;
                        default:
                            s = ((bs - 7) / 7) * 12 + 11;
                            break;
                    }
                    n = new Interval(bs, s);
                }
            } else if (name.startsWith("m")) {
                int bs = Integer.parseInt(name.substring(1));
                int mod = bs % 7;
                if (mod == 2 || mod == 3 || mod == 6 || mod == 0) {
                    // m2 m9 m16 ... m3 m10 m17 ... m6 m13 m20 ... m7 m14 m21
                    // ...
                    int s;
                    switch (mod) {
                        case 2:
                            s = ((bs - 2) / 7) * 12 + 1;
                            break;
                        case 3:
                            s = ((bs - 3) / 7) * 12 + 3;
                            break;
                        case 6:
                            s = ((bs - 6) / 7) * 12 + 8;
                            break;
                        default:
                            s = ((bs - 7) / 7) * 12 + 10;
                            break;
                    }
                    n = new Interval(bs, s);
                }
            } else if (name.startsWith("A")) {
                // A1 AA1 AAA1 ...
                int prefix = name.split("[0-9]+")[0].length();
                int bs = Integer.parseInt(name.substring(prefix));
                int mod = bs % 7;
                if (mod == 1 || mod == 4 || mod == 5) {
                    n = lookup("P" + mod);
                } else {
                    if (mod == 0) {
                        mod = 7;
                    }
                    n = lookup("M" + mod);
                }
                n = new Interval(bs, n.semis + ((bs - mod) / 7) * 12 + prefix);
            } else if (name.startsWith("d")) {
                // d8 dd8 ddd8 ...
                int prefix = name.split("[0-9]+")[0].length();
                int bs = Integer.parseInt(name.substring(prefix));
                int mod = bs % 7;
                if (mod == 1 || mod == 4 || mod == 5) {
                    n = lookup("P" + mod);
                } else {
                    if (mod == 0) {
                        mod = 7;
                    }
                    n = lookup("m" + mod);
                }
                n = new Interval(bs, n.semis + ((bs - mod) / 7) * 12 - prefix);
            }
        }
        if (n == null) {
            throw new IllegalArgumentException("unknown Interval: " + name);
        }
        nameMap.put(name, n);
        return n;
    }

    /**
     * Is this Interval "basic" (Perfect, Major, or Minor)?
     *
     * @return true if this is a "basic" Interval
     */
    private boolean isBasicKind() {
        switch (this.getQuality()) {
            case Perfect:
            case Major:
            case Minor:
                return true;
            default:
                return false;
        }
    }

    /**
     * Construct a new Interval, given a basicSize and number of semitones
     *
     * @param basicSize the number of note names spanned (1 = unison)
     * @param semis the semitone difference (0 == unison)
     */
    public Interval(int basicSize, int semis) {
        if (basicSize < 1 || semis < 0) {
            throw new IllegalArgumentException(
                    "Notes are not in ascending order!");
        }
        this.basicSize = basicSize;
        this.semis = semis;
    }

    /**
     * Return the number of semitones in this Interval
     *
     * @return the number of semitones (0 == unison)
     */
    public int semitones() {
        return semis;
    }

    /**
     * Return the basic size of this Interval
     *
     * @return the number of note names spanned (1 == unison)
     */
    public int basicSize() {
        return basicSize;
    }

    /**
     * Create a new Interval, given two notes (in ascending order)
     *
     * @param one the lower note
     * @param two the upper note
     * @return an Interval
     */
    public static Interval between(Note one, Note two) {
        return new Interval(one.nameDist(two), two.minus(one));
    }

    /**
     * Return the inversion of this Interval; defined as (n*octaves - this)
     *
     * @return an Interval which, when added to this, will make an octave (or an
     * integer number of octaves)
     */
    public Interval invert() {
        int oct = spannedOctaves() + 1;
        while (oct * 12 < semis) {
            oct++;
        }
        return new Interval(oct * 7 + 2 - basicSize, oct * 12 - semis);
    }

    /**
     * Return an Interval representing the sum of this and "other"
     *
     * @param other another Interval
     * @return a new Interval representing the sum
     */
    public Interval plus(Interval other) {
        return new Interval(this.basicSize + other.basicSize - 1, this.semis
                + other.semis);
    }

    /**
     * Return an Interval representing the difference of this and "other"
     *
     * @param other another Interval
     * @return the difference, or null if "other" is bigger than "this"
     */
    public Interval minus(Interval other) {
        if (this.semis >= other.semis && this.basicSize >= other.basicSize) {
            return new Interval(this.basicSize - other.basicSize + 1,
                    this.semis - other.semis);
        } else {
            return null; // can't do it!
        }
    }

    /**
     * Return a double which represents the Equal Tempered ratio of this
     * interval. All values are >= 1.0 (unison).
     *
     * @return the Equal Tempered ratio as a double >= 1.0
     */
    public double getETratio() {
        return Math.pow(2.0, semis / 12.0);
    }

    /**
     * Return true if this Interval is the same as the given one
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Interval) {
            Interval other = (Interval) obj;
            return this.basicSize == other.basicSize
                    && this.semis == other.semis;
        }
        return false;
    }

    /**
     * Return a hash code (for storing in Hashtables)
     */
    @Override
    public int hashCode() {
        return basicSize * 1000 + semis;
    }

    /**
     * Return the String representation of this Interval
     */
    @Override
    public String toString() {
        return getQuality().toString() + " " + basicName();
    }

    /**
     * Return the Quality of this Interval (Perfect, Major, etc.)
     *
     * @return the Quality
     */
    public Quality getQuality() {
        Quality quality = qualityMap.get(this);
        if (quality != null) {
            return quality;
        }
        int bs = basicSize % 7;
        Interval base = basicInterval[bs];
        quality = base.getQuality();
        if (this.basicSize % 7 == base.basicSize % 7
                && this.semis % 12 == base.semis % 12) {
            // we're OK; it's just an octave-equivalence
        } else {
            // have to figure a modification to the base
            int oct = (this.basicSize - base.basicSize) / 7;
            int nSemis = this.semis - oct * 12;
            int n = nSemis - base.semis;
            if (n > 6) {
                n -= 12;
            }
            if (n < -6) {
                n += 12;
            }
            if (n > 0) {
                while (n-- != 0) {
                    quality = quality.next();
                    if (quality == null) {
                        throw new IllegalArgumentException();
                    }
                }
            } else {
                while (n++ != 0) {
                    quality = quality.prev();
                    if (quality == null) {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
        qualityMap.put(this, quality);
        return quality;
    }

    /**
     * Return the basic name (based on the number of spanned note names); works
     * for all possible intervals between MIDI notes (10.5 octaves)
     *
     * @return "unison", "second", etc.
     */
    public String basicName() {
        switch (basicSize) {
            case 1:
                return "unison";
            case 2:
                return "second";
            case 3:
                return "third";
            case 4:
                return "fourth";
            case 5:
                return "fifth";
            case 6:
                return "sixth";
            case 7:
                return "seventh";
            case 8:
                return "octave";
            case 9:
                return "ninth";
            case 10:
                return "tenth";
            case 11:
                return "eleventh";
            case 12:
                return "twelfth";
            case 13:
                return "thirteenth";
            case 14:
                return "fourteenth";
            case 15:
                return "double octave";
            case 22:
                return "triple octave";
            case 29:
                return "quadruple octave";
            case 36:
                return "quintuple octave";
            case 43:
                return "sextuple octave";
            case 50:
                return "septuple octave";
            case 57:
                return "octuple octave";
            case 64:
                return "nonuple octave";
            case 71:
                return "decuple octave";
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
                return "" + basicSize + "th";
            default:
                switch (basicSize % 10) {
                    case 1:
                        return "" + basicSize + "st";
                    case 2:
                        return "" + basicSize + "nd";
                    case 3:
                        return "" + basicSize + "rd";
                    default:
                        return "" + basicSize + "th";
                }
        }
    }

    private static Pitch[] getReal(Pitch[] pitches) {
        ArrayList<Pitch> arr = new ArrayList<>();
        for (Pitch p : pitches) {
            if (p.isReal()) {
                arr.add(p);
            }
        }
        return arr.toArray(new Pitch[0]);
    }

    /**
     * Return an array of Intervals for the given pitches, relative to the first
     * Pitch
     *
     * @param pitches array of Pitches
     * @param onlyReal true if we should ignore PhantomPitches
     * @return array of Intervals (size is 1 less than the given Pitch array)
     */
    public static Interval[] analyze(Pitch[] pitches, boolean onlyReal) {
        if (onlyReal) {
            pitches = getReal(pitches);
        }
        if (pitches.length == 0) {
            return new Interval[0];
        }
        Interval[] arr = new Interval[pitches.length - 1];
        Pitch root = pitches[0];
        for (int i = 1; i < pitches.length; i++) {
            Pitch p = pitches[i];
            arr[i - 1] = new Interval(root.distTo(p) + 1, p.getNumber()
                    - root.getNumber());
        }
        return arr;
    }

    /**
     * Return the best choice of Note for this Interval, given a root Note
     *
     * @param base the root Note
     * @param choices array of possible Note spellings
     * @return the best match
     */
    public Note bestMatch(Note base, Note[] choices) {
        int bs = this.basicSize();
        for (Note n : choices) {
            int nameDist = base.nameDist(n);
            if (nameDist == bs || nameDist + 7 == bs
                    || nameDist + 14 == bs) {
                return n;
            }
        }
        return null;
    }

    /**
     * Compare this Interval to another
     * @param obj the object to compare this to
     */
    @Override
    public int compareTo(Object obj) {
        if (obj instanceof Interval) {
            Interval other = (Interval) obj;
            if (this.basicSize == other.basicSize) {
                return this.semis - other.semis;
            } else {
                return this.basicSize - other.basicSize;
            }
        }
        return 0;
    }

    /**
     * @return the ID String (e.g., "P11")
     */
    public String id() {
        StringBuilder sb = new StringBuilder();
        Quality quality = getQuality();
        switch (quality) {
            case Perfect:
                sb.append("P");
                break;
            case Major:
                sb.append("M");
                break;
            case Minor:
                sb.append("m");
                break;
            case Augmented:
                sb.append("A");
                break;
            case Diminished:
                sb.append("d");
                break;
            case Augmented2:
                sb.append("AA");
                break;
            case Diminished2:
                sb.append("dd");
                break;
            case Augmented3:
                sb.append("AAA");
                break;
            case Diminished3:
                sb.append("ddd");
                break;
            case Augmented4:
                sb.append("AAAA");
                break;
            case Diminished4:
                sb.append("dddd");
                break;
            case Augmented5:
                sb.append("AAAAA");
                break;
            case Diminished5:
                sb.append("ddddd");
                break;
        }
        sb.append(Integer.toString(basicSize));
        return sb.toString();
    }

    /**
     * Return two ints which represents the Just Intonation ratio of this
     * interval. The first is the numerator; the second, the denominator.
     *
     * @return the Just Intonation ratio as two ints: [n,d]
     */
    public int[] getJIfraction() {
        int n;
        int d;
        int[] arr = new int[2];
        switch (semitones()) {
            case 0: // P1
                n = 1;
                d = 1;
                break;
            case 1:
                n = 16;
                d = 15;
                break;
            case 2: // M2
                n = 9;
                d = 8;
                break;
            case 3:
                n = 6;
                d = 5;
                break;
            case 4: // M3
                n = 5;
                d = 4;
                break;
            case 5: // P4
                n = 4;
                d = 3;
                break;
            case 6:
                n = 45;
                d = 32;
                break;
            case 7: // P5
                n = 3;
                d = 2;
                break;
            case 8:
                n = 8;
                d = 5;
                break;
            case 9: // M6
                n = 5;
                d = 3;
                break;
            case 10: // m7
                n = 9;
                d = 5;
                break;
            case 11:
                n = 15;
                d = 8;
                break;
            case 12: // P8
                n = 2;
                d = 1;
                break;
            default:
                if (basicSize > 8) { // bigger than an octave
                    int[] arr2 = new Interval(basicSize - 7, semis - 12)
                            .getJIfraction();
                    n = arr2[0];
                    d = arr2[1];
                    if ((d % 2) == 0) {
                        d = d / 2;
                    } else {
                        n = n * 2;
                    }
                } else {
                    // smaller than an octave, but basicSize > 12
                    // e.g., AA7
                    int[] arr2 = new Interval(basicSize, semis - 12)
                            .getJIfraction();
                    n = arr2[0];
                    d = arr2[1];
                    if ((d % 2) == 0) {
                        d = d / 2;
                    } else {
                        n = n * 2;
                    }
                }
                break;
        }
        arr[0] = n;
        arr[1] = d;
        return arr;
    }

    /**
     * Return a double which represents the Just Intonation ratio of this
     * interval. All values are >= 1.0 (unison).
     *
     * @return the Just Intonation ratio as a double >= 1.0
     */
    public double getJIratio() {
        int[] arr = getJIfraction();
        return (double) arr[0] / (double) arr[1];
    }

    /**
     * Is this Interval "octave-equivalent" to the other?
     *
     * @param other
     * @return true if they differ only by octave(s)
     */
    public boolean differsByOctaveWith(Interval other) {
        int bs1 = this.basicSize % 7;
        int bs2 = other.basicSize % 7;
        Quality q1 = this.getQuality();
        Quality q2 = other.getQuality();
        return bs1 == bs2 && q1 == q2;
    }

    /**
     * Return the number of octaves spanned by this Interval
     *
     * @return an int, the number of octaves spanned
     */
    public int spannedOctaves() {
        return (basicSize - 1) / 7;
    }

    /**
     * Is this Interval enharmonic to the other?
     *
     * @param other another Interval
     * @return true if span the same number of semitones
     */
    public boolean isEnharmonicWith(Interval other) {
        return this.semis == other.semis;
    }

    /**
     * Return an "equivalent" Interval that is less than 1 octave wide
     *
     * @return simplified Interval
     */
    public Interval simplify() {
        if (basicSize > 7 && semis > 11) {
            return new Interval(basicSize - 7, semis - 12).simplify();
        }
        return this;
    }

    /**
     * Return true if this Interval is the same as other Interval, ignoring
     * octaves and enharmonic spellings.
     *
     * @param other another Interval
     * @return true if equivalent
     */
    public boolean isEquivalentTo(Interval other) {
        return this.simplify().isEnharmonicWith(other.simplify());
    }

}
