package partA;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Searcher {
    //private File queryFile;  //todo - dont forget in the gui change the path to file
    private boolean isStemm;
    private Ranker ranker;

    public Searcher(Ranker ranker) {
        this.ranker = ranker;
    }

    public String parseQuery(String query) {
        Parse parse = new Parse(isStemm, true);
        String parsedQuery = parse.callParseForQuery(query);
        return parsedQuery;
    }

    public List<String> parseQueriesInFile(File queryFile) {
        List<String> parsedQueries = new ArrayList<>();
        try {
            Parse parse = new Parse(isStemm, true);
            BufferedReader readQueries = new BufferedReader(new FileReader(queryFile));
            String line = readQueries.readLine();
            while (line != null) {
                if (line.contains("<title>")) {
                    String specificQ = line.substring(line.indexOf(" ") + 1);
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
        String nQuery = query+" ";
        String parsedQuery = parseQuery(nQuery);
        System.out.println(parsedQuery);
        List<String> results = ranker.ranking(parsedQuery);
        return results;
    }

    public HashMap<String, List<String>> SearchForFile(File queryFile) {
        List<String> parsedQueries = parseQueriesInFile(queryFile);
        HashMap<String, List<String>> results = new HashMap<String, List<String>>();
        for (int i = 0; i < parsedQueries.size(); i++) {
            String specificQuery = parsedQueries.get(i);
            List<String> result = ranker.ranking(specificQuery);
            results.put(specificQuery, result);
        }
        return results;
    }

    public List<String> searchDocument(String docno, String corpusPath) {
        String docnoEntry = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("documents")));

            while ((docnoEntry = bufferedReader.readLine()) != null) {
                if (docnoEntry.substring(0, docnoEntry.indexOf(':')).equals(docno)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int index = docnoEntry.indexOf(':') + 1;
        String fileName = docnoEntry.substring(index, docnoEntry.indexOf(','));
        index = docnoEntry.indexOf(',') + 1;
        String positionInFile = docnoEntry.substring(index);
        ReadFile readFile = new ReadFile();
        String content = getTheDocumentText(corpusPath, fileName, positionInFile);
        return getFiveImportantSentences(content);
    }

    private String getTheDocumentText(String corpusPath, String fileName, String positionInFile) {
        String documentText = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(corpusPath + "\\" + fileName + "\\" + fileName)));
            bufferedReader.skip(Long.parseLong(positionInFile));
            StringBuilder docText = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                docText.append(line);
                docText.append(" ");
                if (line.length() <= 8 && line.contains("</TEXT>")) {
                    break;

                }
            }
            documentText = docText.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ReadFile readFile = new ReadFile();
        return readFile.readDocContent(documentText);
    }

    private List<String> getFiveImportantSentences(String content) {
        int documentLength = 0;
        Parse parse = new Parse(false, true);
        HashMap<String, Integer> terms = new HashMap<>();
        LinkedHashMap<String, HashMap<String, Integer>> termsInSentences = new LinkedHashMap<>();
        String[] sentences = content.split(Pattern.quote(". "));
        for (int i = 0; i < sentences.length; i++) {
            if(sentences[i].length() < 3 || sentences[i].contains("<"))
                continue;
            HashMap<String, Integer> termsIndexes = new HashMap<>();
            String sentence = sentences[i];
            String ParsedSentence = parse.callParseForQuery(sentence + " ");
            String[] termsInSentence = ParsedSentence.split(" ");
            for (int j = 0; j < termsInSentence.length; j++) {
                if(termsInSentence[j].length() == 0|| termsInSentence[j].contains("<"))
                    continue;
                if (terms.containsKey(termsInSentence[j])) {
                    Integer tf = terms.get(termsInSentence[j]);
                    terms.put(termsInSentence[j], tf + 1);
                } else {
                    terms.put(termsInSentence[j], 1);
                }
                if (!termsIndexes.containsKey(termsInSentence[j]))
                    termsIndexes.put(termsInSentence[i], documentLength);
                documentLength++;
            }
            termsInSentences.put(sentences[i], termsIndexes);
        }
        HashMap<String, Double> rankedSentences = new HashMap<>();
        for (Map.Entry<String, HashMap<String, Integer>> sentence : termsInSentences.entrySet()) {
            HashMap<String, Integer> termsInSentence = sentence.getValue();
            double totalRank = 0;
            for (Map.Entry<String, Integer> term : termsInSentence.entrySet()) {
                double rank = (double) term.getValue();
                rank = (documentLength - rank) / documentLength;
                int tf = terms.get(term.getKey());
                totalRank += (tf/sentence.getKey().length()+rank);
            }
            rankedSentences.put(sentence.getKey(), totalRank);
        }
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

        List<String> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            result.add(toSort.get(i).getKey());
        }
        return result;
    }
    public static void main(String[] args) {
        Searcher searcher = new Searcher(new Ranker(new Indexer()));
        List<String> result = searcher.searchDocument("FBIS3-5285", "D:\\corpus");
        for (int i = 0; i < result.size(); i++) {
            System.out.println(result.get(i));
        }
    }
    }
