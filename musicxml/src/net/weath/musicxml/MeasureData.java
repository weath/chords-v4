package net.weath.musicxml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.weath.musicutil.KeySignature;

public class MeasureData implements Comparable<MeasureData> {

    private static int lastFifths = 0;

    private int number; // 1-based
    private int fifths; // key, 0 = C, -1 = F, +1 = G, etc.
    private String mode;
    private Double tempo; // quarter-notes per minute
    private int start = -1; // in jiffies
    private int length = -1; // in jiffies
    private int beatType = -1; // as notated
    private int beats = -1; // as notated, may be shorter (pick-up/final)
    private double actualBeats = -1.0; // computed
    private RepeatType repeatType = RepeatType.None;
    private EndingType endingType = EndingType.None;
    private List<Integer> endings = new ArrayList<>();
    private List<NoteData> notes;

    public MeasureData() {
        fifths = lastFifths;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Integer getFifths() {
        return fifths;
    }

    public void setFifths(int fifths) {
        this.fifths = fifths;
        lastFifths = fifths;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Double getTempo() {
        return tempo;
    }

    public void setTempo(double tempo) {
        this.tempo = tempo;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        assert this.start == -1 || start == this.start;
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        assert this.length == -1 || length == this.length;
        this.length = length;
    }

    public boolean contains(NoteData data) {
        return notes.contains(data);
    }

    public NoteData[] getNotes() {
        if (notes == null) {
            return new NoteData[0];
        }
        NoteData[] arr = notes.toArray(new NoteData[0]);
        Arrays.sort(arr);
        return arr;
    }

    public void addNote(NoteData data) {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        notes.add(data);
    }

    public boolean deleteNote(NoteData data) {
        if (notes.contains(data)) {
            notes.remove(data);
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(MeasureData other) {
        return this.number - other.number;
    }

    @Override
    public String toString() {
        String repeatstring = "";
        if (endings.size() > 0) {
            repeatstring += ", ending=<";
            for (Integer i : endings) {
                repeatstring += i.toString();
                repeatstring += ",";
            }
            repeatstring = repeatstring.substring(0, repeatstring.length() - 1);
            repeatstring += ">";
        }
        if (endingType != EndingType.None) {
            repeatstring += ", type=" + endingType;
        }
        if (repeatType != RepeatType.None) {
            repeatstring += ", repeat=" + repeatType;
        }
        return String.format("Measure{n=%d,start=%d,len=%d, beats=%d, actual=%3.1f, beatType=%d%s}",
                number, start, length, beats, actualBeats, beatType, repeatstring);
    }

    public KeySignature getKey() {
        return new KeySignature(this.start, this.fifths, (mode != null && mode != "major"));
    }

    public int getBeatType() {
        return beatType;
    }

    public void setBeatType(int beatType) {
        assert beatType > 0;
        this.beatType = beatType;
    }

    public int getBeats() {
        return beats;
    }

    public void setBeats(int beats) {
        assert beats > 0;
        this.beats = beats;
    }

    public double getActualBeats() {
        return actualBeats;
    }

    public void setActualBeats(double actualBeats) {
        this.actualBeats = actualBeats;
    }

    public int getJiffiesPerBeat() {
        assert actualBeats > 0;
        assert length > 0;
        return (int) (((double) length) / actualBeats);
    }

    public EndingType getEndingType() {
        return endingType;
    }

    public void setEndingType(EndingType endingType) {
        if (this.endingType == EndingType.Start && endingType == EndingType.Stop) {
            endingType = EndingType.StartStop;
        } else if (this.endingType == EndingType.Stop && endingType == EndingType.Start) {
            endingType = EndingType.StartStop;
        }
        this.endingType = endingType;
    }

    public boolean hasEnding(int n) {
        return endings.contains(n);
    }

    public void setEnding(int ending) {
        if (!hasEnding(ending)) {
            endings.add(ending);
        }
    }

    public RepeatType getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(RepeatType repeatType) {
        if (this.repeatType != RepeatType.None && this.repeatType != repeatType) {
            System.err.println("Cannot handle single measure repeats!");
            this.repeatType = RepeatType.None;
            return;
        }
        this.repeatType = repeatType;
    }

    /**
     * Return the tick, given a beat and a tick of that beat
     *
     * @param b the beat (1-based)
     * @param t the tick of that beat (0-based)
     * @return the absolute tick
     */
    public int getTick(int b, int t) {
        return getStart() + (b - 1) * getJiffiesPerBeat() + t;
    }
}
