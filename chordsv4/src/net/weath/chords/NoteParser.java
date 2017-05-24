package net.weath.chords;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.weath.musicutil.Note;
import net.weath.musicutil.Pitch;

public class NoteParser {

    private Exception exception = null;

    public Pitch[] parse(String input) {
        exception = null;
        boolean explicitOctave = false;
//	boolean haveUserBends = false;
        int underscores = 0;
        StringTokenizer st = new StringTokenizer(input, " ");
        ArrayList<Pitch> pitches = new ArrayList<>();
        int octave = 4;
        try {
            while (st.hasMoreTokens()) {
                String str = st.nextToken();
                if (str.equals("_")) {
                    ++underscores;
                    continue;
                }
                if (str.matches("[0-9]+")) {
                    octave = Integer.parseInt(str);
                    explicitOctave = true;
                    continue;
                }
                if (str.matches("[a-gA-G](#|x|b|bb)?[0-9]+.*")) {
                    explicitOctave = true;
                    int n = 0;
                    for (; n < str.length(); n++) {
                        if ("0123456789".indexOf(str.charAt(n)) >= 0) {
                            break;
                        }
                    }
                    int m = n + 1;
                    if (m < str.length()
                            && "0123456789".indexOf(str.charAt(m)) >= 0) {
                        m++;
                    }
                    String oString;
                    if (m < str.length()) {
                        oString = str.substring(n, m);
                    } else {
                        oString = str.substring(n);
                    }
                    str = str.substring(0, n) + str.substring(m);
                    octave = Integer.parseInt(oString);
                    // fall through
                }
                Note n;
                int cents = 0;
                int bend = 0;
                if (str.matches(".+[+-][0-9]+.*")) {
//					haveUserBends = true;
                    String[] bStr = str.split("[+-]");
                    cents = Integer.parseInt(bStr[1]);
                    if (str.indexOf('-') > 0) {
                        cents = -cents;
                    }
                    str = bStr[0];
                } else if (str.matches(".+[<>][0-9]+.*")) {
//					haveUserBends = true;
                    String[] bStr = str.split("[<>]");
                    bend = Integer.parseInt(bStr[1]);
                    if (str.indexOf('<') > 0) {
                        bend = -bend;
                    }
                    str = bStr[0];
                }
                n = Note.lookup(str);
                Pitch p = new Pitch(n, octave);
                if (cents != 0) {
                    p.setCents(cents);
                }
                if (bend != 0) {
                    p.setBend(bend);
                }
                if (!explicitOctave && pitches.size() > 0) {
                    Pitch prev = pitches.get(pitches.size() - 1);
                    while (p.getNumber() <= prev.getNumber()) {
                        p.plusOctave();
                    }
                    if (underscores > 0 && prev.distTo(p) < 8) {
                        p.setOctave(p.getOctave() + underscores);
                    }
                }
                pitches.add(p);
                underscores = 0;
                octave = p.getOctave();
                explicitOctave = false;
            }
        } catch (NumberFormatException e) {
            pitches.clear();
            exception = e;
        }
        return pitches.toArray(new Pitch[0]);
    }

    public Exception getException() {
        return exception;
    }
}
