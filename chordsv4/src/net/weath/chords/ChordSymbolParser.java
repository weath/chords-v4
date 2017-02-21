package net.weath.chords;

import static net.weath.musicutil.Interval.A11;
import static net.weath.musicutil.Interval.A5;
import static net.weath.musicutil.Interval.A9;
import static net.weath.musicutil.Interval.M10;
import static net.weath.musicutil.Interval.M13;
import static net.weath.musicutil.Interval.M14;
import static net.weath.musicutil.Interval.M2;
import static net.weath.musicutil.Interval.M3;
import static net.weath.musicutil.Interval.M6;
import static net.weath.musicutil.Interval.M7;
import static net.weath.musicutil.Interval.M9;
import static net.weath.musicutil.Interval.P11;
import static net.weath.musicutil.Interval.P12;
import static net.weath.musicutil.Interval.P4;
import static net.weath.musicutil.Interval.P5;
import static net.weath.musicutil.Interval.P8;
import static net.weath.musicutil.Interval.d5;
import static net.weath.musicutil.Interval.d7;
import static net.weath.musicutil.Interval.m10;
import static net.weath.musicutil.Interval.m13;
import static net.weath.musicutil.Interval.m3;
import static net.weath.musicutil.Interval.m7;
import static net.weath.musicutil.Interval.m9;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import net.weath.chords.parser.Symbol;
import net.weath.chords.parser.SymbolConstants;
import net.weath.chords.parser.Token;
import net.weath.musicutil.Chord;
import net.weath.musicutil.Interval;
import net.weath.musicutil.Note;

public class ChordSymbolParser {

    private Note root;
    private int inversion;
    private Chord chord;
    private Note bass;

    @SuppressWarnings("unused") // accessed statically
    private static final Symbol symParser = new Symbol(System.in); // chord symbol parser

