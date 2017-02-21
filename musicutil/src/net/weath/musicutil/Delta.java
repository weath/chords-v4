package net.weath.musicutil;

/**
 * Represents a distance (in semitones) between two notes
 *
 * @author weath
 *
 */
public class Delta implements Comparable<Object> {

    private final int semis;

    /**
     * Create a new Delta object
     *
     * @param semis an int (the number of semitones)
     */
    public Delta(int semis) {
        this.semis = semis;
    }

    /**
     * Create a new Delta object from two Notes
     *
     * @param root the root Note
     * @param other another Note
     */
    public Delta(Note root, Note other) {
        semis = other.minus(root);
    }

    /**
     * Return the number of semitones represented by this Delta
     *
     * @return the number of semitones
     */
    public int semitones() {
        return semis;
    }

    /**
     * Return the String representation of this Delta
     */
    @Override
    public String toString() {
        return Integer.toString(semis);
    }

    /**
     * Compare two Delta objects
     * @param arg0 the object to compare this to
     */
    @Override
    public int compareTo(Object arg0) {
        if (arg0 instanceof Delta) {
            Delta other = (Delta) arg0;
            return this.semis - other.semis;
        }
        return 0;
    }
}
