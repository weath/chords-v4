package net.weath.musicxml;

public class NoteData implements Comparable<NoteData> {

    private Step degree; // null if rest
    private int alter;
    private int octave;
    private int part;
    private int voice;
    private int startTime; // in divisions, measure relative
    private int length; // in divisions
    private boolean fermata;

    public boolean isFermata() {
        return fermata;
    }

    public void setFermata(boolean fermata) {
        this.fermata = fermata;
    }

    public Step getDegree() {
        return degree;
    }

    public void setDegree(Step degree) {
        this.degree = degree;
    }

    public int getAlter() {
        return alter;
    }

    public void setAlter(int alter) {
        this.alter = alter;
    }

    public int getOctave() {
        return octave;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public int getVoice() {
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    public int getStartTime(int jiffy, int div) {
        int ticks = startTime * (jiffy / div);
        return ticks;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getLength(int jiffy, int div) {
        int ticks = length * (jiffy / div);
        return ticks;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getMidiNoteNumber() {
        if (degree == null) {
            return -1; // rest
        }
        int n = 0; // distance (in semitones) from A in the same octave
        switch (degree) {
            case A:
                n = 0;
                break;
            case B:
                n = 2;
                break;
            case C:
                n = -9;
                break;
            case D:
                n = -7;
                break;
            case E:
                n = -5;
                break;
            case F:
                n = -4;
                break;
            case G:
                n = -2;
                break;
        }
        // MIDI note number 21 is A in octave 0
        return 21 + (12 * octave) + n + alter;
    }

    @Override
    public int compareTo(NoteData other) {
        if (this.startTime != other.startTime) {
            return (int) (this.startTime - other.startTime);
        }
        int thisNum = this.getMidiNoteNumber();
        int otherNum = other.getMidiNoteNumber();
        if (thisNum != otherNum) {
            return thisNum - otherNum;
        }
        if (this.part != other.part) {
            return this.part - other.part;
        }
        if (this.voice != other.voice) {
            return this.voice - other.voice;
        }
        if (this.length != other.length) {
            return (int) (this.length - other.length);
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("Note{" + degree + ",%d,oct=%d,num=%d,part=%d,voice=%d,start=%d,len=%d}",
                alter, octave, getMidiNoteNumber(), part, voice, startTime, length);
    }
}
