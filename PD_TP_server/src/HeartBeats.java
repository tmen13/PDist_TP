import java.io.*;
import java.net.*;

class HeartBeats extends Thread {
	public static final int MAX_SIZE = 4*1024;
	protected int PORT_SERVICEDIRECTORY;
	private static final String IP_SERVICEDIRECTORY = "225.15.15.15";
	public static int FailtHeartBeats;
	DatagramPacket pkt;
	InetAddress ADDRESS;
	MulticastSocket socket;
	ByteArrayOutputStream bOut;
	ObjectOutputStream out;
	public int PortoDeEscuta;

	public HeartBeats(int PORT_SERVICEDIRECTORY,int port) {
		this.PortoDeEscuta = port;
		pkt = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
		FailtHeartBeats=0;
		this.PORT_SERVICEDIRECTORY = PORT_SERVICEDIRECTORY;
		
		try {
			bOut = new ByteArrayOutputStream(MAX_SIZE);
			out = new ObjectOutputStream(bOut);
			ADDRESS = InetAddress.getByName(IP_SERVICEDIRECTORY);
			socket = new MulticastSocket(PORT_SERVICEDIRECTORY);
			socket.joinGroup(ADDRESS);
		}catch (UnknownHostException e){
			System.out.println(e);
		}catch (IOException e){
			System.out.println(e);
		}

	}

	@Override
	public void run() {
		ObjectInputStream in;
		Object obj;

		//String Resquest = null;
		Boolean active = true;
		HBMensagem mensagem;
		while (active) {
			try {
				while (true) {
					mensagem = new HBMensagem("Activo",PortoDeEscuta);
					out.writeObject(mensagem);
					pkt = new DatagramPacket(bOut.toByteArray(), bOut.size(),InetAddress.getByName(IP_SERVICEDIRECTORY),PORT_SERVICEDIRECTORY);
					Thread.sleep(5000);
					socket.send(pkt);
					socket.setSoTimeout(1000); // esperar para receber a mensagem em 1 seg.
					socket.receive(pkt); // lê o que escreveu para o grupo
					socket.receive(pkt); // recebe uma mensagem vinda de fora
					in = new ObjectInputStream(new ByteArrayInputStream(pkt.getData(), 0, pkt.getLength()));
					obj = in.readObject();
					in.close();

					if( obj instanceof HBMensagem){
						HBMensagem msg = (HBMensagem) obj;
						System.out.println("From: " + pkt.getAddress() + ":" + pkt.getPort() + " - " + msg.getMensagem());
						//TODO: Indicar o primario caso seja perguntado
					}
					//
				}
			}catch (ClassNotFoundException e){
				System.out.println(e);
			}catch (InterruptedException e) {
				System.out.println("Error #1 - " + e);
			}
			catch (SocketTimeoutException e) {
				System.out.println("TIME OUT - " + e);
				//TODO: ver se é desta maneira que se para um thread
				if (++FailtHeartBeats == 3)
					active=false;
				// HeartBeats.currentThread().interrupt();
			} catch (SocketException e) {
				System.out.println("Error #2 -" + e);
			} catch (UnknownHostException e) {
				System.out.println("Error #3 -" + e);
			} catch (IOException e) {
				if (++FailtHeartBeats == 3)
					HeartBeats.currentThread().interrupt();
				System.out.println("Error #4 -" + e.toString());
			}
		}
		System.out.println("System shutdown");
	}
}