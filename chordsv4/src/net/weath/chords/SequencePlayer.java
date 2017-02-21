package net.weath.chords;

import java.util.HashMap;

import javax.swing.JSpinner;
import javax.swing.JTextField;

import net.weath.musicutil.ChordSeq;
import net.weath.musicutil.Item;
import net.weath.musicutil.Pitch;
import net.weath.musicxml.EndingType;
import net.weath.musicxml.MeasureData;
import net.weath.musicxml.MeasureMap;
import net.weath.musicxml.RepeatType;

public class SequencePlayer implements IChordController {

    private ChordSeq seq = null;

    private int cursor = 0; // index of current chord in seq

    private final ChordModel model;

    private final ErrMsg errMsg;

    private JTextField tickField;

    private JSpinner tempoSpinner;

    private MeasureMap measureMap;

    private final HashMap<Integer, Integer> repeatMap = new HashMap<>();

    private Thread thread;

    private int tempo;

    public SequencePlayer(ChordModel model, ErrMsg errMsg) {
        this.model = model;
        this.errMsg = errMsg;
    }

    @Override
    public Origin getOrigin() {
        return Origin.ChordSeq;
    }

    public MeasureMap getMeasureMap() {
        return measureMap;
    }

    public void setSequence(ChordSeq seq) {
        this.seq = seq;
        cursor = 0;
        playOne();
    }

    public void back() {
        if (seq == null) {
            return;
        }
        cursor--;
        if (cursor < 0) {
            cursor = seq.length() - 1;
        }
        playOne();
    }

    public void forward() {
        if (seq == null) {
            return;
        }
        jump();
        playOne();
    }

