import java.io.Serializable;
import java.util.Calendar;

public class HBMensagem implements Serializable {
    static final long serialVersionUID = 4L;
    protected  String mensagem;
    protected  int primario;
    protected Calendar data;
    protected int segundos;
    protected int portoEscuta;

    public HBMensagem(String mensagem,int port) {
    	this.portoEscuta = port;
        this.mensagem = mensagem;
        this.primario = -1;
        this.data = Calendar.getInstance();
        segundos = (data.get(Calendar.HOUR_OF_DAY) * 60) * 60 +
                data.get(Calendar.MINUTE) * 60 +
                data.get(Calendar.SECOND);
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }

    public int getPrimario() {
        return primario;
    }

    public void setPrimario(int primario) {
        this.primario = primario;
    }

    public int getSegundos() {
        return segundos;
    }

    public void setSegundos(int segundos) {
        this.segundos = segundos;
    }
    public int horaEmSegundos(){
        return segundos;
    }

	public int getPortoEscuta() {
		return portoEscuta;
	}

	public void setPortoEscuta(int portoEscuta) {
		this.portoEscuta = portoEscuta;
	}
}