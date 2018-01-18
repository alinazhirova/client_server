package ru.geekbrains.java2.dz.dz8.AlinaZhirova.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyServer {

    private ServerSocket server;
    private final int PORT = 8189;
    private AuthService authService;
    private ServerBot bot;

    public ServerBot getBot() {
        return bot;
    }

    public AuthService getAuthService() {
        return authService;
    }

    private List<ClientHandler> clients;


    public MyServer() {
        Socket socket = null;
        try {
            server = new ServerSocket(PORT);
            clients = new CopyOnWriteArrayList<ClientHandler>();
            bot = new ServerBot(this);
            authService = new BaseAuthService();
            authService.start(this);
            while (true) {
                System.out.println("The server is waiting for clients...");
                socket = server.accept();
                System.out.println("Client is connected: " + socket);
                new ClientHandler(this, socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            authService.stop();
            try {
                System.out.println("Server is closed!");
                server.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public synchronized void subscribe(ClientHandler curClient) {
        broadcastMsg("Client " + curClient.getName() + " came in to the chat!");
        clients.add(curClient);
        broadcastClientsList();
    }


    public synchronized void unsubscribe(ClientHandler curClient) {
        clients.remove(curClient);
        broadcastMsg("Client " + curClient.getName() + " left the chat!");
        broadcastClientsList();
    }


    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) return true;
        }
        return false;
    }


    public synchronized void broadcastMsg(String msg) {
        String curTime = getCurrentTime();
        for (ClientHandler o : clients) {
            o.sendMsg(curTime + " " + msg);
        }
    }


    public synchronized String getCurrentTime() {
        return "" + Calendar.getInstance().get(Calendar.HOUR) + ":" + Calendar.getInstance().get(Calendar.MINUTE) + ":" + Calendar.getInstance().get(Calendar.SECOND);
    }


    public synchronized void broadcastClientsList() {
        StringBuilder allClientsNames = new StringBuilder("/clients ");
        for (ClientHandler o : clients) {
            allClientsNames.append(o.getName() + " ");
        }
        String list = allClientsNames.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(list);
        }
    }


    public synchronized void sendPersonalMsg(ClientHandler sender, String recipient, String msg) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(recipient)) {
                o.sendMsg(getCurrentTime() + " from " + sender.getName() + ": " + msg);
                sender.sendMsg(getCurrentTime() + " to " + recipient + ": " + msg);
                return;
            }
        }
        sender.sendMsg("This client " + recipient + " is not in a chat-room!");
    }


    public synchronized void startBot() {
        bot.start();
    }


}
