import  java.util.*;
import java.net.*;
import java.io.*;

public class server{
	private static int PORT;
	private static FileDir localDirectory;
	private static int ListeningPortTCP = 7001;

	public static ArrayList<File> files = new ArrayList<File>();

	public static void main(String[] args){
		/*
		 *Validação para o código INPUT dos args
		 */
		if (args.length != 2){
			System.out.println("Erro#1 - Falta args");
			return;
		}
		localDirectory =  new FileDir(args[0].trim());
		PORT = Integer.parseInt(args[1]);
		ServerSocket server; //TCP
		Socket client;
		ObjectInputStream in;
		ObjectOutputStream out;
		Object obj;

		//TODO: Ver erro de verificação de ficheiros
		// if (!localDirectory.exists() || !localDirectory.isDirectory() || !localDirectory.canRead() || !localDirectory.canWrite()) {
		//   System.out.println("Erro#2 - Erro na directoria");
		//  return;
		//}

		/*
		 *Inicio dos HeartBeats e processo do servidor
		 */
		Thread threadHeartBeat = new HeartBeats(PORT,ListeningPortTCP);
		threadHeartBeat.start();

		/*
		 *Lançar thread para atender client
		 */
		try{
			server = new ServerSocket(ListeningPortTCP);
			while(true) {
				try {
					try {
						System.out.println("Espera da liga�ao de um cliente");
						client = server.accept();
					} catch (IOException e) {
						System.out.println("Erro enquanto aguarda por um pedido de liga�ao:\n\t" + e);
						return;
					}
					System.out.println("Cliente Connectou-se");

					out = new ObjectOutputStream(client.getOutputStream());
					in = new ObjectInputStream(client.getInputStream());
					obj = in.readObject();

					/*
                    Validação para caso de enviar null como objecto.
					 */

					if (obj == null) {
						System.out.println("Objecto null");
						client.close();
						continue;
					}

					if (obj instanceof MensagemTCP) {
						MensagemTCP msg = (MensagemTCP) obj;
						System.out.println("Mensagem do cliente: " + msg.getMsg());
						//Criar uma thread para atender cada cliente
						Thread threadRepository = new Repository(localDirectory,ListeningPortTCP,msg,client);
						threadRepository.start();
						client.shutdownInput(); // desliga a entrada de informação por parte do cliente
					}

				}catch(IOException e){
					System.out.println(e);
					client = null;
				}catch(ClassNotFoundException e){
					System.out.println(e);
				}
			}
		}catch (IOException e){
			System.out.println(e);
		}
		System.out.println("System will shutdown!");
	}
}