package net.weath.musicxml;

public class MBT {

    private int ticks;
    private int measure;
    private int beat;
    private int tick;

    public MBT(int ticks, MeasureMap map) {
        this.ticks = ticks;
        MeasureData m = map.getMeasureAt(ticks);
        measure = m.getNumber();
        int jiffiesPerBeat = m.getJiffiesPerBeat();
        beat = (ticks - m.getStart()) / jiffiesPerBeat + 1;
        tick = ticks - (m.getStart() + (beat - 1) * jiffiesPerBeat);
    }

    public MBT(int measure, int beat, int tick, MeasureMap map) {
        this.measure = measure;
        this.beat = beat;
        this.tick = tick;
        MeasureData m = map.getMeasure(measure);
        ticks = m.getStart() + (beat - 1) * m.getJiffiesPerBeat() + tick;
    }

    public int getTicks() {
        return ticks;
    }

    public int getMeasure() {
        return measure;
    }

    public int getBeat() {
        return beat;
    }

    public int getTick() {
        return tick;
    }

    @Override
    public String toString() {
        return String.format("%d:%d:%d", measure, beat, tick);
    }
}
