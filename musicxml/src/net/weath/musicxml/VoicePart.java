package net.weath.musicxml;

public class VoicePart implements Comparable<VoicePart> {

    public int voice;
    public int part;

    public VoicePart(NoteData nd) {
        voice = nd.getVoice();
        part = nd.getPart();
    }

    public VoicePart(int part, int voice) {
        this.part = part;
        this.voice = voice;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + part;
        result = prime * result + voice;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VoicePart other = (VoicePart) obj;
        if (part != other.part) {
            return false;
        }
        if (voice != other.voice) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "(" + part + "/" + voice + ")";
    }

    @Override
    public int compareTo(VoicePart other) {
        if (this.part != other.part) {
            return this.part - other.part;
        }
        return this.voice - other.voice;
    }

}
