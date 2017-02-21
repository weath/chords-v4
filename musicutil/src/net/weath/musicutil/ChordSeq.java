package net.weath.musicutil;

import java.util.ArrayList;
import java.util.List;

/**
 * Sequence of Items (Chords at particular MIDI ticks)
 *
 * @author weath
 *
 */
public class ChordSeq {

    /**
     * the list of Items
     */
    private final List<Item> list = new ArrayList<>();

    /**
     * the current Item index
     */
    private int cursor;

    /**
     * create a new ChordSeq
     */
    public ChordSeq() {

    }

    /**
     * Insert an Item into the sequence, maintaining the sort order
     *
     * @param e an Item to insert
     */
    public void insert(Item e) {
        int i = 0;
//		if (e.getPitches().length == 0)
//			return;
        while (i < list.size() && list.get(i).getTick() < e.getTick()) {
            i++;
        }
        if (i < list.size()) {
            if (list.get(i).getTick() == e.getTick()) {
                System.err.println("dup tick at " + e.getTick());
            }
        }
        if (i > 0) {
            if (list.get(i - 1).getTick() == e.getTick()) {
                System.err.println("dup tick at " + e.getTick());
            }
        }
        list.add(i, e);
    }

    /**
     * @return the length of the sequence
     */
    public int length() {
        return list.size();
    }

    /**
     * Set the current position
     *
     * @param i the new current position
     */
    public void setCursor(int i) {
        cursor = i;
    }

    /**
     * @return the current index
     */
    public int getCursor() {
        return cursor;
    }

    /**
     * @return the next Item and increment the current index (no error check!)
     */
    public Item next() {
        return list.get(cursor++);
    }

    /**
     * @return the previous Item and decrement the current index (no error
     * check!)
     */
    public Item prev() {
        return list.get(--cursor);
    }

    /**
     * Return the whole list of Items
     *
     * @return the list of Items
     */
    public List<Item> getItems() {
        return list;
    }

    /**
     * Return the Item at the given index
     *
     * @param index the item number (0-based)
     * @return the Item at that index
     * @throws IllegalArgumentException if index is out of bounds
     */
    public Item get(int index) {
        if (index < 0 || index >= list.size()) {
            throw new IllegalArgumentException("Out of range: " + index
                    + " (min = 0, max = " + (list.size() - 1) + ")");
        }
        return list.get(index);
    }
}
