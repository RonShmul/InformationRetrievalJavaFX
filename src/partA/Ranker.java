package partA;

import java.io.*;
import java.util.*;

public class Ranker {

    private Indexer indexer;
    private HashMap<String, Double> calculateW;
    private double b = 0.75;
    private double k = 1.2;
    private static final int avgDocLength = ((114737761)/(468370));

    /**
     * constructor
     * @param indexer
     */
    public Ranker(Indexer indexer) {
        this.indexer = indexer;
        calculateW = new HashMap<>();
    }

    /**
     * setter to the indexer
     * @return
     */
    public Indexer getIndexer() {
        return indexer;
    }

    /**
     * calculate the rank for all the documents that contain the words in a given query.
     * @param query
     * @return
     */
    public List<String> ranking(String query) {
        String[] queriesWords = query.split(" ");
        int len = queriesWords.length;

        for (int j = 0; j < len; j++) {
            Term t = indexer.getDictionary().get(queriesWords[j]);  //get the specific term from the dictionary for the path, position and df.
            int df = t.getDf();
            String path = t.getPostingFilePath();
            long position = t.getPointerToPostings();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
                reader.skip(position);  //skip to the right position of the term in the posting file.
                String termInPosting = reader.readLine();
                termInPosting = termInPosting.substring(termInPosting.indexOf(':') +1);
                String[] documentsOfTerm = termInPosting.split(",");
                for (int i = 0; i < documentsOfTerm.length; i++) {   //foreach DOCNO in the posting line calculate his part in the cosSim and BM, and add it to calculateW
                    String docNo = documentsOfTerm[i].substring(0, documentsOfTerm[i].indexOf(":"));
                    double tf = Double.parseDouble(documentsOfTerm[i].substring(documentsOfTerm[i].indexOf(":") + 1));
                    Double CosSimResult = calcCosSim(docNo, tf, df, len,0.01);  //send to calculate CosSim
                    Double BMResult = BM(docNo, tf, df, 0.99);   //send to calculate BM
                    if (calculateW.get(docNo) == null) {
                        calculateW.put(docNo,CosSimResult + BMResult);
                    } else {
                        calculateW.put(docNo, calculateW.get(docNo) + ( CosSimResult) + BMResult );
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<String> relevantDocs = sortByRank();
        calculateW.clear();
        return relevantDocs;
    }

    /**
     * calculate Cosin Similarity for a given document.
     * @param docNo
     * @param tf
     * @param df
     * @param length
     * @param percent
     * @return
     */
    public Double calcCosSim(String docNo,double tf, int df, int length, double percent) {
        double Idf = Math.log(indexer.getN() / df);
        double constDivide = Math.sqrt(indexer.getDocuments().get(docNo).getWeight()*length);  // The denominator
        Double weightIJ = (tf*Idf)/constDivide;  // the numerator in the formula is tf*idf
        return weightIJ*percent;
    }

    /**
     * calculate BM25 algorithm for a given document.
     * @param docNo
     * @param tf
     * @param df
     * @param percent
     * @return
     */
    public Double BM(String docNo, double tf, int df, double percent){
        double Idf = Math.log((indexer.getN()-df+0.5)/(df+0.5));  //calculate new idf by the formula of BM25.
        Document doc = indexer.getDocuments().get(docNo);
            double numerator = tf*doc.getLength()*(k+1);
            double denominator = tf*doc.getLength() + k*(1-b+b*(doc.getLength()/ avgDocLength));
            double result = Idf*(numerator/denominator);
            return result*percent;
    }

    /**
     * sort the relevant documents for a query  by their ranking.
     * @return
     */
    public List<String> sortByRank(){
        List<Map.Entry<String, Double>> forSort = new ArrayList<>(calculateW.entrySet());
        forSort.sort(new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                if(o1.getValue() < o2.getValue()) {
                    return 1;
                }
                else if(o1.getValue() > o2.getValue()) {
                    return -1;
                }
                return 0;
            }
        });
        List<String> relevantDocs = new ArrayList<>();
        int i = 1;
        for (Map.Entry<String, Double> doc : forSort) {
            relevantDocs.add(doc.getKey());
            if(i==50)
                break;
            i++;
        }
        return relevantDocs;
    }
}
