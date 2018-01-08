package partA;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Searcher {
    private String query;
    private File queryFile;  //todo - dont forget in the gui change the path to file
    private boolean isStemm;

    public Searcher(String query, boolean isStemm) {
        this.query = query;
        this.isStemm = isStemm;
    }

    public Searcher(File queryFile, boolean isStemm) {
        this.queryFile = queryFile;
        this.isStemm = isStemm;
    }

    public String parseQuery(){
        Parse parse = new Parse(isStemm, true);
        String parsedQuery = parse.callParseForQuery(query);
        return parsedQuery;
    }

    public List<String> parseQueriesInFile(){
        List<String> parsedQueries = new ArrayList<>();
        try {
            Parse parse = new Parse(isStemm, true);
            BufferedReader readQueries = new BufferedReader(new FileReader(queryFile));
            String line = readQueries.readLine();
            while(line != null){
                if(line.contains("<title>")){
                    String specificQ = line.substring(line.indexOf(" ")+1);
                    String afterParse = parse.callParseForQuery(specificQ);
                    parsedQueries.add(afterParse);
                }
                line = readQueries.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parsedQueries;
    }

        public List<String> searchResultForString() {
        List<String> results = new ArrayList<>();
        String parsedQuery = parseQuery();
        Ranker ranker = new Ranker();
        //todo: now the ranker suppose to return the relevant docno

        return results;
    }

    public HashMap<String, List<String>> SearchResultForFile() {
        List<String> parsedQueries = parseQueriesInFile();
        HashMap<String, List<String>> results = new HashMap<String, List<String>>();
        Ranker ranker = new Ranker();
        //todo: now the ranker suppose to return the relevant docno

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
