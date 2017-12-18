package partA;
import java.io.*;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sivan on 12/5/2017.
 */
public class ReadFile {
    private static String pathStr;
    private int positionTracker;
    private File headDir;
    private File[] listOfDirs;
    private Pattern patternDocNo;
    private Pattern patternText;

    private List<Document> documents;

    public ReadFile(String path){
        documents = new ArrayList<Document>();
        pathStr = path;
        headDir = new File(pathStr);
        listOfDirs = headDir.listFiles();
        patternDocNo = Pattern.compile("(?<=<DOCNO>)(.*?)(?=</DOCNO>)");
        patternText = Pattern.compile("(?<=<TEXT>)(.*?)(?=</TEXT>)");
    }

    public void setPathStr(String path){
        pathStr = path;
    }

    public void setListOfDirs(){

        headDir = new File(pathStr);
        listOfDirs = headDir.listFiles();
    }


 /*   public void readCorpus() {
        for (int i = 0; i < listOfDirs.length; i++) {
            //get to the wanted file
            File temp = listOfDirs[i];
            File[] currDir = temp.listFiles();
            File currFile = currDir[0];
            readFile(currFile);
        }
    }*/

    public LinkedHashMap<Document, String> readFile(File currentFile) {
        LinkedHashMap<Document, String> documentsOfFile = new LinkedHashMap<Document, String>();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(currentFile));
            String path = currentFile.getAbsolutePath();

            StringBuilder fileString = new StringBuilder();

            String temp;
            boolean isPositionDefined = false;
            positionTracker = 0;
            int position = 0;
            //the garbage at the end make the "bufferedReader.ready()" return true and the DOCNO and content are null.. should be handled
            while ((temp = bufferedReader.readLine()) != null) {
                if(!isPositionDefined) {
                    position = positionTracker;
                    isPositionDefined = true;
                }

                positionTracker += (temp.getBytes().length);

                fileString.append(temp);
                fileString.append(" ");

                if (temp.length() <= 7 && temp.contains("</TEXT>")) {
                    readDoc(fileString.toString(), position, path, documentsOfFile);
                    fileString = new StringBuilder();
                }
                if(temp.length() <= 5 && temp.contains("<DOC>")) {
                    isPositionDefined = false;
                }
            }
            bufferedReader.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentsOfFile;
    }
    public void readDoc(String documentText,long position, String path, LinkedHashMap<Document, String> documentsOfFile) throws Exception {
        Matcher matchDocNo = patternDocNo.matcher(documentText);
        Matcher matchText = patternText.matcher(documentText);
        Document document = new Document();
        document.setPath(path);
        document.setPositionInFile(position);
        if(matchDocNo.find()) {
            document.setDocNo(matchDocNo.group());
//            System.out.println(document.getDocNo());
        }
        documents.add(document);

        if(matchText.find()) {
            documentsOfFile.put(document, matchText.group());
        }
        else throw new Exception("problem with content of document"+ document.getDocNo());

    }

    public List<Document> getDocuments() {
        return documents;
    }


}
