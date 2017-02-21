package net.weath.chords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import net.weath.musicutil.ChordSeq;
import net.weath.musicutil.Item;
import net.weath.musicutil.Note;
import net.weath.musicutil.Pitch;
import net.weath.musicxml.DivData;
import net.weath.musicxml.MeasureData;
import net.weath.musicxml.MeasureMap;
import net.weath.musicxml.NoteData;
import net.weath.musicxml.Score;
import net.weath.musicxml.VoicePart;

public class MusicXmlReader {

    /**
     * map of mute status for each voice part
     */
    private final TreeMap<VoicePart, Boolean> muteMap;

    /**
     * the sequence of chords from the current XML file
     */
    private ChordSeq chordSeq = null;

    /**
     * map a tick to a measure
     */
    private MeasureMap measureMap;

    private boolean swing8;

    private boolean swingdot;

    private final DivData divData = new DivData();

    private String fileName = null;

    public MusicXmlReader(String fileName, TreeMap<VoicePart, Boolean> muteMap,
            boolean swing8, boolean swingdot) {
        this.fileName = fileName;
        this.muteMap = muteMap;
        this.swing8 = swing8;
        this.swingdot = swingdot;
    }

    /**
     * Extract a list of ticks at which the chord changes
     *
     * @param m a MeasureData
     * @return array of int
     */
    private int[] getTimes(MeasureData m) {
        HashSet<Integer> set = new HashSet<>();
        for (NoteData nd : m.getNotes()) {
//	    int div = divData.divMap.get(nd.getPart());
            int startTime = getStartTime(m, nd);
            set.add(startTime);
        }
        int i = 0;
        int[] arr = new int[set.size()];
        for (Integer tick : set) {
            arr[i++] = tick;
        }
        Arrays.sort(arr);

        return arr;
    }

    private int getStartTime(MeasureData m, NoteData nd) {
        int s = nd.getStartTime(1, 1);
        int beat = m.getJiffiesPerBeat();
        int t = s % beat;
        s -= t;
        if (swing8) {
            if ((double) t / (double) beat == 0.5) {
                t = 2 * beat / 3;
            }
        }
        if (swingdot) {
            if ((double) t / (double) beat == 0.75) {
                t = 2 * beat / 3;
            }
        }
        return s + t;
    }

    private int getLength(MeasureData m, NoteData nd) {
        int s = getStartTime(m, nd);
        int beat = m.getJiffiesPerBeat();
        int len = nd.getLength(1, 1);
        if (swing8) {
            if (len == beat / 2 && s % beat == 0) {
                len = 2 * beat / 3;
            } else if (len == beat / 2 && s % beat == 2 * beat / 3) {
                len = beat / 3;
            }
        }
        if (swingdot) {
            if (len == 3 * beat / 4 && s % beat == 0) {
                len = 2 * beat / 3;
            } else if (len == beat / 4 && s % beat == beat / 4) {
                len = beat / 3;
            }
        }
        return len;
    }

    /**
     * Extract an Item from the measure at the given tick
     *
     * @param m a MeasureData
     * @param tick
     * @return the Item for that tick
     */
    private Item extractItem(MeasureData m, int tick) {
        Item item = new Item(m.getStart() + tick);
        HashMap<VoicePart, Pitch> map = new HashMap<>();
        for (NoteData nd : m.getNotes()) {
//          int div = divData.divMap.get(nd.getPart());
            int start = getStartTime(m, nd); // in jiffies, measure-relative
            int dur = getLength(m, nd); // in jiffies
            if (start > tick) {
                continue; // too late a start
            }
            if (start + dur <= tick) {
                continue; // too early a finish
            }	// if we get here, the note is sounding at time "tick"
            VoicePart vp = new VoicePart(nd);
            if (!muteMap.containsKey(vp)) {
                muteMap.put(vp, false);
            }
            Pitch p = getPitch(nd);
            if (p == null) {
                continue; // a rest
            }
            Pitch existing = map.get(vp);
            if (p.equals(existing)) {
                continue; // dup
            }
            if (existing != null) {
                // already have a note for the current voice part, create a new voice part
                int part = vp.part;
                int voice = vp.voice + 1;
                vp = new VoicePart(part, voice);
                while (map.get(vp) != null) {
                    vp.voice++;
                }
            }
            p.setVoicePart(vp);
            map.put(vp, p);
            item.add(p);
            if (nd.isFermata()) {
                item.setFermata(true);
            }
            System.err.println(vp + ": " + nd + " --> " + p);
        }
        return item;
    }

    private Pitch getPitch(NoteData nd) {
        if (nd.getDegree() == null) {
            return null;
        }
        Note n = Note.lookup(nd.getDegree().name());
        int alter = nd.getAlter();
        while (alter < 0) {
            n = n.flatten();
            alter++;
        }
        while (alter > 0) {
            n = n.sharpen();
            alter--;
        }
        Pitch p = new Pitch(n, nd.getOctave());
        if (nd.getMidiNoteNumber() != p.getNumber()
                || !nd.getDegree().toString().equals(p.getNote().baseName())) {
            throw new RuntimeException("Oops.... Mistranslation!");
        }
        return p;
    }

    public ChordSeq parseMusicXml() {
        muteMap.clear();
        chordSeq = new ChordSeq();
        Score score = new Score(divData, fileName);
        ArrayList<MeasureData> measures = score.getMeasureData();
        measureMap = new MeasureMap(measures);
        return createSequence(measures);
    }

    public ChordSeq createSequence(ArrayList<MeasureData> measures) {
        for (MeasureData m : measures) {
            System.err.println("===== " + m + " =====");
            int[] times = getTimes(m);
            for (int tick : times) {
                Item item = extractItem(m, tick);
                System.err.println(item);
                chordSeq.insert(item);
            }
        }
        return chordSeq;
    }

    public MeasureMap getMeasureMap() {
        return measureMap;
    }

    public boolean isSwing8() {
        return swing8;
    }

    public void setSwing8(boolean swing8) {
        this.swing8 = swing8;
    }

    public boolean isSwingdot() {
        return swingdot;
    }

    public void setSwingdot(boolean swingdot) {
        this.swingdot = swingdot;
    }
}
