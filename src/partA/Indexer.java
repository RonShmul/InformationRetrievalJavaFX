package partA;
import java.io.*;
import java.util.*;

public class Indexer {

    private HashMap<String, Term> tempDictionary;
    private HashSet<Term> dictionary;
    private HashMap<String, String> cache;
    private String corpusPath;
    private String filesPath;
    private int counterForFiles;
    private boolean toStemm;

    public Indexer(String corpusPath, String filesPath, boolean toStemm) {
        this.toStemm = toStemm;
        this.corpusPath = corpusPath;
        this.filesPath = filesPath;
        tempDictionary = new HashMap<>();
        counterForFiles = 0;
        cache = new HashMap<>();

    }

    public HashSet<Term> getDictionary() {
        return dictionary;
    }

    public HashMap<String, String> getCache() {
        return cache;
    }

    public void setDictionary(HashSet<Term> dictionary) {
        this.dictionary = dictionary;
    }

    public void setCache(HashMap<String, String> cache) {
        this.cache = cache;
    }

    public void initialize() {
        //get to the main directory
        File headDir = new File(corpusPath);

        //get all the directories from the main directory:
        File[] listOfDirs = headDir.listFiles();

        //create new readFile
        ReadFile corpus = new ReadFile(corpusPath);

        //iterate on all the files in the corpus
        for (int i = 0; i < listOfDirs.length; i++) {
            counterForFiles = i;
            //get to the wanted file
            File temp = listOfDirs[i];
            File[] currDir = temp.listFiles();
            File currFile = currDir[0];

            //create a new parse
            Parse parse = new Parse(toStemm);

            //get all the parsed terms of a specific file
            HashMap<String, MetaData> termsToIndex = parse.parse(corpus.readFile(currFile));

//            for( Map.Entry<String,MetaData> term : termsToIndex.entrySet()){
//                System.out.println(term.getKey());
//            }
            //send to a method that construct the indexing
            constructPosting(termsToIndex);
        }
        mergePosting();
        createCache();
        for (Map.Entry<String, String> cacheEntry : cache.entrySet()) {
            System.out.println(cacheEntry.getKey());
        }
    }

