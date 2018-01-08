package partA;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Searcher {
    //private File queryFile;  //todo - dont forget in the gui change the path to file
    private boolean isStemm;
    private Ranker ranker;

    public Searcher(Ranker ranker) {
        this.ranker = ranker;
    }

    public String parseQuery(String query){
        Parse parse = new Parse(isStemm, true);
        String parsedQuery = parse.callParseForQuery(query);
        return parsedQuery;
    }

    public List<String> parseQueriesInFile(File queryFile){
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

        public List<String> searchForQuery(String query) {
        List<String> results = new ArrayList<>();
        String parsedQuery = parseQuery(query);

        //todo: now the ranker suppose to return the relevant docno

        return results;
    }

    public HashMap<String, List<String>> SearchForFile(File queryFile) {
        List<String> parsedQueries = parseQueriesInFile(queryFile);
        HashMap<String, List<String>> results = new HashMap<String, List<String>>();

        //todo: now the ranker suppose to return the relevant docno

        return results;
    }
}
