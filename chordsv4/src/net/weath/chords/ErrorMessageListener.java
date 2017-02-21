package net.weath.chords;

import java.util.EventListener;

public interface ErrorMessageListener extends EventListener {

    void errorMessageChanged(String newErrMsg);
}
