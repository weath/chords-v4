package net.weath.chords;

import java.util.EventListener;

public interface ChordChangeListener extends EventListener {

    void chordChanged(ChordChangeEvent event);
}
