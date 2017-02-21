package net.weath.musicutil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Map a String to an ArrayList of Chords
 *
 * @author weath
 *
 */
public class MultiMap {

    /**
     * the hash map
     */
    private final HashMap<String, ArrayList<Chord>> map;

    /**
     * Construct an empty MultiMap
     */
    public MultiMap() {
        map = new HashMap<>();
    }

    /**
     * Add the given Chord to the map under the given String
     *
     * @param key a String
     * @param c a Chord to add
     */
    public void add(String key, Chord c) {
        ArrayList<Chord> a = map.get(key);
        if (a == null) {
            a = new ArrayList<>();
        }
        if (!a.contains(c)) {
            a.add(c);
        }
        map.put(key, a);
    }

    /**
     * Return a (possibly empty) ArrayList of Chords
     *
     * @param key the key to look up, a String
     * @return an ArrayList of Chords
     */
    public ArrayList<Chord> get(String key) {
        ArrayList<Chord> a = map.get(key);
        if (a == null) {
            a = new ArrayList<>();
        }
        return a;
    }

    /**
     * Return true if this MultiMap contains the given key
     *
     * @param key a String
     * @return true if there are any Chords with that key in this map
     */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * @return all the Chords in this map as a single ArrayList
     */
    public ArrayList<Chord> values() {
        ArrayList<Chord> a = new ArrayList<>();
        for (ArrayList<Chord> l : map.values()) {
            a.addAll(l);
        }
        return a;
    }
}
