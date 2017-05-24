package net.weath.musicutil;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * A chord represented as a set of intervals (relative to the bass note) A chord
 * is always in root position; A ChordInstance may have a different inversion.
 *
 * @author weath
 */
public class Chord implements Comparable<Object> {

    /**
     * properties set via the command line; see parseArgs() and getResource()
     */
    private static HashMap<String, String> propMap = new HashMap<>();

    private static String propsPath = null;

    /**
     * all the chords that have been created
     */
    static ArrayList<Chord> allChords = new ArrayList<>();
    
    /**
     * the names of all the known chords
     */
    static HashSet<String> chordNames = new HashSet<>();

    /**
     * per-instance data
     */
    /**
     * is this an interval? some special handling
     */
    boolean isInterval = false;

    /**
     * relative measure of complexity, for sorting
     */
    private int complexity = 0;

    /**
     * list of Intervals, relative to some bass note
     */
    Interval[] intervals;

    /**
     * the suffix ("name"); e.g., m7
     */
    String suffix;

    /**
     * one or more RatioSets, for tuning
     */
    private RatioSet[] ratios;

    /**
     * the currently selected tuning, 0 .. ratios.length-1
     */
    private int selectedTuning;

    /**
     * alternate names for this chord
     */
    private final HashSet<String> aliases = new HashSet<>();

    /**
     * ways of looking up a chord
     */
    /**
     * map a suffix to a list of Chords
     */
    private static MultiMap suffixMap = new MultiMap();

    /**
     * map a set of notes to a list of Chords
     */
    private static MultiMap noteMap = new MultiMap();

    /**
     * map a set of deltas (pitch distances) to a set of Chords
     */
    private static MultiMap deltaMap = new MultiMap();

    /**
     * the "power" chord (only non-interval Chord that contains only two notes)
     */
    public static Chord power;

    /**
     * the major chord
     */
    public static Chord major;

