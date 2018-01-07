package partA;
import java.io.Serializable;

/**
 * Created by Ronshmul on 30/11/2017.
 */
public class Document implements Serializable{ // A class that helps us to save data on a specific documents besides the content

    private String path;
    private String docNo;
    private long positionInFile; //the first bit of the document - where it starts
    private int length;
    private String commonTerm;
    private int maxTf;

    public Document() {
        length = 0;
    }

    public Document(String path, String docNo, long positionInFile, int length, String commonTerm) {
        this.path = path;
        this.docNo = docNo;
        this.positionInFile = positionInFile;
        this.length = length;
        this.commonTerm = commonTerm;
    }

    public int getMaxTf() {
        return maxTf;
    }

    public void setMaxTf(int maxTf) {
        this.maxTf = maxTf;
    }

    public int getLength() {
        return length;
    }

    public String getCommonTerm() {
        return commonTerm;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setCommonTerm(String commonTerm) {
        this.commonTerm = commonTerm;
    }

    public String getPath() {
        return path;
    }

    public String getDocNo() {
        return docNo;
    }

    public long getPositionInFile() {
        return positionInFile;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDocNo(String docNo) {
        if(docNo!= null)
            this.docNo = docNo.replaceAll("\\s+" , "");
    }

    public void setPositionInFile(long positionInFile) {
        this.positionInFile = positionInFile;
    }
}
