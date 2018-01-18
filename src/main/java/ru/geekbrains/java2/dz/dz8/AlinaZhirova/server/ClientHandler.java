package ru.geekbrains.java2.dz.dz8.AlinaZhirova.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;


    public String getName() {
        return name;
    }


    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.name = "";
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            Thread curThread = new Thread(() -> {
                try {
                    while (true) {
                        String authorizationInfo = in.readUTF();
                        if (authorizationInfo.startsWith("/auth ")) {
                            String[] s = authorizationInfo.split("\\s");
                            String newNick = myServer.getAuthService().getNickByLoginPass(s[1], s[2]);
                            if (newNick != null) {
                                if (!myServer.isNickBusy(newNick)) {
                                    name = newNick;
                                    sendMsg("/authok " + newNick);
                                    myServer.subscribe(this);
                                    break;
                                }
                                else sendMsg("This account is already used!");
                            }
                            else sendMsg("Incorrect username/password!");
                        }
                        else sendMsg("You need to login!");
                    }
                    while (true) {
                        String message = in.readUTF();
                        System.out.println(name + ": " + message);
                        if (message.startsWith("/")) {
                            if (message.equalsIgnoreCase("/end")) {
                                break;
                            }
                            if (message.startsWith("/w ")) {
                                String[] parts = message.split("\\s", 3);
                                myServer.sendPersonalMsg(this, parts[1], parts[2]);
                            }
                            if (message.startsWith("/answer ")) {
                                myServer.getBot().tryToAnswer(this, message.split("\\s")[1]);
                            }
                            if (message.equalsIgnoreCase("/vik")) {
                                myServer.getBot().start();
                            }
                        }
                        else {
                            myServer.broadcastMsg(name + ": " + message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    myServer.unsubscribe(this);
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            curThread.setDaemon(true);
            curThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
