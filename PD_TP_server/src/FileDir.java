import java.io.Serializable;

/**
 * Created by JoaoBrandao on 05/12/15.
 */
public class FileDir implements Serializable {
    private String fileName;
    static final long serialVersionUID = 3L;

    FileDir(String name){
        this.fileName = name;
    }

    public  String getFileName() {
        return fileName;
    }
}
