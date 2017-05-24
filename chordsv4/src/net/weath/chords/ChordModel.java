package net.weath.chords;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.event.EventListenerList;

import net.weath.musicutil.Chord;
import net.weath.musicutil.ChordSeq;
import net.weath.musicutil.Interval;
import net.weath.musicutil.Item;
import net.weath.musicutil.Note;
import net.weath.musicutil.PhantomPitch;
import net.weath.musicutil.Pitch;
import net.weath.musicutil.RatioSet;
import net.weath.musicutil.RootlessChord;
import net.weath.musicxml.MBT;
import net.weath.musicxml.MeasureData;
import net.weath.musicxml.MeasureMap;
import net.weath.musicxml.VoicePart;

public class ChordModel {

    private final ErrMsg errMsg;

    private boolean suppressEvent;

    // a set of pitches, may be zero-length
    private Pitch[] pitches = new Pitch[0];
    
    // did the user specify pitch bends?
    private boolean hasUserBends = false;

    // the possible interpretations
    private ChordInstance[] instances = new ChordInstance[0];
    
    // an overriding RatioSet from the UI
    private RatioSet ratioSet = null;

    // the index of the selected chord in the instances[] array, or -1
    private int selection = -1;

    // should we use Just Intonation?
    private boolean just = false;

    // which note of the chord should we tune to?
    private ArrayList<TuningSpec> tuningSpec = null;

    // where we got the pitches
    private Origin origin;

    private SequencePlayer player = null;

    private final EventListenerList listenerList = new EventListenerList();

    public ChordModel(ErrMsg errMsg) {
        this.errMsg = errMsg;
    }

    public Pitch[] getPitches() {
        return pitches.clone();
    }

    public void setPitches(Origin origin, Pitch[] newPitches) {
        errMsg.setErrMsg(null);
        pitches = newPitches.clone();
        this.origin = origin;
        int oldSelection = selection;
        ChordInstance oldChord = (selection >= 0) ? instances[selection] : null;
        try {
            analyze();
        } catch (IllegalArgumentException ex) {
            errMsg.setErrMsg(ex.getMessage());
        }
        if (origin.equals(Origin.JustToggle)) {
            selection = oldSelection;
            if (selection >= 0) {
                assert(instances.length > selection);
                assert(instances[selection].equals(oldChord));
            }
        }
        fireChordChangeEvent();
    }
    
    public void setHasUserBends(boolean b) {
        this.hasUserBends = b;
    }
    
    public boolean hasUserBends() {
        return hasUserBends;
    }

    public int getSelection() {
        return selection;
    }

    public void setSelection(Origin origin, int selection) {
        this.origin = origin;
        if (origin == Origin.Tuning) {
            instances[selection] = new ChordInstance(pitches,
                    instances[selection].getChord(),
                    hasUserBends);
        }
        selectionChanged(selection);
        fireChordChangeEvent();
    }

    public Note getRoot() {
        if (instances.length == 0) {
            return null;
        }
        ChordInstance instance = instances[getSelection()];
        if (instance == null)
            return null;
        return instance.getRoot();
    }

    public boolean isJust() {
        return just;
    }

    public void setJust(boolean just) {
        this.just = just;
        setPitches(Origin.JustToggle, pitches);
    }

    private void analyze() {
        if (pitches.length > 15) {
            throw new IllegalArgumentException("Too many notes: "
                    + pitches.length);
        }
        if (pitches.length == 0) { // not a chord
            instances = new ChordInstance[0];
        } else {
            Chord[] chords = Chord.lookup(pitches, true).toArray(new Chord[0]);
            if (chords.length == 0) {
                chords = new Chord[]{new UnrecognizedChord()};
            }
            update(chords);
        }
        if (instances.length > 0)
            selectionChanged(0);
        else
            selectionChanged(-1);
    }

    private void update(Chord[] chords) {
        if (chords.length == 1 && chords[0] instanceof UnrecognizedChord) {
            return;
        }
        instances = new ChordInstance[chords.length];
        for (int i = 0; i < chords.length; i++) {
            ChordInstance instance = new ChordInstance(pitches, chords[i], hasUserBends);
            instances[i] = instance;
        }
    }

    private void selectionChanged(int selection) {
        this.selection = selection;
        setPitchBends();
    }

    public void addListener(ChordChangeListener listener) {
        listenerList.add(ChordChangeListener.class, listener);
    }

    public void removeListener(ChordChangeListener listener) {
        listenerList.remove(ChordChangeListener.class, listener);
    }

