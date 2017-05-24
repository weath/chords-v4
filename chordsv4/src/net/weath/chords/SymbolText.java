package net.weath.chords;

import java.awt.event.ActionEvent;

import javax.swing.JTextField;

import net.weath.musicutil.Chord;
import net.weath.musicutil.Note;

public class SymbolText extends JTextField implements IChordController, IChordViewer {

    private static final long serialVersionUID = 1L;

    private final ChordSymbolParser symParser;

    private final ChordModel model;

    public SymbolText(int nColumns, final ErrMsg errMsg, ChordSymbolParser symParser, ChordModel model) {
        super(nColumns);
        this.symParser = symParser;
        this.model = model;
        model.addListener(this);
        addActionListener((ActionEvent arg0) -> {
            //	System.err.println("symbol: " + symbolText.getText());
            errMsg.setErrMsg(null);
            try {
                parseSymbol(getText());
            } catch (Exception t) {
                errMsg.setErrMsg(t.getMessage());
            }
        });
    }

    @Override
    public void chordChanged(ChordChangeEvent event) {
        Note bass = event.getBass();
        Note root = event.getRoot();
        Chord chord = event.getSelectedChord();
        if (chord == null || chord instanceof UnrecognizedChord) {
            setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(chord.getName(root));
        if (bass != null) {
            sb.append("/");
            sb.append(bass.toString());
        }
        int inv = event.getInversion();
        if (inv != 0) {
            sb.append(" (");
            sb.append(Chord.ordinal(inv));
            sb.append(" inversion)");
        }
        setText(sb.toString());
    }

    @Override
    public Origin getOrigin() {
        return Origin.Symbol;
    }

    private void parseSymbol(String symbol) throws Exception {
        boolean res = symParser.parse(symbol);
        if (res) {
            Chord chord = symParser.getChord();
            Note root = symParser.getRoot();
            System.err.print(symbol + " => " + root + chord);
            Note bass = symParser.getBass();
            if (bass != null) {
                System.err.print("/" + bass);
            }
            model.setChord(getOrigin(), bass, root, chord);
            int inv = symParser.getInversion();
            if (inv != 0) {
                System.err.print(" (inv = " + inv + ")");
                model.setInversion(getOrigin(), inv);
            }
            System.err.println();
        }
    }
}
