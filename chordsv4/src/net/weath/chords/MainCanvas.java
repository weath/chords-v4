package net.weath.chords;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import net.weath.musicutil.Accidental;
import net.weath.musicutil.AccidentalKind;
import net.weath.musicutil.Note;
import net.weath.musicutil.Pitch;

/**
 * The main canvas displays a grand staff with the notes of the current chord
 *
 * @author weath
 *
 */
public class MainCanvas extends JPanel implements ChordChangeListener {

    private static final long serialVersionUID = 1L;

    BufferedImage bracketImage;

    /**
     * the Accidental for each pitch in "pitches"
     */
    private Accidental[] acc;

    /**
     * the X offset of each notehead
     */
    private int noteoffsets[];

    /**
     * the Y offset of each notehead
     */
    private int notey[];

    /**
     * the X value for middle-C
     */
    private int centerX;

    /**
     * the Y value for middle-C
     */
    private int middleCy;

    private final Font maestro;

    private Pitch[] pitches;

    private Note[] noteArr;
    
    private boolean just;

    /**
     * the X value of the upper left corner of the staff
     */
    private static final int xBase = 50;

    /**
     * the Y value of the upper left corner of the staff
     */
    private static final int yBase = 180;

    public MainCanvas(Font maestro) {
        this.maestro = maestro;
        loadBracketImage();
    }

    private void loadBracketImage() {
        try {
            URL url = getClass().getResource("brace.jpg");
            bracketImage = ImageIO.read(url);
        } catch (IOException e) {
            System.err.println("could not load image: " + e.getMessage());
        }
    }

    @Override
    public void chordChanged(ChordChangeEvent event) {
        just = event.isJust();
        pitches = event.getPitches();
        noteArr = new Note[pitches.length];
        for (int i = 0; i < pitches.length; i++) {
            noteArr[i] = pitches[i].getNote();
        }
        repaint();
    }

    /**
     * Paint the canvas
     */
    @Override
    public void paintComponent(Graphics g) {
        Rectangle r = getBounds();
        Point origin = new Point();
        g.clearRect(0, 0, r.width, r.height);
        g.drawImage(bracketImage, xBase - 16, yBase - 32, null);
        g.setFont(maestro.deriveFont(36.0F));
        int x = xBase;
        int y = yBase;
        g.drawString("\uF026", x + 10, y);
        y -= 30;
        origin.x = x;
        origin.y = y;
        for (int i = 0; i < 5; i++) {
            g.drawLine(x, y, r.width - xBase, y);
            y += 10;
        }
        y += 30;
        g.drawString("\uF03F", x + 10, y);
        y -= 10;
        for (int i = 0; i < 5; i++) {
            g.drawLine(x, y, r.width - xBase, y);
            y += 10;
        }
        g.drawLine(origin.x, origin.y, x, y - 10);
        if (noteArr == null) {
            return;
        }
        noteoffsets = new int[noteArr.length];
        acc = new Accidental[noteArr.length];
        notey = new int[noteArr.length];
        x = r.width / 2;
        centerX = x;
        // first, get y coordinates
        middleCy = yBase + 20;
        if (pitches == null) {
            pitches = new Pitch[0];
        }
        for (int i = 0; i < pitches.length; i++) {
            Pitch p = pitches[i];
            y = middleCy - Pitch.middleC.distTo(p) * 5;
            if (y > middleCy) {
                y += 10; // adjust for gap in staves
            }
            notey[i] = y;
        }
        // now, layout x coords of notes
        for (int i = 1; i < noteArr.length; i++) {
            int d = notey[i - 1] - notey[i];
            if (d < 10) {
                // adjust upper note if lower note is in normal position
                if (noteoffsets[i - 1] == 0) {
                    noteoffsets[i] = 12;
                } else {
                    noteoffsets[i] = 0;
                }
            }
        }
        // now, layout x coords of accidentals
        for (int i = 0; i < noteArr.length; i++) {
            Note n = noteArr[i];
            AccidentalKind kind = n.getAccidentalKind();
            y = notey[i];
            Accidental a = null;
            if (kind != null) {
                int w = g.getFontMetrics().stringWidth(kind.getString());
                int h = (kind == AccidentalKind.doubleSharp) ? 12 : 24;
                a = new Accidental(kind, 0, y, w, h);
            }
            acc[i] = a;
        }
        Accidental.layout(acc);
        // finally, draw the notes and accidentals
        for (int i = 0; i < noteArr.length; i++) {
            drawNote(g, i, x);
        }
    }

    /**
     * Return the tooltip text to display for this mouse position
     * @param event MouseEvent
     * @return tooltip text
     */
    @Override
    public String getToolTipText(MouseEvent event) {
        if (notey == null) {
            return null;
        }

        // determine the note under the mouse
        int x = event.getX();
        int y = event.getY();

        String s = null;

        for (int i = 0; i < notey.length; i++) {
            int ny = notey[i];
            if (Math.abs(y - ny) < 3) {
                int nx = centerX + noteoffsets[i] + 5;
                if (Math.abs(x - nx) < 5) {
                    s = pitches[i].toString();
                    break;
                }
            }
        }

        return s;
    }

    /**
     * draw note "i" at ("x","notey[i]")
     */
    private void drawNote(Graphics g, int i, int x) {
        int y = notey[i];
        Note n = noteArr[i];
        int mod = n.modifier();
        int x0;
        int x1;
        Color saved = g.getColor();
        g.setColor(getColor(i, saved));
        if (mod != 0) {
            String m = acc[i].getKind().getString();
            int delta = g.getFontMetrics().stringWidth(m);
            g.drawString(m, x - delta - acc[i].getX(), y);
            x0 = x - delta - acc[i].getX() - 3;
        } else {
            x0 = x - 3;
        }
        g.drawString("\uF077", x + noteoffsets[i], y);
        g.setColor(saved);
        x1 = x + noteoffsets[i] + g.getFontMetrics().stringWidth("\uF077")
                + 3;
        if (y == middleCy) {
            g.drawLine(x0, y, x1, y);
        } else if (y < yBase - 30) {
            for (; y < yBase; y += 5) {
                if ((y % 10) == 0) {
                    g.drawLine(x0, y, x1, y);
                }
            }
        } else if (y > yBase + 80) {
            for (; y > yBase + 80; y -= 5) {
                if ((y % 10) == 0) {
                    g.drawLine(x0, y, x1, y);
                }
            }
        }
    }

    /**
     * Return the Color for the "i"th note, based on its pitch bend
     *
     * @param i which note
     * @param orig Color to return for 0 bend
     * @return the Color to use when drawing the note
     */
    public Color getColor(int i, Color orig) {
        if (pitches == null || pitches.length == 0) {
            return orig;
        }
        Pitch p = pitches[i];
        if (!p.isReal())
            return new Color(100, 100, 100, 127); // ghostly gray
        if (!just)
            return orig;
        double bend = p.getCents();
        int mag = (int) ((Math.abs(bend) + 20) / 50.0 * 255);
        mag = Math.min(mag, 255);
        if (bend < 0) {
            return new Color(0, 0, mag); // blue
        } else if (bend > 0) {
            return new Color(mag, 0, 0); // red
        } else {
            return orig;
        }
    }
}
