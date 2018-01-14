package partA;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Searcher {
    private Ranker ranker;

    public Searcher(Ranker ranker) { //constructor
        this.ranker = ranker;
    }

    /**
     * parsing the query
     * @param query
     * @return
     */
    public String parseQuery(String query) {
        Parse parse = new Parse(ranker.getIndexer().isToStemm(), true);  //create parse for the query
        String parsedQuery = parse.callParseForQuery(query);
        return parsedQuery;
    }

    /**
     * insert query number and query content into a hashMap
     * @param queryFile
     * @return
     */
    public HashMap<String, String> QueriesInFile(File queryFile) {

        HashMap<String, String> Queries = new HashMap<>();
        try {
            BufferedReader readQueries = new BufferedReader(new FileReader(queryFile));
            String line = readQueries.readLine();  //read from the query file
            String query = null;
            String number=null;
            while (line != null) {
                if(line.contains("<num>")) {  //find the query's number
                    number = line.substring(line.indexOf(" ", line.indexOf(":")) + 1).trim();
                }
                if (line.contains("<title>")) {   //find the query's content
                    query = line.substring(line.indexOf(" ") + 1);

                }
                if(number != null && query !=null) {
                    Queries.put(query, number);   //insert number and content to hashMap
                    number = null;
                    query = null;
                }
                line = readQueries.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Queries;
    }

    /**
     * returns the relevant DOCNOs for a specific query
     * @param query
     * @return
     */
    public List<String> searchForQuery(String query) {
        String nQuery = query+" ";  //the parser works with space in the end
        String parsedQuery = parseQuery(nQuery);  //send the query to parse
        List<String> results = ranker.ranking(parsedQuery);  // the ranking is in the Ranker class
        return results;
    }

    /**
     * returns relevant DOCNOs for each query in the file
     * @param queryFile
     * @return
     */
    public HashMap<String, List<String>> SearchForFile(File queryFile) {
        HashMap<String, String> Queries = QueriesInFile(queryFile);
        HashMap<String, List<String>> results = new HashMap<String, List<String>>();
        Parse parse = new Parse(ranker.getIndexer().isToStemm(), true);
        for (Map.Entry<String, String> query : Queries.entrySet()) { //for each query send it to parse and then to ranking
            String specificQueryNumber = query.getValue();
            String specificQuery = parse.callParseForQuery(query.getKey());
            List<String> result = ranker.ranking(specificQuery);
            results.put(specificQueryNumber+": "+ query.getKey() , result);
        }
        return results;
    }

        /**
     * get the results for a queries file and a file to save the result to and write the results to the file in the TREC format
     * @param file
     * @param result
     */
    public void createQueriesResultFile(File file, HashMap<String, List<String>> result) {
        //insert the result data structure to a list for sorting by query number
        List<Map.Entry<String, List<String>>> toSort = new ArrayList<>(result.entrySet());
        toSort.sort(new Comparator<Map.Entry<String, List<String>>>() {
            @Override
            public int compare(Map.Entry<String, List<String>> o1, Map.Entry<String, List<String>> o2) {
                int q1 = Integer.parseInt(o1.getKey().substring(0, o1.getKey().indexOf(":")));
                int q2 = Integer.parseInt(o2.getKey().substring(0, o2.getKey().indexOf(":")));
                if(q1 < q2)
                    return -1;
                else if(q1 > q2)
                    return 1;
                return 0;
            }
        });
        //write the sorted results to the given file
        try {
            BufferedWriter writeToResult = new BufferedWriter(new FileWriter(file));
            for(Map.Entry<String, List<String>> queryResult : toSort) {
                List<String> s = queryResult.getValue();
                for (int i = 0; i < s.size() ; i++) {
                    String queryNumber = queryResult.getKey().substring(0, queryResult.getKey().indexOf(":"));
                    String toInsert =  queryNumber + " 0 " + s.get(i) + " 1 1 mt" + "\r\n";
                    writeToResult.write(toInsert);
                }
            }
            writeToResult.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get query result and write it to the given file in the TREC format
     * @param file
     * @param result
     */
    public void createQueryTextResultFile(File file, List<String> result) {
        //give a random id
        int Min = 1, Max = 1000;
        int ID = Min + (int)(Math.random() * ((Max - Min) + 1));
        //write the result to the given file
        try {
            BufferedWriter writeToResult = new BufferedWriter(new FileWriter(file));

                for (int i = 0; i < result.size() ; i++) {
                    String toInsert =  ID + " 0 " + result.get(i) + " 1 1 mt" + "\r\n";
                    writeToResult.write(toInsert);
                }
            writeToResult.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Search document in the corpus and return 5 most relevant sentences in the given DOCNO
     * @param docno
     * @param corpusPath
     * @return List
     */
    public List<String> searchDocument(String docno, String corpusPath) {
        //get the documents data structure
        HashMap<String, Document> documents = ranker.getIndexer().getDocuments();
        Document document = documents.get(docno);
        //get all the properties needed to get in the corpus to the given DOCNO
        String fileName = document.getPath();
        long positionInFile = document.getPositionInFile();
        //get the content of the given DOCNO
        String content = getTheDocumentText(corpusPath, fileName, positionInFile);
        //return the 5 most relevant sentences in the given content
        return getFiveImportantSentences(content);
    }

    /**
     * return the content of a given document - get the corpus path, file name and the position of the wanted document in the file.
     * @param corpusPath
     * @param fileName
     * @param positionInFile
     * @return
     */
    private String getTheDocumentText(String corpusPath, String fileName, long positionInFile) {
        String documentText = null;
        try {
            //open the file where the wanted document is
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(corpusPath + "\\" + fileName + "\\" + fileName)));
            //skip to the correct position in the file where the document content begin
            bufferedReader.skip(positionInFile);
            StringBuilder docText = new StringBuilder();
            String line = null;
            //get all the document content into a StringBuilder (docText)
            while ((line = bufferedReader.readLine()) != null) {
                docText.append(line);
                docText.append(" ");
                if (line.length() <= 8 && line.contains("</TEXT>")) {
                    break;
                }
            }
            //get the content as string
            documentText = docText.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ReadFile readFile = new ReadFile();
        //get only the content between <TEXT> and </TEXT> and return it
        return readFile.readDocContent(documentText);
    }

    /**
     * return the 5 most relevant sentences in a given content
     * @param content
     * @return List
     */
    private List<String> getFiveImportantSentences(String content) {
        int documentLength = 0;
        Parse parse = new Parse(false, true);
        //data structure for all the term in the content and their frequency
        HashMap<String, Integer> terms = new HashMap<>();
        //data structure for each sentence - get the terms in the sentence and their corresponding index in the document
        LinkedHashMap<String, HashMap<String, Integer>> termsInSentences = new LinkedHashMap<>();
        String[] sentences = content.split(Pattern.quote(". "));
        for (int i = 0; i < sentences.length; i++) {
            //ignore irrelevant sentences - sentences with tags and sentences with less then 3 words.
            if(sentences[i].length() < 3 || sentences[i].contains("<"))
                continue;
            //save for each term its index in the document
            HashMap<String, Integer> termsIndexes = new HashMap<>();
            String sentence = sentences[i];
            //parse the sentence and insert its terms to an array
            String ParsedSentence = parse.callParseForQuery(sentence + " ");
            String[] termsInSentence = ParsedSentence.split(" ");
            //iterate the parsed terms and insert them to the terms map and indexes map
            for (int j = 0; j < termsInSentence.length; j++) {
                //ignore irrelevant terms - empty string and tags
                if(termsInSentence[j].length() == 0|| termsInSentence[j].contains("<"))
                    continue;
                if (terms.containsKey(termsInSentence[j])) {
                    Integer tf = terms.get(termsInSentence[j]);
                    terms.put(termsInSentence[j], tf + 1);  //update tf
                } else {
                    terms.put(termsInSentence[j], 1);
                }
                if (!termsIndexes.containsKey(termsInSentence[j]))
                    termsIndexes.put(termsInSentence[i], documentLength);
                documentLength++;
            }
            //insert the sentence with its terms and their indexes
            termsInSentences.put(sentences[i], termsIndexes);
        }
        //create the data structure to the sentences with their rank
        HashMap<String, Double> rankedSentences = new HashMap<>();
        //iterate the sentences and calculate the rank for each sentence from the indexes and tf
        for (Map.Entry<String, HashMap<String, Integer>> sentence : termsInSentences.entrySet()) {
            HashMap<String, Integer> termsInSentence = sentence.getValue();
            double totalRank = 0;
            for (Map.Entry<String, Integer> term : termsInSentence.entrySet()) {
                double rank = (double) term.getValue();
                //normalize the index
                rank = (documentLength - rank) / documentLength;
                int tf = terms.get(term.getKey());
                //normalize the frequency (tf) and add the normalized index - this is the total rank for the sentence
                totalRank += (tf/sentence.getKey().length()+rank);
            }
            //put the result in the data structure
            rankedSentences.put(sentence.getKey(), totalRank);
        }
        //sort by rank
        List<Map.Entry<String, Double>> toSort = new ArrayList<>(rankedSentences.entrySet());
        toSort.sort(new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                if (o1.getValue() < o2.getValue()) {
                    return 1;
                } else if (o1.getValue() > o2.getValue()) {
                    return -1;
                } else return 0;
            }
        });
        //create the returned list of 5 most relevant sentences
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            result.add(toSort.get(i).getKey());
        }
        return result;
    }
    public static void main(String[] args) {
        Indexer indexer = new Indexer("D:\\corpus", "D:\\Posting", true);
        indexer.generateIndex("D:\\Posting");
        Searcher searcher = new Searcher(new Ranker(indexer));
        HashMap<String, List<String>> results = searcher.SearchForFile(new File("D:\\queries.txt"));
        searcher.createQueriesResultFile(new File("C:\\Users\\sivanrej\\Downloads\\results.txt"), results);
    }
    }