    public boolean parse(String text) throws Exception {
        boolean unrooted = false;
        Reader rdr = new StringReader(text);
        Symbol.ReInit(rdr);
        int res = Symbol.one_line();
        if (res != 0) {
            return false;
        }
        Token[] tok = Symbol.tok.toArray(new Token[0]);
        Symbol.tok.clear();
        if (Symbol.root[0] == null && tok.length == 0) {
            throw new IllegalArgumentException("Please enter a chord symbol");
        }
        inversion = 0;
        String r = "C";
        if (Symbol.root[0] != null) {
            r = Symbol.root[0].image;
        }
        if (Symbol.root[1] != null) {
            r = r + Symbol.root[1].image;
        }
        root = Note.lookup(r);
        if (root == null) {
            throw new IllegalArgumentException("Unrecognized root note name: "
                    + r);
        }
        String type = "";
        if (Symbol.type != null) {
            type = Symbol.type.image; // basic type
        }
        Chord c = Chord.lookup(type);
        if (c == null) {
            throw new IllegalArgumentException("Unrecognized chord: "
                    + text);
        }
        HashSet<Interval> intervals = new HashSet<>();
        intervals.addAll(Arrays.asList(c.getIntervals()));
        for (int i = 0; i < tok.length; i++) {
            // process modifiers
            if (tok[i] == null) {
                continue;
            }
            int k = tok[i].kind;
            switch (k) {
                case SymbolConstants.AUGMENTED:
                case SymbolConstants.DIMINISHED:
                case SymbolConstants.MINOR:
                case SymbolConstants.MAJOR:
                case SymbolConstants.PERFECT:
                    String s = tok[i].image + " " + tok[i + 1].image;
                    c = Chord.lookup(s);
                    if (c == null) {
                        throw new IllegalArgumentException(
                                "Unrecognized interval: " + s);
                    }
                    chord = c;
                    return true;
                case SymbolConstants.OCTAVE:
                case SymbolConstants.UNISON:
                case SymbolConstants.DBLOCTAVE:
                    s = "perfect " + tok[i].image;
                    c = Chord.lookup(s);
                    if (c == null) {
                        throw new IllegalArgumentException(
                                "Unrecognized interval: " + s);
                    }
                    chord = c;
                    return true;
            }
            // not an interval -- parse a chord name
            String m = tok[i].image;
            switch (m) {
                case "noroot":
                case "no root":
                    if (unrooted) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    unrooted = true;
                    break;
                case "no3":
                case "no 3": {
                    boolean ok = intervals.remove(M3);
                    if (!ok) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    break;
                }
                case "b5":
                case "-5":
                    intervals.remove(P5);
                    if (intervals.contains(d5)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(d5);
                    break;
                case "#5":
                case "+5":
                case "+":
                    intervals.remove(P5);
                    if (intervals.contains(A5)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(A5);
                    break;
                case "no5":
                case "no 5": {
                    boolean ok = intervals.remove(P5);
                    if (!ok || intervals.contains(A5)
                            || intervals.contains(d5)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    break;
                }
                case "6":
                    if (intervals.contains(M6)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M6);
                    break;
                case "7":
                    if (type.equals("dim")) {
                        if (intervals.contains(d7)) {
                            throw new IllegalArgumentException("Unexpected: " + m);
                        }
                        intervals.add(d7);
                    } else {
                        if (intervals.contains(m7)) {
                            throw new IllegalArgumentException("Unexpected: " + m);
                        }
                        intervals.add(m7);
                    }
                    break;
                case "M7":
                case "maj7":
                case "-maj7":
                    if (type.equals("") && m.equals("-maj7")) { // should be minor triad plus maj7
                        intervals.remove(M3);
                        intervals.add(m3);
                    }
                    if (intervals.contains(M7)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M7);
                    break;
                case "9":
                    if (intervals.contains(M9)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M9);
                    if (!intervals.contains(M6)) {
                        intervals.add(m7);
                    }
                    break;
                case "M9":
                case "maj9":
                case "-maj9":
                    if (intervals.contains(M9)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M9);
                    intervals.add(M7);
                    break;
                case "b9":
                case "-9":
                    if (intervals.contains(m9)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(m9);
                    intervals.remove(M9);
                    break;
                case "#9":
                case "+9":
                    if (intervals.contains(A9)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(A9);
                    intervals.remove(M9);
                    break;
                case "b10":
                case "-10":
                    if (intervals.contains(m10)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(m10);
                    intervals.remove(M10);
                    break;
                case "11":
                    if (intervals.contains(P11)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(P11);
                    intervals.add(M9);
                    intervals.add(m7);
                    break;
                case "M11":
                case "maj11":
                case "-maj11":
                    if (intervals.contains(P11)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(P11);
                    intervals.add(M9);
                    intervals.add(M7);
                    break;
                case "#11":
                case "+11":
                    if (intervals.contains(P11)) {
                        intervals.remove(P11);
                    }
                    intervals.add(A11);
                    if (!intervals.contains(m9) && !intervals.contains(m10)) {
                        intervals.add(M9);
                    }
                    if (!intervals.contains(M7)) {
                        intervals.add(m7);
                    }
                    break;
                case "no11":
                case "no 11":
                    // if (!intervals.contains(P11) &&
                    // !intervals.contains(A11))
                    // throw new IllegalArgumentException("Unexpected: " + m);
                    intervals.remove(P11);
                    intervals.remove(A11);
                    break;
                case "13":
                    if (intervals.contains(M13)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M13);
                    if (!intervals.contains(M3)) {
                        intervals.add(P11);
                    }
                    intervals.add(M9);
                    intervals.add(m7);
                    break;
                case "maj13":
                case "M13":
                    if (intervals.contains(M13)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M13);
                    intervals.add(A11);
                    intervals.add(M9);
                    intervals.add(M7);
                    break;
                case "b13":
                    if (intervals.contains(m13)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(m13);
                    if (!intervals.contains(M3)) {
                        intervals.add(P11);
                    }
                    if (!intervals.contains(m9) && !intervals.contains(A9) && !intervals.contains(m10)) {
                        intervals.add(M9);
                    }
                    if (!intervals.contains(M7)) {
                        intervals.add(m7);
                    }
                    break;
                case "sus":
                case "sus4":
                    if (intervals.contains(P4)
                            || (!intervals.contains(M3) && !intervals.contains(m3))) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.remove(M3);
                    intervals.remove(m3);
                    intervals.add(P4);
                    break;
                case "sus2":
                    if (intervals.contains(M2)
                            || !intervals.contains(M3)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.remove(M3);
                    intervals.add(M2);
                    break;
                case "add2":
                case "2":
                case "/2":
                    if (intervals.contains(M2)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M2);
                    break;
                case "add4":
                case "4":
                case "/4":
                    if (intervals.contains(P4)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(P4);
                    break;
                case "add6":
                case "/6":
                    if (intervals.contains(M6)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M6);
                    break;
                case "add9":
                case "/9":
                    if (intervals.contains(M9)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M9);
                    break;
                case "over":
                    Interval[] overtones = {M3, P5, m7, P8, M9, M10, A11, P12, M13, M14};
                    intervals.addAll(Arrays.asList(overtones));
                    break;
                case "/7":
                    if (intervals.contains(m7)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(m7);
                    break;
                case "/11":
                    if (intervals.contains(P11)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(P11);
                    break;
                case "/13":
                    if (intervals.contains(M13)) {
                        throw new IllegalArgumentException("Unexpected: " + m);
                    }
                    intervals.add(M13);
                    break;
                case "/":
                    // alternate bass note
                    if (++i == tok.length || tok[i] == null) {
                        throw new IllegalArgumentException(
                                "Missing bass note after /");
                    }
                    String s = tok[i++].image;
                    if (i < tok.length && tok[i] != null) {
                        String a = tok[i].image;
                        if (a.equals("b") || a.equals("bb") || a.equals("#")
                                || a.equals("x")) {
                            s = s + a;
                            i++;
                        }
                    }
                    bass = Note.lookup(s);
                    if (bass == null) {
                        throw new IllegalArgumentException(
                                "Unrecognized bass note: " + s);
                    }
                    break;
                default:
                    // assume it's an inversion indicator
                    if ("123456789".indexOf(m.charAt(0)) < 0) {
                        throw new IllegalArgumentException(
                                "Expecting a digit, saw: " + m);
                    }
                    switch (m) {
                        case "1st":
                            inversion = 1;
                            break;
                        case "2nd":
                            inversion = 2;
                            break;
                        case "3rd":
                            inversion = 3;
                            break;
                        default:
                            int n = m.indexOf("th");
                            if (n < 1) {
                                throw new IllegalArgumentException(
                                        "Unrecognized inversion: " + m);
                            }
                            inversion = Integer.parseInt(m.substring(0, n));
                            break;
                    }
                    break;
            }
        }
        if (intervals.isEmpty()) {
            throw new IllegalArgumentException("No intervals!");
        }
        Interval[] intervalArr = intervals.toArray(new Interval[0]);
        ArrayList<Chord> chords = Chord.lookup(intervalArr);
        if (!chords.isEmpty()) {
            chord = chords.get(0);
            if (unrooted) {
                if (chord.getName().contains("9")) {
                    chord = chord.unrooted();
                } else {
                    throw new IllegalArgumentException("Only 9th chords may be rootless");
                }
            }
        }
        return true;
    }

    public Note getRoot() {
        return root;
    }

    public int getInversion() {
        return inversion;
    }

    public Chord getChord() {
        return chord;
    }

    public Note getBass() {
        return bass;
    }

}
