package ru.geekbrains.java2.dz.dz8.AlinaZhirova.server;

public class User {

    private String login;
    private int pass;
    private String nick;


    public User(String login, String pass, String nick) {
        this.login = login;
        this.pass = pass.hashCode();
        this.nick = nick;
    }


    public String getLogin() {
        return login;
    }


    public int getPass() {
        return pass;
    }


    public String getNick() {
        return nick;
    }


}
