package partA;
import javax.print.Doc;
import java.io.*;
import java.nio.CharBuffer;
import java.util.*;
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
    private HashMap<String, String[]> docsFile;
    //private List<Document> documents;

    public ReadFile() {
        patternDocNo = Pattern.compile("(?<=<DOCNO>)(.*?)(?=</DOCNO>)");
        patternText = Pattern.compile("(?<=<TEXT>)(.*?)(?=</TEXT>)");
    }
    public ReadFile(String path){
        docsFile= new HashMap<String, String[]> ();

        //documents = new ArrayList<Document>();
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

    public LinkedHashMap<Document, String> readFile(File currentFile) {
        LinkedHashMap<Document, String> documentsOfFile = new LinkedHashMap<Document, String>();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(currentFile));
            String path = currentFile.getAbsolutePath();

            StringBuilder fileString = new StringBuilder();

            String temp;
            boolean isPositionDefined = true;
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
        Document document = new Document();
        String fileName = path.substring(path.lastIndexOf('\\') + 1);
        document.setPath(fileName);
        document.setPositionInFile(position);
        if(matchDocNo.find()) {
            document.setDocNo(matchDocNo.group());
        }
        //documents.add(document);  // todo - insert to file and save it for one time
        String[] docDetails = {document.getPath(), String.valueOf((document.getPositionInFile()))};
        docsFile.put(document.getDocNo(), docDetails);
        documentsOfFile.put(document, readDocContent(documentText));
    }

    public String readDocContent(String documentText) {
        Matcher matchDocNo = patternDocNo.matcher(documentText);
        if(matchDocNo.find()) {
            String DOCNO = matchDocNo.group();
           //System.out.println(DOCNO);
        }
        Matcher matchText = patternText.matcher(documentText);
        if(matchText.find()) {
            return matchText.group();
        }
        else return null;
    }

    //public List<Document> getDocuments() {
   //     return documents;
   // }
    public void writedocumentsToFile() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File("documents")));
            for(Map.Entry<String, String[]> doc : docsFile.entrySet()) {
                String entryToWrite = doc.getKey() + ":" + doc.getValue()[0] + "," + doc.getValue()[1];
                bufferedWriter.write(entryToWrite + "\n");
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void main(String[] args) {
        String path = "D:\\corpus";
        //get to the main directory
        File headDir = new File(path);
        //get all the directories from the main directory:
        File[] listOfDirs = headDir.listFiles();
        //create new readFile
        ReadFile corpus = new ReadFile(path);

        //iterate on all the files in the corpus
        for (int i = 0; i < listOfDirs.length; i++) {
            //get to the wanted file
            File temp = listOfDirs[i];
            File[] currDir = temp.listFiles();
            File currFile = currDir[0];
            corpus.readFile(currFile);

        }
        corpus.writedocumentsToFile();


    }


    }
