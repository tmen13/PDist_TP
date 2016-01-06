import java.util.*;
import java.io.*;
import java.io.File;
import java.net.*;

public class DirectoryService {

	public static final  int ListeningPort = 7000;
	public static final String ListeningAddressGroup = "225.15.15.15";
	public static final String FileLAuthentication = "UserLogin.txt";
	public static final String loginDirectory = "logins";
	public static final int Max_Size = 4*1024;
	public static final int MAX_SERVER = 5;
	public static MulticastSocket socket;
	public static InetAddress group;

	protected  static void SendUDPMessage(DatagramPacket packet,DatagramSocket socket,String Msg){
		packet = new DatagramPacket(Msg.getBytes(),Msg.length(),packet.getAddress(),packet.getPort());
		try {socket.send(packet);}catch (IOException e){
			System.out.println("erro SendUDPMessage: " + e.toString());
		}
		System.out.println("Enviei Mensagem UDP");
	}

	public static ArrayList<Utilizadores>  LoadUtilizadores(File dirlocal,ArrayList<Utilizadores> users){
		//byte[] fileChunck = new byte[Max_Size];
		//int nbytes;

		//String requestedFileName;
		String requestedCanonicalFilePath = null;
		FileInputStream requestedFileInputStream;
		int readAux = -1;
		String strLine;
		//boolean existLogin;
		BufferedReader br;
		String firstNameRead = "";
		try {
			//byte bytes[] = new byte[Max_Size];

			requestedCanonicalFilePath = new File(dirlocal + File.separator+FileLAuthentication).getCanonicalPath();
			if (!requestedCanonicalFilePath.startsWith(dirlocal.getCanonicalPath() + File.separator)) {
				System.out.print("Nao é permitido aceder ao ficheiro.");
			}

			requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
			br = new BufferedReader(new InputStreamReader(requestedFileInputStream));

			while ( (strLine = br.readLine()) != null){

				readAux++;
				if(readAux == 0)
					firstNameRead = strLine;
				else if( readAux == 1){

					users.add(new Utilizadores(firstNameRead,strLine));
					readAux=-1;
				}
			}
			br.close();
		}catch (IOException e){
			System.out.print("Erro#3 -" +e);
		}
		return users;
	}

	protected static boolean searchUser(ArrayList<Utilizadores> users,String user,String pass){
		Utilizadores usertemporario = new Utilizadores(user,pass);

		for(int i = 0; i<users.size();i++){
			if(users.get(i).getUsername().equals(usertemporario.getUsername()) &&users.get(i).getPassword().equals(usertemporario.getPassword()) ){
				System.out.println("User foi encontrado");
				return true;
			}
		}
		System.out.println("User não foi encontrado");
		return  false;
	}

	//TODO: corrigir erro do equals
	protected static boolean searchServer(ArrayList<Server> servers,Server key){
		for(Server serv : servers) {			
			if (key == serv)
				return true;
		}
		return false;
	}

