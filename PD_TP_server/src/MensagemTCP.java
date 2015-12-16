import java.io.Serializable;

public class MensagemTCP  implements Serializable{
    static final long serialVersionUID = 2L;
    protected String msg;
    protected FileDir file;

    public MensagemTCP(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public FileDir getFile() {
        return file;
    }

    public void setFile(FileDir file) {
        this.file = file;
    }
}
