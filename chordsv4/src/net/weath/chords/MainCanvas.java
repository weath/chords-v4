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
import javax.swing.event.MouseInputAdapter;

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
    
    private Note[] notes;

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

	private ChordModel model;

    /**
     * the X value of the upper left corner of the staff
     */
    private static final int xBase = 50;

    /**
     * the Y value of the upper left corner of the staff
     */
    private static final int yBase = 180;

    public MainCanvas(Font maestro, ChordModel model) {
        this.maestro = maestro;
        this.model = model;
        loadBracketImage();
		MyMouseListener l = new MyMouseListener();
		addMouseMotionListener(l);
		addMouseListener(l);
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

		int n = getNoteIndex(x, y);
		
		if (n < 0)
			return null;
		
		String s = pitches[n].toString();

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
    
	private int getNoteIndex(int x, int y) {
		for (int i = 0; i < notey.length; i++) {
			int ny = notey[i];
			if (Math.abs(y - ny) < 3) {
				int nx = centerX + noteoffsets[i] + 5;
				if (Math.abs(x - nx) < 5) {
					return i;
				}
			}
		}
		return -1;
	}

	public int whichNote(int x, int y) {
		int n = getNoteIndex(x, y);
		//System.out.println("note = " + n + ", " + ((n >= 0) ? pitches[n].toString() : "<none>"));
		return n;
	}

	private Note[] clone(Note[] noteArr) {
		if (noteArr == null)
			return new Note[0];
		Note[] n = new Note[noteArr.length];
		System.arraycopy(noteArr, 0, n, 0, noteArr.length);
		return n;
	}

	class MyMouseListener extends MouseInputAdapter {
		private int x;
		private int y;
		private int note;
		private int originalMod;
		
		@Override
		public void mousePressed(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			note = whichNote(x, y);
			if (note > 0) {
				Pitch p = pitches[note];
				Note n = p.getNote();
				AccidentalKind a = (n == null) ? null : n.getAccidentalKind();
				originalMod = (a == null) ? 0 : a.modifier();
			} else {
				originalMod = 0;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (note < 0 || pitches == null || pitches.length < note)
				return;
			pitches[note] = new Pitch(notes[note], pitches[note].getOctave());
			noteArr = MainCanvas.this.clone(notes);
			x = y = -1;
			note = -1;
			model.setPitches(Origin.Main, pitches);
		}
		
		/*
		 * Define an imaginary ruler, 100 pixels long, with 5 20-pixel-wide zones. The 5 zones
		 * represent the 5 possible modifiers, left to right: bb, b, nat, #, x
		 * Whatever the original accidental is, center that zone over the original location. Require
		 * movement of the mouse into an adjacent zone before changing the applied accidental.
		 * 
		 * 		-50        -30			-10				10			30				50
		 * 		 |	  bb	 |		b	 |		nat		|	  #		|		x		|
		 * 
		 * (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			int deltaX = e.getX() - x;
			if (note < 0)
				return;
			Pitch p = pitches[note];
			Note n = p.getNote();
			AccidentalKind curAcc = n.getAccidentalKind();
			int m = originalMod;
			// which modifier was applied to note initially? place the center of its
			// section of the ruler at the original mouse position
			int center = m * 20; // one of [ -40, -20, 0, 20, 40 ]
			int r = center + deltaX; // convert to ruler-relative coord
			int d = (r + 9) / 20; // how many slices did the mouse move from the center?
			//System.out.println("deltaX = " + deltaX + ", m = " + m + ", d = " + d);
			if (m + d <= 2 && m + d >= -2) {
				m += d;
				AccidentalKind a = AccidentalKind.forModifier(m);
				if (a != curAcc) {
					n = n.apply(a);
					//System.out.println("Original = " + p + ", new = " + n);
					notes = new Note[noteArr.length];
					System.arraycopy(noteArr, 0, notes, 0, noteArr.length);
					notes[note] = n;
					Pitch[] newPitches = new Pitch[model.getPitches().length];
					System.arraycopy(model.getPitches(), 0, newPitches, 0, newPitches.length);
					newPitches[note] = new Pitch(notes[note], newPitches[note].getOctave());
					model.setPitches(Origin.Main, newPitches);
				}
			}
		}
	}

}
