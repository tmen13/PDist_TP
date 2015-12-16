import java.io.Serializable;

public class MensagemTCP implements Serializable {
  static final long serialVersionUID = 2L;
  protected String msg;
  protected FileDir file;

  public MensagemTCP(String msg, FileDir file) {
    this.msg = msg;
    this.file = file;
  }

  public MensagemTCP(String string) {
    this.msg = string;
  }

  public FileDir getFile() {
    return file;
  }

  public String getMsg() {
    return msg;
  }

  public void setFile(FileDir file) {
    this.file = file;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }
}
