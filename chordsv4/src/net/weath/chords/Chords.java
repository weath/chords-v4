package net.weath.chords;

import static net.weath.musicutil.Interval.M2;
import static net.weath.musicutil.Interval.M3;
import static net.weath.musicutil.Interval.M6;
import static net.weath.musicutil.Interval.M7;
import static net.weath.musicutil.Interval.P4;
import static net.weath.musicutil.Interval.P5;
import static net.weath.musicutil.Interval.P8;
import static net.weath.musicutil.Interval.d5;
import static net.weath.musicutil.Interval.m2;
import static net.weath.musicutil.Interval.m3;
import static net.weath.musicutil.Interval.m6;
import static net.weath.musicutil.Interval.m7;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.sound.midi.Instrument;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import net.weath.musicutil.AccidentalKind;
import net.weath.musicutil.Chord;
import net.weath.musicutil.ChordSeq;
import net.weath.musicutil.Interval;
import net.weath.musicutil.KeyboardPanel;
import net.weath.musicutil.Note;
import net.weath.musicutil.RootlessChord;
import net.weath.musicxml.MeasureData;
import net.weath.musicxml.MeasureMap;
import net.weath.musicxml.VoicePart;

/**
 * Analyze and play chords, in ET or JI.
 *
 * No warranty of any kind. Free for non-commercial use.
 *
 * @author weath@weath.net
 *
 * Originally created 29 April 2007 Current version 15 February 2012
 */
public class Chords {

    /*
	 * The ChordModel is manipulated by Controllers, which
	 * implement IChordController, and displayed by Viewers,
	 * which implement IChordViewer. This class creates the
	 * various Viewers and Controllers and displays them in
	 * one frame.
     */
    private ErrMsg errMsg = new ErrMsg();

    private ChordModel model = new ChordModel(errMsg);

    private ChordSymbolParser symParser = new ChordSymbolParser();

    private JFrame frame;

    private Font maestro;

    private Font arial;

    private static Font defaultFont;

    private Font arialBold;

    private Color black = new Color(0, 0, 0);

    private Color gray = new Color(240, 240, 240);

    private SymbolText symbolText;

    private NotesText notesText;

    private JToggleButton equalTemp;

    private JToggleButton just;

    private TreeMap<VoicePart, Boolean> muteMap = new TreeMap<>();

    private ChordPlayer chordPlayer = new ChordPlayer(model, muteMap);

    private KeyboardAdapter keyboardAdapter = new KeyboardAdapter(model);

    private KeyboardPanel keyboardPanel;

    private MainCanvas canvas;

    private ChordListPanel chordList;

    private NotePanel notePanel;

    private JPanel bottomPanel = new JPanel();

    private JMenuBar menuBar;

    private JFileChooser fileChooser = new JFileChooser(new File("."));

    private SequencePlayer player = new SequencePlayer(model, errMsg);

    private JButton back;

    private JButton forw;

    private JTextField tickField;

    private JSpinner tempo;

    private JLabel tuneToLabel;

    private JTextField tuneTo;

    private JLabel ratiosLabel;

    private Ratios ratios;

    private JLabel instrument;

    private ArrayList<TuningSpec> tuningSpec = new ArrayList<>();

    private MeasureMap measureMap;

    private JMenu muteMenu;

    private JMenu soloMenu;

    private Instrument[] instruments;

    private static Interval[] intervals = {
        m2, M2, m3, M3, P4, d5, P5, m6, M6, m7, M7, P8
    };

    private FooterCanvas footer;

    private MusicXmlReader musicXmlReader;

    private boolean swing8;

    private boolean swingdot;
    private JButton play;
    private JButton stop;

    public Chords(String[] args) {
        frame = new JFrame("Chords");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getFonts();
        createUI(args);
        frame.pack();
        model.addListener(new ChordPrinter());
        model.setPlayer(player);
    }

    public void setVisible(boolean b) {
        getFrame().setVisible(b);
    }

