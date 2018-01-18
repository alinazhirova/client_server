package ru.geekbrains.java2.dz.dz8.AlinaZhirova.clientfx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public TextField msgField;
    public TextField authLogin;
    public TextArea textArea;
    public PasswordField authPass;
    public ListView clientsList;

    public HBox bottomPanel;
    public HBox upperPanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authorized;
    private String nick;

    final String SERVER_ADDR = "localhost";
    final int SERVER_PORT = 8189;
    final long MAX_TIME_AUTHORIZATION = 120000;


    public synchronized boolean getAuthorizationState() {
        return authorized;
    }


    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (!this.authorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        }
        else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
        clientsList.setItems(FXCollections.observableArrayList());
        timeout();
    }


    public void timeout() {
        long startTime = System.currentTimeMillis();

        Thread timeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isNeedToCloseConnection = false;
                while (!getAuthorizationState()) {
                    long curTime = System.currentTimeMillis() - startTime;
                    if (curTime >= MAX_TIME_AUTHORIZATION) {
                        isNeedToCloseConnection = true;
                        break;
                    }
                }

                if (isNeedToCloseConnection) {
                    Platform.exit();
                }
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();
    }


    public void start() {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread curThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String message = in.readUTF();
                            if (message.startsWith("/authok ")) {
                                nick = message.split("\\s")[1];
                                setAuthorized(true);
                                textArea.clear();
                                break;
                            }
                            textArea.appendText(message + "\n");
                        }
                        while (true) {
                            String message = in.readUTF();
                            if (message.startsWith("/")) {
                                if (message.startsWith("/clients ")) {
                                    String[] names = message.split("\\s");
                                    Platform.runLater(() -> {
                                        clientsList.getItems().clear();
                                        for (int i = 1; i < names.length; i++) {
                                            clientsList.getItems().add(names[i]);
                                        }
                                    });
                                }
                            }
                            else {
                                textArea.appendText(message + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        setAuthorized(false);
                        try {
                            socket.close();
                            in.close();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            curThread.setDaemon(true);
            curThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendAuth() {
        if (socket == null || socket.isClosed()) {
            start();
        }
        try {
            out.writeUTF("/auth " + authLogin.getText() + " " + authPass.getText());
            authLogin.clear();
            authPass.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void clientsListClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            msgField.setText("/w " + (String)clientsList.getSelectionModel().getSelectedItem() + " ");
            msgField.requestFocus();
            msgField.selectEnd();
        }
    }


}