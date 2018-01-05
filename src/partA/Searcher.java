package partA;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Searcher {
    private String query;
    private File queryFile;

    public Searcher(String query) {
        this.query = query;
    }

    public Searcher(File queryFile) {
        this.queryFile = queryFile;
    }

    public List<String> searchResultForString() {
        List<String> results = new ArrayList<>();
        Ranker ranker = new Ranker();
        //todo: continue after ranker is ready


        return results;
    }

    public HashMap<String, List<String>> SearchResultForFile(List<String> queries) {
        HashMap<String, List<String>> results = new HashMap<String, List<String>>();
        Ranker ranker = new Ranker();
        //todo: continue after ranker is ready


        return results;
    }

    public List<String> FromFileToQueries() {
        List<String> queriesInfile = new ArrayList<String>();
        String queryFromFile = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(queryFile));
            while((queryFromFile = bufferedReader.readLine())!= null) {
                if(queryFromFile.contains("<title>")) {
                    String toAdd = queryFromFile.replaceAll("<title> ", "").replaceAll("\r\n","");
                    queriesInfile.add(toAdd);
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return queriesInfile;
    }
}
