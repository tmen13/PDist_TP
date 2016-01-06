import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {

  // UDP
  public static String IP_SERVICEDIRECTORY;
  public static int PORT_SERVICEDIRECTORY;
  public static final int timeout = 10;
  public static final int MaxSizeMsg = 4*1024;
  public static String DIR;
  public static boolean ClientActive = true;

  // TCP
  public static int ServerPort;
  public static String ServerIP;
  public static Socket socketServer;
  public static ArrayList<String> ficheirosDisponiveis;

  public static void recebeFicheiro(String filename){
    File f, localDirectory;
    String localFilePath = null;
    FileOutputStream localFileOutputStream = null;
    PrintWriter pout;
    InputStream in;
    byte []fileChunk = new byte[4000];
    int nbytes;
    int timeout = 5; //Segundos

    localDirectory = new File(DIR);

    if (!localDirectory.exists()){
      System.out.println("A directoria " + localDirectory + " nao existe!");
      return;
    }

    if (!localDirectory.isDirectory()){
      System.out.println("O caminho " + localDirectory + " nao se refere a uma directoria!");
      return;
    }

    if (!localDirectory.canWrite()){
      System.out.println("Sem permissoes de escrita na directoria " + localDirectory);
      return;
    }

    try{
      try{

        f = new File(localFilePath);
        if (f.exists())
          return;

        localFilePath = localDirectory.getCanonicalPath() + File.separator + filename;
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

        socketServer.setSoTimeout(timeout*1000);

        in = socketServer.getInputStream();

        pout = new PrintWriter(socketServer.getOutputStream(), true);
        pout.println(filename);
        pout.flush();

        while((nbytes = in.read(fileChunk)) > 0) {
          localFileOutputStream.write(fileChunk, 0, nbytes);
        }

      }catch(UnknownHostException e){
        System.out.println("Destino desconhecido:\n\t"+e);
      }catch(NumberFormatException e){
        System.out.println("O porto do servidor deve ser um inteiro positivo:\n\t"+e);
      }catch(SocketTimeoutException e){
        System.out.println("N�o foi recebida qualquer bloco adicional, podendo a transferencia estar incompleta:\n\t"+e);
      }catch(SocketException e){
        System.out.println("Ocorreu um erro ao n�vel do socket TCP:\n\t"+e);
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

  } //TESTAR E VERIFICAR PRINTWRITER

  public static void enviaFicheiro(String filename){
    OutputStream out;

    byte[]fileChunk = new byte[4000];
    int nbytes;
    int timeout = 5; //Segundos
    String requestedCanonicalFilePath = null;
    FileInputStream requestedFileInputStream = null;

    File directory = new File(DIR);

    if (!directory.exists()){
      System.out.println("A directoria " + directory + " nao existe!");
      return;
    }

    if(!directory.isDirectory()){
      System.out.println("O caminho " + directory + " nao se refere a uma directoria!");
      return;
    }

    if (directory.canRead()){
      System.out.println("Sem permissoes de leitura na directoria " + directory + "!");
      return;
    }

    try {
      socketServer.setSoTimeout(timeout * 1000);

      out = socketServer.getOutputStream();

      requestedCanonicalFilePath = new File(directory + File.separator + filename).getCanonicalPath();

      if (!requestedCanonicalFilePath.startsWith(directory.getCanonicalPath() + File.separator)) {
        System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
        System.out.println("A directoria de base nao corresponde a " + directory.getCanonicalPath() + "!");
        return;
      }

      requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
      System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");

      while ((nbytes = requestedFileInputStream.read(fileChunk)) > 0) {
        out.write(fileChunk, 0, nbytes);
        out.flush();
      }

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
          ex.printStackTrace();
        }
      }
    }
  } //TESTAR

  public static void recebeListaFicheiros(){
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(socketServer.getInputStream());
      Object o = null;
      o = ois.readObject();
      System.out.println(ois.getClass());
      MensagemTCP msg = (MensagemTCP)o;

      //ficheirosDisponiveis = msg.getListaFicheiros();
      imprimeListaFicheiros(msg.getListaFicheiros());
    }catch (Exception e){
      e.printStackTrace();
    }

  } //TESTAR

  public static void imprimeListaFicheiros(ArrayList<String> listaFicheiros){
    if(listaFicheiros.size() == 0)
      System.out.println("Sem ficheiros no servidor");
    else {
      for (int i = 0; i <listaFicheiros.size() ; i++) {
        System.out.println(listaFicheiros.get(i));
      }
    }
  } //FUNCIONA

  public static void limpaFicheiros() {
    try {
      File file = new File(DIR).getCanonicalFile();
      Path path;
      if (!file.exists()) {
        file.mkdir();
      }

      File[] lista = file.listFiles();

      for (File i : lista) {
        path = Paths.get(i.getAbsolutePath());
        //System.out.println(path.getFileName());
        Files.deleteIfExists(path);
      }

    } catch (IOException e) {
      System.err.println("ERRO: impossivel criar ficheiro");
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {

    if (args.length != 3){
      System.out.println("Erro#1 - Falta args: directoria 'IP Directoria' 'porto directoria' ");
      return;
    }

    MensagemUDP mensagem;
    MensagemTCP mensagemTCP;
    Scanner sc = new Scanner(System.in);
    String cmd;
    String filename;
    DIR = args[0];
    IP_SERVICEDIRECTORY = args[1];
    PORT_SERVICEDIRECTORY = Integer.parseInt(args[2]);
    //    limpaFicheiros();

    try {
      DatagramSocket socket = new DatagramSocket();
      DatagramPacket packet = new DatagramPacket(new byte[MaxSizeMsg], MaxSizeMsg);

      // Pedir os dados ao utilizador
      System.out.print("User: ");
      String user = sc.nextLine();
      System.out.print("Password: ");
      String password = sc.nextLine();

      mensagem = new MensagemUDP(user, password, "Login");
      MensagemUDP(mensagem, packet, socket);

      try {
        socketServer = new Socket(ServerIP, ServerPort);
        System.out.println("Socket TCP preenchido");
      } catch (IOException e) {
        System.out.print(e.toString());
      }

      /*
       * Comment: - Apartir daqui, iremos fazer a ligação ao Servidor e poder fazer uma opçoes
       * referidas em baixo - Lançar uma thread para cada caso
       *
       */

      while (ClientActive) {
        System.out.println("\n\n\nLista de comandos:");
        System.out.println("\"dir\" - Listar ficheiros");
        System.out.println("\"logout\" - Desconectar");
        System.out.println("\"up 'nome ficheiro'\" - Upload do ficheiro");
        System.out.println("\"down 'nome ficheiro'\" - Download do ficheiro");
        System.out.println("\"del 'nome ficheiro'\" - Elimina o ficheiro");
        System.out.println("\"'nome ficheiro'\" - Visualiza ficheiro");

        System.out.print("comando: ");
        cmd = sc.nextLine();
        switch (cmd) {
        case "dir":
          mensagemTCP = new MensagemTCP("dir");
          MensagemTCP(mensagemTCP, socketServer);
          System.out.println("Pedir lista de ficheiros");
          recebeListaFicheiros();
          break;
        case "logout":
          ClientActive = false;
          socket.close(); // Fechar liga�ao UDP
          System.out.println("Ligacao UDP fechada");
          try {
            socketServer.close(); // Fecha ligação TCP
            System.out.println("Ligacao TCP fechada");

          } catch (IOException e) {
            System.out.println("Erro ao tentar fechar ligação TCP..");
            e.toString();
          }
          return;
        case "down":
          filename = sc.nextLine();
          mensagemTCP = new MensagemTCP("down", new FileDir(filename));
          MensagemTCP(mensagemTCP, socketServer);
          recebeFicheiro(filename);
          break;
        case "up":
          filename = sc.nextLine();
          mensagemTCP = new MensagemTCP("up", new FileDir(filename));
          MensagemTCP(mensagemTCP, socketServer);
          enviaFicheiro(filename);
          break;
        case "del":
          filename = sc.nextLine();
          mensagemTCP = new MensagemTCP("del", new FileDir(filename));
          MensagemTCP(mensagemTCP, socketServer);
          break;
        default:
          continue;
        }
      }
      sc.close();
    } catch (SocketException e) {
      e.printStackTrace();
    } 
    //    limpaFicheiros();
    System.out.print("Aplicacao vai desligar-se");
    return;
  }

  public static void MensagemTCP(MensagemTCP mensagem, Socket socket) {

    try {
      ObjectOutput out = new ObjectOutputStream(socket.getOutputStream());
      out.writeObject(mensagem);
      out.flush();
      System.out.println("Mensagem foi enviada");

    } catch (IOException e) {
      System.out.println("Erro na tentativa de enviar mensagem TCP: " + e.toString());
    }
  }

  public static void MensagemUDP(MensagemUDP mensagem, DatagramPacket packetUDP,
      DatagramSocket socketUDP) {
    // Object obj = null;
    try {
      // ObjectInputStream in;
      ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bOut);
      out.writeObject(mensagem);
      out.flush();
      packetUDP = new DatagramPacket(bOut.toByteArray(), bOut.size(),
          InetAddress.getByName(IP_SERVICEDIRECTORY), PORT_SERVICEDIRECTORY);
      socketUDP.send(packetUDP);
      System.out.println("Enviei Mensagem UDP");

      socketUDP.setSoTimeout(5000); // espera 5 segundos pela resposta
      System.out.println("<" + IP_SERVICEDIRECTORY + ":" + PORT_SERVICEDIRECTORY + "> - Mensagem Enviada");
      socketUDP.receive(packetUDP);

      String msgRecebida = new String(packetUDP.getData(), 0, packetUDP.getLength());
      StringTokenizer tokens = new StringTokenizer(msgRecebida," :");
      ServerIP = tokens.nextToken();
      ServerPort = Integer.parseInt(tokens.nextToken());
      System.out.println("Recebi PacketUDP com o endere�o do servidor (" + ServerIP + ":" + ServerPort + ")");

    } catch (SocketException e) {
      ClientActive = false;
      System.out.println("timeout" + e.toString());
    } catch (InterruptedIOException iie) {
      System.out.println("Erro#3 - " + iie.toString());
      ClientActive = false;
    } catch (IOException ie) {
      ClientActive = false;
      System.out.println("Erro#4 - " + ie.toString());
    }
  }
}