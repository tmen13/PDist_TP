import java.util.*;
import java.net.*;

class Server {
    private InetAddress address;
    private int port;
    private boolean primario = false;
	private boolean serverConnected = false;
    private Calendar data;
	private int segundos;

    public Server(int port, InetAddress inetAddress) {
        this.port = port;
        this.address = inetAddress;
        this.data = Calendar.getInstance();
        segundos = (data.get(Calendar.HOUR_OF_DAY) * 60) * 60 +
                data.get(Calendar.MINUTE) * 60 +
                data.get(Calendar.SECOND);
    }
    
    public boolean isPrimario() {
		return primario;
	}

	public void setPrimario(boolean primario) {
		this.primario = primario;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public void setSegundos(int segundos) {
		this.segundos = segundos;
	}
    
	public boolean isServerConnected() {
		return serverConnected;
	}

	public void setServerConnected(boolean serverConnected) {
		this.serverConnected = serverConnected;
	}

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int horaEmSegundos(){
    	return segundos;
    }
    
    public void setHoraEmSegundos(int seg){
    	this.segundos = seg;
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}