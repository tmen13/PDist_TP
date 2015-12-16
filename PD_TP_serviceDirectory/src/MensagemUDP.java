import java.io.Serializable;

/*
*   Class MensagemUDP
*   mensagem tipo string
*   username tipo String
*   password tipo String
*
*   class usada para a passagem de mensagens entre clientes,Serviços de Directorias,Servidores
*   Obrigatório ter um determinado user a password
 */
public class MensagemUDP  implements Serializable{
    protected String mensagem;
    protected String username;
    protected String password;
    static final long serialVersionUID = 1L;

    MensagemUDP(String _username,String _password,String _mensagem){
        this.username = _username;
        this.password = _password;
        this.mensagem=_mensagem;
    }

    MensagemUDP(String _username,String _password){
        this.username = _username;
        this.password = _password;
        this.mensagem="";
    }

    public void setMensagem(String _mensagem){
        this.mensagem=_mensagem;
    }

    public String getMensagem(){
        return this.mensagem;
    }

    public String getUser(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public String toString(){
        return "User: "+this.username+"\nPassword: "+this.password;
    }
}