    private void createUI(String[] args) {
        boolean small = false;
        for (String arg : args) {
            if (arg != null && arg.equals("-s")) {
                small = true;
            }
        }
        JPanel topPanel = new JPanel();
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
        topPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(panel1);
        topPanel.add(panel2);
        getFrame().add(topPanel, BorderLayout.NORTH);

        JLabel lab = new JLabel("Chord symbol:");
        lab.setFont(getDefaultFont());
        panel1.add(lab);
        setSymbolText(new SymbolText(20, getErrMsg(), getSymParser(), getModel()));
        getSymbolText().setFont(getDefaultFont());
        panel1.add(getSymbolText());
        getSymbolText().setToolTipText("Enter a chord symbol: C7, Ab sus, etc.");

        lab = new JLabel("Notes:");
        lab.setFont(getDefaultFont());
        panel1.add(lab);
        setNotesText(new NotesText(25, getErrMsg(), getModel()));
        getNotesText().setFont(getDefaultFont());
        panel1.add(getNotesText());
        getNotesText().setToolTipText("<html>Enter a sequence of notes, from the bottom: C Eb G, eg.<br>"
                + "See Help for more options.</html>");

        setTuneToLabel(new JLabel("Tune to:"));
        getTuneToLabel().setFont(getDefaultFont());
        panel2.add(getTuneToLabel());
        getTuneToLabel().setEnabled(false);
        setTuneTo(new JTextField(20));
        getTuneTo().setFont(getDefaultFont());
        panel2.add(getTuneTo());
        getTuneTo().setToolTipText("Enter a voice ID, or a sequence of voice IDs and M:B:T values, to set the voice to tune to in JI mode");
        getModel().addListener((ChordChangeEvent event) -> {
            getTuneToLabel().setEnabled(getModel().isJust());
            getTuneTo().setEnabled(getModel().isJust());
        });
        getTuneTo().addActionListener((ActionEvent arg0) -> {
            System.err.println("tuneTo = " + getTuneTo().getText());
            parseTuneTo();
        });

        setRatiosLabel(new JLabel("   Ratios:"));
        getRatiosLabel().setFont(getDefaultFont());
        panel2.add(getRatiosLabel());
        getRatiosLabel().setEnabled(false);

        setRatios(new Ratios(getModel(), getRatiosLabel(), getErrMsg()));
        panel2.add(getRatios());

        panel2.add(new Tuning(getModel()));

        setPlay(new JButton("Play"));
        getPlay().setFont(getDefaultFont());
        getPlay().addActionListener((ActionEvent e) -> {
            getPlayer().start();
        });
        getPlay().setEnabled(false);
        setStop(new JButton("Stop"));
        getStop().setFont(getDefaultFont());
        getStop().addActionListener((ActionEvent e) -> {
            getPlayer().stop();
        });
        getStop().setEnabled(false);
        panel2.add(getPlay());
        panel2.add(getStop());

        setTempo(new JSpinner());
        getTempo().setFont(getDefaultFont());
        getTempo().setPreferredSize(new Dimension(80, 12));
        getTempo().addChangeListener((ChangeEvent arg0) -> {
            Object value = getTempo().getValue();
            setTempo((Integer) value);
        });
        lab = new JLabel("Tempo:");
        lab.setFont(getDefaultFont());
        panel2.add(lab);
        getTempo().setToolTipText("Enter the tempo (in quarter notes per minute) for playback");
        getTempo().setValue(60);
        getTempo().setEnabled(false);
        panel2.add(getTempo());

        setEqualTemp(new JRadioButton("E.T."));
        getEqualTemp().setFont(getDefaultFont());
        setJust(new JRadioButton("J.I."));
        getJust().setFont(getDefaultFont());
        panel2.add(getEqualTemp());
        panel2.add(getJust());
        getEqualTemp().setSelected(true);
        getJust().setSelected(false);
        getEqualTemp().setToolTipText("Select Equal Temperament");
        getJust().setToolTipText("Select Just Intonation");
        ButtonGroup group = new ButtonGroup();
        group.add(getEqualTemp());
        group.add(getJust());
        getEqualTemp().addActionListener((ActionEvent arg0) -> {
            getModel().setJust(false);
        });
        getJust().addActionListener((ActionEvent arg0) -> {
            getModel().setJust(true);
        });

        addSeqControls(panel1);

        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(1, 2);
        panel.setLayout(new BorderLayout());
        JPanel subPanel = new JPanel();
        subPanel.setLayout(gridLayout);
        setCanvas(new MainCanvas(getMaestro()));
        getModel().addListener(getCanvas());
        subPanel.add(getCanvas());
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        setChordList(new ChordListPanel(getModel()));
        rightPanel.add(getChordList(), BorderLayout.CENTER);
        setNotePanel(new NotePanel(getModel()));
        rightPanel.add(getNotePanel(), BorderLayout.SOUTH);
        subPanel.add(rightPanel);
        setKeyboardPanel(new KeyboardPanel(getKeyboardAdapter()));
        if (!small) {
            panel.add(getKeyboardPanel(), BorderLayout.NORTH);
        }
        getKeyboardAdapter().setKeyboardPanel(getKeyboardPanel());
        getModel().addListener(getKeyboardAdapter());
        getKeyboardPanel().setBorder(new BevelBorder(BevelBorder.RAISED));
        if (!small) {
            panel.add(subPanel, BorderLayout.CENTER);
        }
        getFrame().add(panel, BorderLayout.CENTER);
        getKeyboardPanel().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                super.componentResized(arg0);
                getKeyboardPanel().center();
            }
        });
        setFooter(new FooterCanvas(getMaestro(), getArial(), getErrMsg()));
        getModel().addListener(getFooter());
        getModel().addErrorMessageListener(getFooter());
        getFooter().setPreferredSize(new Dimension(400, 30));
        JPanel fPanel = new JPanel(new BorderLayout());
        fPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        fPanel.add(getFooter(), BorderLayout.CENTER);
        BoxLayout layout = new BoxLayout(getBottomPanel(), BoxLayout.X_AXIS);
        getBottomPanel().setLayout(layout);
        getBottomPanel().add(fPanel);
        JButton prev = new JButton("<");
        prev.setFont(getDefaultFont());
        getBottomPanel().add(prev);
        setInstrument(new JLabel());
        getInstrument().setFont(getDefaultFont());
        getBottomPanel().add(getInstrument());
        JButton next = new JButton(">");
        next.setFont(getDefaultFont());
        getBottomPanel().add(next);
        prev.addActionListener((ActionEvent arg0) -> {
            getChordPlayer().prevInstrument();
            getModel().play();
        });
        next.addActionListener((ActionEvent e) -> {
            getChordPlayer().nextInstrument();
            getModel().play();
        });
        getFrame().add(getBottomPanel(), BorderLayout.SOUTH);
        prev.setToolTipText("Select previous instrument for playback");
        next.setToolTipText("Select next instrument for playback");
        getInstrument().setToolTipText("Displays currently selected instrument");

        InstrumentListener instListener = (String name) -> {
            getInstrument().setText(name);
        };
        getChordPlayer().addInstrumentListener(instListener);

        setMenuBar(createMenuBar());
        getFrame().setJMenuBar(getMenuBar());
        getFrame().setTitle("Chords");
    }

    protected void setTempo(int value) {
        getPlayer().setTempo(value);
    }

    protected void parseTuneTo() {
        setErrMsg((String)null);
        String str = getTuneTo().getText().trim();
        int tick = 0;
        if (str.length() == 0) {
            getTuningSpec().clear();
        } else {
            getTuningSpec().clear();
            StringTokenizer st = new StringTokenizer(str);
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (s.startsWith("(")) {
                    String[] arr = s.split("[(/)]");
                    TuningSpec spec = new TuningSpec();
                    if (arr.length == 2) {
                        int part = Integer.parseInt(arr[1]);
                        spec.setStartTick(tick);
                        spec.setVoicePart(new VoicePart(part, 0));
                    } else {
                        int part = Integer.parseInt(arr[1]);
                        int voice = Integer.parseInt(arr[2]);
                        spec.setStartTick(tick);
                        spec.setVoicePart(new VoicePart(part, voice));
                    }
                    getTuningSpec().add(spec);
                } else if (s.matches("[0-9][0-9]*:[0-9][0-9]*:[0-9][0-9]*")) {
                    String[] arr = s.split(":");
                    int m = Integer.parseInt(arr[0]);
                    int b = Integer.parseInt(arr[1]);
                    int t = Integer.parseInt(arr[2]);
                    MeasureData meas = getMeasureMap().getMeasure(m);
                    tick = meas.getStart() + getTick(meas, b, t);
                } else if (s.startsWith("-") || s.startsWith("+")) {
                    double cents = Double.parseDouble(s);
                    TuningSpec spec = new TuningSpec();
                    spec.setStartTick(tick);
                    spec.setCents(cents);
                    getTuningSpec().add(spec);
                } else {
                    // must be a note name -- validate it!
                    double cents = 0.0;
                    String[] arr = s.split("[-+]");
                    if (arr.length == 2) { // has +cents or -cents modifier
                        int n = s.indexOf('-');
                        if (n < 0) {
                            n = s.indexOf('+');
                        }
                        String cString = s.substring(n);
                        cents = Double.parseDouble(cString);
                    }
                    try {
                        Note note = Note.lookup(arr[0]);
                        TuningSpec spec = new TuningSpec();
                        spec.setStartTick(tick);
                        spec.setNote(note);
                        spec.setCents(cents);
                        getTuningSpec().add(spec);
                    } catch (IllegalArgumentException e) {
                        setErrMsg(e.getMessage());
                    }
                }
            }
        }
        getModel().setTuningSpec(getTuningSpec());
    }

    /**
     * return the ticks from the beginning of the measure, given a beat and tick
     * offset
     *
     * @param meas
     * @param beat
     * @param tick
     * @return ticks from start of the measure
     */
    private int getTick(MeasureData meas, int beat, int tick) {
        int jiffies = meas.getJiffiesPerBeat();
        return jiffies * (beat - 1) + tick;
    }

    private void addSeqControls(JPanel panel) {
        setBack(new JButton("<"));
        getBack().setFont(getDefaultFont());
        setForw(new JButton(">"));
        getForw().setFont(getDefaultFont());
        setTickField(new JTextField());
        getTickField().setFont(defaultFont);
        getTickField().setColumns(6);
        getTickField().setHorizontalAlignment(JTextField.CENTER);
        panel.add(getBack());
        panel.add(getTickField());
        panel.add(getForw());
        panel.validate();
        getBack().addActionListener((ActionEvent e) -> {
            getPlayer().back();
        });
        getForw().addActionListener((ActionEvent e) -> {
            getPlayer().forward();
        });
//		tickField
//				.setToolTipText("Current position in sequence (measure:beat:tick)");
        getTickField().addActionListener((ActionEvent e) -> {
            getPlayer().setTick(getTickField().getText());
        });
        getPlayer().setTickField(getTickField());
        getPlayer().setTempoSpinner(getTempo());
        getTickField().setToolTipText("Enter a playback position as Measure : Beat : Tick");
        getForw().setToolTipText("Next chord in the sequence");
        getBack().setToolTipText("Previous chord in the sequence");
    }

    private class MyMenu extends JMenu {

        private MyMenu(String str) {
            super(str);
            setFont(getDefaultFont());
        }

    }

    private class MyMenuItem extends JMenuItem {

        private MyMenuItem(String str) {
            super(str);
            setFont(getDefaultFont());
        }

    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setFont(getDefaultFont());
        JMenu menu = new MyMenu("File");
        mb.add(menu);
        JMenuItem fileItem = new MyMenuItem("Open");
        fileItem.addActionListener((ActionEvent e) -> {
            openFile();
        });
        menu.add(fileItem);
        JMenu submenu = new MyMenu("Instrument");
        populateInstrumentMenu(submenu);
        menu.add(submenu);
        menu.addSeparator();
        JMenuItem exitItem = new MyMenuItem("Exit");
        exitItem.addActionListener((ActionEvent arg0) -> {
            System.exit(0);
        });
        menu.add(exitItem);
        menu = new MyMenu("Root");
        createRootMenu(menu);
        mb.add(menu);
        menu = new MyMenu("Type");
        createTypeMenu(menu);
        mb.add(menu);
        menu = new MyMenu("Inversion");
        createInversionMenu(menu);
        mb.add(menu);
        menu = new MyMenu("Transpose");
        createTransposeMenu(menu);
        mb.add(menu);
        menu = new MyMenu("Options");
        createOptionMenu(menu);
        mb.add(menu);
        menu = new MyMenu("Help");
        createHelpMenu(menu);
        mb.add(menu);
        return mb;
    }

    private void createTransposeMenu(JMenu menu) {
        JMenu submenu = new MyMenu("Up");
        menu.add(submenu);
        for (Interval n : getIntervals()) {
            final Interval fn = n;
            JMenuItem mi = new MyMenuItem(n.toString());
            mi.addActionListener((ActionEvent e) -> {
                transposeUp(fn);
            });
            submenu.add(mi);
        }
        submenu = new MyMenu("Down");
        menu.add(submenu);
        for (Interval n : getIntervals()) {
            final Interval fn = n;
            JMenuItem mi = new MyMenuItem(n.toString());
            mi.addActionListener((ActionEvent e) -> {
                transposeDown(fn);
            });
            submenu.add(mi);
        }
    }

    protected void transposeDown(Interval n) {
        getModel().transpose(Origin.Menus, n, false);
    }

    protected void transposeUp(Interval n) {
        getModel().transpose(Origin.Menus, n, true);
    }

    private String readHelpFile(String filename) {
        URL url = getClass().getResource("help/" + filename);
        StringBuilder sb = new StringBuilder();
        InputStreamReader rdr = null;
        try {
            rdr = new InputStreamReader(url.openStream());
            char[] buf = new char[1024];
            while (true) {
                int result = rdr.read(buf);
                if (result <= 0) {
                    break;
                }
                sb.append(buf, 0, result);
            }
        } catch (IOException e) {
            System.err.println("Cannot open " + filename + ": " + e.getMessage());
            return e.getMessage();
        } finally {
            if (rdr != null) {
                try {
                    rdr.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return sb.toString();
    }

    /**
     * @return
     */
    private JMenuItem createHelpMenuItem(String topic,
            final String dialogTitle, final String contents) {
        JMenuItem options = new MyMenuItem(topic);
        options.addActionListener((ActionEvent e) -> {
            HelpWindow w = new HelpWindow(dialogTitle, contents);
            w.setSize(new Dimension(600, 400));
            w.setVisible(true);
            w.scrollToTop();
        });
        return options;
    }

    /**
     * Create the Help menu
     */
    private void createHelpMenu(JMenu menu) {
        JMenuItem overview = createHelpMenuItem("Overview", "Overview",
                readHelpFile("Overview.html"));
        JMenuItem cmdline = createHelpMenuItem("Command Line",
                "Command Line Flags", readHelpFile("CommandLine.html"));
        JMenuItem syntax = createHelpMenuItem("Syntax", "Syntax Help",
                readHelpFile("Syntax.html"));
        JMenuItem options = createHelpMenuItem("Options", "Options Help",
                readHelpFile("Options.html"));
        JMenuItem about = createHelpMenuItem("About Chords", "About Chords",
                readHelpFile("About.html"));
        menu.add(overview);
        menu.add(cmdline);
        menu.add(syntax);
        menu.add(options);
        menu.add(about);
    }

    /**
     * create the Inversion menu
     */
    private void createInversionMenu(JMenu menu) {
        JMenuItem mi = new MyMenuItem("Root position");
        mi.addActionListener((ActionEvent e) -> {
            getModel().setInversion(Origin.Menus, 0);
        });
        menu.add(mi);
        for (int i = 1; i <= 7; i++) {
            mi = new MyMenuItem(Chord.ordinal(i) + " inversion");
            final int n = i;
            mi.addActionListener((ActionEvent e) -> {
                getModel().setInversion(Origin.Menus, n);
            });
            menu.add(mi);
        }
    }

    /**
     * create the (chord or interval) Type menu
     */
    private void createTypeMenu(JMenu menu) {
        ArrayList<ChordMenuItem> simpleIntervals = new ArrayList<>();
        ArrayList<ChordMenuItem> compoundIntervals = new ArrayList<>();
        JMenu intervalMenu = new MyMenu("Interval");
        menu.add(intervalMenu);
        JMenu simpleMenu = new MyMenu("Simple");
        intervalMenu.add(simpleMenu);
        JMenu compoundMenu = new MyMenu("Compound");
        intervalMenu.add(compoundMenu);
        JMenu majorMenu = new MyMenu("Major");
        menu.add(majorMenu);
        JMenu minorMenu = new MyMenu("Minor");
        menu.add(minorMenu);
        JMenu min7Menu = new MyMenu("Minor Seventh");
        menu.add(min7Menu);
        JMenu sevenMenu = new MyMenu("Dom. Seventh");
        sevenMenu.getPopupMenu().setLayout(new GridLayout(0, 2));
        menu.add(sevenMenu);
        JMenu maj7Menu = new MyMenu("Major Seventh");
        maj7Menu.getPopupMenu().setLayout(new GridLayout(0, 2));
        menu.add(maj7Menu);
        // JMenu augMenu = new MyMenu("Augmented");
        // menu.add(augMenu);
        // JMenu dimMenu = new MyMenu("Diminshed");
        // menu.add(dimMenu);
        JMenu otherMenu = new MyMenu("Other");
        otherMenu.getPopupMenu().setLayout(new GridLayout(0, 2));
        menu.add(otherMenu);
        for (Chord c : Chord.getAll()) {
            // filter out inversions
            if (c instanceof RootlessChord) {
                ChordMenuItem mi = new ChordMenuItem(c);
                switch (c.getName()) {
                    case "9 (no root)":
                        sevenMenu.add(mi);
                        continue;
                    case "m9 (no root)":
                        min7Menu.add(mi);
                        continue;
                    case "mM9 (no root)":
                        maj7Menu.add(mi);
                        continue;
                    case "M9 (no root)":
                        maj7Menu.add(mi);
                        continue;
                    default:
                        throw new IllegalArgumentException("Bad case: " + c.getName());
                }
            }
            // filter out doubly-aug/doubly-dim, triply-aug/triply-dim, etc.
            if (c.isInterval()
                    && c.getIntervals()[0].getQuality().modLevel() > 1) {
                continue;
            }
            ChordMenuItem mi = new ChordMenuItem(c);
            if (c.isInterval()) {
                Interval n = c.getIntervals()[0];
                if (n.basicSize() <= 8) {
                    simpleIntervals.add(mi);
                } else {
                    compoundIntervals.add(mi);
                }
            } else if (c.toString().indexOf(" (no 5)") > 0) {
                // omit (no 5) chords
            } else if (c.contains(m7) && c.contains(M3)) {
                sevenMenu.add(mi);
            } else if (c.contains(m7) && c.contains(m3)) {
                min7Menu.add(mi);
            } else if (c.contains(M7) && c.contains(M3)) {
                maj7Menu.add(mi);
            } else if (c.contains(M3) && c.contains(P5)) {
                majorMenu.add(mi);
            } else if (c.contains(m3) && c.contains(P5)) {
                minorMenu.add(mi);
            } else {
                otherMenu.add(mi);
            }
        }
        ChordMenuItem[] simpleArr = simpleIntervals
                .toArray(new ChordMenuItem[0]);
        ChordMenuItem[] compoundArr = compoundIntervals
                .toArray(new ChordMenuItem[0]);
        Arrays.sort(simpleArr, new ChordMenuItemComparator());
        Arrays.sort(compoundArr, new ChordMenuItemComparator());
        for (ChordMenuItem mi : simpleArr) {
            simpleMenu.add(mi);
        }
        for (ChordMenuItem mi : compoundArr) {
            compoundMenu.add(mi);
        }
    }

    /**
     * fill in the values of the Instrument menu
     */
    private void populateInstrumentMenu(JMenu submenu) {
        setInstruments(getChordPlayer().getInstruments());
        if (getInstruments().length == 0) {
            JMenuItem item = new MyMenuItem("(no soundbank)");
            submenu.add(item);
            return;
        }
        int nSubs = getInstruments().length / 32 + 1;
        JMenu[] sub = new MyMenu[nSubs];
        for (int i = 0; i < nSubs; i++) {
            int from = i * 32;
            int to = Math.min((i + 1) * 32 - 1, getInstruments().length - 1);
            sub[i] = new MyMenu(Integer.toString(from) + "-"
                    + Integer.toString(to));
            sub[i].getPopupMenu().setLayout(new GridLayout(0, 2));
            submenu.add(sub[i]);
        }
        int n = 0;
        for (Instrument inst : getInstruments()) {
            // System.err.println(inst.toString());
            JMenuItem item = new MyMenuItem("" + n + " " + inst.getName() + " ("
                    + inst.getPatch().getBank() + ","
                    + inst.getPatch().getProgram() + ")");
            final int p = n;
            item.addActionListener((ActionEvent e) -> {
                getChordPlayer().setInstrument(p);
                getModel().play();
            });
            sub[n++ / 32].add(item);
        }
    }

    /**
     * Create the Root menu
     */
    private void createRootMenu(JMenu menu) {
        for (Note nat : new Note[]{Note.A, Note.B, Note.C, Note.D, Note.E,
            Note.F, Note.G}) {

            JMenu submenu = new MyMenu(nat.toString());
            menu.add(submenu);
            for (AccidentalKind ak : new AccidentalKind[]{
                AccidentalKind.doubleSharp, AccidentalKind.sharp,
                AccidentalKind.natural, AccidentalKind.flat,
                AccidentalKind.doubleFlat}) {
                final Note n = nat.apply(ak);
                String s = (ak == null) ? "" : ak.getString();
                BufferedImage image = new BufferedImage(20, 15,
                        BufferedImage.TYPE_INT_RGB);
                Graphics gr = image.createGraphics();
                gr.setColor(getGray());
                gr.fillRect(0, 0, 20, 20);
                gr.setFont(getArialBold());
                gr.setColor(getBlack());
                gr.drawString(n.baseName(), 2, 12);
                gr.setFont(getMaestro());
                gr.drawString(s, 15, 8);
                Icon icon = new ImageIcon(image);
                JMenuItem mi = new JMenuItem(icon);
                mi.addActionListener((ActionEvent e) -> {
                    getModel().setRoot(Origin.Menus, n);
                });
                submenu.add(mi);
            }
        }
    }

    private void createOptionMenu(JMenu menu) {
        JMenuItem delay = new MyMenuItem("Inter-note delay");
        menu.add(delay);
        delay.addActionListener((ActionEvent e) -> {
            String inputValue = JOptionPane
                    .showInputDialog("Enter milliseconds (current = "
                            + ChordPlayer.getSleepTime() + ")");
            System.err.println("value = " + inputValue);
            if (inputValue == null) {
                return; // canceled dialog
            }
            int millis = -1;
            try {
                millis = Integer.parseInt(inputValue);
            } catch (NumberFormatException ex) {
            }
            if (millis < 0) {
                JOptionPane.showMessageDialog(getFrame(), "Illegal value: "
                        + inputValue, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                ChordPlayer.setSleepTime(millis);
            }
        });
        JCheckBoxMenuItem swing8 = new JCheckBoxMenuItem("Swing eighths");
        swing8.setFont(getDefaultFont());
        swing8.addActionListener((ActionEvent arg0) -> {
            Chords.this.setSwing8(!Chords.this.isSwing8());
            if (getMusicXmlReader() != null) {
                getMusicXmlReader().setSwing8(Chords.this.isSwing8());
                getChordSequence();
            }
        });
        JMenuItem swingdot = new JCheckBoxMenuItem("Swing dotted eighths");
        swingdot.setFont(getDefaultFont());
        swingdot.addActionListener((ActionEvent arg0) -> {
            Chords.this.setSwingdot(!Chords.this.isSwingdot());
            if (getMusicXmlReader() != null) {
                getMusicXmlReader().setSwingdot(Chords.this.isSwingdot());
                getChordSequence();
            }
        });
        menu.add(swing8);
        menu.add(swingdot);
        setMuteMenu(new MyMenu("Mute"));
        menu.add(getMuteMenu());
        setSoloMenu(new MyMenu("Solo"));
        menu.add(getSoloMenu());
        updateMuteMenu();
    }

    private void updateMuteMenu() {
        getMuteMenu().removeAll();
        getSoloMenu().removeAll();
        for (VoicePart vp : getMuteMap().keySet()) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(vp.toString());
            item.setFont(getDefaultFont());
            item.setSelected(getMuteMap().get(vp));
            final VoicePart myvp = vp;
            item.addActionListener((ActionEvent arg0) -> {
                boolean b = getMuteMap().get(myvp);
                getMuteMap().put(myvp, !b);
                System.err.println("" + myvp + " is now " + (b ? "unmuted" : "muted"));
            });
            getMuteMenu().add(item);
        }
        for (VoicePart vp : getMuteMap().keySet()) {
            JMenuItem item = new MyMenuItem(vp.toString());
            final VoicePart myvp = vp;
            item.addActionListener((ActionEvent arg0) -> {
                for (VoicePart vp1 : getMuteMap().keySet()) {
                    getMuteMap().put(vp1, !vp1.equals(myvp));
                }
                updateMuteMenu();
            });
            getSoloMenu().add(item);
        }
        if (getMuteMap().isEmpty()) {
            JMenuItem item = new MyMenuItem("<No XML File>");
            getMuteMenu().add(item);
            item = new MyMenuItem("<No XML File>");
            getSoloMenu().add(item);
        } else {
            JMenuItem item = new MyMenuItem("<None>");
            item.addActionListener((ActionEvent e) -> {
                for (VoicePart vp : getMuteMap().keySet()) {
                    getMuteMap().put(vp, false);
                }
                updateMuteMenu();
            });
            getSoloMenu().add(item);
            item = new MyMenuItem("<None>");
            item.addActionListener((ActionEvent e) -> {
                for (VoicePart vp : getMuteMap().keySet()) {
                    getMuteMap().put(vp, false);
                }
                updateMuteMenu();
            });
            getMuteMenu().add(item);
        }
    }

    public void setErrMsg(String errMsg) {
        this.getErrMsg().setErrMsg(errMsg);
    }

    private void getFonts() {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        setDefaultFont(new Font("Dialog.plain", Font.BOLD, 16));
        for (Font font : fonts) {
//            System.out.println(font.getName());
            if (font.getName().equals("Maestro")) {
                setMaestro(font.deriveFont(24.0f));
            } else if (font.getName().equals("Arial") || font.getName().equals("SansSerif.plain")) {
                setArial(font.deriveFont(16.0f));
                setArialBold(getArial().deriveFont(Font.BOLD));
            }
        }
        if (getMaestro() == null || getArial() == null || getDefaultFont() == null) {
            if (getMaestro() == null) {
                System.err.println("missing font: Maestro");
            }
            if (getArial() == null) {
                System.err.println("missing font: Arial/SansSerif.plain");
            }
            if (getDefaultFont() == null) {
                System.err.println("missing font: Dialog.plain");
            }
            System.exit(1);
        }
    }

    /**
     * Open a MusicXML file and analyze it
     */
    protected void openFile() {
        getFileChooser().setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String name = f.getName();
                return name.matches(".*[.]xml");
            }

            @Override
            public String getDescription() {
                return "MusicXML files (.xml)";
            }
        });
        int ret = getFileChooser().showOpenDialog(getFrame());
        if (ret == JFileChooser.APPROVE_OPTION) {
            getMuteMap().clear();
            File file = getFileChooser().getSelectedFile();
            getFrame().setTitle("Chords - " + file.getName());
            setMusicXmlReader(new MusicXmlReader(file.toString(), getMuteMap(), isSwing8(), isSwingdot()));
            getChordSequence();
            getPlay().setEnabled(true);
            getStop().setEnabled(true);
            getTempo().setEnabled(true);
        }
    }

    private void getChordSequence() {
        ChordSeq chordSeq = getMusicXmlReader().parseMusicXml();
        setMeasureMap(getMusicXmlReader().getMeasureMap());
        getPlayer().setMeasureMap(getMeasureMap());
        getPlayer().setSequence(chordSeq);
        updateMuteMenu();
    }

    /**
     * Represents a single menu item in the chord Type menu
     *
     * @author weath
     *
     */
    class ChordMenuItem extends MyMenuItem {

        private static final long serialVersionUID = 1L;
        private Chord chord;

        /**
         * Construct a new menu item
         *
         * @param chord the Chord that it represents
         */
        ChordMenuItem(Chord chord) {
            super(chord.getName());
            if (chord.getName().equals("")) {
                this.setText("(major)");
            }
            this.chord = chord;
            super.addActionListener((ActionEvent e) -> {
                Chords.this.getModel().setChord(Origin.Menus, ChordMenuItem.this.chord);
            });
        }

        /**
         * Get the Chord that this menu item represents
         *
         * @return a Chord
         */
        public Chord getChord() {
            return chord;
        }
    }

    /**
     * Comparator for ChordMenuItem
     *
     * @author weath
     *
     */
    class ChordMenuItemComparator implements Comparator<ChordMenuItem> {

        @Override
        public int compare(ChordMenuItem m1, ChordMenuItem m2) {
            Chord c1 = m1.getChord();
            Chord c2 = m2.getChord();
            if (c1.isInterval() || c2.isInterval()) {
                Interval n1 = c1.getIntervals()[0];
                Interval n2 = c2.getIntervals()[0];
                return n1.compareTo(n2);
            }
            return c1.compareTo(c2);
        }
    }

    public static void main(String[] args) {
        Chord.parseArgs(args);
        Chords chords = new Chords(args);
        chords.setVisible(true);
    }

    /**
     * @return the errMsg
     */
    public ErrMsg getErrMsg() {
        return errMsg;
    }

    /**
     * @param errMsg the errMsg to set
     */
    public void setErrMsg(ErrMsg errMsg) {
        this.errMsg = errMsg;
    }

    /**
     * @return the model
     */
    public ChordModel getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(ChordModel model) {
        this.model = model;
    }

    /**
     * @return the symParser
     */
    public ChordSymbolParser getSymParser() {
        return symParser;
    }

    /**
     * @param symParser the symParser to set
     */
    public void setSymParser(ChordSymbolParser symParser) {
        this.symParser = symParser;
    }

    /**
     * @return the frame
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * @param frame the frame to set
     */
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    /**
     * @return the maestro
     */
    public Font getMaestro() {
        return maestro;
    }

    /**
     * @param maestro the maestro to set
     */
    public void setMaestro(Font maestro) {
        this.maestro = maestro;
    }

    /**
     * @return the arial
     */
    public Font getArial() {
        return arial;
    }

    /**
     * @param arial the arial to set
     */
    public void setArial(Font arial) {
        this.arial = arial;
    }

    /**
     * @return the defaultFont
     */
    public static Font getDefaultFont() {
        return defaultFont;
    }

    /**
     * @param aDefaultFont the defaultFont to set
     */
    public static void setDefaultFont(Font aDefaultFont) {
        defaultFont = aDefaultFont;
    }

    /**
     * @return the arialBold
     */
    public Font getArialBold() {
        return arialBold;
    }

    /**
     * @param arialBold the arialBold to set
     */
    public void setArialBold(Font arialBold) {
        this.arialBold = arialBold;
    }

    /**
     * @return the black
     */
    public Color getBlack() {
        return black;
    }

    /**
     * @param black the black to set
     */
    public void setBlack(Color black) {
        this.black = black;
    }

    /**
     * @return the gray
     */
    public Color getGray() {
        return gray;
    }

    /**
     * @param gray the gray to set
     */
    public void setGray(Color gray) {
        this.gray = gray;
    }

    /**
     * @return the symbolText
     */
    public SymbolText getSymbolText() {
        return symbolText;
    }

    /**
     * @param symbolText the symbolText to set
     */
    public void setSymbolText(SymbolText symbolText) {
        this.symbolText = symbolText;
    }

    /**
     * @return the notesText
     */
    public NotesText getNotesText() {
        return notesText;
    }

    /**
     * @param notesText the notesText to set
     */
    public void setNotesText(NotesText notesText) {
        this.notesText = notesText;
    }

    /**
     * @return the equalTemp
     */
    public JToggleButton getEqualTemp() {
        return equalTemp;
    }

    /**
     * @param equalTemp the equalTemp to set
     */
    public void setEqualTemp(JToggleButton equalTemp) {
        this.equalTemp = equalTemp;
    }

    /**
     * @return the just
     */
    public JToggleButton getJust() {
        return just;
    }

    /**
     * @param just the just to set
     */
    public void setJust(JToggleButton just) {
        this.just = just;
    }

    /**
     * @return the muteMap
     */
    public TreeMap<VoicePart, Boolean> getMuteMap() {
        return muteMap;
    }

    /**
     * @param muteMap the muteMap to set
     */
    public void setMuteMap(TreeMap<VoicePart, Boolean> muteMap) {
        this.muteMap = muteMap;
    }

    /**
     * @return the chordPlayer
     */
    public ChordPlayer getChordPlayer() {
        return chordPlayer;
    }

    /**
     * @param chordPlayer the chordPlayer to set
     */
    public void setChordPlayer(ChordPlayer chordPlayer) {
        this.chordPlayer = chordPlayer;
    }

    /**
     * @return the keyboardAdapter
     */
    public KeyboardAdapter getKeyboardAdapter() {
        return keyboardAdapter;
    }

    /**
     * @param keyboardAdapter the keyboardAdapter to set
     */
    public void setKeyboardAdapter(KeyboardAdapter keyboardAdapter) {
        this.keyboardAdapter = keyboardAdapter;
    }

    /**
     * @return the keyboardPanel
     */
    public KeyboardPanel getKeyboardPanel() {
        return keyboardPanel;
    }

    /**
     * @param keyboardPanel the keyboardPanel to set
     */
    public void setKeyboardPanel(KeyboardPanel keyboardPanel) {
        this.keyboardPanel = keyboardPanel;
    }

    /**
     * @return the canvas
     */
    public MainCanvas getCanvas() {
        return canvas;
    }

    /**
     * @param canvas the canvas to set
     */
    public void setCanvas(MainCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * @return the chordList
     */
    public ChordListPanel getChordList() {
        return chordList;
    }

    /**
     * @param chordList the chordList to set
     */
    public void setChordList(ChordListPanel chordList) {
        this.chordList = chordList;
    }

    /**
     * @return the notePanel
     */
    public NotePanel getNotePanel() {
        return notePanel;
    }

    /**
     * @param notePanel the notePanel to set
     */
    public void setNotePanel(NotePanel notePanel) {
        this.notePanel = notePanel;
    }

    /**
     * @return the bottomPanel
     */
    public JPanel getBottomPanel() {
        return bottomPanel;
    }

    /**
     * @param bottomPanel the bottomPanel to set
     */
    public void setBottomPanel(JPanel bottomPanel) {
        this.bottomPanel = bottomPanel;
    }

    /**
     * @return the menuBar
     */
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * @param menuBar the menuBar to set
     */
    public void setMenuBar(JMenuBar menuBar) {
        this.menuBar = menuBar;
    }

    /**
     * @return the fileChooser
     */
    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    /**
     * @param fileChooser the fileChooser to set
     */
    public void setFileChooser(JFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    /**
     * @return the player
     */
    public SequencePlayer getPlayer() {
        return player;
    }

    /**
     * @param player the player to set
     */
    public void setPlayer(SequencePlayer player) {
        this.player = player;
    }

    /**
     * @return the back
     */
    public JButton getBack() {
        return back;
    }

    /**
     * @param back the back to set
     */
    public void setBack(JButton back) {
        this.back = back;
    }

    /**
     * @return the forw
     */
    public JButton getForw() {
        return forw;
    }

    /**
     * @param forw the forw to set
     */
    public void setForw(JButton forw) {
        this.forw = forw;
    }

    /**
     * @return the tickField
     */
    public JTextField getTickField() {
        return tickField;
    }

    /**
     * @param tickField the tickField to set
     */
    public void setTickField(JTextField tickField) {
        this.tickField = tickField;
    }

    /**
     * @return the tempo
     */
    public JSpinner getTempo() {
        return tempo;
    }

    /**
     * @param tempo the tempo to set
     */
    public void setTempo(JSpinner tempo) {
        this.tempo = tempo;
    }

    /**
     * @return the tuneToLabel
     */
    public JLabel getTuneToLabel() {
        return tuneToLabel;
    }

    /**
     * @param tuneToLabel the tuneToLabel to set
     */
    public void setTuneToLabel(JLabel tuneToLabel) {
        this.tuneToLabel = tuneToLabel;
    }

    /**
     * @return the tuneTo
     */
    public JTextField getTuneTo() {
        return tuneTo;
    }

    /**
     * @param tuneTo the tuneTo to set
     */
    public void setTuneTo(JTextField tuneTo) {
        this.tuneTo = tuneTo;
    }

    /**
     * @return the ratiosLabel
     */
    public JLabel getRatiosLabel() {
        return ratiosLabel;
    }

    /**
     * @param ratiosLabel the ratiosLabel to set
     */
    public void setRatiosLabel(JLabel ratiosLabel) {
        this.ratiosLabel = ratiosLabel;
    }

    /**
     * @return the ratios
     */
    public Ratios getRatios() {
        return ratios;
    }

    /**
     * @param ratios the ratios to set
     */
    public void setRatios(Ratios ratios) {
        this.ratios = ratios;
    }

    /**
     * @return the instrument
     */
    public JLabel getInstrument() {
        return instrument;
    }

    /**
     * @param instrument the instrument to set
     */
    public void setInstrument(JLabel instrument) {
        this.instrument = instrument;
    }

    /**
     * @return the tuningSpec
     */
    public ArrayList<TuningSpec> getTuningSpec() {
        return tuningSpec;
    }

    /**
     * @param tuningSpec the tuningSpec to set
     */
    public void setTuningSpec(ArrayList<TuningSpec> tuningSpec) {
        this.tuningSpec = tuningSpec;
    }

    /**
     * @return the measureMap
     */
    public MeasureMap getMeasureMap() {
        return measureMap;
    }

    /**
     * @param measureMap the measureMap to set
     */
    public void setMeasureMap(MeasureMap measureMap) {
        this.measureMap = measureMap;
    }

    /**
     * @return the muteMenu
     */
    public JMenu getMuteMenu() {
        return muteMenu;
    }

    /**
     * @param muteMenu the muteMenu to set
     */
    public void setMuteMenu(JMenu muteMenu) {
        this.muteMenu = muteMenu;
    }

    /**
     * @return the soloMenu
     */
    public JMenu getSoloMenu() {
        return soloMenu;
    }

    /**
     * @param soloMenu the soloMenu to set
     */
    public void setSoloMenu(JMenu soloMenu) {
        this.soloMenu = soloMenu;
    }

    /**
     * @return the instruments
     */
    public Instrument[] getInstruments() {
        return instruments;
    }

    /**
     * @param instruments the instruments to set
     */
    public void setInstruments(Instrument[] instruments) {
        this.instruments = instruments;
    }

    /**
     * @return the intervals
     */
    public static Interval[] getIntervals() {
        return intervals;
    }

    /**
     * @param aIntervals the intervals to set
     */
    public static void setIntervals(Interval[] aIntervals) {
        intervals = aIntervals;
    }

    /**
     * @return the footer
     */
    public FooterCanvas getFooter() {
        return footer;
    }

    /**
     * @param footer the footer to set
     */
    public void setFooter(FooterCanvas footer) {
        this.footer = footer;
    }

    /**
     * @return the musicXmlReader
     */
    public MusicXmlReader getMusicXmlReader() {
        return musicXmlReader;
    }

    /**
     * @param musicXmlReader the musicXmlReader to set
     */
    public void setMusicXmlReader(MusicXmlReader musicXmlReader) {
        this.musicXmlReader = musicXmlReader;
    }

    /**
     * @return the swing8
     */
    public boolean isSwing8() {
        return swing8;
    }

    /**
     * @param swing8 the swing8 to set
     */
    public void setSwing8(boolean swing8) {
        this.swing8 = swing8;
    }

    /**
     * @return the swingdot
     */
    public boolean isSwingdot() {
        return swingdot;
    }

    /**
     * @param swingdot the swingdot to set
     */
    public void setSwingdot(boolean swingdot) {
        this.swingdot = swingdot;
    }

    /**
     * @return the play
     */
    public JButton getPlay() {
        return play;
    }

    /**
     * @param play the play to set
     */
    public void setPlay(JButton play) {
        this.play = play;
    }

    /**
     * @return the stop
     */
    public JButton getStop() {
        return stop;
    }

    /**
     * @param stop the stop to set
     */
    public void setStop(JButton stop) {
        this.stop = stop;
    }
}
