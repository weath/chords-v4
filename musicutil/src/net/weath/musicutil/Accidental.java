package net.weath.musicutil;

import java.awt.Rectangle;

/**
 * Represents an accidental on the page
 *
 * @author weath
 *
 */
public class Accidental {

    private final Rectangle r;

    private final AccidentalKind kind;

    /**
     * Create an Accidental object
     *
     * @param kind the AccidentalKind
     * @param x X coord
     * @param y Y coord
     * @param w width
     * @param h height
     */
    public Accidental(AccidentalKind kind, int x, int y, int w, int h) {
        this.kind = kind;
        this.r = new Rectangle();
        this.r.x = x;
        this.r.y = y;
        this.r.width = w;
        this.r.height = h;
    }

    /**
     * Set the position of this object
     *
     * @param x new X coord
     * @param y new Y coord
     */
    public void place(int x, int y) {
        this.r.x = x;
        this.r.y = y;
    }

    /**
     * Does this object collide with the given one?
     *
     * @param a an Accidental
     * @return true if they overlap
     */
    public boolean collidesWith(Accidental a) {
        return this.r.intersects(a.r);
    }

    public AccidentalKind getKind() {
        return kind;
    }

    /**
     * Adjust the X coords of the given array of Accidental objects such that no
     * two of them overlap
     *
     * @param acc array of Accidental
     */
    public static void layout(Accidental[] acc) {
        boolean ok = false;

        while (!ok) {
            ok = true;
            for (int i = acc.length - 1; i >= 0; i--) {
                if (acc[i] == null) {
                    continue;
                }
                for (int j = i + 1; j < acc.length; j++) {
                    if (acc[j] == null) {
                        continue;
                    }
                    if (acc[i].collidesWith(acc[j])) {
                        ok = false;
                        acc[i].r.x = acc[j].r.x + acc[j].r.width + 2;
                        break;
                    }
                }
                if (!ok) {
                    break;
                }
            }
        }

    }

    /**
     * Return the X coord of this Accidental
     *
     * @return the X coord
     */
    public int getX() {
        return r.x;
    }

}
