package net.weath.musicxml;

import java.util.ArrayList;

public class MeasureMap {

    /**
     * list of all Measures, ordered by measure number
     */
    private MeasureData[] measures;

    public MeasureMap(ArrayList<MeasureData> data) {
        measures = data.toArray(new MeasureData[0]);
    }

    public MeasureData getMeasure(int m) {
        for (MeasureData md : measures) {
            if (md.getNumber() == m) {
                return md;
            }
        }
        return null; // error message?
    }

    public MeasureData getMeasureAt(int tick) {
        MeasureData prev = measures[0];
        for (MeasureData md : measures) {
            if (md.getStart() > tick) {
                return prev;
            }
            prev = md;
        }
        return prev;
    }

}
