package net.weath.chords;

import net.weath.musicutil.Note;
import net.weath.musicxml.VoicePart;

public class TuningSpec {

    private int startTick;
    private VoicePart voicePart; // may be null
    private Note note; // may be null
    private double cents;

    public TuningSpec() {
    }

    public int getStartTick() {
        return startTick;
    }

    public void setStartTick(int startTick) {
        this.startTick = startTick;
    }

    public VoicePart getVoicePart() {
        return voicePart;
    }

    public void setVoicePart(VoicePart voicePart) {
        this.voicePart = voicePart;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public double getCents() {
        return cents;
    }

    public void setCents(double cents) {
        this.cents = cents;
    }

    public int getPitchBend() {
        if (cents == 0.0) {
            return 0;
        }
        // 4096 PBU == 1 semitone == 100 cents
        int bend = (int) (cents * 40.96);
        return bend;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TuningSpec(");
        sb.append(Integer.toString(startTick));
        sb.append("): ");
        if (voicePart != null) {
            sb.append(voicePart.toString());
        } else if (note != null) {
            sb.append(note.toString());
        } else {
            sb.append(Double.toString(cents));
        }
        return sb.toString();
    }
}
