package net.weath.musicutil;

/**
 *
 * @author weath
 */
public class PhantomPitch extends Pitch {
    
    public PhantomPitch(Pitch base) {
        super(base);
        this.note = PhantomNote.forNote(Note.forMidiNumber(base.getNumber()));
    }
    
    @Override
    public PhantomPitch plus(Interval n) {
        Pitch p = super.plus(n);
        PhantomPitch pp = new PhantomPitch(p);
        return pp;
    }
}
