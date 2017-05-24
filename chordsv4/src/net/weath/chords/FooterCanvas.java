package net.weath.chords;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;

import net.weath.musicutil.AccidentalKind;
import net.weath.musicutil.Chord;
import net.weath.musicutil.Note;
import net.weath.musicutil.Pitch;
import net.weath.musicutil.RatioSet;

/**
 * The status/error message area
 *
 * @author weath
 *
 */
public class FooterCanvas extends JPanel implements ChordChangeListener, ErrorMessageListener {

    private static final long serialVersionUID = 1L;
    private final Font maestro;
    private final Font arial;
    private final ErrMsg errMsg;

    private Pitch[] pitches;
    private Note root;
    private Chord chord;
    private RatioSet ratioSet;
    private boolean just;

    public FooterCanvas(Font maestro, Font arial, ErrMsg errMsg) {
        this.maestro = maestro;
        this.arial = arial;
        this.errMsg = errMsg;
    }

    private String getErrMsg() {
        return errMsg.getErrMsg();
    }

    /**
     * Paint the footer
     */
    @Override
    public void paint(Graphics g) {
        Rectangle r = getBounds();
        g.clearRect(0, 0, r.width, r.height);
        if (getErrMsg() != null) {
            g.setColor(new Color(255, 200, 200));
            g.fillRect(0, 0, r.width, r.height);
            g.setColor(Color.BLACK);
            g.setFont(arial);
            g.drawString(getErrMsg(), 5, 20);
        } else {
            g.setColor(Color.white);
            g.fillRect(0, 0, r.width, r.height);
            g.setColor(Color.black);
        }
        if (root == null || chord == null) {
            return;
        }
        int x = 15 + g.getFontMetrics().stringWidth((getErrMsg() != null) ? getErrMsg() : "");
        int y = 20;
        String s = (root == null) ? "" : root.toString().substring(0, 1);
        g.setFont(arial);
        g.drawString(s, x, y);
        x += g.getFontMetrics().stringWidth(s);
        String m = "";
        AccidentalKind kind = (root == null) ? null : root.getAccidentalKind();
        if (kind != null) {
            m = kind.getString();
        }
        g.setFont(maestro);
        g.drawString(m, x, y - 5);
        g.setFont(arial);
        x += g.getFontMetrics().stringWidth(m) + 5;

        s = chord.getName();
        char[] a = s.toCharArray();
        int delta = 0;
        for (int i = 0; i < a.length; i++) {
            char c = a[i];
            if (!chord.isInterval()) {
                switch (c) {
                    case 'b':
                        delta = 5;
                        a[i] = '\uF062';
                        g.setFont(maestro);
                        break;
                    case '#':
                        delta = 5;
                        a[i] = '\uF023';
                        g.setFont(maestro);
                        break;
                    default:
                        delta = 0;
                        g.setFont(arial);
                        break;
                }
            }
            g.drawChars(a, i, 1, x, y - delta);
            x += g.getFontMetrics().charWidth(a[i]);
        }

        g.setFont(arial);
        g.drawString(" = ", x, y);
        x += g.getFontMetrics().stringWidth(" = ");
        for (Pitch p : pitches) {
            Note n = p.getNote();
            s = n.toString().substring(0, 1);
            kind = n.getAccidentalKind();
            if (kind == null) {
                m = "";
            } else {
                m = kind.getString();
            }
            g.setFont(arial);
            if (!p.isReal()) {
                g.drawString("(", x, y);
                x += g.getFontMetrics().stringWidth("(");
            }
            g.drawString(s, x, y);
            x += g.getFontMetrics().stringWidth(s);
            g.setFont(maestro);
            g.drawString(m, x, y - 5);
            x += g.getFontMetrics().stringWidth(m);
            g.setFont(arial);
            if (!p.isReal()) {
                g.drawString(")", x, y);
                x += g.getFontMetrics().stringWidth(")");
            }
            x += 5;
        }
        RatioSet rs = ratioSet;
        if (rs != null && just) {
            s = "  [ " + rs.toString() + " ]";
            g.setFont(arial);
            g.drawString(s, x, y);
        }
    }

    @Override
    public void chordChanged(ChordChangeEvent event) {
        this.pitches = event.getPitches();
        this.root = event.getRoot();
        this.chord = event.getSelectedChord();
        this.ratioSet = event.getRatioSet();
        this.just = true;
        repaint();
    }

    @Override
    public void errorMessageChanged(String newErrMsg) {
        repaint();
    }
}
