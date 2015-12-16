import java.io.Serializable;

/*
*   Class MensagemUDP
*   mensagem tipo string
*   username tipo String
*   password tipo String
*
*   class usada para a passagem de mensagens entre clientes,Serviços de Directorias,Servidores
*   Obrigatorio ter um determinado user a password
 */
public class MensagemUDP implements Serializable {
  static final long serialVersionUID = 1L;
  protected String mensagem;
  protected String username;
  protected String password;

  MensagemUDP(String username, String password) {
    this.username = username;
    this.password = password;
    mensagem = "";
  }

  MensagemUDP(String username, String password, String mensagem) {
    this.username = username;
    this.password = password;
    this.mensagem = mensagem;
  }

  public String getMensagem() {
    return mensagem;
  }

  public String getPassword() {
    return password;
  }

  public String getUser() {
    return username;
  }

  public void setMensagem(String mensagem) {
    this.mensagem = mensagem;
  }

  @Override
  public String toString() {
    return "User: " + username + "\nPassword: " + password;
  }
}
