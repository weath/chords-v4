package net.weath.musicutil;

import java.util.HashMap;

/**
 *
 * @author weath
 */
public class PhantomNote extends Note {
    private static final HashMap<Note, PhantomNote> map = new HashMap<>();
    
    public static final PhantomNote pAbb = new PhantomNote();
    public static final PhantomNote pAb  = new PhantomNote();
    public static final PhantomNote pA   = new PhantomNote();
    public static final PhantomNote pAs  = new PhantomNote();
    public static final PhantomNote pAx  = new PhantomNote();
    public static final PhantomNote pBbb = new PhantomNote();
    public static final PhantomNote pBb  = new PhantomNote();
    public static final PhantomNote pB   = new PhantomNote();
    public static final PhantomNote pBs  = new PhantomNote();
    public static final PhantomNote pBx  = new PhantomNote();
    public static final PhantomNote pCbb = new PhantomNote();
    public static final PhantomNote pCb  = new PhantomNote();
    public static final PhantomNote pC   = new PhantomNote();
    public static final PhantomNote pCs  = new PhantomNote();
    public static final PhantomNote pCx  = new PhantomNote();
    public static final PhantomNote pDbb = new PhantomNote();
    public static final PhantomNote pDb  = new PhantomNote();
    public static final PhantomNote pD   = new PhantomNote();
    public static final PhantomNote pDs  = new PhantomNote();
    public static final PhantomNote pDx  = new PhantomNote();
    public static final PhantomNote pEbb = new PhantomNote();
    public static final PhantomNote pEb  = new PhantomNote();
    public static final PhantomNote pE   = new PhantomNote();
    public static final PhantomNote pEs  = new PhantomNote();
    public static final PhantomNote pEx  = new PhantomNote();
    public static final PhantomNote pFbb = new PhantomNote();
    public static final PhantomNote pFb  = new PhantomNote();
    public static final PhantomNote pF   = new PhantomNote();
    public static final PhantomNote pFs  = new PhantomNote();
    public static final PhantomNote pFx  = new PhantomNote();
    public static final PhantomNote pGbb = new PhantomNote();
    public static final PhantomNote pGb  = new PhantomNote();
    public static final PhantomNote pG   = new PhantomNote();
    public static final PhantomNote pGs  = new PhantomNote();
    public static final PhantomNote pGx  = new PhantomNote();
   
    private PhantomNote() {
        super();
        this.ordinal -= 35;
        map.put(Note.lookup(this.toString()), this);
    }
    
    public static PhantomNote forNote(Note note) {
        return map.get(note);
    }
    
    @Override
    public boolean isReal() {
        return false;
    }
}
