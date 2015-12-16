/*
*   class utilizadores
*
*   username tipo String
*   password tipo String
*
*   Usado para guardar informaçao de utilizadores registados
 */
public class Utilizadores {
    protected String username;
    protected  String password;

    Utilizadores(String _username,String _password){
        this.password=_password;
        this.username=_username;
    }
    public String getUsername(){
        return this.username;
    }
    public  String getPassword(){
        return this.password;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
