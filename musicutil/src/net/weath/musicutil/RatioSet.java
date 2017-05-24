package net.weath.musicutil;

import java.util.ArrayList;

/**
 * A set of integers representing the frequency ratios in a Just Intonation
 * chord
 *
 * @author weath
 *
 */
public class RatioSet {

    /**
     * the integer values for the ratios
     */
    private int[] values;
    boolean degenerate;

    /**
     * Construct an empty RatioSet (private!)
     */
    private RatioSet() {
        // private constructor leaves values == null!
    }

    /**
     * Construct a RatioSet from the given array of ints
     *
     * @param arg one or more ints
     */
    public RatioSet(int... arg) {
        int n = arg.length;
        values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = arg[i];
            if (values[0] == 1 && i > 0 && values[i] == 1) {
                degenerate = true;
            }
            if (i > 0) {
                if (!degenerate && values[i] < values[i - 1]) {
                    throw new IllegalArgumentException(
                            "Ratio components must be in increasing order");
                }
            }
        }
        reduce();
    }

    /**
     * Construct a RatioSet from an int plus an array of ints
     *
     * @param r1 the first int in the set
     * @param arg the other ints
     */
    public RatioSet(int r1, int[] arg) {
        int n = arg.length;
        values = new int[n + 1];
        values[0] = r1;
        for (int i = 0; i < n; i++) {
            values[i + 1] = arg[i];
            if (values[0] == 1 && i > 0 && values[i + 1] == 1) {
                degenerate = true;
            }
        }
        reduce();
    }

    /**
     * Reduce the ratios to least common denominators
     */
    private void reduce() {
        if (values.length == 0) {
            return;
        }
        int base = values[0];
        ArrayList<Integer> factors = Util.factor(base);
        for (int f : factors) {
            boolean isFactor = true;
            if (f == 0) {
                throw new IllegalArgumentException("Divide by zero!");
            }
            for (int i = 1; i < values.length; i++) {
                if (values[i] % f != 0) {
                    isFactor = false;
                    break;
                }
            }
            if (isFactor) {
                for (int i = 0; i < values.length; i++) {
                    values[i] /= f;
                }
            }
        }
    }

    /**
     * @return the size of the set
     */
    public int size() {
        return values.length;
    }

    /**
     * Return the ratio values[n] / values[0] as a double
     *
     * @param n the index
     * @return a double representing the ratio values[n] / values[0]
     */
    public double get(int n) {
        if (values[0] == 0.0) {
            throw new IllegalArgumentException("Divide by zero!");
        }
        return (double) values[n] / (double) values[0];
    }

    /**
     * Return a modified set of ratios, with the 2nd note as the new root.
     *
     * @return a new RatioSet, rotated appropriately
     */
    public RatioSet invert() {
        RatioSet rs = new RatioSet();
        rs.values = new int[this.values.length];
        if (rs.values.length == 0) {
            return rs;
        }
        System.arraycopy(this.values, 1, rs.values, 0, this.values.length - 1);
        int n = this.values[0];
        n *= 2; // up one octave
        int m = this.values[this.values.length - 1];
        while (n < m) {
            n *= 2; // ensure the ratios remain in increasing order
        }
        rs.values[rs.values.length - 1] = n;
        rs.reduce();
        return rs;
    }

    /**
     * @return the String representation of this RatioSet
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(values[0]));
        for (int i = 1; i < values.length; i++) {
            sb.append(" : ");
            sb.append(Integer.toString(values[i]));
        }
        return sb.toString();
    }

    /**
     * Return the ith value in the set
     *
     * @param i the index
     * @return values[i]
     */
    public int value(int i) {
        if (i >= values.length) {
            return 1;
        }
        return values[i];
    }

    public boolean isDummy() {
        if (degenerate) {
            return true;
        }
        for (int n : this.values) {
            if (n != 1) {
                return false;
            }
        }
        return true;
    }
}
