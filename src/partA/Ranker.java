package partA;

import java.io.*;
import java.util.*;

public class Ranker {

    private Indexer indexer;
    private HashMap<String, Double> calculateW;
    private HashMap<String, Double> weights;

    public Ranker(Indexer indexer) {
        this.indexer = indexer;
        calculateW = new HashMap<>();
    }

    public List<String> cosSim(String query) {  //todo - maybe pass the load weights to here
        String[] parts = query.split(" ");
        int len = parts.length;
        indexer.loadWeights();
        weights = indexer.getWeights();
        for (int i = 0; i < len; i++) {
            Term t = indexer.getDictionary().get(parts[i]);
            int df = t.getDf();
            String path = t.getPostingFilePath();  //todo - indexing for saving the path
            //String path =
            long position = t.getPointerToPostings();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
                long skiped = reader.skip(position);
                String termInPosting = reader.readLine();
                calcCosSim(termInPosting , df , len);
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<String> relevantDocs = sortByRank();
        return relevantDocs;
    }

    public void calcCosSim(String termInPosting , int df , int length) {
        String[] parts = termInPosting.split(": | ,"); //todo change
        double idf = Math.log(indexer.getN() / df);
        for (int i = 1; i < parts.length; i=i+2) {
            Double constDivide = Math.sqrt((weights.get(parts[i]))*length);
            if (calculateW.get(parts[i]) == null) {
                double weight = ((Double.parseDouble(parts[i + 1]))*idf)/constDivide;
                calculateW.put(parts[i], weight);
            } else {
                double weight = ((Double.parseDouble(parts[i + 1]))*idf)/constDivide;
                calculateW.put(parts[i], calculateW.get(parts[i]) + weight);
            }
        }
    }

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

        for (Map.Entry<String, Double> doc : forSort) {
            relevantDocs.add(doc.getKey());
        }
        return relevantDocs;
    }
}
