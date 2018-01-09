package partA;

import java.io.*;
import java.util.Dictionary;
import java.util.HashMap;

public class Ranker {

    private Indexer indexer;
    private HashMap<String, Double> calculateW;

    public Ranker(Indexer indexer) {
        this.indexer = indexer;
        calculateW = new HashMap<>();
    }

    public void cosSim(String query) {

        String[] parts = query.split(" ");
        int len = parts.length;
        for (int i = 0; i < len; i++) {
            Term t = indexer.getDictionary().get(parts[i]);
            int df = t.getDf();
            String path = t.getPostingFilePath();
            long position = t.getPointerToPostings();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
                long skiped = reader.skip(position);
                String termInPosting = reader.readLine();
                insertToMap(termInPosting , df);
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertToMap(String termInPosting , int df) {
        String[] parts = termInPosting.split(": | ,");
        double idf = Math.log(indexer.getN() / df);
        for (int i = 1; i < parts.length; i=i+2) {
            if (calculateW.get(parts[i]) == null) {
                double weight = (Double.parseDouble(parts[i + 1]))*idf;
                calculateW.put(parts[i], weight);
            } else {
                double weight = (Double.parseDouble(parts[i + 1]))*idf;
                calculateW.put(parts[i], calculateW.get(parts[i]) + weight);
            }
        }
    }

    public void calcCosSim(){

    }
}
