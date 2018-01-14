package partA;

import javafx.collections.ObservableMap;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.*;

public class Controller {


    private boolean toStemm;
    private String corpusPath;
    private String postingsPath;
    private String dictAndcachePath;
    private Indexer indexer;
    private Searcher searcher;

    public Controller() {
        toStemm = false;
        indexer = new Indexer();
        searcher = new Searcher(new Ranker(indexer));
    }

    public void setCorpusPath(String corpusPath) {
        this.corpusPath = corpusPath;
    }

    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    public String getCorpusPath() {
        return corpusPath;
    }

    public Indexer getIndexer() {
        return indexer;
    }

    public void setToStemm(Boolean newValue) {
        this.toStemm = newValue;
        indexer.setToStemm(newValue);
    }

    public boolean isToStemm() {

        return toStemm;
    }

    public String chooseFolder() {
        DirectoryChooser fc = new DirectoryChooser();
        fc.setTitle("Choose Folder");
        //fc.setInitialDirectory(new File("resources"));
        File file = fc.showDialog(null);
        String path = null;
        if(file != null) {
            path = file.getAbsolutePath();
        }
        return path;
    }

    public void startIndexing(String DataSetpath, String LoactionPath) {
        postingsPath = LoactionPath;
        corpusPath = DataSetpath;
        indexer = new Indexer(DataSetpath, LoactionPath, toStemm);
        indexer.initialize();
    }

    public void saveDictionaryAndCache() {
        DirectoryChooser fc = new DirectoryChooser();
        fc.setTitle("Save Dictionary And Cache (choose folder)");
        File file = fc.showDialog(null);
        if(file != null) {
            try {
                String path = file.getAbsolutePath();
                int index = path.lastIndexOf('\\');
                if(index >= path.length()-1) {
                    path = path.substring(0, index);
                }
                String dictionarySavePath = path + "\\" +"dictionary";
                String cacheSavePath =  path +"\\" +"cache";
                dictAndcachePath = file.getAbsolutePath();
                ObjectOutputStream objectOutputStreamDict = new ObjectOutputStream(new FileOutputStream(new File(dictionarySavePath)));
                ObjectOutputStream objectOutputStreamCache = new ObjectOutputStream(new FileOutputStream(new File(cacheSavePath)));
                objectOutputStreamDict.writeObject(indexer.getDictionary());
                objectOutputStreamCache.writeObject(indexer.getCache());
                objectOutputStreamDict.close();
                objectOutputStreamCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadIndex() {
        DirectoryChooser fc = new DirectoryChooser();
        fc.setTitle("Load Index Files");
        File file = fc.showDialog(null);
        if (file != null) {
            String path = file.getAbsolutePath();
            int index = path.lastIndexOf('\\');
            if (index >= path.length() - 1) {
                path = path.substring(0, index);
            }
            corpusPath = path + "\\" + "corpus";
            indexer.generateIndex(path);
        }
    }

    public List<Term> showDictionary() {
        List<Term> terms = indexer.getDictionaryForShow();
        terms.sort(new Comparator<Term>() {
            @Override
            public int compare(Term o1, Term o2) {
                return o1.getTerm().compareTo(o2.getTerm());
            }
        });
        return terms;
    }

    public TreeMap<String, String> showCache() {
        return new TreeMap<>(indexer.getCache());
    }

    public List<String> getSentencesForDocno(String Docno) {
        Searcher searcher = new Searcher(new Ranker(indexer));
        List<String> result = searcher.searchDocument(Docno, corpusPath);
       return result;
    }

    public List<String> getDocnosListForAQuery(String searchText) {
        Searcher searcher = new Searcher(new Ranker(indexer));
        List<String> result = searcher.searchForQuery(searchText);
        return result;
    }

    public HashMap<String, List<String>> getDocnosListsForQueriesFile(String filePath) {
        Searcher searcher = new Searcher(new Ranker(indexer));
       HashMap<String, List<String>> result = searcher.SearchForFile(new File(filePath));

        return result;
    }

    public String chooseFile() {
        DirectoryChooser fc = new DirectoryChooser();
        fc.setTitle("Choose Folder");
        //fc.setInitialDirectory(new File("resources"));

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Query File");
        File file = fileChooser.showOpenDialog(null);
        String path = null;
        if(file != null) {
            path = file.getAbsolutePath();
        }
        return path;
    }
    public File chooseSaveFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save result");
        File file = fc.showSaveDialog(null);
        return file;
    }
    public void writeQueryResultToFile(File file, List<String> list) {
        searcher.createQueryTextResultFile(file, list);
    }
    public void writeQueriesFileResultToFile(File file, HashMap<String, List<String>> list) {
        searcher.createQueriesResultFile(file, list);
    }
}
