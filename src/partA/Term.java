package partA;

import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;

/**
 * Created by Sivan on 12/4/2017.
 */
public class Term implements Serializable, Comparable<Term>{
    private String term;
    private int df;
    private int frequencyInCorup;
    private long pointerToPostings;
    private String postingFilePath;

    private  SimpleStringProperty termProp;
    private  SimpleStringProperty dfProp;
    private  SimpleStringProperty frequencyInCotpusProp;

        public Term() {
            this.termProp = new SimpleStringProperty();
            this.dfProp = new SimpleStringProperty();
            this.frequencyInCotpusProp = new SimpleStringProperty();
        }

    public Term(String term, int df, int frequencyInCorup, int pointerToPostings) {
        this.term = term;
        this.df = df;
        this.frequencyInCorup = frequencyInCorup;
        this.pointerToPostings = pointerToPostings;

        this.termProp = new SimpleStringProperty(term);
        this.dfProp = new SimpleStringProperty("" + df);
        this.frequencyInCotpusProp = new SimpleStringProperty("" + frequencyInCorup);

    }

    //constructors

    public Term(String term) {
        this.term = term;
        this.df = 1;
        this.frequencyInCorup = 1;

        this.termProp = new SimpleStringProperty(term);
        this.dfProp = new SimpleStringProperty("" + df);
        this.frequencyInCotpusProp = new SimpleStringProperty("" + frequencyInCorup);
    }

    //getters

    public String getPostingFilePath() {
        return postingFilePath;
    }

    public String getTerm() {
        return term;
    }

    public int getDf() {
        return df;
    }

    public int getFrequencyInCorup() {
        return frequencyInCorup;
    }

    public long getPointerToPostings() {
        return pointerToPostings;
    }


    //setters
    public void setPostingFilePath(String postingFilePath) {
        this.postingFilePath = postingFilePath;

    }

    public void setTerm(String term) {
        this.term = term;
        this.termProp = new SimpleStringProperty(term);
    }

    public void setDf(int df) {
        this.df = df;
        this.dfProp = new SimpleStringProperty("" + df);
    }

    public void setFrequencyInCorup(int frequencyInCorup) {
        this.frequencyInCorup = frequencyInCorup;
        this.frequencyInCotpusProp = new SimpleStringProperty("" + frequencyInCorup);
    }

    public void setPointerToPostings(long pointerToPostings) {
        this.pointerToPostings = pointerToPostings;
    }

    public String getTermProp() {
        return termProp.get();
    }

    public SimpleStringProperty termPropProperty() {
        return termProp;
    }

    public String getDfProp() {
        return dfProp.get();
    }

    public SimpleStringProperty dfPropProperty() {
        return dfProp;
    }

    public String getFrequencyInCotpusProp() {
        return frequencyInCotpusProp.get();
    }

    public SimpleStringProperty frequencyInCotpusPropProperty() {
        return frequencyInCotpusProp;
    }

    public void setTermProp(String termProp) {
        this.termProp.set(termProp);
    }

    public void setDfProp(String dfProp) {
        this.dfProp.set(dfProp);
    }

    public void setFrequencyInCotpusProp(String frequencyInCotpusProp) {
        this.frequencyInCotpusProp.set(frequencyInCotpusProp);
    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Term otherTerm = (Term)obj;
        return this.term.equals(otherTerm.getTerm());
    }

    @Override
    public int compareTo(Term o) {
        if(this.frequencyInCorup < o.frequencyInCorup)
            return 1;
        else if(this.frequencyInCorup > o.frequencyInCorup)
            return -1;
        else return 0;
    }



}
