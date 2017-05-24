package net.weath.musicutil;

/**
 * Represents the five kinds of accidentals
 *
 * @author weath
 *
 */
public enum AccidentalKind {
    doubleFlat, flat, natural, sharp, doubleSharp;

    /**
     * Return the AccidentalKind corresponding to the given modifier.
     *
     * -2 = doubleFlat, -1 = flat, 0 = natural, 1 = sharp, 2 = doubleSharp
     *
     * @param i the modifier
     * @return the corresponding AccidentalKind, or null
     */
    public static AccidentalKind forModifier(int i) {
        switch (i) {
            case -2:
                return doubleFlat;
            case -1:
                return flat;
            case 1:
                return sharp;
            case 2:
                return doubleSharp;
        }
        return null;
    }

    /**
     * @return the Unicode string (in Maestro font) for the given AccidentalKind
     */
    public String getString() {
        String m = "";
        switch (this) {
            case doubleFlat:
                m = "\uF0BA";
                break;
            case flat:
                m = "\uF062";
                break;
            case sharp:
                m = "\uF023";
                break;
            case doubleSharp:
                m = "\uF0DC";
                break;
		case natural:
			break;
		default:
			break;
        }
        return m;
    }

    public int modifier() {
        switch (this) {
            case doubleFlat:
                return -2;
            case flat:
                return -1;
            case natural:
                return 0;
            case sharp:
                return 1;
            case doubleSharp:
                return 2;
        }
        return -100;
    }

}
