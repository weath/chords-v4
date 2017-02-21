package net.weath.musicutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public class KeyboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JScrollPane scrollPane;

    private final JPanel mainPanel;

    private final JPanel canvas;

    private final JPanel labelCanvas;

    private Rectangle[] key = new Rectangle[128]; // only keep outline of MIDI keys

    private String[] labels = new String[128];

    private int[] factor = new int[128];

    public final static int border = 10;

    private final static int MIDDLE_C_X_POS;

    // black/white key width at top edge
    private final static int[] b = {14, 14, 14, 14, 14, 13, 14, 13, 14, 13, 14, 13};

    // is key N black?
    private final static boolean[] isBlack = {false, true, false, true, false, false, true, false,
        true, false, true, false};

    // white key width at bottom edge
    private final static int[] w = {23, 24, 23, 24, 23, 23, 24};

    // difference between left top and left bottom for white keys
    private final static int[] diff = {0, -1, 5, -1, 10, 0, -1, 3, -1, 7, -1, 11, 0};

    private boolean[] pressed = new boolean[128]; // state of each MIDI key

    private final Font labelFont;

    private KeyboardListener listener;

    static {
        int n = border;
        for (int oct = 4; oct > 0; oct--) {
            for (int i = 0; i < w.length; i++) {
                n += w[i];
            }
        }
        n += w[0] / 2;
//		System.err.println("Middle C is at " + n);
        MIDDLE_C_X_POS = n;
    }

    public void clear() {
        for (int i = 0; i < 128; i++) {
            setPressed(i, false);
        }
        labels = new String[128];
        factor = new int[128];
    }

    public KeyboardPanel(KeyboardListener listener) {
        addKeyboardListener(listener);
        setLayout(new BorderLayout());
        mainPanel = new JPanel();
        labelFont = getFont().deriveFont(14.0F);
        mainPanel.setLayout(new BorderLayout());
        labelCanvas = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Rectangle bounds = g.getClipBounds();
                g.setColor(Color.decode("#eeeeee"));
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g.setColor(Color.BLACK);
                for (int i = 12; i < 128; i++) {
                    if (key[i] != null) {
                        if (labels[i] != null) {
                            int x = key[i].x;
                            int y = 17;
                            g.drawString(labels[i], x, y);
                        }
                        if (factor[i] != 0) {
                            int x = key[i].x;
                            int y = 30;
                            g.drawString("" + factor[i], x, y);
                        }
                    }
                }
            }
        };
        labelCanvas.setPreferredSize(new Dimension(1605, 30));
        labelCanvas.setFont(labelFont);
        mainPanel.add(labelCanvas, BorderLayout.NORTH);
        canvas = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                // outer rectangle
                Rectangle bounds = getBounds();
                int mid = bounds.height / 2;
                g.setColor(Color.BLACK);
                g.drawRect(border, border, bounds.width - 2 * border, bounds.height - 2 * border);
                // draw as many keys as will fit
                int midi = 12; // C 0 is lowest note we draw
                int x = border;
                int i = 0;
                // white keys
                while (x < bounds.width - border) {
                    g.drawLine(x, border, x, bounds.height - border);
                    x += w[i++];
                    if (i >= w.length) {
                        i = 0;
                    }
                }
                // black keys
                x = border;
                i = 0;
                while (x + b[i] < bounds.width - border) {
                    if (isBlack[i]) {
                        g.fillRect(x, border, b[i], mid);
                    }
                    if (midi < 128) {
                        key[midi] = new Rectangle();
                        key[midi].x = x;
                        key[midi].y = border;
                        key[midi].height = mid;
                    }
                    x += b[i++];
                    if (midi < 128) {
                        key[midi].width = x - key[midi].x;
                    }
                    if (i >= b.length) {
                        i = 0;
                    }
                    midi++;
                }
                if (midi == 127) {
                    key[midi] = new Rectangle();
                    key[midi].x = x;
                    key[midi].y = border;
                    key[midi].width = bounds.width - border - x;
                    key[midi].height = mid;
                }
                if (midi > 127) {
                    // shade keys that are out of range
                    g.setColor(Color.LIGHT_GRAY);
                    int x1 = key[127].x + b[127 % 12];
                    g.fillRect(x1, border, bounds.width - border - x1, bounds.height - 2 * border);
                }
                // mark pressed keys
                g.setColor(Color.RED);
                for (i = 12; i < 128; i++) {
                    if (pressed[i]) {
                        g.fillRect(key[i].x, key[i].y, key[i].width, key[i].height);
                        int m = i % 12;
                        if (isBlack[m] == false) {
                            x = key[i].x - diff[m];
                            int width = 24;
                            if (m == 2 || m == 5 || m == 11) {
                                width++;
                            }
                            if (i == 127) {
                                width = key[i].width + diff[m];
                            }
                            g.fillRect(x, border + mid, width, bounds.height - mid - 2 * border);
                        }
                    }
                }
                // middle C marker
                g.setColor(Color.BLACK);
                int cx = MIDDLE_C_X_POS - 4;
                int cy = bounds.height - border - 12;
                g.fillOval(cx, cy, 10, 10);
            }
        };
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                released(e);
            }
        });
        canvas.setPreferredSize(new Dimension(1605, 150));
        mainPanel.add(canvas, BorderLayout.CENTER);
        scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
    }

    void pressed(MouseEvent e) {
        int midinumber = getMidiNumber(e);
        listener.keyPressed(midinumber, e);
    }

    void released(MouseEvent e) {
        int midinumber = getMidiNumber(e);
        listener.keyReleased(midinumber, e);
    }

    void addKeyboardListener(KeyboardListener listener) {
        this.listener = listener;
    }

    protected int getMidiNumber(MouseEvent e) {
        Point point = e.getPoint();
        JPanel source = (JPanel) e.getSource();
        Rectangle bounds = source.getBounds();
        int octave = 0;
        if (point.x < border || point.x > bounds.width - border) {
            return -1;
        }
        if (point.y < border || point.y > bounds.height - border) {
            return -1;
        }
        int i;
        if (point.y < bounds.height / 2 + border) {
            // above the midpoint, may be white or black key
            int x = point.x - border;
            i = 0;
            while (x > 0) {
                x -= b[i];
                if (x > 0) {
                    i++;
                    if (i >= b.length) {
                        i = 0;
                        octave++;
                    }
                }
            }
        } else {
            // below the midpoint, must be a white key
            int x = point.x - border;
            i = 0;
            while (x > 0) {
                x -= w[i];
                if (x > 0) {
                    i++;
                    if (i >= w.length) {
                        i = 0;
                        octave++;
                    }
                }
            }
            // adjust for the black keys
            switch (i) {
                case 0:
                    break;
                case 1:
                    i += 1;
                    break;
                case 2:
                    i += 2;
                    break;
                case 3:
                    i += 2;
                    break;
                case 4:
                    i += 3;
                    break;
                case 5:
                    i += 4;
                    break;
                case 6:
                    i += 5;
                    break;
            }
        }
        return 12 + octave * 12 + i;
    }

    public boolean isPressed(int n) {
        return pressed[n];
    }

    public void setPressed(int n, boolean b) {
        pressed[n] = b;
        canvas.repaint();
    }

    public void setLabel(int n, String s) {
        labels[n] = s;
        labelCanvas.repaint();
    }

    public void setFactor(int n, int fact) {
        factor[n] = fact;
        labelCanvas.repaint();
    }

    public void refresh() {
        canvas.repaint();
        labelCanvas.repaint();
    }

    public void center() {
        // scroll so Middle C is in the middle, if possible
        Dimension size = scrollPane.getViewport().getSize();
        int viewWidth = size.width;
        JScrollBar bar = scrollPane.getHorizontalScrollBar();
        if (viewWidth >= canvas.getWidth()) {
            return; // can't adjust scrollbar
        }
        int value = MIDDLE_C_X_POS - viewWidth / 2;
        bar.setValue(value);
    }
}
