package net.weath.chords;

import java.util.EventListener;

import javax.swing.event.EventListenerList;

public class ErrMsg {

    private String errMsg = null;
    private final EventListenerList listenerList = new EventListenerList();

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        if (errMsg != null) {
            System.err.println("ERROR: " + errMsg);
        }
        for (EventListener l : listenerList.getListeners(ErrorMessageListener.class)) {
            ErrorMessageListener eml = (ErrorMessageListener) l;
            eml.errorMessageChanged(errMsg);
        }
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void addListener(ErrorMessageListener listener) {
        listenerList.add(ErrorMessageListener.class, listener);
    }
}