    protected void fireChordChangeEvent() {
        if (suppressEvent) {
            return;
        }
        ChordChangeEvent event = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChordChangeListener.class) {
                if (event == null) {
                    Note root = getRoot();
                    RatioSet rs = getRatioSet();
                    event = new ChordChangeEvent(pitches, instances, selection,
                            root, null, rs, just, hasUserBends, origin);
                }
                ChordChangeListener listener = (ChordChangeListener) listeners[i + 1];
                listener.chordChanged(event);
            }
        }
    }

    private RatioSet getRatioSet() {
        if (ratioSet != null)
            return ratioSet;
        if (instances.length == 0) {
            return null;
        }
        ChordInstance instance = instances[selection];
        if (instance == null)
            return null;
        return instance.getRatioSet();
    }

    private void setPitches(Origin origin, int... arg) {
        int n = arg.length;
        Pitch[] arr = new Pitch[n];
        for (int i = 0; i < n; i++) {
            arr[i] = new Pitch(arg[i]);
            System.err.print(arr[i].getNote().toString() + " ");
        }
        System.err.print("=> ");
        setPitches(origin, arr);
    }

    private static void playChordSeq(ChordModel m, ChordSeq chordSeq, MeasureMap measureMap) {
        for (Item item : chordSeq.getItems()) {
            System.err.print(new MBT(item.getTick(), measureMap).toString() + " " + item + " => ");
            m.setPitches(Origin.ChordSeq, item.getPitches());
        }
    }

    public static void main(String[] args) {
        TreeMap<VoicePart, Boolean> muteMap = new TreeMap<>();
        Chord.parseArgs(args);
        ChordModel model = new ChordModel(new ErrMsg());
        model.addListener(new ChordPrinter());
        boolean found = false;
        for (String arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg.endsWith(".xml")) {
                System.err.println("###### " + arg + " ######");
                MusicXmlReader xmlReader = new MusicXmlReader(arg, muteMap, false, false);
                ChordSeq chordSeq = xmlReader.parseMusicXml();
                MeasureMap measureMap = xmlReader.getMeasureMap();
                playChordSeq(model, chordSeq, measureMap);
                found = true;
            } else if (arg.startsWith(":")) {
                String symbol = arg.substring(1);
                try {
                    ChordSymbolParser parser = new ChordSymbolParser();
                    boolean res = parser.parse(symbol);
                    if (res) {
                        System.err.print(symbol + " => " + parser.getRoot() + parser.getChord());
                        Note bass = parser.getBass();
                        if (bass != null) {
                            System.err.print("/" + bass);
                        }
                        int inv = parser.getInversion();
                        if (inv != 0) {
                            System.err.print(" (inv = " + inv + ")");
                        }
                        System.err.println();
                    }
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                found = true;
            } else {
                NoteParser np = new NoteParser();
                System.err.print(arg + " => ");
                model.setPitches(Origin.Notes, np.parse(arg));
                found = true;
            }
        }
        if (!found) {
            model.setPitches(Origin.Main);
            model.setPitches(Origin.Main, 60);
            model.setPitches(Origin.Main, 55, 64, 72);
            System.err.print(" selection(1) => ");
            model.setSelection(Origin.Main, 1);
            model.setPitches(Origin.Main, 60, 65, 71, 73);
        }
    }

    public void setChord(Origin origin, Note bass, Note root, Chord chord) {
        this.origin = origin;
        selection = 0;
        pitches = chord.getPitches(root, 3);
        ChordInstance instance = new ChordInstance(pitches, chord, hasUserBends);
        instances = new ChordInstance[]{instance};
//	setPitchBends();
        fireChordChangeEvent();
    }

    /**
     * set the pitch bend values for the pitches
     */
    private void setPitchBends() {
        if (instances.length == 0) {
            return;
        }
        int sel = getSelection();
        if (sel < 0) {
            sel = 0;
        }
        ChordInstance instance = instances[sel];
        Chord chord = instance.getChord();
        if (chord == null) {
            return;
        }
        if (just) {
            Chord rootPosition = chord;
            RatioSet rs = rootPosition.getRatios();
            setRatios(rootPosition, rs);
            TuningSpec spec = getTuningSpec();
            if (spec != null) {
                int delta = 0;
                VoicePart vp = spec.getVoicePart();
                if (vp != null) {
                    // choose the note being sung by the given VoicePart at the current tick
                    int pb = 0;
                    Note note = null;
                    if (vp.voice == 0) { // not a true part/voice, just a note number
                        Pitch p = null;
                        if (vp.part < pitches.length) {
                            p = pitches[vp.part];
                            note = p.getNote();
                            pb = p.getPitchBend();
                            System.err.println("Tuning to " + note);
                        }
                    } else {
                        for (Pitch p : pitches) {
                            if (vp.equals(p.getVoicePart())) {
                                pb = p.getPitchBend();
                                note = p.getNote();
                                System.err.println("Tuning to " + note);
                                break;
                            }
                        }
                    }
                    if (note != null) {
                        // compute desired pitchbend based on current key
                        delta = getDelta(note, pb);
                    } else {
                        // note to tune to was not found; tune to the key
                        // first, pick a note; if lead is singing, use that
                        Pitch pitch = null;
                        for (Pitch p : pitches) {
                            vp = (VoicePart) p.getVoicePart();
                            if (vp != null && vp.part == 0 && vp.voice == 2) {
                                pitch = p;
                                break;
                            }
                        }
                        if (pitch == null) {
                            // next, if bass is singing, use that note
                            for (Pitch p : pitches) {
                                vp = (VoicePart) p.getVoicePart();
                                if (vp != null && vp.part == 1 && vp.voice == 2) {
                                    pitch = p;
                                    break;
                                }
                            }
                        }
                        if (pitch == null) {
                            // just pick the lowest (first) pitch
                            if (pitches != null && pitches.length > 0) {
                                pitch = pitches[0];
                            }
                        }
                        if (pitch != null) {
                            delta = getDelta(pitch.getNote(), pitch.getPitchBend());
                        }
                    }
                } else {
                    // not a VoicePart; must be a Note or just cents
                    Note note = spec.getNote();
                    if (note == null) {
                        // just a cents value; tune the root relative to E.T.
                        Note root = instance.getRoot();
                        for (Pitch p : pitches) {
                            if (p.getNote().equals(root)) {
                                delta = spec.getPitchBend();
                                delta += p.getPitchBend();
                                break;
                            }
                        }
                    } else {
                        // find the note in the chord
                        boolean found = false;
                        for (Pitch p : pitches) {
                            if (p.getNote().equals(note)) {
                                delta = getDelta(p.getNote(), p.getPitchBend());
                                delta += spec.getPitchBend();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            errMsg.setErrMsg("unable to locate " + note + " in chord!");
                        }
                    }
                }
                System.err.println("Delta = " + delta);
                for (Pitch p : pitches) {
                    p.addBend(delta);
                }
            }
        } else {
            for (Pitch p : pitches) {
                p.setBend(0);
            }
        }
    }

    private TuningSpec getTuningSpec() {
        if (tuningSpec == null || tuningSpec.isEmpty()) {
            return null;
        }
        int tick = player.getTick();
        int n = 0;
        for (; n < tuningSpec.size(); n++) {
            if (tuningSpec.get(n).getStartTick() > tick) {
                n--;
                break;
            }
        }
        if (n == tuningSpec.size()) {
            n--; // use the last one
        }
        TuningSpec spec = tuningSpec.get(n);
        System.err.println("using " + spec);
        return spec;
    }

    public void setTuningSpec(ArrayList<TuningSpec> tuningSpec) {
        this.tuningSpec = tuningSpec;
        setPitchBends();
        fireChordChangeEvent();
    }

    /**
     * set the pitch bend values from the given RatioSet
     */
    private void setRatios(Chord chord, RatioSet rs) {
//		if (haveUserBends)
//			return;
        if (rs == null) {
            rs = chord.getRatios();
        }
        if (rs.isDummy()) {
            return;
        }
        ratioSet = rs;
        ChordInstance instance = instances[selection];
        Note root = instance.getRoot();
        Pitch[] pArr = pitches.clone();
        if (chord instanceof RootlessChord) {
            RootlessChord rc = (RootlessChord) chord;
            pArr = rc.insertRootPitch(pArr);
        }
        int[] factor = new int[pArr.length];
        int fudgeFactor = 1;
        for (int i = 0; i < pArr.length; i++) {
            Pitch p = pArr[i];
            Pitch[] chordPitches = chord.getPitches(root, pArr[0].getOctave());
            if (chord instanceof RootlessChord) {
                RootlessChord rc = (RootlessChord) chord;
                chordPitches = rc.insertRootPitch(chordPitches);
            }
            // find the right factor for this pitch
            for (Pitch cp : chordPitches) {
                if (cp.equalsET(p)) {
                    // easy case
                    int j = findIndex(cp, chordPitches);
                    factor[i] = rs.value(j) * fudgeFactor;
                    break;
                }
            }
            int octdiff = 0;
            if (factor[i] == 0) {
                // no exact match; try octave
                for (Pitch cp : chordPitches) {
                    if (cp.getNote().equals(p.getNote())) {
                        octdiff = p.getOctave() - cp.getOctave();
                        while (octdiff < 0) {
                            octdiff++;
                            fudgeFactor *= 2;
                            for (int k = 0; k < i; k++) {
                                factor[k] *= 2;
                            }
                        }
                        int j = findIndex(cp, chordPitches);
                        factor[i] = (int) (rs.value(j) * Math.pow(2.0, octdiff)) * fudgeFactor;
                        break;
                    }
                }
            }
            if (factor[i] == 0) {
                // hmm, maybe we need to try enharmonic spellings AND octave diff...
                for (Pitch cp : chordPitches) {
                    if (cp.getNote().getNumber() == p.getNote().getNumber()) {
                        octdiff = p.getOctave() - cp.getOctave();
                        while (octdiff < 0) {
                            octdiff++;
                            fudgeFactor *= 2;
                            for (int k = 0; k < i; k++) {
                                factor[k] *= 2;
                            }
                        }
                        int j = findIndex(cp, chordPitches);
                        factor[i] = (int) (rs.value(j) * Math.pow(2.0, octdiff)) * fudgeFactor;
                        break;
                    }
                }
            }
            if (factor[i] == 0) {
                System.err.println("Cannot compute factor for " + p);
                continue;
            }
            if (factor[0] == 0) {
                return;
            }
            if (i > 0 && factor[i] <= factor[i - 1]) {
                while (factor[i] <= factor[i - 1]) {
                    factor[i] *= 2;
                }
            }
            double d = factor[i];
            double c = factor[0] * Math.pow(2.0, octdiff);
            double cents = 1200.0 * Math.log(d / c) / Math.log(2.0);
            if (i > 0) {
                cents -= p.minus(pArr[0]).semitones() * 100.0;
            }
            while (cents > 100.0) {
                cents -= 1200.0;
            }
            while (cents < -100.0) {
                cents += 1200.0;
            }
            if (Math.abs(cents) >= 200.0) {
                System.err.println("bad pitch bend: " + cents + " for " + p);
                cents = 0;
            }
            p.setBend((int) (cents * 40.96));
        }
    }

    private int findIndex(Pitch p, Pitch[] pitches) {
        for (int i = 0; i < pitches.length; i++) {
            if (pitches[i].equals(p)) {
                return i;
            }
        }
        return -1;
    }

    public void setPlayer(SequencePlayer player) {
        this.player = player;
    }

    private int getDelta(Note note, int pb) {
        int delta;
        int tick = player.getTick();
        int fifths = 0;
        String mode = "major";
        MeasureMap measureMap = player.getMeasureMap();
        if (measureMap != null) {
            MeasureData m = measureMap.getMeasureAt(tick);
            fifths = m.getFifths();
            mode = m.getMode();
        }
        Note keyNote = getKey(fifths, mode);
        Interval i = Interval.between(keyNote, note);
        double ratio = i.getJIratio();
        double etRatio = i.getETratio();
        double pRatio = ratio / etRatio;
        delta = (int) Math.round(4096 * 12 * Math.log(pRatio) / Math.log(2.0));
        System.err.println("delta = " + delta + ", pb = " + pb + ", new delta = " + (pb - delta));
        delta -= pb;
        return delta;
    }

    public void setRoot(Origin origin, Note n) {
        Note root;
        Pitch rootPitch;
        if (instances.length > 0) {
            ChordInstance instance = instances[getSelection()];
            rootPitch = instance.getChord().getRootPitch(instance.getRoot(), pitches);
            root = rootPitch.getNote();
        } else {
            root = Note.C;
            suppressEvent = true;
            setPitches(origin, 60, 64, 67);
            rootPitch = pitches[0];
            suppressEvent = false;
        }
        Interval v = Interval.between(root, n);
        if (v.isEquivalentTo(Interval.P1)) {
            if (n.equals(root)) {
                // nothing to do, just re-notify
                fireChordChangeEvent();
            } else {
                // enharmonic spelling; respell the chord
                Pitch[] pArr = new Pitch[pitches.length];
                System.arraycopy(pitches, 0, pArr, 0, pitches.length);
                Chord c = instances[getSelection()].getChord();
                boolean success = true;
                outer:
                for (Pitch p : pArr) {
                    Note[] aliases = p.getNote().getAliases();
                    v = c.getIntervalForPitch(rootPitch, p);
                    for (Note note : aliases) {
                        if (Interval.between(n, note).differsByOctaveWith(v)) {
                            p = p.respell(note);
                            continue outer;
                        }
                    }
                    success = false;
                }
                if (success) {
                    setPitches(origin, pArr);
                } else {
                    errMsg.setErrMsg("Couldn't respell chord");
                    fireChordChangeEvent();
                }
            }
            return;
        }
        boolean up = v.basicSize() < 5;
        if (!up) {
            v = v.invert();
        }
        transpose(origin, v, up);
    }

    public void setChord(Origin origin, Chord chord) {
        Note n;
        if (instances.length == 0 || getSelection() < 0) {
            n = Note.C;
        } else {
            ChordInstance instance = instances[getSelection()];
            n = instance.getPitches()[instance.getRootIndex()].getNote();
        }
        Pitch[] pArr = chord.getPitches(n, 3);
        setPitches(origin, pArr);
    }

    public void setInversion(Origin origin, int n) {
        Chord c;
        Note root;
        int octave = 3;
        if (instances.length == 0) {
            c = Chord.lookup("");
            pitches = c.getPitches(Note.C, 3);
            root = Note.C;
        } else {
            ChordInstance instance = instances[getSelection()];
            c = instance.getChord();
            root = instance.getRoot();
        }
        Pitch[] pArr = c.getPitches(root, octave);
        Pitch[] result = new Pitch[pArr.length];
        if (n >= pArr.length) {
            errMsg.setErrMsg("No such inversion: " + n);
            return;
        }
        for (int i = 0; i < pArr.length; i++) {
            result[i] = pArr[(i + n) % pArr.length];
            if (i > 0 && result[i].getNumber() <= result[i-1].getNumber()) {
                while (result[i].getNumber() <= result[i-1].getNumber()) {
                    result[i].plusOctave();
                }
            }
        }        
        suppressEvent = true;
        setPitches(origin, result);
        Pitch newRoot = pitches[instances[getSelection()].getRootIndex()];
        if (newRoot.getOctave() > octave) {
            transpose(origin, Interval.P8, false);
        }
        suppressEvent = false;
        fireChordChangeEvent();
    }

    public void transpose(Origin origin, Interval n, boolean up) {
        Pitch[] pArr = new Pitch[pitches.length];
        for (int i = 0; i < pArr.length; i++) {
            if (up) {
                pArr[i] = pitches[i].plus(n);
            } else {
                pArr[i] = pitches[i].minus(n);
            }
            if (!pitches[i].isReal()) {
                pArr[i] = new PhantomPitch(pArr[i]);
            }
        }
        adjustSpelling(pArr);
        setPitches(origin, pArr);
    }

    public void play() {
        // just rebroadcast the same chord change event
        fireChordChangeEvent();
    }

    private void adjustSpelling(Pitch[] pArr) {
        for (int i = 1; i < pArr.length; i++) {
            Interval n1 = pArr[i].minus(pArr[0]);
            Interval n2 = pitches[i].minus(pitches[0]);
            if (!n1.id().equals(n2.id())) {
                // adjust the note to get the same interval as in the pitches[] array
                pArr[i].enharmonicallyRespell(n1.basicSize() < n2.basicSize());
            }
        }
    }

    private Note getKey(int fifths, String mode) {
        Note n = null;
        if (mode == null || mode.equals("major")) {
            switch (fifths) {
                case -7:
                    return Note.Cb;
                case -6:
                    return Note.Gb;
                case -5:
                    return Note.Db;
                case -4:
                    return Note.Ab;
                case -3:
                    return Note.Eb;
                case -2:
                    return Note.Bb;
                case -1:
                    return Note.F;
                case 0:
                    return Note.C;
                case 1:
                    return Note.G;
                case 2:
                    return Note.D;
                case 3:
                    return Note.A;
                case 4:
                    return Note.E;
                case 5:
                    return Note.B;
                case 6:
                    return Note.Fs;
                case 7:
                    return Note.Cs;
            }
        } else { // minor
            switch (fifths) {
                case -7:
                    return Note.Ab;
                case -6:
                    return Note.Eb;
                case -5:
                    return Note.Bb;
                case -4:
                    return Note.F;
                case -3:
                    return Note.C;
                case -2:
                    return Note.G;
                case -1:
                    return Note.D;
                case 0:
                    return Note.A;
                case 1:
                    return Note.E;
                case 2:
                    return Note.B;
                case 3:
                    return Note.Fs;
                case 4:
                    return Note.Cs;
                case 5:
                    return Note.Gs;
                case 6:
                    return Note.Ds;
                case 7:
                    return Note.As;
            }
        }
        return n;
    }

    public void addErrorMessageListener(ErrorMessageListener listener) {
        errMsg.addListener(listener);
    }

    public void setRatios(Origin origin, Chord chord, RatioSet ratioSet) {
        setRatios(chord, ratioSet);
        this.origin = origin;
        fireChordChangeEvent();
    }
}