    private void constructPosting(HashMap<String, MetaData> termsToIndex) {
        try {
            BufferedWriter temporaryPostingFile = new BufferedWriter(new FileWriter(filesPath + "\\" + counterForFiles));
            LinkedList<String> listToTempPosting = new LinkedList<>();
            for (Map.Entry<String, MetaData> term : termsToIndex.entrySet()) {
                if (tempDictionary.get(term.getKey()) == null) {
                    Term newTerm = new Term(term.getKey(), term.getValue().getDf(), term.getValue().getFrequencyInCorpus(), -1);
                    tempDictionary.put(term.getKey(), newTerm);
                } else {
                    Term existTerm = tempDictionary.get(term.getKey());
                    existTerm.setDf(existTerm.getDf() + term.getValue().getDf());
                    existTerm.setFrequencyInCorup(existTerm.getFrequencyInCorup() + term.getValue().getFrequencyInCorpus());
                }

                String toTemp = term.getKey() + ":";
                for (Map.Entry<Document, Integer> tf : term.getValue().getFrequencyInDoc().entrySet()) {
                    toTemp = toTemp.concat(tf.getKey().getDocNo() + ":" + tf.getValue() + ",");
                }
                listToTempPosting.add(toTemp);
            }

            listToTempPosting.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
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

        try {   //6 posting files
            BufferedReader readFirst = new BufferedReader(new FileReader(new File(filesPath + "\\" + "0")));
            BufferedReader readSecond = new BufferedReader(new FileReader(filesPath + "\\" + "1"));
            BufferedWriter writeToFile = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "finalNumbers")));
            BufferedWriter writeToA = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "A-E")));
            BufferedWriter writeToF = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "F-J")));
            BufferedWriter writeToK = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "K-O")));
            BufferedWriter writeToP = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "P-T")));
            BufferedWriter writeToU = new BufferedWriter(new FileWriter(new File(filesPath + "\\" + "U-Z")));


            String fLine = readFirst.readLine();
            String sLine = readSecond.readLine();
            while(fLine!=null && sLine!= null &&!(Character.isLetter(fLine.charAt(0))) && !(Character.isLetter(sLine.charAt(0)))){ //the first posting is for anything but letters
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) == 1) {
                    writeToFile.write(sLine + "\n");
                    tempDictionary.get(sTerm).setPointerToPostings(num);
                    tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                    num += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) == -1) {
                    writeToFile.write(fLine+ "\n");
                    tempDictionary.get(fTerm).setPointerToPostings(num);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                    num += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1, sLine.length() - 1);
                    fLine = fLine.concat(sLine);
                    fLine = fLine.concat("\n");
                    writeToFile.write(fLine);
                    tempDictionary.get(sTerm).setPointerToPostings(num);
                    tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                    num += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && !(Character.isLetter(sLine.charAt(0)))){
                writeToFile.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                tempDictionary.get(sTerm).setPointerToPostings(num);
                tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                num += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && !(Character.isLetter(fLine.charAt(0)))){
                writeToFile.write(fLine);
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                tempDictionary.get(fTerm).setPointerToPostings(num);
                tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "finalNumbers");
                num += fLine.length() + 1;
                fLine = readFirst.readLine();
            }

            writeToFile.close();

            while(fLine!=null && sLine!= null && ae.contains(fLine.substring(0,1)) && ae.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) == 1) {
                    writeToA.write(sLine + "\n");
                    tempDictionary.get(sTerm).setPointerToPostings(a_e);
                    tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                    a_e += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) == -1) {
                    writeToA.write(fLine + "\n");
                    tempDictionary.get(fTerm).setPointerToPostings(a_e);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                    a_e += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1, sLine.length() - 1);
                    fLine = fLine.concat(sLine);
                    fLine = fLine.concat("\n");
                    writeToA.write(fLine);
                    tempDictionary.get(sTerm).setPointerToPostings(a_e);
                    tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                    a_e += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && ae.contains((sLine.substring(0,1)))){
                writeToA.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                tempDictionary.get(sTerm).setPointerToPostings(a_e);
                tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                a_e += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && ae.contains((fLine.substring(0,1)))){
                writeToA.write(fLine+"\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                tempDictionary.get(fTerm).setPointerToPostings(a_e);
                tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "A-E");
                a_e += fLine.length() + 1;
                fLine = readFirst.readLine();
            }
            writeToA.close();

            while(fLine!=null && sLine!= null && fj.contains(fLine.substring(0,1)) && fj.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) == 1) {
                    writeToF.write(sLine + "\n");
                    tempDictionary.get(sTerm).setPointerToPostings(f_j);
                    tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                    f_j += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) == -1) {
                    writeToF.write(fLine + "\n");
                    tempDictionary.get(fTerm).setPointerToPostings(f_j);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                    f_j += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1, sLine.length() - 1);
                    fLine = fLine.concat(sLine);
                    fLine = fLine.concat("\n");
                    writeToF.write(fLine);
                    tempDictionary.get(fTerm).setPointerToPostings(f_j);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                    f_j += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && fj.contains((sLine.substring(0,1)))){
                writeToF.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                tempDictionary.get(sTerm).setPointerToPostings(f_j);
                tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                f_j += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && fj.contains((fLine.substring(0,1)))){
                writeToF.write(fLine + "\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                tempDictionary.get(fTerm).setPointerToPostings(f_j);
                tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "F-J");
                f_j += fLine.length() + 1;
                fLine = readFirst.readLine();
            }

            writeToF.close();

            while(fLine!=null && sLine!= null && ko.contains(fLine.substring(0,1)) && ko.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) == 1) {
                    writeToK.write(sLine+ "\n");
                    tempDictionary.get(sTerm).setPointerToPostings(k_o);
                    tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                    k_o += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) == -1) {
                    writeToK.write(fLine+ "\n");
                    tempDictionary.get(fTerm).setPointerToPostings(k_o);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                    k_o += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1, sLine.length() - 1);
                    fLine = fLine.concat(sLine);
                    fLine = fLine.concat("\n");
                    writeToK.write(fLine);
                    tempDictionary.get(fTerm).setPointerToPostings(k_o);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                    k_o += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && ko.contains((sLine.substring(0,1)))){
                writeToK.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                tempDictionary.get(sTerm).setPointerToPostings(k_o);
                tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                k_o += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && ko.contains((fLine.substring(0,1)))){
                writeToK.write(fLine + "\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                tempDictionary.get(fTerm).setPointerToPostings(k_o);
                tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "K-O");
                k_o += fLine.length() + 1;
                fLine = readFirst.readLine();
            }

            writeToK.close();

            while(fLine!=null && sLine!= null && pt.contains(fLine.substring(0,1)) && pt.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) == 1) {
                    writeToP.write(sLine+ "\n");
                    tempDictionary.get(sTerm).setPointerToPostings(p_t);
                    tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                    p_t += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) == -1) {
                    writeToP.write(fLine+ "\n");
                    tempDictionary.get(fTerm).setPointerToPostings(p_t);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                    p_t += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1, sLine.length() - 1);
                    fLine = fLine.concat(sLine);
                    fLine = fLine.concat("\n");
                    writeToP.write(fLine);
                    tempDictionary.get(fTerm).setPointerToPostings(p_t);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                    p_t += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && pt.contains((sLine.substring(0,1)))){
                writeToP.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                tempDictionary.get(sTerm).setPointerToPostings(p_t);
                tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                p_t += sLine.length() + 1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && pt.contains((fLine.substring(0,1)))){
                writeToP.write(fLine + "\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                tempDictionary.get(fTerm).setPointerToPostings(p_t);
                tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "P-T");
                p_t += fLine.length() + 1;
                fLine = readFirst.readLine();
            }

            writeToP.close();

            while(fLine!=null && sLine!= null && uz.contains(fLine.substring(0,1)) && uz.contains((sLine.substring(0,1)))){
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) == 1) {
                    writeToU.write(sLine+ "\n");
                    tempDictionary.get(sTerm).setPointerToPostings(u_z);
                    tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                    u_z += sLine.length() + 1;
                    sLine = readSecond.readLine();
                } else if (fTerm.compareTo(sTerm) == -1) {
                    writeToU.write(fLine+ "\n");
                    tempDictionary.get(fTerm).setPointerToPostings(u_z);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                    u_z += fLine.length() + 1;
                    fLine = readFirst.readLine();
                } else {
                    sLine = sLine.substring(sLine.indexOf(":") + 1, sLine.length() - 1);
                    fLine = fLine.concat(sLine);
                    fLine = fLine.concat("\n");
                    writeToU.write(fLine);
                    tempDictionary.get(fTerm).setPointerToPostings(u_z);
                    tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                    u_z += fLine.length();
                    fLine = readFirst.readLine();
                    sLine = readSecond.readLine();
                }
            }
            while(sLine!=null && uz.contains((sLine.substring(0,1)))){
                writeToU.write(sLine + "\n");
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                tempDictionary.get(sTerm).setPointerToPostings(u_z);
                tempDictionary.get(sTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                u_z += sLine.length()+1;
                sLine = readSecond.readLine();
            }
            while(fLine!=null && uz.contains((fLine.substring(0,1)))){
                writeToU.write(fLine + "\n");
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                tempDictionary.get(fTerm).setPointerToPostings(u_z);
                tempDictionary.get(fTerm).setPostingFilePath(filesPath + "\\" + "U-Z");
                u_z += fLine.length()+1;
                fLine = readFirst.readLine();
            }

            readFirst.close();
            readSecond.close();
            writeToFile.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void helpMerge(BufferedReader first, BufferedReader second, BufferedWriter writeToFile) {    // a help function for merging 2 temp posting file for one
        try {
            //read the first lines from 2 files
            String fLine = first.readLine();
            String sLine = second.readLine();

            //running until end of one file
            while (fLine != null && sLine != null) {
                String fTerm = fLine.substring(0, fLine.indexOf(":", 0));
                String sTerm = sLine.substring(0, sLine.indexOf(":", 0));
                if (fTerm.compareTo(sTerm) == 1) {  //if the second term is smaller than the first term - we will write the second to the file
                    writeToFile.write(sLine + "\n");
                    sLine = second.readLine();
                } else if (fTerm.compareTo(sTerm) == -1) {  // if the first term is smaller than the second
                    writeToFile.write(fLine + "\n");
                    fLine = first.readLine();
                } else {    //if its the same term - we need to merge between the information
                    sLine = sLine.substring(sLine.indexOf(":") + 1, sLine.length());
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

    public void createCache() {
        List<Term> allTerms = new ArrayList<>(tempDictionary.values());
        allTerms.sort(new Comparator<Term>() {
            @Override
            public int compare(Term o1, Term o2) {
                return o1.compareTo(o2);
            }
        });
        int counter=0;

        for (int i = 0; counter <10000; i++) {
            try {
                if(allTerms.get(i).getPostingFilePath()!= null && allTerms.get(i).getTerm()!= null) {
                    BufferedReader posting = new BufferedReader(new FileReader(new File(allTerms.get(i).getPostingFilePath())));
                    posting.skip(allTerms.get(i).getPointerToPostings());
                    String fromPosting = posting.readLine();
                    if (fromPosting != null) {
                        int index = 0;
                        for (int j = 0; j < 3; j++) {
                            if (index < fromPosting.length())
                                index = fromPosting.indexOf(",", index + 1);
                        }
                        if (index > 0) {
                            String toCache = fromPosting.substring(fromPosting.indexOf(":") + 1, index);
                            cache.put(allTerms.get(i).getTerm(), toCache);
                        } else {
                            String toCache = fromPosting.substring(fromPosting.indexOf(":") + 1);
                            cache.put(allTerms.get(i).getTerm(), toCache);
                        }
                        counter++;
                        posting.close();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dictionary = new HashSet<>(allTerms);
    }

    public void resetIndex(String finalPostingFilesPath, String dictionaryFilePath, String caheFilePath) {
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

}
