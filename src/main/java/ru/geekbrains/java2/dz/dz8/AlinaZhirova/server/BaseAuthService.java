package ru.geekbrains.java2.dz.dz8.AlinaZhirova.server;

import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {

    private List<User> users;


    public BaseAuthService() {
        users = new ArrayList<User>();
        for (int i = 1; i < 40; i++) {
            users.add(new User("login" + i, "pass" + i, "nick" + i));
        }
    }


    @Override
    public void start(MyServer server) {
        System.out.println("The authorization service is running!");
    }


    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (User o : users) {
            if (o.getLogin().equals(login) && o.getPass() == pass.hashCode()) {
                return o.getNick();
            }
        }
        return null;
    }


    @Override
    public void stop() {
        System.out.println("The authorization service is stopped!");
    }


}
