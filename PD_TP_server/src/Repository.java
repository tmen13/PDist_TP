import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Repository extends Thread {
	protected File directory;
	protected FileDir dirFD;
	protected int PORT;
	protected Socket client;
	protected MensagemTCP msg;
	protected ArrayList<String> listaFicheiros;
	protected static File fileCliente = null;
	protected static ObjectInputStream in;
	protected static ObjectOutputStream out;
	protected static Object obj;

	/*public Repository(File directory, int PORT, MensagemTCP msg,Socket client) {
		this.directory = directory;
		this.PORT = PORT;
		this.msg = msg;
		this.client = client;
		this.listaFicheiros = new ArrayList<String>();
	} */

	public Repository(File directory, FileDir localDirectory, int listeningPortTCP, MensagemTCP msg, Socket client, 
			ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
		this.dirFD = localDirectory;
		this.PORT = listeningPortTCP;
		this.msg = msg;
		this.client = client;
		this.listaFicheiros = new ArrayList<String>();
		Repository.out = out;
		Repository.in = in;
		this.directory = directory;
	}

	public MensagemTCP getMsg() {
		return msg;
	}

	public void setMsg(MensagemTCP msg) {
		this.msg = msg;
	}

	public File getDirectory() {
		return directory;
	}

	public int getPORT() {
		return PORT;
	}

	public void setPORT(int PORT) {
		this.PORT = PORT;
	}

	public void preencheListaFicheiros(){
		File[] listOfFiles = directory.listFiles();

		//Apaga a lista para voltar a preencher
		listaFicheiros.clear();

		//Preenche a lista
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println(listOfFiles[i].getName());
				listaFicheiros.add(listOfFiles[i].getName());
			}
		}
	} //FUNCIONA

	public void enviaListaFicheiros(){
		MensagemTCP msg = new MensagemTCP("envia lista ficheiros");
		ArrayList<String> aux = new ArrayList<>();
		try {
			for(String str : listaFicheiros){
				aux.add(str);
			}
			msg.setListaFicheiros(aux);
			Repository.out.writeObject(msg);
			Repository.out.flush();
			//Repository.out.close();

			System.out.println("Lista enviada");
		}catch(IOException e){
			System.out.println("Erro a enviar a lista");
		}
	} //TESTAR

	public void enviaFicheiro(String filename){
		OutputStream out;

		byte[]fileChunk = new byte[4000];
		int nbytes;
		int timeout = 5; //Segundos
		String requestedCanonicalFilePath = null;
		FileInputStream requestedFileInputStream = null;

		if (!directory.exists()){
			System.out.println("A directoria " + directory + " nao existe!");
			return;
		}

		if(!directory.isDirectory()){
			System.out.println("O caminho " + directory + " nao se refere a uma directoria!");
			return;
		}

		if (!directory.canRead()){
			System.out.println("Sem permissoes de leitura na directoria " + directory + "!");
			return;
		}

		try {
			client.setSoTimeout(timeout * 1000);

			out = client.getOutputStream();

			requestedCanonicalFilePath = new File(directory + File.separator + filename).getCanonicalPath();

			if (!requestedCanonicalFilePath.startsWith(directory.getCanonicalPath() + File.separator)) {
				System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
				System.out.println("A directoria de base nao corresponde a " + directory.getCanonicalPath() + "!");
				return;
			}

			requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
			System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");

			while ((nbytes = requestedFileInputStream.read(fileChunk)) > 0 ) {
				out.write(fileChunk, 0, nbytes);
				out.flush();
			}
			out.close();
			System.out.println("Transferencia concluida");

		} catch (FileNotFoundException e) {
			System.out.println("Ocorreu a excepcao {" + e + "} ao tentar abrir o ficheiro " + requestedCanonicalFilePath + "!");
		} catch (IOException e) {
			System.out.println("Ocorreu a excepcao de E/S: \n\t" + e);
		} finally {
			if (requestedFileInputStream != null) {
				try {
					requestedFileInputStream.close();
				} catch (IOException ex) {
					System.out.println("Erro ao fechar o FileInputStream");
				}
			}
		}


	} //TESTAR

	public void recebeFicheiro(String filename){
		String localFilePath = null;
		FileOutputStream localFileOutputStream = null;
		PrintWriter pout;
		InputStream in;
		byte []fileChunk = new byte[4000];
		int nbytes;
		int timeout = 5; //segundos
		File f;


		if (!directory.exists()){
			System.out.println("A directoria " + directory + " nao existe!");
			return;
		}

		if (!directory.isDirectory()){
			System.out.println("O caminho " + directory + " nao se refere a uma directoria!");
			return;
		}

		if (!directory.canWrite()){
			System.out.println("Sem permissoes de escrita na directoria " + directory);
			return;
		}

		try{
			try{
				localFilePath = directory.getCanonicalPath() + File.separator + filename;

				localFileOutputStream = new FileOutputStream(localFilePath);
				System.out.println("Ficheiro " + localFilePath + " criado.");

			}catch(IOException e){

				if(localFilePath == null){
					System.out.println("Ocorreu a excepcao {" + e +"} ao obter o caminho canonico para o ficheiro local!");
				}else{
					System.out.println("Ocorreu a excepcao {" + e +"} ao tentar criar o ficheiro " + localFilePath + "!");
				}
				return;
			}

			try{

				client.setSoTimeout(timeout*1000);

				in = client.getInputStream();

				while((nbytes = in.read(fileChunk)) > 0) {
					localFileOutputStream.write(fileChunk, 0, nbytes);
				}
				in.close();
			}catch(UnknownHostException e){
				System.out.println("Destino desconhecido:\n\t"+e);
			}catch(NumberFormatException e){
				System.out.println("O porto do servidor deve ser um inteiro positivo:\n\t"+e);
			}catch(SocketTimeoutException e){
				System.out.println("Não foi recebida qualquer bloco adicional, podendo a transferencia estar incompleta:\n\t"+e);
			}catch(SocketException e){
				System.out.println("Ocorreu um erro ao nível do socket TCP:\n\t"+e);
			}catch(IOException e){
				System.out.println("Ocorreu um erro no acesso ao socket ou ao ficheiro local " + localFilePath +":\n\t"+e);
			}

		}finally{

			if(localFileOutputStream != null){
				try{
					localFileOutputStream.close();
				}catch(IOException e){}
			}
		}

	} //TESTAR

	void apagaFicheiro(String filename){
		String localFilePath = null;
		File f;

		if (!directory.exists()){
			System.out.println("A directoria " + directory + " nao existe!");
			return;
		}

		if (!directory.isDirectory()){
			System.out.println("O caminho " + directory + " nao se refere a uma directoria!");
			return;
		}

		if (!directory.canWrite()){
			System.out.println("Sem permissoes de escrita na directoria " + directory);
			return;
		}

		try {
			localFilePath = directory.getCanonicalPath() + File.separator + filename;

			f = new File(localFilePath);
			if (f.exists())
				f.delete();

		} catch (IOException e) {
			System.out.println("Erro de I/O");
		}
	} //FUNCIONA

	@Override
	public void run() {
		System.out.println("From: "+ client.getInetAddress()+":"+client.getPort()+" - "+msg.getMsg());

		switch (msg.getMsg()){
		case "dir":
			System.out.println("Cliente fez request do nome dos ficheiros");
			preencheListaFicheiros();
			enviaListaFicheiros();
			break;
		case  "down":
			System.out.println("Download: " + msg.getFile().getFileName());
			//enviaFicheiro("pa.png");
			break;
		case "up":
			System.out.println("Upload: " + msg.getFile().getFileName());
			recebeFicheiro("pa.png");
			break;
		case "del":
			System.out.println("Delete: " + msg.getFile().getFileName());
			apagaFicheiro(msg.getFile().getFileName());
			break;
		}
	}
}