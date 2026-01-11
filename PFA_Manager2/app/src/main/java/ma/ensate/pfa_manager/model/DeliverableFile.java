package ma.ensate.pfa_manager.model;

import java.io.Serializable;

public class DeliverableFile implements Serializable {
    private String fileName;
    private long fileSize;
    private String fileUri;
    
    public DeliverableFile(String fileName, long fileSize, String fileUri) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileUri = fileUri;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileUri() {
        return fileUri;
    }
    
    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }
}
