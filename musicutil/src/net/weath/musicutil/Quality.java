package net.weath.musicutil;

/**
 * The quality of an Interval: Perfect, Major, Minor, Diminished, etc.
 *
 * No interval will be more than quintuply-augmented (or quintuply-diminished)
 * because there is no such thing as a triple flat (or triple sharp) and the
 * unmodified (natural) notes produce at most a singly-augmented (or singly-
 * diminished) interval (F to B is aug 4th, B to F is dim 5th, so Fbb to Bx is
 * quintuply-augmented 4th, and Bx to Fbb is quintuply-diminished 5th).
 *
 * @author weath
 *
 */
public enum Quality {
    Perfect, Major, Minor, Augmented, Diminished, Augmented2, Diminished2, Augmented3, Diminished3, Augmented4, Diminished4, Augmented5, Diminished5;

    /**
     * @return the String representation of this Quality
     */
    @Override
    public String toString() {
        switch (this) {
            case Perfect:
                return "perfect";
            case Major:
                return "major";
            case Minor:
                return "minor";
            case Augmented:
                return "augmented";
            case Diminished:
                return "diminished";
            case Augmented2:
                return "doubly-augmented";
            case Diminished2:
                return "doubly-diminished";
            case Augmented3:
                return "triply-augmented";
            case Diminished3:
                return "triply-diminished";
            case Augmented4:
                return "quadruply-augmented";
            case Diminished4:
                return "quadruply-diminished";
            case Augmented5:
                return "quintuply-augmented";
            case Diminished5:
                return "quintuply-diminished";
        }
        return "no such interval kind!";
    }

    /**
     * Return the next Quality, in the direction of increasing width. Note that
     * Diminished.next() is undefined, because it could be either Minor or
     * Perfect, depending on the Interval.
     *
     * @return the next larger Quality, or null
     */
    public Quality next() {
        switch (this) {
            case Minor:
                return Major;
            case Perfect:
            case Major:
                return Augmented;
            case Augmented:
                return Augmented2;
            case Augmented2:
                return Augmented3;
            case Augmented3:
                return Augmented4;
            case Augmented4:
                return Augmented5;
            default:
                // throw new IllegalArgumentException();
                return null;
        }
    }

    /**
     * Return the next Quality, in the direction of decreasing width. Note that
     * Augmented.prev() is undefined, because it could be either Major or
     * Perfect, depending on the Interval.
     *
     * @return the next smaller Quality, or null
     */
    public Quality prev() {
        switch (this) {
            case Major:
                return Minor;
            case Perfect:
            case Minor:
                return Diminished;
            case Diminished:
                return Diminished2;
            case Diminished2:
                return Diminished3;
            case Diminished3:
                return Diminished4;
            case Diminished4:
                return Diminished5;
            default:
                // throw new IllegalArgumentException();
                return null;
        }
    }

    /**
     * Return the "modification level" for this Quality, defined as 0 for
     * Perfect, Major, and Minor; +1 for Augmented/Diminished; +2 for
     * Augmented2/Diminished2; etc.
     *
     * @return the "modification level" as an int
     */
    public int modLevel() {
        switch (this) {
            case Perfect:
            case Major:
            case Minor:
                return 0;
            case Augmented:
            case Diminished:
                return 1;
            case Augmented2:
            case Diminished2:
                return 2;
            case Augmented3:
            case Diminished3:
                return 3;
            case Augmented4:
            case Diminished4:
                return 4;
            case Augmented5:
            case Diminished5:
                return 5;
        }
        return -1; // can't happen
    }
}
