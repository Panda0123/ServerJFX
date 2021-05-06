package sample;

import javafx.scene.control.TextArea;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Server {

    private static String memo;
    private static final List<ClientPOJO> clients = new LinkedList<>();
    private static Stack<ClientPOJO> listOfClientToRemove = new Stack<>();
    private static int port = 8080;
    private static int size = 0;

    private static boolean isServerOn = false;
    private static ServerSocket serverSocket = null;

    public static TextArea chatTextArea;

    public static void start(int port) {
        memo = "mNone";
        Server.port = port;
        isServerOn = true;
        new Thread(new ReceiveClient()).start();
    }

    public static void stop() {
        if (!isServerOn) return;
        isServerOn = false;
        try {
            serverSocket.close();

            // stop all clients' thread
            for (ClientPOJO cliPOJO: clients) {
                cliPOJO.setOn(false);
                cliPOJO.getSocket().close();
            }
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    public static void sendMemo(String newMemo) {
        memo = "m" + newMemo;
        for (ClientPOJO tempCli : clients) {
            try {
                tempCli.getOut().writeUTF(memo);
            } catch (SocketException ex) {
                if (ex.getMessage().equals("Broken pipe (Write failed)"))
                    listOfClientToRemove.push(tempCli);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        size = listOfClientToRemove.size();
        ClientPOJO cliDisconnected;
        for (int i = 0; i < size; i++) {
            try {
                cliDisconnected = listOfClientToRemove.pop();
                cliDisconnected.setOn(false);
                cliDisconnected.getSocket().close();
            } catch (IOException ex) { ex.printStackTrace(); }
        }
    }

    private static class ReceiveClient implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                while (isServerOn) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new HandleClient(clientSocket)).start(); // might use this when implemented with chat
                }
                serverSocket.close();
            } catch (SocketException ex) { }
              catch (IOException ex) { }
        }
    }

    private static class HandleClient implements Runnable {
        private static Socket sc;

        HandleClient(Socket sc) {
            HandleClient.sc = sc;
        }

        @Override
        public void run() {
            try {
                DataOutputStream out = new DataOutputStream(sc.getOutputStream());
                DataInputStream in = new DataInputStream(sc.getInputStream());
                String username = in.readUTF();
                ClientPOJO clientPOJO = new ClientPOJO(sc, out, in, username, true);
                ClientPOJO cliDisconnected;
                clientPOJO.getOut().writeUTF(memo);
                clients.add(clientPOJO);

                String message;
                while (clientPOJO.isOn()) {
                    message = in.readUTF();
                    message = String.format("c[%s] (%s): %s",
                            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), username, message);
                    chatTextArea.setText(chatTextArea.getText() + message.substring(1, message.length()) + "\n");
                    for (ClientPOJO tempSc : clients) {
                        try {
                            tempSc.getOut().writeUTF(message);
                        } catch (SocketException ex) {
                            // broken pipe (someone exited) so remove from list
                            if (ex.getMessage().equals("Broken pipe (Write failed)"))
                                listOfClientToRemove.push(tempSc);
                        }
                        size = listOfClientToRemove.size();
                        for (int i = 0; i < size; i++) {
                            cliDisconnected = listOfClientToRemove.pop();
                            cliDisconnected.setOn(false);
                            cliDisconnected.getSocket().close();
                            clients.remove(clientPOJO);
                        }
                    }
                }
            } catch (SocketException ex) { System.out.println("Client disconnected through closing it from cmd Ctrl+c"); }
              catch (EOFException ex) { System.out.println("Client disconnected through closing the application"); }
              catch (IOException ex) { ex.printStackTrace(); }
        }
    }

    public static boolean isIsServerOn() {
        return isServerOn;
    }
}
