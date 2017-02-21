package net.weath.chords;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.TreeMap;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import net.weath.musicutil.Chord;
import net.weath.musicutil.Pitch;
import net.weath.musicxml.VoicePart;

public class ChordPlayer implements IChordViewer {

    /**
     * enable debug output
     */
    private final static boolean debug = true;

    /**
     * the Thread playing the current chord
     */
    private Thread t;

    /**
     * the Synthesizer makes music
     */
    private Synthesizer synth;

    /**
     * the Soundbank contains Instruments
     */
    private Soundbank soundbank;

    /**
     * array of available Instruments
     */
    private Instrument[] instruments;

    /**
     * selected Instrument (bank#, program#)
     */
    private Patch patch;

    /**
     * selected Instrument (index in instruments[] array
     */
    private int curInstrument = 0;

    /**
     * millis to sleep between notes of a chord
     */
    private static int sleepTime = -1;

    /**
     * map of mute status for each voice part
     */
    private TreeMap<VoicePart, Boolean> muteMap;

    /**
     * should we use JI or not?
     */
    private boolean just = true;

    private HashMap<InstrumentListener, InstrumentListener> listeners = new HashMap<>();

    public ChordPlayer(ChordModel model, TreeMap<VoicePart, Boolean> muteMap) {
        model.addListener(this);
        this.muteMap = muteMap;
        if (sleepTime < 0) {
            String s;
            s = getResource("delay");
            sleepTime = Integer.parseInt(s);
        }
        try {
            synth = MidiSystem.getSynthesizer();
            if (!synth.isOpen()) {
                synth.open();
            }
            // synth.getDeviceInfo();
            // synth.getAvailableInstruments();
            // synth.getLoadedInstruments();
            System.err.println("Default soundbank: " + synth.getDefaultSoundbank());

            try {
                String soundbankFile = null;
                try {
                    soundbankFile = getResource("soundbank");
                } catch (MissingResourceException e) {
                    // ignore
                }
                if (soundbankFile == null || soundbankFile.length() == 0) {
                    if (debug) {
                        System.err
                                .println("Missing soundbank(" + soundbankFile + "), falling back to default");
                    }
                    soundbank = synth.getDefaultSoundbank();
//					soundbankFile = System.getProperty("java.home")
//							+ "/lib/audio/soundbank.gm";
                } else {
                    if (!new File(soundbankFile).exists()) {
                        System.err.println("Cannot locate soundbank file "
                                + soundbankFile + "\n {java.home} = "
                                + System.getProperty("java.home"));
                        System.err.println("Sound will be disabled.");
                    } else {
                        soundbank = MidiSystem
                                .getSoundbank(new File(soundbankFile));
                    }
                }
            } catch (InvalidMidiDataException | IOException e1) {
                e1.printStackTrace();
            }
            // Patch[] patchList = new Patch[1];
            // patchList[0] = new Patch(0,0);
            // synth.loadInstruments(soundbank, patchList);
            if (soundbank != null) {
                System.err.println("Using soundbank: " + (soundbank = synth.getDefaultSoundbank()));
                synth.loadAllInstruments(soundbank);
                System.err.println("soundbank supported: " + synth.isSoundbankSupported(soundbank));
                boolean bInstrumentsLoaded = synth.loadAllInstruments(soundbank);
                System.err.println("Instruments loaded: " + bInstrumentsLoaded);
                String inst = "0,0";
                try {
                    inst = getResource("instrument");
                } catch (MissingResourceException e) {
                    // ignore
                }
                String[] s = inst.split(",");
                int bank = Integer.parseInt(s[0]);
                int instNum = Integer.parseInt(s[1]);
                patch = new Patch(bank, instNum);
                instruments = synth.getLoadedInstruments();
                for (int i = 0; i < instruments.length; i++) {
                    Patch curPatch = instruments[i].getPatch();
                    if (curPatch.getBank() == bank && curPatch.getProgram() == instNum) {
                        curInstrument = i;
                        break;
                    }
                }
                MidiDevice.Info info = synth.getDeviceInfo();
                System.err.println(info.toString());
                System.err.println(info.getVersion());
                System.err.println(info.getDescription());
                System.err.println("max poly = " + synth.getMaxPolyphony());
                System.err.println("channels = " + synth.getChannels().length);
                System.err.println("soundbank = " + soundbank.getName() + ": " + soundbank.getVersion());
            }
        } catch (MidiUnavailableException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void chordChanged(final ChordChangeEvent event) {
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
        t = new Thread(() -> {
            player(event);
        });
        t.start();
    }

    /**
     * Play the chord
     */
    private synchronized void player(ChordChangeEvent event) {
        if (patch == null) {
            return;
        }
        just = event.isJust();
        Pitch[] pitches = event.getPitches();
        if (pitches == null) {
            return;
        }
        MidiChannel[] chan = synth.getChannels();
        System.arraycopy(chan, 10, chan, 9, 6); // don't use MIDI channel 10!
        for (MidiChannel chan1 : chan) {
            chan1.allNotesOff();
        }
        for (int i = 0; i < chan.length && i < pitches.length; i++) {
            if (!pitches[i].isReal())
                continue;
            if (pitches[i].getVoicePart() != null) {
                Boolean muted = muteMap.get((VoicePart) pitches[i].getVoicePart());
                if (muted != null && muted) {
                    continue;
                }
            }
            chan[i].programChange(patch.getBank(), patch.getProgram());
            if (just) {
                int bend = 8192 + pitches[i].getPitchBend();
                chan[i].setPitchBend(bend);
                System.err.println("" + pitches[i] + " (" + ((bend - 8192 >= 0) ? "+" : "") + (bend - 8192) + " pbu)");
            } else {
                chan[i].setPitchBend(8192);
                System.err.println("" + pitches[i].asET());
            }
            if (pitches[i] != null) {
                chan[i].noteOn(pitches[i].getNumber(), 100);
//				System.err.println("note on, chan = " + i);
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        for (int i = 0; i < chan.length && i < pitches.length; i++) {
            chan[i].setPitchBend(8192);
            if (pitches[i] != null) {
//				System.err.println("note off, chan = " + i);
                chan[i].noteOff(pitches[i].getNumber());
            }
        }
//		System.err.println("------------------------------");
    }

    public static String getResource(String name) {
        return Chord.getResource(name);
    }

    public boolean isJust() {
        return just;
    }

    public void setJust(boolean just) {
        this.just = just;
    }

    public static int getSleepTime() {
        return sleepTime;
    }

    public static void setSleepTime(int sleepTime) {
        ChordPlayer.sleepTime = sleepTime;
    }

    public void nextInstrument() {
        curInstrument++;
        if (curInstrument == instruments.length) {
            curInstrument = 0;
        }
        patch = instruments[curInstrument].getPatch();
        System.err.println(instruments[curInstrument]);
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
        notifyListeners();
    }

    public void prevInstrument() {
        curInstrument--;
        if (curInstrument < 0) {
            curInstrument = instruments.length - 1;
        }
        patch = instruments[curInstrument].getPatch();
        System.err.println(getCurInstName());
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
        notifyListeners();
    }

    synchronized private void notifyListeners() {
        for (InstrumentListener listener : listeners.keySet()) {
            listener.instrumentChanged(getCurInstName());
        }
    }

    private final static int startIndex = "Instrument: ".length();

    private String getCurInstName() {
        String s = instruments[curInstrument].toString();
        return s.substring(startIndex);
    }

    synchronized public void addInstrumentListener(InstrumentListener instListener) {
        listeners.put(instListener, instListener);
        instListener.instrumentChanged(getCurInstName());
    }

    synchronized public void removeInstrumentListener(InstrumentListener instListener) {
        listeners.remove(instListener);
    }

    public Instrument[] getInstruments() {
        return instruments;
    }

    public void setInstrument(int p) {
        curInstrument = p;
        patch = instruments[curInstrument].getPatch();
        System.err.println(instruments[curInstrument]);
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
        notifyListeners();
    }
}