    /**
     * Advance the cursor, and possibly jump to another tick (repeats,
     * first/second endings)
     *
     * @return true if we jumped, else false
     */
    private boolean jump() {
        MeasureData m = measureMap.getMeasureAt(getTick());
        cursor++;
        if (cursor >= seq.length()) {
            cursor = 0;
            repeatMap.clear();
            return false;
        }
        MeasureData m2 = measureMap.getMeasureAt(getTick());
        if (m.getRepeatType() == RepeatType.Backward) {
            if (m2.getNumber() != m.getNumber()) {
                // we're at the end of a measure marked as a repeat :||
                Integer r = repeatMap.get(m.getNumber());
                if (r == null) {
                    r = 1;
                }
                repeatMap.put(m.getNumber(), r + 1);
                // if next measure is not marked as an nth ending, repeat once
                if (m2.getEndingType() == EndingType.None && r > 1) {
                    return false; // no repeat
                }				// if next measure is an nth ending, don't repeat unless it
                // matches the next iteration
                if (m2.getEndingType() != EndingType.None && !m2.hasEnding(r + 1)) {
                    return false;
                }
                // scan backward to beginning of repeat ||:
                int num = m.getNumber();
                while (num > 1) {
                    num--;
                    m2 = measureMap.getMeasure(num);
                    if (m2.getRepeatType() == RepeatType.Forward) {
                        int tick = m2.getStart();
                        for (int n = 0; n < seq.length(); n++) {
                            Item item = seq.get(n);
                            int iTick = item.getTick();
                            if (iTick >= tick) {
                                cursor = n;
                                return true;
                            }
                        }
                    }
                }
            }
        } else if (m2.getNumber() != m.getNumber()
                && (m2.getEndingType() == EndingType.Start || m2.getEndingType() == EndingType.StartStop)) {
            // stepping into a first ending?
            int n = m2.getNumber();
            Integer t = repeatMap.get(n);
            if (t == null) { // first time, continue till repeat sign
                repeatMap.put(n, 2);
            } else { // we've been here before
                if (m2.hasEnding(t)) {
                    repeatMap.put(n, t + 1); // OK
                } else {
                    // skip it!
                    do {
                        n++;
                        m2 = measureMap.getMeasure(n);
                    } while (!m2.hasEnding(t));
                    int tick = m2.getStart();
                    for (n = 0; n < seq.length(); n++) {
                        Item item = seq.get(n);
                        int iTick = item.getTick();
                        if (iTick >= tick) {
                            cursor = n;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int getTick() {
        if (seq == null) {
            return 0;
        }
        return seq.get(cursor).getTick();
    }

    private void playOne() {
        Item item = seq.get(cursor);
        int tick = item.getTick();
        showTick(tick);
        Pitch[] pitches = item.getPitches();
        model.setPitches(getOrigin(), pitches);
    }

    private void showTick(int tick) {
        MeasureData md = measureMap.getMeasureAt(tick);
        int jiffies = md.getJiffiesPerBeat();
        int j = tick - md.getStart();
        int m = md.getNumber();
        int b = j / jiffies + 1;
        int t = j % jiffies;
        System.err.println("===== " + String.format("%d:%d:%d", m, b, t) + " ======");
        tickField.setText(String.format("%d:%d:%d", m, b, t));
    }

    public void setTick(String text) {
        int tick = parseTick(text);
        for (int n = 0; n < seq.length(); n++) {
            Item item = seq.get(n);
            int iTick = item.getTick();
            if (iTick >= tick) {
                cursor = n;
                repeatMap.clear();
                showTick(iTick);
                playOne();
                return;
            }
        }
        errMsg.setErrMsg("Could not locate chord at " + text);
    }

    public void setTickField(JTextField tickField) {
        this.tickField = tickField;
    }

    public void setTempoSpinner(JSpinner tempoSpinner) {
        this.tempoSpinner = tempoSpinner;
    }

    public void setMeasureMap(MeasureMap measureMap) {
        this.measureMap = measureMap;
    }

    /**
     * Parse a tick specification (meas:beat:tick) or (tick)
     *
     * @param str the input, meas:beat:tick or just an integer
     * @return
     */
    private int parseTick(String str) {
        int tick = 0;
        if (str.indexOf(':') > 0) {
            String[] arr = str.split(":");
            if (arr.length != 3) {
                throw new IllegalArgumentException(
                        "Tick must be <int> or <int>:<int>:<int>");
            }
            try {
                int m = Integer.parseInt(arr[0]);
                int b = Integer.parseInt(arr[1]);
                int t = Integer.parseInt(arr[2]);
                MeasureData meas = measureMap.getMeasure(m);
                if (meas == null) {
                    return 0;
                }
                tick = meas.getTick(b, t);
            } catch (NumberFormatException e) {
                errMsg.setErrMsg(e.getMessage());
                return 0;
            }
        } else {
            tick = Integer.parseInt(str);
        }
//		System.err.println("parseTick(" + str + ") returns " + tick);
        return tick;
    }

    public void start() {
        if (seq == null)
            return;
        thread = new Thread(() -> {
            boolean finish = false;
            if (cursor == seq.length() - 1) {
                cursor = 0;
            }
            playOne();
            while (!finish) {
                int t = getTick();
                int delta;
                boolean fermata = seq.get(cursor).isFermata();
                if (cursor < seq.length() - 1) {
                    Item item = seq.get(cursor + 1);
                    int t2 = item.getTick();
                    delta = t2 - t;
//		    System.err.println("cursor = " + cursor + ", tick = " + t + ", next tick = " + t2 + ", delta = " + delta);
                    if (delta < 0) {
                        return; // error message?
                    }
                } else {
                    delta = 10;
                    repeatMap.clear();
                    finish = true;
                }
                try {
                    if (fermata) {
                        delta *= 3;
                    }
                    Thread.sleep(delta * quantum(t));
                } catch (InterruptedException e) {
                    break;
                }
                if (!finish) {
                    forward();
                }
            }
        });
        thread.start();
    }

    private int quantum(int tick) {
        // figure milliseconds per jiffy based on tempo
        MeasureData m = measureMap.getMeasureAt(tick);
        int jiffies = m.getJiffiesPerBeat();
//	System.err.println("**** " + m + " --> " + jiffies);
//	Double mTempo = m.getTempo();
//	if (mTempo != null && mTempo > 0.0) {
//	    tempo = (int) mTempo.doubleValue();
//	    tempoSpinner.setValue(tempo);
//	}
        double bpm = tempo;
        double bps = bpm / 60.0;
        double jps = jiffies * bps;
        int q = (int) (1000.0 / jps);
        return (q < 0) ? 0 : q;
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }
}
