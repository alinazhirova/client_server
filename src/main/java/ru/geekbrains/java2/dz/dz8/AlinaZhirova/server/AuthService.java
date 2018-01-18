package ru.geekbrains.java2.dz.dz8.AlinaZhirova.server;


public interface AuthService {
    void start(MyServer server);
    String getNickByLoginPass(String login, String pass);
    void stop();
}
