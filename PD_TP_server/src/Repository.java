import java.net.Socket;

public class Repository extends Thread {
    protected FileDir directory;
    protected int PORT;
    protected Socket client;
    protected MensagemTCP msg;

    public Repository(FileDir directory, int PORT,MensagemTCP msg,Socket client) {
        this.directory = directory;
        this.PORT = PORT;
        this.msg = msg;
        this.client = client;
    }

    public MensagemTCP getMsg() {
        return msg;
    }

    public void setMsg(MensagemTCP msg) {
        this.msg = msg;
    }

    public FileDir getDirectory() {
        return directory;
    }

    public int getPORT() {
        return PORT;
    }

    public void setPORT(int PORT) {
        this.PORT = PORT;
    }


    @Override
    public void run() {
        System.out.println("From: "+ client.getInetAddress()+":"+client.getPort()+" - "+msg.getMsg());

         switch (msg.getMsg()){
             case "dir":
                 System.out.println("Cliente fez request do nome dos ficheiros");
                 break;
             case  "down":
                 System.out.println("Download: " + msg.getFile().getFileName());
                 break;
             case "up":
                 System.out.println("Upload: " + msg.getFile().getFileName());
                 break;
             case "del":
                 System.out.println("Download: " + msg.getFile().getFileName());
                 break;
         }
    }
}