	public static void main(String[] args) {

		File localDirectory = new File(loginDirectory);
		MensagemUDP mensagemClient;

		DatagramPacket packet;
		ObjectInputStream in;
		ByteArrayOutputStream bOut;
		ObjectOutputStream out;
		Object obj;
		ArrayList<Utilizadores> utilizadores = new ArrayList<>();
		ArrayList<Server> servers = new ArrayList<>();
		utilizadores = LoadUtilizadores(localDirectory, utilizadores);
		boolean UserExist;
		int NextServerSendPos = -1;
		int contaHB = 0;
		StringBuilder IPplusPort = new StringBuilder();

		try {
			group = InetAddress.getByName(ListeningAddressGroup);
			socket = new MulticastSocket(ListeningPort);
			socket.joinGroup(group);
		} catch (IOException e) {
			System.out.println(e);
		}

		try {
			while (true) {
				packet = new DatagramPacket(new byte[Max_Size], Max_Size);
				socket.receive(packet);
				System.out.println("Recebi um pacote");
				try {
					bOut = new ByteArrayOutputStream();
					out = new ObjectOutputStream(bOut);
					in = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
					obj = in.readObject();
					in.close();

					if (obj instanceof MensagemUDP) {
						System.out.println("Recebi Mensagem UDP");
						mensagemClient = (MensagemUDP) obj;
						UserExist = searchUser(utilizadores, mensagemClient.getUser(), mensagemClient.getPassword());
						if (UserExist == true) {

							NextServerSendPos++;
							if(NextServerSendPos > servers.size()){
								NextServerSendPos = 0;
							}
							//TODO: vai enviar uma mensagem tipo String que contem um PORT
							IPplusPort.setLength(0);
							IPplusPort.append(servers.get(NextServerSendPos).getAddress().getHostAddress());
							IPplusPort.append(":");
							IPplusPort.append(servers.get(NextServerSendPos).getPort());
							IPplusPort.toString();
							SendUDPMessage(packet,socket,IPplusPort.toString());
						}

					} else if (obj instanceof HBMensagem) {
						HBMensagem msg = (HBMensagem) obj;
						System.out.println("Recebi Mensagem do tipo HeartBeat:" + packet.getAddress() + msg.getPortoEscuta());
						Server srv = new Server(msg.getPortoEscuta(), packet.getAddress());

						if(!searchServer(servers,srv)){ //adicionar servidor Activo
							if(servers.isEmpty())
								srv.setPrimario(true);
							servers.add(srv);
						}

						//O serviço de directorias verifica o tempo dos HeartBeats 
						//dos servidores quando tem que ir la mexer no array para atribuir um port ao cliente
						
						msg.setSegundos(getHoraDirectoriaEmSeg());

						if((srv.horaEmSegundos()-msg.horaEmSegundos())>15){
							System.out.println("passaram 15s");
							servers.remove(srv);
						} else {
							//System.out.println(srv.horaEmSegundos() + ":" + msg.horaEmSegundos() + ":" + (srv.horaEmSegundos()-msg.horaEmSegundos()));
							if((srv.horaEmSegundos()-msg.horaEmSegundos())>5){// se passou mais de 5s, perde um periodo
								contaHB++;
								System.out.println("Passaram 5s. contaHB: " + contaHB);
							}
							if(contaHB < 3){
								//System.out.println("vou atualizar tudo");
								servers = atualizaServersHB(servers);
								System.out.println("HeartBeat:\n" + "From: " + packet.getAddress()
								+ ":" + msg.getPortoEscuta() + " - " + msg.getMensagem());
								msg.setMensagem("HeartBeat recebido");
								out.writeObject(msg);
								out.flush();

								packet = new DatagramPacket(bOut.toByteArray(), bOut.size(), group, ListeningPort);
								socket.send(packet);
								socket.receive(packet); // recebe a propria mensagem
								//System.out.println("contaHB: " + contaHB);
								contaHB = 0; //recebeu HB com sucesso, reset contador
							} else {
								System.out.println("Passaram 3 periodos, vai remover server. contaHB: " + contaHB);
								servers.remove(srv); //passaram 3 periodos, remove servidor
							}
						}
					}
				} catch (ClassNotFoundException e) {
					System.out.println("Mensagem recebida de um tipo inesperado" + e.toString());
				} catch (IOException e) {
					System.out.println("Impossibilidade de aceder ao conteudo da mensagem recebida!" + e.toString());
				}

			}
		}catch (IOException e){
			System.out.println("erro #13: " + e.toString());
		}
	}

	private static ArrayList<Server> atualizaServersHB(ArrayList<Server> servers) {
		Calendar data = Calendar.getInstance();
		int segundos = (data.get(Calendar.HOUR_OF_DAY) * 60) * 60 +
				data.get(Calendar.MINUTE) * 60 +
				data.get(Calendar.SECOND);

		for(Server s : servers){
			s.setHoraEmSegundos(segundos);
		}
		return servers;     	
	}

	public static int getHoraDirectoriaEmSeg(){
		Calendar data = Calendar.getInstance();
		return (data.get(Calendar.HOUR_OF_DAY) * 60) * 60 +
				data.get(Calendar.MINUTE) * 60 +
				data.get(Calendar.SECOND);
	}

}