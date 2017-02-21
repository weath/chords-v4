package net.weath.musicxml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.weath.musicutil.Util;

public class Score {

    private static Element root;

    private DivData divData;

    private ArrayList<MeasureData> measures = new ArrayList<>();

    private ArrayList<String> partIds = new ArrayList<>();

    private XMLEventReader reader;

    // for testing
    public void printElements(String[] args) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        Reader fileReader = new FileReader(args[0]);
        reader = factory.createXMLEventReader(fileReader);

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
//			System.out.println("===== " + event.getEventType() + " =====");
            if (event.isStartElement()) {
                StartElement element = (StartElement) event;
                System.out.println("Start Element: " + element.getName());

                Iterator<?> iterator = element.getAttributes();
                while (iterator.hasNext()) {
                    javax.xml.stream.events.Attribute attribute
                            = (javax.xml.stream.events.Attribute) iterator.next();
                    QName name = attribute.getName();
                    String value = attribute.getValue();
                    System.out.println("Attribute name/value: " + name + "/"
                            + value);
                }
            }
            if (event.isEndElement()) {
                EndElement element = (EndElement) event;
                System.out.println("End element:" + element.getName());
            }
            if (event.isCharacters()) {
                Characters element = (Characters) event;
                String text = element.getData();
                if (text.isEmpty() || text.matches("[ \t\n]*"))
					; // ignore whitespace
                else {
                    System.out.println("Text: " + text);
                }
            }
        }
    }

    // for testing
    public static void main(String[] args) throws Exception {
        Score score = new Score(new DivData(), args[0]);

        score.dump();
    }

    public Score(DivData divData, String fileName) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        Reader fileReader = null;
        this.divData = divData;
        try {
            fileReader = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            init(factory.createXMLEventReader(fileReader));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public Score(XMLEventReader reader) {
        init(reader);
    }

    private void init(XMLEventReader reader) {
        this.reader = reader;
        root = parse(reader);
        traverse(root);
    }

    public ArrayList<MeasureData> getMeasureData() {
        return measures;
    }

    // for testing
    public void dump() {
        for (MeasureData m : measures) {
            System.out.println(m);
            for (NoteData n : m.getNotes()) {
                System.out.println("  " + n);
            }
        }
    }

    private void traverse(Element element) {
        while (element != null) {
            processScore(element);
            element = element.getSibling();
        }
    }

    private void processScore(Element element) {
        String name = element.getName();
        if (name.equals("score-partwise")) {
            processParts(element.getChild());
        } else if (name.equals("score-timewise")) {
//			processMeasures(element.getChild());
        } else {
            // ignore element and its children
        }
        reconcile();
    }

    private void reconcile() {
        // walk over the measure and note data, setting start values and lengths
        // in terms of jiffies, now that we know all the possible values of "divisions"
        int now = 0;
        int[] pCursor = new int[divData.divMap.keySet().size()];
        for (MeasureData m : measures) {
            m.setStart(now);
            int length = 0;
            for (int i = 0; i < pCursor.length; i++) {
                pCursor[i] = 0;
            }
            for (NoteData n : m.getNotes()) {
                int part = n.getPart();
                int div = divData.divMap.get(part);
                n.setStartTime(n.getStartTime(divData.jiffy, div));
                n.setLength(n.getLength(divData.jiffy, div));
                pCursor[part] = Math.max(pCursor[part], n.getStartTime(1, 1) + n.getLength(1, 1));
            }
            for (int i = 0; i < pCursor.length; i++) {
                if (pCursor[i] > length) {
                    length = pCursor[i];
                }
            }
            m.setLength(length);
            m.setActualBeats(((double) length) / divData.jiffy);
            now += length;
        }
    }

    private void processParts(Element element) {
        while (element != null) {
            String name = element.getName();
            if (name.equals("part")) {
                processPart(element);
            } else {
                // ignore element and its children
            }
            element = element.getSibling();
        }
    }

    private void processPart(Element element) {
        Attribute partId = element.getNamedAttribute("id");
        int partNumber = -1;
        int measureNumber = -1;
        if (partId != null) {
            partNumber = lookupPartId(partId.getValue());
        }
        Element child = element.getChild();
        while (child != null) {
            String name = child.getName();
            if (name.equals("measure")) {
                Attribute mNum = child.getNamedAttribute("number");
                if (mNum != null) {
                    measureNumber = Integer.parseInt(mNum.getValue());
                }
                processMusicData(child.getChild(), partNumber, measureNumber);
            }
            child = child.getSibling();
        }
    }

    private void processMusicData(Element element, int pNum, int mNum) {
        MeasureData measure = findMeasure(mNum);
//		System.err.println("MEASURE " + mNum);
        int firstMeasureNumber = measures.get(0).getNumber();
        MeasureData prevMeas = (mNum > firstMeasureNumber) ? findMeasure(mNum - 1) : null;
        if (prevMeas != null) {
            if (measure.getBeatType() == -1 && prevMeas.getBeatType() != -1) {
                measure.setBeatType(prevMeas.getBeatType());
                measure.setBeats(prevMeas.getBeats());
            }
        }
        int now = 0; // measure-relative, in "divisions" (part-specific)
        int delta = 0;
        NoteData lastNote = null;
        while (element != null) {
            String name = element.getName();
//			System.err.println("ELEMENT: " + name);
            if (name.equals("barline")) {
                Element e = element.getChild();
                while (e != null) {
                    String s = e.getName();
                    if (s.equals("repeat")) {
                        List<Attribute> list = e.getAttributeList();
                        for (Attribute a : list) {
                            String aname = a.getName();
                            if (aname.equals("direction")) {
                                String v = a.getValue();
                                if (v.equals("forward")) {
                                    measure.setRepeatType(RepeatType.Forward);
                                } else {
                                    measure.setRepeatType(RepeatType.Backward);
                                }
                            }
                        }
                    } else if (s.equals("ending")) {
                        String number = e.getNamedAttribute("number").getValue();
                        String type = e.getNamedAttribute("type").getValue();
                        measure.setEnding(Integer.parseInt(number));
                        if (type.equals("start")) {
                            measure.setEndingType(EndingType.Start);
                        } else if (type.equals("stop")) {
                            measure.setEndingType(EndingType.Stop);
                        }
                    }
                    e = e.getSibling();
                }
            } else if (name.equals("note")) {
                NoteData note = new NoteData();
                note.setPart(pNum);
                note.setStartTime(now);
                Element e = element.getChild();
                while (e != null) {
                    String s = e.getName();
                    if (s.equals("pitch")) {
                        Element t = e.getChild();
                        while (t != null) {
                            String n = t.getName();
                            if (n.equals("step")) {
                                note.setDegree(Step.valueOf(t.getValue()));
                            } else if (n.equals("alter")) {
                                note.setAlter(Integer.parseInt(t.getValue()));
                            } else if (n.equals("octave")) {
                                note.setOctave(Integer.parseInt(t.getValue()));
                            }
                            t = t.getSibling();
                        }
                    } else if (s.equals("chord")) {
                        note.setStartTime(lastNote.getStartTime(1, 1));
                    } else if (s.equals("duration")) {
                        note.setLength(delta = Integer.parseInt(e.getValue()));
                    } else if (s.equals("voice")) {
                        note.setVoice(Integer.parseInt(e.getValue()));
                    } else if (s.equals("notations")) {
                        Element t = e.getChild();
                        while (t != null) {
                            String n = t.getName();
                            if (n.equals("fermata")) {
                                note.setFermata(true);
                            }
                            t = t.getSibling();
                        }
                    }
                    e = e.getSibling();
                }
                now += delta;
                delta = 0;
                lastNote = note;
                measure.addNote(note);
            } else if (name.equals("backup")) {
                Element e = element.getChild();
                if (e.getName().equals("duration")) {
                    now -= Integer.parseInt(e.getValue());
                }
            } else if (name.equals("forward")) {
                Element e = element.getChild();
                if (e.getName().equals("duration")) {
                    now += Integer.parseInt(e.getValue());
                }
            } else if (name.equals("sound")) {
                Attribute attr = element.getNamedAttribute("tempo");
                if (attr != null) {
                    measure.setTempo(Double.parseDouble(attr.getValue()));
                }
            } else if (name.equals("attributes")) {
                Element e = element.getChild();
                while (e != null) {
                    String s = e.getName();
                    if (s.equals("divisions")) {
                        int n = Integer.parseInt(e.getValue());
                        updateDiv(pNum, n);
                    } else if (s.equals("key")) {
                        Element el = e.getChild();
                        while (el != null) {
                            if (el.getName().equals("fifths")) {
                                int fifths = Integer.parseInt(el.getValue());
                                measure.setFifths(fifths);
                            } else if (el.getName().equals("mode")) {
                                measure.setMode(el.getValue());
                            }
                            el = el.getSibling();
                        }
                    } else if (s.equals("time")) {
                        Element el = e.getChild();
                        int beats = -1;
                        int beatType = -1;
                        while (el != null) {
                            if (el.getName().equals("beats")) {
                                beats = Integer.parseInt(el.getValue());
                            } else if (el.getName().equals("beat-type")) {
                                beatType = Integer.parseInt(el.getValue());
                            }
                            el = el.getSibling();
                        }
                        if (beats > 0 && beatType > 0) {
                            measure.setBeats(beats);
                            measure.setBeatType(beatType);
                        }
                    }
                    e = e.getSibling();
                }
            }
            element = element.getSibling();
        }
    }

    private void updateDiv(int part, int n) {
        // maintain jiffy as the Least Common Multiple of all divisions
        HashSet<Integer> set = new HashSet<>();
        set.add(n);
        for (int div : divData.divMap.values()) {
            set.add(div);
        }
        divData.divMap.put(part, n);
        Integer[] divs = set.toArray(new Integer[0]);
        int newJiffy = Util.lcm(divs);
//		System.err.println("div for part " + part + " = " + n + ", jiffy = " + newJiffy);
        divData.jiffy = newJiffy;
    }

    private MeasureData findMeasure(int num) {
        for (MeasureData mData : measures) {
            if (mData.getNumber() == num) {
                return mData;
            }
        }
        MeasureData newMeasure = new MeasureData();
        newMeasure.setNumber(num);
        measures.add(newMeasure);
        return newMeasure;
    }

    private int lookupPartId(String id) {
        int n = partIds.indexOf(id);
        if (n < 0) {
            partIds.add(id);
        }
        n = partIds.indexOf(id);
        return n;
    }

    @SuppressWarnings("unused")
    private void printTree(Element e, int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("    ");
        }
        System.out.println(e.getName() + ": " + e.getValue());
        List<Attribute> list = e.getAttributeList();
        if (list.size() > 0) {
            for (Attribute a : list) {
                for (int i = 0; i < indent; i++) {
                    System.out.print("    ");
                }
                System.out.println("  " + a.getName() + "=" + a.getValue());
            }
        }
        if (e.getChild() != null) {
            printTree(e.getChild(), indent + 1);
        }
        if (e.getSibling() != null) {
            printTree(e.getSibling(), indent);
        }
    }

    private static Element parse(XMLEventReader reader) {
        Element element = null;
        try {
            while (true) {
                XMLEvent next = reader.peek();
                if (next == null) {
                    return element;
                }
                int type = next.getEventType();
                switch (type) {
                    case XMLEvent.START_ELEMENT:
                        element = parseElement(reader);
                        break;
                    case XMLEvent.END_DOCUMENT:
                        return element;
                    default:
                        reader.nextEvent(); // ignore
                        break;
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return element;
    }

    private static Element parseElement(XMLEventReader reader) throws XMLStreamException {
        StartElement e = (StartElement) reader.nextEvent();
        Element element = new Element(e.getName());
        Iterator<?> it = e.getAttributes();
        while (it.hasNext()) {
            javax.xml.stream.events.Attribute attribute
                    = (javax.xml.stream.events.Attribute) it.next();
            QName name = attribute.getName();
            String value = attribute.getValue();
            element.addAttribute(new Attribute(name, value));
        }
        XMLEvent next = reader.peek();
        while (next.getEventType() != XMLEvent.END_ELEMENT) {
            if (next.isCharacters()) {
                element.append(reader.nextEvent().asCharacters());
            } else if (next.isStartElement()) {
                Element child = parseElement(reader);
                element.addChild(child);
            } else {
                reader.nextEvent(); // ignore it
            }
            next = reader.peek();
        }
        if (next.isEndElement()) {
            EndElement end = (EndElement) reader.nextEvent();
            if (!end.getName().toString().equals(element.getName())) {
                throw new XMLStreamException("out of synch!");
            }
            return element;
        }
        return element;
    }
}
