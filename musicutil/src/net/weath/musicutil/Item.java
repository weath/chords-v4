package net.weath.musicutil;

import java.util.ArrayList;

/**
 * An array of Pitches (a chord), at a particular MIDI tick, with a duration
 *
 * @author weath
 *
 */
public class Item {

    /**
     * the MIDI tick for the start of this chord
     */
    private final int tick;

    /**
     * the duration, in ticks
     */
    private int duration;

    /**
     * the array of Pitches
     */
    private final ArrayList<Pitch> pitches = new ArrayList<>();

    private boolean fermata;

    public boolean isFermata() {
        return fermata;
    }

    public void setFermata(boolean fermata) {
        this.fermata = fermata;
    }

    /**
     * Create a new Item
     *
     * @param tick the MIDI tick for the start of the chord
     * @param duration the duration, in ticks
     * @param pitches one or more Pitches
     */
    public Item(int tick, int duration, Pitch... pitches) {
        this.tick = tick;
        this.duration = duration;
        // remove null items
        for (Pitch p : pitches) {
            if (p != null) {
                add(p);
            }
        }
    }

    public Item(int tick) {
        this.tick = tick;
        this.duration = 1;
        this.pitches.clear();
    }

    public void add(Pitch p) {
        if (p == null) {
            throw new IllegalArgumentException("Pitch cannot be null");
        }
        int n = p.getNumber();
        int i = 0;
        while (i < pitches.size() && n > pitches.get(i).getNumber()) {
            i++;
        }
        pitches.add(i, p);
    }

    /**
     * @return the MIDI tick for the start of this chord
     */
    public int getTick() {
        return tick;
    }

    /**
     * @return the array of Pitches
     */
    public Pitch[] getPitches() {
        return pitches.toArray(new Pitch[0]);
    }

    /**
     * @param i the index
     * @return the Pitch at the given index
     */
    public Pitch getPitch(int i) {
        return pitches.get(i);
    }

    /**
     * @return the length of the Pitch array
     */
    public int length() {
        return pitches.size();
    }

    /**
     * @return the duration (in ticks)
     */
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        assert duration > 0;
        this.duration = duration;
    }

    /**
     * @return the String representation of this Item (for debugging)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d: ", tick));
        sb.append("[");
        for (Pitch p : pitches) {
            sb.append(p.getNote().toString());
            sb.append(p.getOctave());
            sb.append(",");
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }
}
