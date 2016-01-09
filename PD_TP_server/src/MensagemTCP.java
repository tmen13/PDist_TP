import java.io.Serializable;
import java.util.ArrayList;

public class MensagemTCP implements Serializable {
  static final long serialVersionUID = 2L;
  protected String msg;
  protected String file;
  public String ficheiro;
  protected ArrayList<String> listaFicheiros;

  public MensagemTCP(String msg, String file) {
    this.msg = msg;
    this.file = file;
  }

  public MensagemTCP(String string) {
    this.msg = string;
  }

    public ArrayList<String>getListaFicheiros(){
    	return listaFicheiros;
    }

    public void setListaFicheiros(ArrayList<String> array){
    	this.listaFicheiros = array;
    }
  
  public String getFile() {
    return file;
  }

  public String getMsg() {
    return msg;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }
}
