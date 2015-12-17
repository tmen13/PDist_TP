import java.io.*;
import java.net.*;
import java.nio.file.*;
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
    limpaFicheiros();

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
          break;
        case "up":
          filename = sc.nextLine();
          mensagemTCP = new MensagemTCP("up", new FileDir(filename));
          MensagemTCP(mensagemTCP, socketServer);
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

    } catch (SocketException e) {
      e.printStackTrace();
    } finally {
      if (sc != null) {
        sc.close();
      }
    }

    limpaFicheiros();
    System.out.print("Aplicacao vai desligar-se");
    return;
  }

  public static void MensagemTCP(MensagemTCP mensagem, Socket socket) {

    try {
      // InputStream in = new ObjectInputStream(socket.getInputStream());
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
      
      try {
        socketServer = new Socket(ServerIP, 7001);
        System.out.println("Socket TCP preenchido");
      } catch (IOException e) {
        System.out.print(e.toString());
      }

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
