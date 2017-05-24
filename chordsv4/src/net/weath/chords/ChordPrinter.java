package net.weath.chords;

import net.weath.musicutil.Chord;
import net.weath.musicutil.Note;
import net.weath.musicutil.Pitch;

/**
 *
 * @author weath
 */
public class ChordPrinter implements ChordChangeListener {

    @Override
    public void chordChanged(ChordChangeEvent event) {
        Chord c = event.getSelectedChord();
        if (c == null) {
            System.err.println("No chord");
        } else {
            Note root = event.getRoot();
            System.err.print(c.name(root));
            if (event.getBass() != null) {
                System.err.print("/" + event.getBass());
            }
            System.err.print(" => ");
            StringBuilder sb = new StringBuilder();
            Pitch[] pitches = event.getPitches();
            for (Pitch p : pitches) {
                if (!p.isReal())
                    sb.append("(");
                sb.append(p.getNote());
                if (!p.isReal())
                    sb.append(")");
                sb.append(" ");
            }
            sb.setLength(sb.length() - 1);
            System.err.println(sb.toString());
        }
    }
}
