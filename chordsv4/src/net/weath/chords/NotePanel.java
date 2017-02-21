package net.weath.chords;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import net.weath.musicutil.Interval;
import net.weath.musicutil.Note;
import net.weath.musicutil.Pitch;
import net.weath.musicutil.RatioSet;

public class NotePanel extends JPanel implements ChordChangeListener {

    private static final long serialVersionUID = 1L;
    
    private final ChordModel model;

    public NotePanel(String[] args) {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        setBorder(new LineBorder(Color.BLACK));
        parseNotes(args);
        this.model = new ChordModel(new ErrMsg());
    }

    public NotePanel(ChordModel model) {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        setBorder(new LineBorder(Color.BLACK));
        JPanel panel = new JPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        panel.setPreferredSize(new Dimension(15, 100));
        add(panel);
        this.model = model;
        model.addListener(this);
    }

    public void parseNotes(String[] args) {
        removeAll();
        // parse the args; each Note should be a separate argument, with 5 colon-separated fields:
        // (Part/Voice):Notename:cents:Intervalname:Factor
        for (String arg : args) {
            String[] arr = arg.split(":");
            assert arr.length == 5;
            addVoicePart(arr[0]);
            addNotename(arr[1]);
            if (model.isJust()) {
                addCents(arr[2]);
            } else {
                addTextField("");
            }
            addIntervalname(arr[3]);
            if (model.isJust()) {
                addFactor(arr[4]);
                addTuningmeter(arr[2]);
            } else {
                addTextField("");
                addTuningmeter("0");
            }
        }
    }

    private void addTuningmeter(final String string) {
        final double cents = Double.parseDouble(string);
        JPanel p = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Rectangle bounds = getBounds();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, bounds.width, bounds.height);
                int midX = bounds.width / 2;
                int x = bounds.width / 2 + (int) (cents / 100.0 * bounds.width);
                int y = 0;
//				System.err.println(string + ": x = " + x);
                g.setColor(Color.RED);
                g.drawLine(midX, 0, midX, y + bounds.height);
                g.setColor(Color.BLACK);
                g.drawLine(x, y, x, y + bounds.height);
            }
        };
        p.setPreferredSize(new Dimension(100, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 10.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(p, gbc);
    }

    private void addFactor(String string) {
        addTextField(string);
    }

    private void addTextField(String string) {
        JTextField f = new JTextField(string);
        f.setFont(Chords.getDefaultFont());
        f.setEditable(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(f, gbc);
    }

    private void addIntervalname(String string) {
        addTextField(string);
    }

    private void addCents(String string) {
        addTextField(string);
    }

    private void addNotename(String string) {
        addTextField(string);
    }

    private void addVoicePart(String string) {
        addTextField(string);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Note Panel");
        NotePanel panel = new NotePanel(args);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void chordChanged(ChordChangeEvent event) {
        Pitch[] pitches = event.getPitches();
        Note root = event.getRoot();
        RatioSet ratioSet = event.getRatioSet();
        ArrayList<String> arr = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int n = 0;
        if (pitches != null) {
            n = pitches.length;
        }
        while (--n >= 0) {
            Pitch p = pitches[n];
            Object obj = p.getVoicePart();
            if (obj == null) {
                sb.append(String.format("(%d)", n));
            } else {
                sb.append(obj.toString());
            }
            sb.append(":");
            if (!p.isReal())
                sb.append("(");
            sb.append(p.getNote().toString());
            sb.append(String.format("%d", p.getOctave()));
            if (!p.isReal())
                sb.append(")");
            sb.append(":");
            sb.append(String.format("%+2.3g", p.getCents()));
            sb.append(":");
            if (root == null) {
                sb.append("R");
            } else if (root.minus(p.getNote()) == 0) {
                sb.append("R");
            } else {
                Interval intval = Interval.between(root, p.getNote());
                sb.append(intval.id());
            }
            sb.append(":");
            if (ratioSet == null || ratioSet.size() < n) {
                sb.append("?");
            } else {
                sb.append(String.format("%d", ratioSet.value(n)));
            }
            arr.add(sb.toString());
            sb.setLength(0);
        }
        parseNotes(arr.toArray(new String[0]));
        getParent().validate();
    }

}
