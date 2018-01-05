package partA;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class Controller {


    private boolean toStemm;
    private String corpusPath;
    private String postingsPath;
    private String dictAndcachePath;
    private Indexer indexer;


    public Controller() {
        toStemm = false;
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
        this.toStemm = toStemm;
    }

    public boolean isToStemm() {

        return toStemm;
    }

    public String chooseFolder() {
        DirectoryChooser fc = new DirectoryChooser();
        fc.setTitle("Choose DataSet");
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
//        fc.setInitialDirectory(new File("resources"));
        File file = fc.showDialog(null);
        if(file != null) {
            try {
                String dictionarySavePath = file.getAbsolutePath() + "\\"+"dictionary";
                String cacheSavePath =  file.getAbsolutePath()+"\\"+"cache";
                dictAndcachePath = file.getAbsolutePath();
                ObjectOutputStream objectOutputStreamDict = new ObjectOutputStream(new FileOutputStream(dictionarySavePath));
                ObjectOutputStream objectOutputStreamCache = new ObjectOutputStream(new FileOutputStream(cacheSavePath));
                objectOutputStreamDict.writeObject(indexer.getDictionary());
                objectOutputStreamCache.writeObject(indexer.getCache());
                objectOutputStreamCache.close();
                objectOutputStreamDict.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //todo: write the dictionary and cache to 2 different files
        }
//        setChanged();
//        notifyObservers();//todo check if i need it
    }

    public void loadDictionaryAndCache() {
        DirectoryChooser fc = new DirectoryChooser();
        fc.setTitle("Load Dictionary And Cache");
//        fc.setInitialDirectory(new File("resources"));
        File file = fc.showDialog(null);
        if(file != null) {
            try {
                ObjectInputStream objectInputStreamDict = new ObjectInputStream(new FileInputStream(file+ "\\" + "dictionary"));
                ObjectInputStream objectInputStreamCache = new ObjectInputStream(new FileInputStream(file+ "\\" + "cache"));

                indexer.setDictionary((HashSet<Term>) objectInputStreamDict.readObject());
                indexer.setCache((HashMap<String, String>)objectInputStreamCache.readObject());
                objectInputStreamCache.close();
                objectInputStreamDict.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            //todo read from a file to a field here or in the indexer who will be in the field
        }

//        setChanged();
//        notifyObservers();

    }
    public void dataAfterIndexing() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("partA.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage stage = new Stage();
            stage.setTitle("Indexing Is Done");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        } catch (Exception e) {

        }
    }

    public List<Term> showDictionary() {
        List<Term> dictlist = new ArrayList<>(/*indexer.getDictionary()*/);
        dictlist.sort(new Comparator<Term>() {
            @Override
            public int compare(Term o1, Term o2) {
                return o1.getTerm().compareTo(o2.getTerm());
            }
        });
        return dictlist;
    }

    public List<String> showCache() {
        List<String> cache = new ArrayList<String>(indexer.getCache().keySet());
        cache.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        return cache;
    }

    public void reset() {
        indexer.resetIndex(postingsPath,dictAndcachePath+"dictionary", dictAndcachePath+"cache");
    }
}