    /**
     * Recognize and remove args of the form "foo=bar"
     *
     * @param args argument array (modified by this method)
     */
    public static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.indexOf("=") > 0 && !arg.startsWith("dumpProps=")) {
                args[i] = null; // erase from arg list
                String[] arr = arg.split("=");
                propMap.put(arr[0], arr[1]);
                if (arr[0].equals("props")) {
                    propsPath = arr[1];
                }
            }
        }

        readChords();

        createChords();
    }

    private static void readChords() {
        readChordsFromProperties();
        try {
            power = suffixMap.get("5").get(0);
            major = suffixMap.get("").get(0);
        } catch (IndexOutOfBoundsException e) {
            if (major == null) {
                System.err.println("Can't find major chord!");
            } else {
                System.err.println("Can't find power chord!");
            }
            System.err.println("Check ChordProps.properties, \"chord\" property.");
        }
    }

    private static void createChords() {
        Interval.createChordsFromIntervals();

        // create "(no 5)" variants
        for (Chord c : allChords.toArray(new Chord[0])) {
            if (c.contains(Interval.P5)) {
                if (c.isInterval() || c == power || c instanceof RootlessChord) {
                    continue;
                }
                ArrayList<Interval> list = new ArrayList<>();
                if (c.ratios.length == 0 || c.ratios[0].size() == 0) {
                    continue;
                }
                int[] values = new int[c.ratios[0].size() - 1];
                values[0] = c.ratios[0].value(0);
                int i = 1;
                int j = 0;
                for (Interval n : c.intervals) {
                    j++;
                    if (n.equals(Interval.P5)) {
                        continue;
                    }
                    list.add(n);
                    values[i++] = c.ratios[0].value(j);
                }
                if (!chordNames.contains(c.getName() + " (no 5)")) {
                    Chord newChord = create(c.getName() + " (no 5)", list, values, false);
                    newChord.addComplexity(100000);
                }
            }
        }
    }

    /**
     * Look up a chord by name
     *
     * @param suffix the suffix part ("m7", for example)
     * @return the Chord with that suffix
     */
    public static Chord lookup(String suffix) {
        ArrayList<Chord> a = suffixMap.get(suffix);
        if (a.isEmpty()) {
            return null;
        }
        return a.get(0);
    }

    private static void readChordsFromProperties() {
        String str = getResource("chords");
        StringTokenizer st = new StringTokenizer(str, ";");
        while (st.hasMoreTokens()) {
            String names = st.nextToken();
            String intervals = st.nextToken();
            String ratios = st.nextToken();
            StringTokenizer nameTokens = new StringTokenizer(names, "|", false);
            Chord c = null;
            while (nameTokens.hasMoreTokens()) {
                String name = nameTokens.nextToken();
                name = name.replace("(", " (");
                name = name.replace("no", "no ");
                name = name.replace("  ", " ");
                if (c == null) {
                    if (name.equals("maj")) {
                        c = create("", intervals, ratios, false);
                        c.addAlias("maj");
                    } else {
                        c = create(name, intervals, ratios, false);
                        possiblyAddUnrooted(name, intervals, ratios);
                    }
                } else {
                    c.addAlias(name);
                }
            }
        }
    }

    public static String getResource(String name) {
        ResourceBundle bundle = null;
        if (propMap.containsKey(name)) {
            return propMap.get(name);
        }
        if (propsPath != null) {
            Properties props = new Properties();
            String msg = "";
            try {
                LineNumberReader lnr = new LineNumberReader(new FileReader(propsPath));
                props.load(lnr);
            } catch (IOException e) {
                msg = e.getMessage();
            }
            if (props.isEmpty()) {
                System.err.println("Cannot load properties from " + propsPath + ": " + msg);
                System.exit(2);
            } else {
                String value = props.getProperty(name);
                if (value == null) {
                    System.err.println("No such property found: " + name);
                    System.exit(3);
                }
                return value;
            }
        }
        try {
            bundle = ResourceBundle.getBundle("net.weath.musicutil.ChordProps");
        } catch (MissingResourceException ex) {
            // do nothing
        }
        if (bundle == null) {
            return "Cannot locate resource bundle";
        }
        return bundle.getString(name);
    }

    /**
     * Look up a set of pitches
     *
     * @param pitches a sorted array of Pitch
     * @return a list of Chord
     */
    public static ArrayList<Chord> lookup(Pitch[] pitches) {
        Interval[] intervals = Interval.analyze(pitches, true);
        ArrayList<Chord> arr = new ArrayList<>();
        arr.addAll(lookup(intervals));
        // try all inversions
        Pitch[] pArr = new Pitch[pitches.length];
        for (int i = 1; i < pArr.length; i++) {
            System.arraycopy(pitches, i, pArr, 0, pArr.length - i);
            System.arraycopy(pitches, 0, pArr, pArr.length - i, i);
            for (int j = 1; j < pArr.length; j++) {
                while (pArr[j].getNumber() <= pArr[j - 1].getNumber()) {
                    pArr[j] = pArr[j].plus(Interval.P8);
                }
            }
            intervals = Interval.analyze(pArr, true);
            arr.addAll(lookup(intervals));
        }
        return sort(arr);
    }

    /**
     * Look up a set of pitches, with possible respellings
     *
     * @param pitches a sorted array of Pitch
     * @param useEnharmonic if true, allow respellings
     * @return a list of Chord
     */
    public static ArrayList<Chord> lookup(Pitch[] pitches, boolean useEnharmonic) {
        if (useEnharmonic) {
            Interval[] intervals = Interval.analyze(pitches, true);
            if (intervals.length == 1) {
                return lookup(intervals);
            }
            HashSet<Chord> set = new HashSet<>();
            ArrayList<Chord> list = lookup(pitches);
            set.addAll(list);
            intervals = reduce(intervals);
            list = lookup(intervals);
            set.addAll(list);
            Delta[] deltas = getDeltas(intervals);
            list = deltaMap.get(toString(deltas));
            set.addAll(list);
            list = new ArrayList<>();
            list.addAll(set);
            list = sort(list);
            if (list.size() > 0 && list.get(0).isInterval()) {
                Chord c = list.get(0);
                list.clear();
                if (c.intervals[0] == Interval.P5 && pitches.length > 2) {
                    list.add(power);
                }
                list.add(c); // return only best match if interval
            }
            return list;
        } else {
            return lookup(pitches);
        }
    }

    /**
     * is this "chord" just one interval?
     *
     * @return true if it is an interval, not a complete chord
     */
    public boolean isInterval() {
        return isInterval;
    }

    /**
     * Sort an ArrayList of Chords
     *
     * @param chords the ArrayList
     * @return a sorted ArrayList of Chords
     */
    private static ArrayList<Chord> sort(ArrayList<Chord> chords) {
        Chord[] arr = new Chord[chords.size()];
        int i = 0;
        for (Chord c : chords) {
            arr[i++] = c;
        }
        Arrays.sort(arr);
        ArrayList<Chord> result = new ArrayList<>();
        result.addAll(Arrays.asList(arr));
        return result;
    }

    /**
     * Look up a list of Deltas (pitch distances)
     *
     * @param deltas a sorted array of Deltas
     * @return a list of Chords
     */
    private static ArrayList<Chord> lookup(Delta[] deltas) {
        ArrayList<Chord> result = deltaMap.get(toString(deltas));
        if (result == null) {
            result = new ArrayList<>();
        }
        return result;
    }

    /**
     * Generate a set of Deltas from a set of Intervals
     *
     * @param intervals array of Intervals
     * @return array of Deltas
     */
    private static Delta[] getDeltas(Interval[] intervals) {
        ArrayList<Delta> deltas = new ArrayList<>();
        for (Interval interval : intervals) {
            if (interval.isEquivalentTo(Interval.P1)) {
                continue;
            }
            deltas.add(new Delta(interval.semitones()));
        }
        return deltas.toArray(new Delta[0]);
    }

    /**
     * Look up a list of Intervals
     *
     * @param intervals a sorted array of Intervals
     * @return a list of Chords
     */
    public static ArrayList<Chord> lookup(Interval[] intervals) {
        ArrayList<Chord> best = noteMap.get(toString(intervals));
        if (best.size() > 0 && best.get(0).isInterval()) {
            return best;
        }
        if (intervals.length == 1) {
            Interval n = intervals[0];
            if (!suffixMap.containsKey(n.toString())) {
                create(n);
            }
            ArrayList<Chord> list = new ArrayList<>();
            list.add(lookup(n.toString()));
            return list;
        }
        ArrayList<Chord> reduced = noteMap.get(toString(reduce(intervals)));
        ArrayList<Chord> enharmonic = lookup(getDeltas(intervals));
        ArrayList<Chord> result = new ArrayList<>(best);
        for (Chord c : reduced) {
            if (!result.contains(c)) {
                result.add(c);
            }
        }
        for (Chord c : enharmonic) {
            if (!result.contains(c)) {
                result.add(c);
            }
        }
        result = sort(result);
        return result;
    }

    /**
     * Add one or more aliases to this Chord
     *
     * @param args one or more Strings
     */
    private void addAlias(Object... args) {
        for (Object arg : args) {
            String s = (String) arg;
            if (aliases.contains(s) || suffix.equals(s)) {
                System.err.println("DUP ALIAS: " + s);
            }
            aliases.add(s);
            if (suffixMap.containsKey(s)) {
                System.err.println("DUP ALIAS: " + s + ": " + this + ", "
                        + suffixMap.get(s));
            }
            suffixMap.add(s, this);
        }
    }

    /**
     * Return the name (suffix) of this Chord
     *
     * @return the name (a String)
     */
    public String getName() {
        return suffix;
    }

    /**
     * Return the name, given a root note
     *
     * @param root
     * @return the name (a String);
     */
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
     * Create a new Chord from the given Interval
     *
     * @param n an Interval
     * @return a Chord with the same name as the Interval
     */
    public static Chord create(Interval n) {
        int[] arr = n.getJIfraction();
        Chord c = create(n, arr[1], arr[0]);
        return c;
    }

    /**
     * Create a new Chord from the given Interval
     *
     * @param n an Interval
     * @param i the first number in the Intervals ratio
     * @param j the second (higher) number in the ratio
     * @return a new Chord with the same name as the Interval
     */
    private static Chord create(Interval n, int i, int j) {
        Chord c = create(n.toString(), n.id(), i, j);
        return c;
    }

    /**
     * Create a new Chord from the given lists of Intervals and ints (ratios)
     *
     * @param suffix the name of the chord
     * @param list ArrayList of Intervals
     * @param values array of ints (ratios)
     * @param rootless true if the chord should be a RootlessChord
     * @return the new Chord
     */
    protected static Chord create(String suffix, ArrayList<Interval> list,
            int[] values, boolean rootless) {
        Interval[] n = list.toArray(new Interval[0]);
        Chord c;
        if (rootless) {
            c = new RootlessChord(suffix, n);
        } else {
            c = new Chord(suffix, false, n);
        }
        c.ratios = new RatioSet[1];
        c.ratios[0] = new RatioSet(values);
        addToMaps(c);
        return c;
    }

    private static Chord create(String suffix, String intervals, String ratios, boolean rootless) {
        ArrayList<Interval> ia = new ArrayList<>();
        ArrayList<Integer> arr = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(intervals, " ");
        while (st.hasMoreTokens()) {
            ia.add(Interval.lookup(st.nextToken()));
        }
        String[] r = ratios.split("\\|");
        st = new StringTokenizer(r[0], " ");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.equals("*")) {
                arr = figureRatios(suffix, ia.toArray(new Interval[0]));
            } else {
                arr.add(Integer.parseInt(s));
            }
        }
        int[] a = new int[arr.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = arr.get(i);
        }

        Chord chord = create(suffix, ia, a, rootless);

        if (r.length > 1) {
            arr.clear();
            for (int j = 1; j < r.length; j++) {
                st = new StringTokenizer(r[j], " ");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    if (s.equals("*")) {
                        continue; // no ratios
                    }
                    arr.add(Integer.parseInt(s));
                }
                a = new int[arr.size()];
                for (int i = 0; i < a.length; i++) {
                    a[i] = arr.get(i);
                }
                chord.addTuning(a);
            }
        }

        return chord;
    }

    private static String[] canBeRootless = {"maj7", "maj7#5", "m7", "m7b5"};

    private static String rootlessSuffix(String suffix) {
        if (suffix.equals("maj7")) {
            return "m9 (no root)";
        }
        if (suffix.equals("maj7#5")) {
            return "mM9 (no root)";
        }
        if (suffix.equals("m7")) {
            return "M9 (no root)";
        }
        if (suffix.equals("m7b5")) {
            return "9 (no root)";
        }
        return "???";
    }

    @SuppressWarnings("unused")
	private static String rootlessIntervals(String baseName) {
        switch (baseName) {
            case "maj7": // m9
                return "m3 P5 m7 M9";
            case "maj7#5": // mM9
                return "m3 P5 M7 M9";
            case "m7": // M9
                return "M3 P5 M7 M9";
            case "m7b5": // 9
                return "M3 P5 m7 M9";
            default:
                return "???";
        }
    }

    private static String rootlessRatios(String baseName) {
        switch (baseName) {
            case "maj7": // m9
                return "12 14 18 21 27";
            case "maj7#5": // mM9
                return "20 24 30 38 45";
            case "m7": // M9
                return "8 10 12 15 18";
            case "m7b5": // 9
                return "4 5 6 7 9";
            default:
                return "???";
        }
    }

    private static void possiblyAddUnrooted(String name, String intervals, String ratios) {
        for (String s : canBeRootless) {
            if (s.equals(name)) {
                create(rootlessSuffix(name), intervals, rootlessRatios(name), true);
            }
        }

    }

    private static ArrayList<Integer> figureRatios(String name, Interval[] ints) {
        // find LCM of denominators
        int lcm = 1;
        int[] num = new int[ints.length];
        int[] den = new int[ints.length];
        for (int i = 0; i < ints.length; i++) {
            Interval n = ints[i];
            int d = n.getJIfraction()[1];
            num[i] = n.getJIfraction()[0];
            den[i] = d;
            lcm = Util.lcm(lcm, d);
        }
        for (int i = 0; i < num.length; i++) {
            num[i] *= lcm / den[i];
        }
        ArrayList<Integer> r = new ArrayList<>();
        r.add(lcm);
        for (int i = 0; i < num.length; i++) {
            r.add(num[i]);
        }
        System.err.print("Figured ratios for " + name + ": ");
        for (int i = 0; i < r.size(); i++) {
            System.err.print(String.format("%d ", r.get(i)));
        }
        System.err.println();
        return r;
    }

    private void addTuning(int[] a) {
        if (ratios == null) {
            ratios = new RatioSet[0];
        }
        int n = ratios.length;
        RatioSet[] newRatioSet = new RatioSet[n + 1];
        System.arraycopy(ratios, 0, newRatioSet, 0, n);
        newRatioSet[n] = new RatioSet(a);
        ratios = newRatioSet;
    }

    /**
     * Create a new Chord from the given String (intervals) and ints (ratios)
     *
     * @param suffix the Chord name
     * @param s a String containing Interval abbrevs
     * @param r1 the first int in the ratios
     * @param r array of the rest of the ints in the ratios
     * @return a new Chord
     */
    private static Chord create(String suffix, String s, int r1, int... r) {
        Chord c = create(suffix, s);
        c.ratios = new RatioSet[1];
        c.ratios[0] = new RatioSet(r1, r);
        if (c.ratios[0].size() != c.intervals.length + 1) {
            throw new IllegalArgumentException(
                    "number of ints for ratios inconsistent with interval list: "
                    + suffix);
        }
        c.isInterval = c.ratios[0].size() == 2 && !suffix.equals("5");
        addToMaps(c);
        return c;
    }

    /**
     * Create a new Chord with the given name and intervals
     *
     * @param suffix the Chord name
     * @param s a string containing Interval abbrevs
     * @return a new Chord
     */
    private static Chord create(String suffix, String s) {
        StringTokenizer st = new StringTokenizer(s, " ");
        ArrayList<Interval> a = new ArrayList<>();
        do {
            String str = st.nextToken();
            Interval i = Interval.lookup(str);
            a.add(i);

        } while (st.hasMoreTokens());
        Chord c = new Chord(suffix, false, a.toArray(new Interval[0]));
        // addToMaps(c);
        return c;
    }

    /**
     * Add the given Chord to our various maps
     *
     * @param c a Chord
     */
    private static void addToMaps(Chord c) {
        String suffix = c.suffix;
        c.complexity = c.computeComplexity();
        addToDeltaMap(c);
        if (suffixMap.containsKey(suffix)) {
            System.err.println("DUP SUFFIX: " + suffix);
            return;
        }
        suffixMap.add(suffix, c);
        if (noteMap.containsKey(toString(c.intervals))) {
            // ArrayList<Chord> other = noteMap.get(toString(c.intervals));
            // if (c.intervals.length != 1)
            // System.err.println("DUP INTERVALS(" + c + "): " +
            // toString(c.intervals) + " = " + other.get(0));
            ArrayList<Chord> other = noteMap.get(toString(c.intervals));
            other.add(c);
            return;
        }
        addToNoteMap(c);
    }

    /**
     * Add the given Chord to the noteMap
     *
     * @param c a Chord
     */
    private static void addToNoteMap(Chord c) {
        String s = toString(c.intervals);
        noteMap.add(s, c);
        if (!c.isInterval()) {
            String oldStr = toString(c.intervals);
            Interval[] newArr = reduce(c.intervals);
            String newStr = toString(newArr);
            if (!oldStr.equals(newStr)) {
                noteMap.add(newStr, c);
            }
        }
    }

    /**
     * Add the given Chord to the deltaMap
     *
     * @param c a Chord
     */
    private static void addToDeltaMap(Chord c) {
        String s = toString(c.getDeltas());
        deltaMap.add(s, c);
        if (!c.isInterval()) {
            String oldStr = toString(c.getDeltas());
            Delta[] newArr = reduce(c.getDeltas());
            String newStr = toString(newArr);
            if (!oldStr.equals(newStr)) {
                deltaMap.add(newStr, c);
            }
        }
    }

    /**
     * Return the Deltas of this Chord
     *
     * @return an array of Deltas
     */
    private Delta[] getDeltas() {
        Delta[] deltas = new Delta[intervals.length];
        Interval[] reduced = reduce(intervals);
        for (int i = 0; i < reduced.length; i++) {
            deltas[i] = new Delta(reduced[i].semitones());
        }
        return deltas;
    }

    /**
     * Reduce the given array of Intervals. Converts each compound Interval to
     * the equivalent simple Interval (i.e., one that is no larger than one
     * octave)
     *
     * @param intervals array of Intervals
     * @return array of simple Intervals
     */
    public static Interval[] reduce(Interval[] intervals) {
        HashSet<Interval> newInt = new HashSet<>();
        for (Interval n : intervals) {
            Interval diff = n.minus(Interval.P8);
            while (diff != null) {
                n = diff;
                diff = n.minus(Interval.P8);
            }
            newInt.add(n);
        }
        Interval[] newArr = newInt.toArray(new Interval[0]);
        Arrays.sort(newArr);
        return newArr;
    }

    /**
     * Reduce the given array of Deltas. Converts each Delta that is larger than
     * an octave to the corresponding one that is less than an octave.
     *
     * @param deltas array of Deltas
     * @return array of small Deltas
     */
    private static Delta[] reduce(Delta[] deltas) {
        HashSet<Delta> newDeltas = new HashSet<>();
        for (Delta d : deltas) {
            while (d.semitones() >= 12) {
                d = new Delta(d.semitones() - 12);
            }
            if (d.semitones() > 0) {
                newDeltas.add(d);
            }
        }
        Delta[] newArr = newDeltas.toArray(new Delta[0]);
        Arrays.sort(newArr);
        return newArr;
    }

    /**
     * Convert an array of Intervals to a String for the intervalMap
     *
     * @param intervalArr array of Intervals
     * @return a String containing the IDs of the given Intervals
     */
    public static String toString(Interval[] intervalArr) {
        StringBuilder sb = new StringBuilder();
        Interval[] intervals = intervalArr;
        Arrays.sort(intervals);
        iloop:
        for (int i = 0; i < intervals.length; i++) {
            Interval n = intervals[i];
            if (n.isEquivalentTo(Interval.P1)) {
                continue;
            }
            for (int j = 0; j < i; j++) {
                if (intervals[j].isEquivalentTo(n)) {
                    continue iloop;
                }
            }
            sb.append(intervals[i].id());
            sb.append(",");
        }
        if (sb.length() >= 1) {
            sb.setLength(sb.length() - 1);
        }
        if (sb.length() == 0) {
            if (intervals.length > 0) {
                sb.append(intervals[0].id());
            }
        }
        return sb.toString();
    }

    /**
     * Convert an array of Deltas to a String for the deltaMap
     *
     * @param deltas array of Deltas
     * @return a String containing the names of the Deltas
     */
    public static String toString(Delta[] deltas) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deltas.length; i++) {
            sb.append(deltas[i].toString());
            if (i < deltas.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * the main (private) Chord constructor
     *
     * @param suffix the Chord name
     * @param b true if this is really just an interval
     * @param v array of Intervals
     */
    protected Chord(String suffix, boolean b, Interval[] v) {
        this.suffix = suffix;
        this.intervals = v;
        this.isInterval = b;
        allChords.add(this);
        chordNames.add(suffix);
    }

    protected Chord(String message) {
        // a non-chord; used by class UnrecognizedChord
        this.suffix = null;
    }

    /**
     * Generate a number that reflects the "complexity" of this Chord (for
     * sorting)
     *
     * @return an int, 0 to (1000 * (inversion + 1))
     */
    private int computeComplexity() {
        Interval last = intervals[intervals.length - 1];
        if (isInterval) {
            int c = last.basicSize() * 1;
            c += last.getQuality().modLevel() * 100;
            return c;
        }
        int c = 0;
        c += last.basicSize() * 10;
        for (Interval i : intervals) {
            if (i.equals(Interval.A5)) {
                c += 2000; // augmented chords should be last resort
            }
        }
//		System.err.println("COMPLEXITY of " + this + " = " + c);
        return c;
    }

    /**
     * Increment the complexity
     *
     * @param c amount to add
     */
    protected final void addComplexity(int c) {
        this.complexity += c;
    }

    /**
     * Return an array of Notes given a root Note
     *
     * @param root a Note for the root of the Chord
     * @return array of Notes
     */
    public Note[] getNotes(Note root) {
        if (root == null) {
            return null;
        }
        Note[] notes = new Note[intervals.length + 1];
        notes[0] = root;
        for (int i = 1; i < notes.length; i++) {
            Note n = root.plus(intervals[i - 1]);
            notes[i] = n;
        }
        return notes;
    }

    /**
     * Return the String representation of this Chord
     */
    @Override
    public String toString() {
        return suffix;
    }

    /**
     * Return a String ("1st") given an int (1); only accurate for 1 - 20
     *
     * @param inversion which inversion?
     * @return ordinal string
     */
    public static String ordinal(int inversion) {
        switch (inversion) {
            case 1:
                return "1st";
            case 2:
                return "2nd";
            case 3:
                return "3rd";
        }
        return Integer.toString(inversion) + "th";
    }

    /**
     * Return the Intervals for this Chord
     *
     * @return array of Intervals
     */
    public Interval[] getIntervals() {
        return intervals;
    }

    /**
     * Get the actual Intervals for a given set of Pitches that could make up
     * this chord (including octave transpositions)
     *
     * @param root the root Note
     * @param pitches array of Pitches
     * @return array of Intervals
     */
    public Interval[] getIntervals(Note root, Pitch[] pitches) {
        Pitch rootPitch = null;
        for (Pitch p : pitches) {
            if (p.getNote().equals(root)) {
                rootPitch = p;
                break;
            }
        }
        Interval[] arr = new Interval[pitches.length];
        // compute the interval of each pitch from the root
        for (int i = 0; i < arr.length; i++) {
            Interval v = pitches[i].minus(rootPitch);
            arr[i] = v;
        }
        return arr;
    }

    /**
     * Compare to another Chord; returns difference of "complexity"
     *
     * @param arg0 what to compare this to
     */
    @Override
    public int compareTo(Object arg0) {
        if (arg0 instanceof Chord) {
            Chord other = (Chord) arg0;
            return this.complexity - other.complexity;
        }
        return 0;
    }

    /**
     * Get array of Pitches, given a bass Note and octave
     *
     * @param bass the first Note
     * @param octave the octave of the bass Pitch
     * @return array of Pitches
     */
    public Pitch[] getPitches(Note bass, int octave) {
        ArrayList<Pitch> arr = new ArrayList<>();
        Pitch b = new Pitch(bass, octave);
        arr.add(b);
        for (Interval n : intervals) {
            Pitch p = new Pitch(b, n);
            arr.add(p);
        }
        return arr.toArray(new Pitch[0]);
    }

    /**
     * Return the String representation of this chord, given a root Note
     *
     * @param root the Root note
     * @return the String representation
     */
    public String name(Note root) {
        if (root == null) {
            return "";
        }
        if (isInterval() || !root.isWhiteKey()) {
            return root.toString() + " " + toString();
        } else {
            return root.toString() + toString();
        }
    }

    /**
     * Return the RatioSet for this Chord
     *
     * @return the RatioSet
     */
    public RatioSet getRatios() {
        if (ratios == null) {
            return null;
        }
        return ratios[selectedTuning];
    }

    /**
     * Return the Interval between the two given Pitches, constrained to be an
     * Interval in this Chord (or an octave/enharmonic equivalent)
     *
     * @param root the root Pitch
     * @param p another Pitch in the Chord
     * @return the Interval between them, or one that is octave- and/or
     * enharmonic-equivalent
     */
    public Interval getIntervalForPitch(Pitch root, Pitch p) {
        int semis = p.getNote().minus(root.getNote()); // 0 thru 11
        if (semis == 0) {
            return Interval.P1;
        }

        for (Interval v : intervals) {
            if ((v.semitones() % 12) == semis) {
                return v;
            }
        }
        return Interval.P1; // can't happen?
    }

    /**
     * Get all known Chords
     *
     * @return the master list of Chords
     */
    public static ArrayList<Chord> getAll() {
        return allChords;
    }

    /**
     * Return true if this Chord contains the given Interval
     *
     * @param interval
     * @return true if this Chord contains the given Interval
     */
    public boolean contains(Interval interval) {
        for (Interval n : intervals) {
            if (n.equals(interval)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the Just Intonation ratio of the given pitch, relative to the root
     *
     * @param root the root Pitch
     * @param p the requested Pitch
     * @return the frequency ratio of p/root (as a double) in JI
     */
    public double getJIratio(Pitch root, Pitch p) {
        Interval n = p.minus(root);
        if (n.isEquivalentTo(Interval.P1)) {
            return 1.0;
        }
        for (Interval v : intervals) {
            if (n.isEquivalentTo(v)) {
                return v.getJIratio();
            }
        }
        return 0.0;
    }

    /**
     * Return the Equal Temperament ratio of the given pitch, relative to the
     * root
     *
     * @param root the root Pitch
     * @param p the requested Pitch
     * @return the frequency ratio of p/root (as a double) in ET
     */
    public double getETratio(Pitch root, Pitch p) {
        Interval n = p.minus(root);
        if (n.isEquivalentTo(Interval.P1)) {
            return Math.pow(2.0, n.spannedOctaves());
        }
        for (Interval v : intervals) {
            if (n.isEquivalentTo(v)) {
                int oct = n.spannedOctaves() - v.spannedOctaves();
                return v.getETratio() * Math.pow(2, oct);
            }
        }
        return 0.0;
    }

    /**
     * Compute a full ratio set, corresponding to the given pitches
     *
     * @param ratioSet
     *
     * @param pitches array of Pitch
     * @return RatioSet (same length as argument)
     */
    public RatioSet getRatios(RatioSet ratioSet, Pitch[] pitches) {
        if (pitches.length == 0) {
            return new RatioSet();
        }
        int[] values = new int[pitches.length];
        if (ratioSet == null) {
            values[0] = ratios[selectedTuning].value(0);
        } else {
            values[0] = ratioSet.value(0);
        }
        int i = -1;
        for (Pitch p : pitches) {
            if (++i == 0) {
                continue;
            }
            Interval n = p.minus(pitches[0]);
            if (n.differsByOctaveWith(Interval.P1)) {
                int[] arr = n.getJIfraction();
                values[i] = values[0] * arr[0] / arr[1];
                continue;
            }
            for (int j = 0; j < intervals.length; j++) {
                Interval v = intervals[j];
                if (v.simplify().isEnharmonicWith(n.simplify())) {
                    int oct = n.spannedOctaves() - v.spannedOctaves();
                    if (oct < 0) {
                        // to keep integer ratios, multiply previous nums by appropriate
                        // power of 2
                        int factor = (int) Math.pow(2, -oct);
                        for (int k = 0; k < i; k++) {
                            values[k] *= factor;
                        }
                        values[i] = ratios[selectedTuning].value(j + 1);
                        break;
                    }
                    int num = ratios[selectedTuning].value(j + 1);
                    int den = ratios[selectedTuning].value(0);
                    ArrayList<Integer> dFact = Util.factor(den);
                    int base = values[0];
                    for (Integer f : dFact.toArray(new Integer[0])) {
                        if (base % f == 0) {
                            dFact.remove(f);
                            base /= f;
                        }
                    }
                    for (Integer f : dFact) {
                        for (int k = 0; k < i; k++) {
                            values[k] *= f;
                        }
                    }
                    values[i] = (int) (values[0] * num / den * Math.pow(2, oct));
                    break;
                }
            }
        }

        return new RatioSet(values);
    }

    public int getTotalTunings() {
        if (ratios == null) {
            return 0;
        }
        if (ratios[0].size() == 0) {
            return 0;
        }
        return ratios.length;
    }

    public void selectTuning(int n) {
        if (n >= 0 && n < ratios.length) {
            selectedTuning = n;
        }
    }

    public int getSelectedTuning() {
        if (ratios == null) {
            return -1;
        } else if (ratios[0].size() == 0) {
            return -1;
        }
        return selectedTuning;
    }

    public Pitch getRootPitch(Note rootNote, Pitch[] pitches) {
        for (Pitch p : pitches) {
            if (p.getNote().equals(rootNote)) {
                return p;
            }
        }
        return null;
    }

    public Chord unrooted() {
        if (suffix.length() == 0 || !suffix.contains("9")) {
            return this;
        }
        RootlessChord unr = new RootlessChord(this);
        return unr;
    }
}
