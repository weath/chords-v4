package net.weath.chords;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JTextField;

import net.weath.musicutil.Note;
import net.weath.musicutil.Pitch;

public class NotesText extends JTextField implements IChordController,
        IChordViewer {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private final ErrMsg errMsg;

    private final ChordModel model;

    public NotesText(int nColumns, final ErrMsg errMsg, ChordModel model) {
        super(nColumns);
        this.errMsg = errMsg;
        this.model = model;
        model.addListener(this);
        addActionListener((ActionEvent e) -> {
            // System.err.println("notes: " + notes.getText());
            errMsg.setErrMsg(null);
            try {
                parseNotes();
            } catch (Exception t) {
                errMsg.setErrMsg(t.getMessage());
            }
        });
    }

    private void parseNotes() throws Exception {
        boolean explicitOctave = false;
        boolean hasUserBends = false;
        int underscores = 0;
        String s = getText();
        StringTokenizer st = new StringTokenizer(s, " ");
        ArrayList<Pitch> pArr = new ArrayList<>();
        Pitch[] pitches = new Pitch[0];
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
                    hasUserBends = true;
                    String[] bStr = str.split("[+-]");
                    cents = Integer.parseInt(bStr[1]);
                    if (str.indexOf('-') > 0) {
                        cents = -cents;
                    }
                    str = bStr[0];
                } else if (str.matches(".+[<>][0-9]+.*")) {
                    hasUserBends = true;
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
                if (!explicitOctave && pArr.size() > 0) {
                    Pitch prev = pArr.get(pArr.size() - 1);
                    while (p.getNumber() <= prev.getNumber()) {
                        p.plusOctave();
                    }
                    if (underscores > 0 && prev.distTo(p) < 8) {
                        p.setOctave(p.getOctave() + underscores);
                    }
                }
                pArr.add(p);
                underscores = 0;
                octave = p.getOctave();
                explicitOctave = false;
                pitches = pArr.toArray(new Pitch[0]);
                if (pitches.length > 15) {
                    throw new IllegalArgumentException("Too many notes: "
                            + pitches.length);
                }
            }
            model.setHasUserBends(hasUserBends);
            model.setPitches(getOrigin(), pitches);
        } catch (IllegalArgumentException t) {
            throw new Exception(t.getMessage());
        }
    }

    @Override
    public void chordChanged(ChordChangeEvent event) {
        Origin origin = event.getOrigin();
        if (origin != null && origin.equals(getOrigin())) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Pitch p : event.getPitches()) {
            if (!p.isReal())
                continue;
            sb.append(p.getNote().toString());
            sb.append(String.format("%d", p.getOctave()));
            if (event.hasUserBends()) {
                double cents = p.getCents();
                long icents = Math.round(cents);
                if (icents != 0)
                    sb.append(String.format("%+d", icents));
            }   
            sb.append(" ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        setText(sb.toString());
    }

    @Override
    public Origin getOrigin() {
        return Origin.Notes;
    }

}
