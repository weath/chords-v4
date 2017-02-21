package net.weath.chords;

import net.weath.musicutil.Chord;
import net.weath.musicutil.Note;

public class UnrecognizedChord extends Chord {

    public UnrecognizedChord() {
        super("unrecognized");
    }

    @Override
    public String name(Note root) {
        return toString();
    }

    @Override
    public String toString() {
        return "Unrecognized chord";
    }
}
