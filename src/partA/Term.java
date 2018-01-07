package partA;

import java.io.Serializable;

/**
 * Created by Sivan on 12/4/2017.
 */
public class Term implements Serializable, Comparable<Term>{
    private String term;
    private int df;
    private int frequencyInCorpus;
    private long pointerToPostings;
    private String postingFilePath;

    public Term(String term, int df, int frequencyInCorpus, int pointerToPostings) {
        this.term = term;
        this.df = df;
        this.frequencyInCorpus = frequencyInCorpus;
        this.pointerToPostings = pointerToPostings;
    }

    //constructors

    public Term(String term) {
        this.term = term;
        this.df = 1;
        this.frequencyInCorpus = 1;
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

    public int getFrequencyInCorpus() {
        return frequencyInCorpus;
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
    }

    public void setDf(int df) {
        this.df = df;
    }

    public void setFrequencyInCorpus(int frequencyInCorpus) {
        this.frequencyInCorpus = frequencyInCorpus;
    }

    public void setPointerToPostings(long pointerToPostings) {
        this.pointerToPostings = pointerToPostings;
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
        if(this.df < o.df)
            return 1;
        else if(this.df > o.df)
            return -1;
        else return 0;
    }



}
