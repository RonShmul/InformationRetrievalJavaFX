package partA;
import java.io.*;
import java.util.*;

public class Indexer {

    private HashMap<String, Term> Dictionary;
    private HashMap<String, String> cache;
    private String corpusPath;
    private String filesPath;
    private int counterForFiles;
    private boolean toStemm;
    private HashMap<String, Document> documents;
    private static final int N = 468370;

    /*
    constructors
     */
    public Indexer() {

        Dictionary = new HashMap<>();
        counterForFiles = 0;
        cache = new HashMap<>();
        documents = new HashMap<String, Document>();
    }
    public Indexer(String corpusPath, String filesPath, boolean toStemm) {
        this.toStemm = toStemm;
        this.corpusPath = corpusPath;
        this.filesPath = filesPath;
        Dictionary = new HashMap<>();
        counterForFiles = 0;
        cache = new HashMap<>();
        documents = new HashMap<String, Document>();
    }
    /*
    getters and setters
     */
    public String getCorpusPath() {
        return corpusPath;
    }

    public void setCorpusPath(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    public static int getN() {
        return N;
    }

    public HashMap<String, Term> getDictionary() {
        return Dictionary;
    }

    public void setDictionary(HashMap<String, Term> dictionary) {
        Dictionary = dictionary;
    }

    public List<Term> getDictionaryForShow() {
        List<Term> terms = new ArrayList<>(Dictionary.values());
        return terms;
    }

    public HashMap<String, Document> getDocuments() {
        return documents;
    }

    public HashMap<String, String> getCache() {
        return cache;
    }

    public void setCache(HashMap<String, String> cache) {
        this.cache = cache;
    }

    /**
     * get all the needed (indexed) files to the program memory - cache, dictionart and document data structure.
     */
    public void generateIndex(String postingPath) {
        File file = new File("D:\\Posting");

            try {
                String path = file.getAbsolutePath();
                int index = path.lastIndexOf('\\');
                if(index >= path.length()-1) {
                    path = path.substring(0, index);
                }
                ObjectInputStream objectInputStreamDict = new ObjectInputStream(new FileInputStream(new File(path + "\\" + "dictionary")));
                ObjectInputStream objectInputStreamCache = new ObjectInputStream(new FileInputStream(new File(path + "\\" + "cache")));

                setDictionary((HashMap<String, Term>) objectInputStreamDict.readObject());
                setCache((HashMap<String, String>)objectInputStreamCache.readObject());
                objectInputStreamCache.close();
                objectInputStreamDict.close();
                createDocumentsMap();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
    }

    /**
     * create the index from the start - read the files and seperate the documents, parse the terms and create the posting files, document file, dictionary and cache.
     */
    public void initialize() {
        //createCache();
        //create new readFile
        ReadFile corpus = new ReadFile(corpusPath);
        //get to the main directory
        File headDir = new File(corpusPath);
        //get all the directories from the main directory:
        File[] listOfDirs = headDir.listFiles();

        //iterate on all the files in the corpus
        for (int i = 0; i < listOfDirs.length; i++) {
            counterForFiles = i;
            //get to the wanted file
            File temp = listOfDirs[i];
            File[] currDir = temp.listFiles();
            File currFile = currDir[0];

            //create a new parse
            Parse parse = new Parse(toStemm, false);
            LinkedHashMap<Document, String> toParse = corpus.readFile(currFile);
            //get all the parsed terms of a specific file
            HashMap<String, MetaData> termsToIndex = parse.InitializeParseForDoc(toParse);
            //send to a method that construct the indexing
            constructPosting(termsToIndex);
            parse.clearTerms();
        }
        mergePosting();
        insertDocumentsToFile();
        cleanDictionary();
    }

    /**
     * create a temporary posting file for each file int the corpus.
     * @param termsToIndex
     */
    private void constructPosting(HashMap<String, MetaData> termsToIndex) {
        try {
            //create temporary postring file
            BufferedWriter temporaryPostingFile = new BufferedWriter(new FileWriter(filesPath + "\\" + counterForFiles));
            //create the list to add the posting lines to it
            List<String> listToTempPosting = new ArrayList<>();
            //iterate the terms and create the posting entry for each term
            for (Map.Entry<String, MetaData> term : termsToIndex.entrySet()) {
                //if the term does not exist in the dictionary - add it. else - add to the existing term the DF and frequency in corpus
                if (Dictionary.get(term.getKey()) == null) {
                    Term newTerm = new Term(term.getKey(), term.getValue().getDf(), term.getValue().getFrequencyInCorpus(), -1);
                    Dictionary.put(term.getKey(), newTerm);
                } else {
                    Term existTerm = Dictionary.get(term.getKey());
                    existTerm.setDf(existTerm.getDf() + term.getValue().getDf());
                    existTerm.setFrequencyInCorpus(existTerm.getFrequencyInCorpus() + term.getValue().getFrequencyInCorpus());
                }
                //create the entry to the posting file
                String toTemp = term.getKey() + ":";
                for (Map.Entry<Document, Integer> tf : term.getValue().getFrequencyInDoc().entrySet()) {
                    Document doc = tf.getKey();
                    toTemp = toTemp.concat(doc.getDocNo() + ":" + ((double)tf.getValue())/tf.getKey().getLength() + ",");
                    documents.put(doc.getDocNo(), doc);
                }
                listToTempPosting.add(toTemp);
            }
            //sort the terms in the posting file in alphabetic order
            listToTempPosting.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    String term1 = o1.substring(0, o1.indexOf(':'));
                    String term2 = o2.substring(0, o2.indexOf(':'));
                    return term1.compareTo(term2);
                }
            });
            //write the entries to file and close it
            int size = listToTempPosting.size();
            for (int i = 0; i < size; i++) {
                temporaryPostingFile.write(listToTempPosting.get(i));
                if (i < size-1) {
                    temporaryPostingFile.write("\n");
                }
            }
            temporaryPostingFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * marge all the temporary posting files to final posting files.
     */
    public void mergePosting() {   // merging the temporary posting files - two in each iteration
        try {
            int count = 0;
            int realCounter = counterForFiles+1;
            while (realCounter > 2) {  //we will merge the last two files in separate - to the final posting files
                if (realCounter % 2 != 0) {  //if the number of files is odd - we will merge the last two files for even number of files to merge
                    File fileF = new File(filesPath + "\\" + counterForFiles);
                    File fileS = new File(filesPath + "\\" + (counterForFiles - 1));
                    File temp = new File(filesPath + "\\" + "temporaryFile");
                    BufferedReader readFirst = new BufferedReader(new FileReader(fileF));
                    BufferedReader readSecond = new BufferedReader(new FileReader(fileS));
                    BufferedWriter writeToFile = new BufferedWriter(new FileWriter(temp));
                    helpMerge(readFirst, readSecond, writeToFile);
                    fileF.delete();
                    fileS.delete();
                    File newFile = new File(filesPath + "\\" + (counterForFiles - 1));
                    temp.renameTo(newFile);
                    counterForFiles--;
                    realCounter--;
                }

                for (int i = 0; realCounter > 2 && i < realCounter; i = i + 2) {
                    File fileF = new File(filesPath + "\\" + i);
                    File fileS = new File(filesPath + "\\" + (i + 1));
                    File temp = new File(filesPath + "\\" + "temporaryFile");
                    BufferedReader readFirst = new BufferedReader(new FileReader(fileF));
                    BufferedReader readSecond = new BufferedReader(new FileReader(fileS));
                    BufferedWriter writeToFile = new BufferedWriter(new FileWriter(temp));
                    helpMerge(readFirst, readSecond, writeToFile);
                    fileF.delete();
                    fileS.delete();
                    File newFile = new File(filesPath + "\\" + count);  // name the merged file with counter
                    temp.renameTo(newFile);
                    count++;
                }
                counterForFiles = counterForFiles/2;
                realCounter = realCounter/2;
                count =0;
            }
            //merging the two files that left and separate to final posting files
            finalPosting();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this methos is responsible for the last merge iteration to the final posting files.
     */
    private void finalPosting() {   //read the last two posting files

        String ae = "abcde";
        String fj ="fghij";
        String ko = "klmno";
        String pt = "pqrst";
        String uz = "uvwxyz";
        long num= 0;
        long a_e= 0;
        long f_j= 0;
        long k_o= 0;
        long p_t= 0;
        long u_z= 0;
        try {  //6 posting files
            BufferedReader readFirst = new BufferedReader(new FileReader(new File(filesPath + "\\" + "0")));
            BufferedReader readSecond = new BufferedReader(new FileReader(filesPath + "\\" + "1"));
            BufferedWriter writeToFile = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "finalNumbers")));
            String fLine = readFirst.readLine();
            String sLine = readSecond.readLine();
            while(fLine!=null && sLine!= null &&!(Character.isLetter(fLine.charAt(0))) && !(Character.isLetter(sLine.charAt(0)))){ //the first posting is for anything but letters
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) >0) {
                    calculateWeight(sLine);
                    //insertToCache(sLine);
                    writeToFile.write(sLine + "\n");
                    Dictionary.get(sTerm).setPointerToPostings(num);
                    Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                    num += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) < 0) {
                    calculateWeight(fLine);
                    //insertToCache(fLine);
                    writeToFile.write(fLine+ "\n");
                    Dictionary.get(fTerm).setPointerToPostings(num);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                    num += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1);
                    fLine = fLine.concat(sLine);
                    calculateWeight(fLine);
                    //insertToCache(fLine);
                    fLine = fLine.concat("\n");
                    writeToFile.write(fLine);
                    Dictionary.get(sTerm).setPointerToPostings(num);
                    Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                    num += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && !(Character.isLetter(sLine.charAt(0)))){
                calculateWeight(sLine);
                //insertToCache(sLine);
                writeToFile.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                Dictionary.get(sTerm).setPointerToPostings(num);
                Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                num += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && !(Character.isLetter(fLine.charAt(0)))){
                calculateWeight(fLine);
                //insertToCache(fLine);
                writeToFile.write(fLine + "\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                Dictionary.get(fTerm).setPointerToPostings(num);
                Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                num += fLine.length() + 1;
                fLine = readFirst.readLine();
            }
            writeToFile.close();
            BufferedWriter writeToA = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "A-E")));

            while(fLine!=null && sLine!= null && ae.contains(fLine.substring(0,1)) && ae.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) >0) {
                    calculateWeight(sLine);
                    //insertToCache(sLine);
                    writeToA.write(sLine + "\n");
                    Dictionary.get(sTerm).setPointerToPostings(a_e);
                    Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                    a_e += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) <0) {
                    calculateWeight(fLine);
                    // insertToCache(fLine);
                    writeToA.write(fLine + "\n");
                    Dictionary.get(fTerm).setPointerToPostings(a_e);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                    a_e += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1);
                    fLine = fLine.concat(sLine);
                    calculateWeight(fLine);
                    // insertToCache(fLine);
                    fLine = fLine.concat("\n");
                    writeToA.write(fLine);
                    Dictionary.get(sTerm).setPointerToPostings(a_e);
                    Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                    a_e += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && ae.contains((sLine.substring(0,1)))){
                calculateWeight(sLine);
                //insertToCache(sLine);
                writeToA.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                Dictionary.get(sTerm).setPointerToPostings(a_e);
                Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                a_e += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && ae.contains((fLine.substring(0,1)))){
                calculateWeight(fLine);
                //insertToCache(fLine);
                writeToA.write(fLine+"\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                Dictionary.get(fTerm).setPointerToPostings(a_e);
                Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                a_e += fLine.length() + 1;
                fLine = readFirst.readLine();
            }
            writeToA.close();
            BufferedWriter writeToF = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "F-J")));
            while(fLine!=null && sLine!= null && fj.contains(fLine.substring(0,1)) && fj.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) >0) {
                    calculateWeight(sLine);
                    // insertToCache(sLine);
                    writeToF.write(sLine + "\n");
                    Dictionary.get(sTerm).setPointerToPostings(f_j);
                    Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                    f_j += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) <0) {
                    calculateWeight(fLine);
                    // insertToCache(fLine);
                    writeToF.write(fLine + "\n");
                    Dictionary.get(fTerm).setPointerToPostings(f_j);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                    f_j += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1);
                    fLine = fLine.concat(sLine);
                    calculateWeight(fLine);
                    // insertToCache(fLine);
                    fLine = fLine.concat("\n");
                    writeToF.write(fLine);
                    Dictionary.get(fTerm).setPointerToPostings(f_j);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                    f_j += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && fj.contains((sLine.substring(0,1)))){
                calculateWeight(sLine);
                //insertToCache(sLine);
                writeToF.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                Dictionary.get(sTerm).setPointerToPostings(f_j);
                Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                f_j += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && fj.contains((fLine.substring(0,1)))){
                calculateWeight(fLine);
                //insertToCache(fLine);
                writeToF.write(fLine + "\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                Dictionary.get(fTerm).setPointerToPostings(f_j);
                Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                f_j += fLine.length() + 1;
                fLine = readFirst.readLine();
            }

            writeToF.close();
            BufferedWriter writeToK = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "K-O")));

            while(fLine!=null && sLine!= null && ko.contains(fLine.substring(0,1)) && ko.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) >0) {
                    calculateWeight(sLine);
                    // insertToCache(sLine);
                    writeToK.write(sLine+ "\n");
                    Dictionary.get(sTerm).setPointerToPostings(k_o);
                    Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                    k_o += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm)<0) {
                    calculateWeight(fLine);
                    // insertToCache(fLine);
                    writeToK.write(fLine+ "\n");
                    Dictionary.get(fTerm).setPointerToPostings(k_o);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                    k_o += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1);
                    fLine = fLine.concat(sLine);
                    calculateWeight(fLine);
                    // insertToCache(fLine);
                    fLine = fLine.concat("\n");
                    writeToK.write(fLine);
                    Dictionary.get(fTerm).setPointerToPostings(k_o);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                    k_o += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && ko.contains((sLine.substring(0,1)))){
                calculateWeight(sLine);
                // insertToCache(sLine);
                writeToK.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                Dictionary.get(sTerm).setPointerToPostings(k_o);
                Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                k_o += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && ko.contains((fLine.substring(0,1)))){
                calculateWeight(fLine);
                // insertToCache(fLine);
                writeToK.write(fLine + "\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                Dictionary.get(fTerm).setPointerToPostings(k_o);
                Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                k_o += fLine.length() + 1;
                fLine = readFirst.readLine();
            }

            writeToK.close();
            BufferedWriter writeToP = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "P-T")));

            while(fLine!=null && sLine!= null && pt.contains(fLine.substring(0,1)) && pt.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) >0) {
                    calculateWeight(sLine);
                    //  insertToCache(sLine);
                    writeToP.write(sLine+ "\n");
                    Dictionary.get(sTerm).setPointerToPostings(p_t);
                    Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                    p_t += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) <0) {
                    calculateWeight(fLine);
                    //  insertToCache(fLine);
                    writeToP.write(fLine+ "\n");
                    Dictionary.get(fTerm).setPointerToPostings(p_t);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                    p_t += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1);
                    fLine = fLine.concat(sLine);
                    calculateWeight(fLine);
                    // insertToCache(fLine);
                    fLine = fLine.concat("\n");
                    writeToP.write(fLine);
                    Dictionary.get(fTerm).setPointerToPostings(p_t);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                    p_t += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && pt.contains((sLine.substring(0,1)))){
                calculateWeight(sLine);
                // insertToCache(sLine);
                writeToP.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                Dictionary.get(sTerm).setPointerToPostings(p_t);
                Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                p_t += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && pt.contains((fLine.substring(0,1)))){
                calculateWeight(fLine);
                //insertToCache(fLine);
                writeToP.write(fLine + "\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                Dictionary.get(fTerm).setPointerToPostings(p_t);
                Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                p_t += fLine.length() + 1;
                fLine = readFirst.readLine();
            }

            writeToP.close();
            BufferedWriter writeToU = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "U-Z")));

            while(fLine!=null && sLine!= null && uz.contains(fLine.substring(0,1)) && uz.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) >0) {
                    calculateWeight(sLine);
                    // insertToCache(sLine);
                    writeToU.write(sLine+ "\n");
                    Dictionary.get(sTerm).setPointerToPostings(u_z);
                    Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                    u_z += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) <0) {
                    calculateWeight(fLine);
                    // insertToCache(fLine);
                    writeToU.write(fLine+ "\n");
                    Dictionary.get(fTerm).setPointerToPostings(u_z);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                    u_z += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1);
                    fLine = fLine.concat(sLine);
                    calculateWeight(fLine);
                    // insertToCache(fLine);
                    fLine = fLine.concat("\n");
                    writeToU.write(fLine);
                    Dictionary.get(fTerm).setPointerToPostings(u_z);
                    Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                    u_z += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && uz.contains((sLine.substring(0,1)))){
                calculateWeight(sLine);
                // insertToCache(sLine);
                writeToU.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                Dictionary.get(sTerm).setPointerToPostings(u_z);
                Dictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                u_z += sLine.length()+1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && uz.contains((fLine.substring(0,1)))){
                calculateWeight(fLine);
                // insertToCache(fLine);
                writeToU.write(fLine + "\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                Dictionary.get(fTerm).setPointerToPostings(u_z);
                Dictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                u_z += fLine.length()+1;
                fLine = readFirst.readLine();
            }
            writeToU.close();
            readFirst.close();
            readSecond.close();
            writeToFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * method to write teh documents data structure to a file.
     */
    public void insertDocumentsToFile() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("documents")));
            for (Map.Entry<String, Document> doc : documents.entrySet()) {

                Document current = doc.getValue();
                String documentEntry = current.getDocNo() + ":" + current.getPath() + "," + current.getPositionInFile() + "," + current.getLength() + "," + current.getMaxTf() +"," + current.getWeight() + "\n";
                bufferedWriter.write(documentEntry);
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * calculate the weight for all the documents - the sum of the weight of each word in the document power 2
     * @param posting
     */
    public void calculateWeight(String posting){
        String term = posting.substring(0,posting.indexOf(":"));
        if(!term.equals("")) {
            Term t = Dictionary.get(term);
            double idf = Math.log(N / t.getDf());
            String postingDocs = posting.substring(posting.indexOf(":")+1);
            String tempPost = postingDocs;
            while(!tempPost.equals("")) {
                String docNo = tempPost.substring(0, tempPost.indexOf(":"));
                tempPost = tempPost.substring(tempPost.indexOf(":")+1);
                String docTf = tempPost.substring(0, tempPost.indexOf(","));
                Double tf = Double.parseDouble(docTf);
                tempPost = tempPost.substring(tempPost.indexOf(",")+1);
                Double finalWeight = Math.pow(tf * idf, 2);
               Document doc = documents.get(docNo);
               doc.setWeight(finalWeight);
            }
        }
    }

    /**
     * get 2 files and merge them to a third sorted file.
     * @param first
     * @param second
     * @param writeToFile
     */
    public void helpMerge(BufferedReader first, BufferedReader second, BufferedWriter writeToFile) {    // a help function for merging 2 temp posting file for one
        try {
            //read the first lines from 2 files
            String fLine = first.readLine();
            String sLine = second.readLine();

            //running until end of one file
            while (fLine != null && sLine != null) {

                String fTerm = fLine.substring(0, fLine.indexOf(':'));
                String sTerm = sLine.substring(0, sLine.indexOf(':'));

                if (fTerm.compareTo(sTerm) > 0) {  //if the second term is smaller than the first term - we will write the second to the file
                    writeToFile.write(sLine + "\n");
                    sLine = second.readLine();
                } else if (fTerm.compareTo(sTerm) < 0) {  // if the first term is smaller than the second
                    writeToFile.write(fLine + "\n");
                    fLine = first.readLine();
                } else {    //if its the same term - we need to merge between the information
                    sLine = sLine.substring(sLine.indexOf(':') + 1, sLine.length());
                    fLine = fLine.concat(sLine);
                    fLine = fLine.concat("\n");
                    writeToFile.write(fLine);
                    fLine = first.readLine();
                    sLine = second.readLine();
                }
            }
            if (fLine == null) {
                while (sLine != null) {
                    writeToFile.write(sLine + "\n");
                    sLine = second.readLine();
                }
            } else {
                while (fLine != null) {
                    writeToFile.write(fLine + "\n");
                    fLine = first.readLine();
                }
            }
            first.close();
            second.close();
            writeToFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * this method helps create the cache
     */
    public void createCacheWordFile() {
        List<Term> allTerms = new ArrayList<>(Dictionary.values());
        allTerms.sort(new Comparator<Term>() {
            @Override
            public int compare(Term o1, Term o2) {
                return o1.compareTo(o2);
            }
        });
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("CacheWords")));
            for (int i = 0; i < 10000; i++) {
                bufferedWriter.write(allTerms.get(i).getTerm());
                bufferedWriter.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * method to initial the cache
     */
    public void createCache() {
      try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("CacheWords")));
            String term = "";
            while((term = bufferedReader.readLine())!= null) {
                if(term.equals("") || term.equals("\n"))
                    break;
                cache.put(term, "");
            }
            bufferedReader.close();
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * insert a relevant term to the cache and the first 10 relevant documents fromt he postiing file.
     * @param postingLine
     */
    public void insertToCache(String postingLine) {
        if(cache.containsKey(postingLine.substring(0, postingLine.indexOf(":")))) {
            String docs = postingLine.substring((postingLine.indexOf(":") + 1));
            int size = 10;
            Map<Double, String> toSort = new TreeMap<>();
            for (int i = 0; i < docs.length();) {
                String docno = docs.substring(i, docs.indexOf(":", i));
                Double tf = Double.parseDouble(docs.substring(docs.indexOf(":", i)+1, docs.indexOf(",", i)));
                int index = docs.indexOf(",", i);
                i = index;
                if(i == -1)
                    break;
                i++;
                toSort.put(tf, docno);
            }
            String toTheCache=postingLine.substring(0, postingLine.indexOf(":")+1) + ":";
            for (Map.Entry<Double, String> doc : toSort.entrySet()) {
                if(size <= 0)
                    break;
                String str = doc.getValue() + ":" + doc.getKey() + ",";
                toTheCache+= str;
                size--;
            }
            cache.put((postingLine.substring(0, postingLine.indexOf(":"))), toTheCache);
        }
    }

    /**
     * todo: create the reset button for part B
     * @param finalPostingFilesPath
     * @param dictionaryFilePath
     * @param caheFilePath
     */
    public void resetIndex(String finalPostingFilesPath, String dictionaryFilePath, String caheFilePath) {//todo: fix it
        //delete dictionary file
        File dictionaryFile = new File(dictionaryFilePath);
        dictionaryFile.delete();

        //delete cache file
        File cacheFile = new File(caheFilePath);
        dictionaryFile.delete();

        //get to the main directory
        File postings = new File(finalPostingFilesPath);

        //get all the directories from the main directory - the 2 postrings folders: stemmed and not stemmed
        File[] listOfDirs = postings.listFiles();

        //iterate on all the files in the folders and delete them
        for (int i = 0; i < listOfDirs.length; i++) {
            //get to the wanted file
            File temp = listOfDirs[i];
            File[] postingFolder = temp.listFiles();
            for (int j = 0; j < postingFolder.length; i++) {
                postingFolder[j].delete();
            }
            temp.delete();
        }
        postings.delete();
    }

    /**
     * create the documents data structure from the documents file.
     */
    public void createDocumentsMap() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("documents")));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                //get the docno from the line
                int indexAfterDocno = line.indexOf(':');
                String docno = line.substring(0, indexAfterDocno);
                //get the file name from the line
                int indexAfterFileName = line.indexOf(',');
                String fileName = line.substring(indexAfterDocno + 1, indexAfterFileName);
                //get the position from the line
                int indexAfterPosition = line.indexOf(',', indexAfterFileName + 1);
                long position = Long.parseLong(line.substring(indexAfterFileName + 1, indexAfterPosition));
                //get the length from the line
                int indexAfterLength = line.indexOf(',', indexAfterPosition + 1);
                int length = Integer.parseInt(line.substring(indexAfterPosition + 1, indexAfterLength));
                //get the length from the line
                int indexAfterMaxTf = line.lastIndexOf(',');
                int maxTf = Integer.parseInt(line.substring(indexAfterLength + 1, indexAfterMaxTf));
                //get the weight from the line
                double weight = Double.parseDouble(line.substring(indexAfterMaxTf + 1));
                Document document = new Document(fileName, docno, position, length, maxTf, weight);
                documents.put(docno, document);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * remove too rare words from the dictionary.
     */
    public void cleanDictionary() {
        Iterator<Map.Entry<String,Term>> iter = Dictionary.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Term> entry = iter.next();
            if(entry.getValue().getFrequencyInCorpus() < 3){
                iter.remove();
            }
        }
    }


    public static void main(String[] args) {
        Indexer in = new Indexer("D:\\corpus" , "D:\\Posting" , false);
        File file = new File("D:\\Posting\\");

        if(file != null) {
            try {
                String path = file.getAbsolutePath();
                int index = path.lastIndexOf('\\');
                if(index >= path.length()-1) {
                    path = path.substring(0, index);
                }
                ObjectInputStream objectInputStreamDict = new ObjectInputStream(new FileInputStream(new File(path + "\\" + "dictionary")));
                ObjectInputStream objectInputStreamCache = new ObjectInputStream(new FileInputStream(new File(path + "\\" + "cache")));

                in.setDictionary((HashMap<String, Term>) objectInputStreamDict.readObject());
                in.setCache((HashMap<String, String>)objectInputStreamCache.readObject());
                objectInputStreamCache.close();
                objectInputStreamDict.close();
                in.createDocumentsMap();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        Ranker rank = new Ranker(in);
        Searcher searcher = new Searcher(rank);
        List<String> s = searcher.searchForQuery("Falkland petroleum exploration");
        int queryId = 351;
        File results = new File("C:\\Users\\sivanrej\\Downloads\\results.txt");
        try {
            BufferedWriter writeToResult = new BufferedWriter(new FileWriter(results));
            for (int i = 0; i < s.size(); i++) {
                String toInsert = queryId + " 0 " + s.get(i) + " 1 1 mt" +"\r\n";
                writeToResult.write(toInsert);
            }
            writeToResult.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Indexer indexer = new Indexer();
            try {
                ObjectInputStream objectInputStreamDict = new ObjectInputStream(new FileInputStream(new File("D:\\myPosting\\dictionary")));
                HashMap<String, Term> dict = (HashMap<String, Term>) objectInputStreamDict.readObject();
                indexer.setDictionary(dict);
                objectInputStreamDict.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        HashMap<String, Term> dictionary = indexer.getDictionary();
        String term = "POLITICIANS".toLowerCase();
        Term polit = dictionary.get(term);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("D:\\myPosting\\P-T"));
            bufferedReader.skip(polit.getPointerToPostings());
            System.out.println(bufferedReader.readLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}
