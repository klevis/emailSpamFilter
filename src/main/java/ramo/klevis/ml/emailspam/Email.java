package ramo.klevis.ml.emailspam;

import java.io.Serializable;
import java.util.List;

/**
 * Created by klevis.ramo on 9/26/2017.
 */
public class Email implements Serializable {

    public Email(List<String> words, boolean spam) {
        this.words = words;
        this.spam = spam;
    }

    public Email(List<String> words) {
        this.words = words;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public boolean isSpam() {
        return spam;
    }

    public void setSpam(boolean spam) {
        this.spam = spam;
    }

    private List<String> words;
    private boolean spam;
}
