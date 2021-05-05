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
    private static final List<String> memos = new LinkedList<>();
    private static final List<ClientPOJO> clients = new LinkedList<>();
    private static Stack<ClientPOJO> listOfClientToRemove = new Stack<>();
    private static int port = 8080;
    private static int size = 0;

   private static volatile Thread receiveClientThread = null;

   public static TextArea chatTextArea;

    public static void start (int port) {
        memos.add("mNone");
        Server.port = port;
        receiveClientThread = new Thread(new ReceiveClient());
        receiveClientThread.start();
    }

    public static void stop () {
        Thread tempThread = receiveClientThread;
        receiveClientThread = null;
        tempThread.interrupt();
    }

    public static void sendMemo(String memo) {
        memo = "m" + memo;
        memos.add(memo);
        for (ClientPOJO tempCli: clients) {
            try {
                tempCli.getOut().writeUTF(memo);
            } catch (SocketException ex) {
                if(ex.getMessage().equals("Broken pipe (Write failed)"))
                    listOfClientToRemove.push(tempCli);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        size = listOfClientToRemove.size();
        for (int i = 0; i < size; i++) {
            clients.remove(listOfClientToRemove.pop());
        }
    }

    private static class ReceiveClient implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Thread thisThread = Thread.currentThread();
                while (thisThread == receiveClientThread) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        new Thread(new HandleClient(clientSocket)).start();  // might use this when implemented with chat system
                    } catch (InterruptedIOException ex) { }
                }
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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

                ClientPOJO clientPOJO = new ClientPOJO(sc, out, in, username);
                clientPOJO.getOut().writeUTF(memos.get(memos.size() - 1));
                clients.add(clientPOJO);

                String message;

                while (true) {
//                    message = clientPOJO.getIn().readUTF();
                    message = in.readUTF();
                    System.out.println(message);
                    message = String.format("c[%s] (%s): %s",
                            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), username, message);
                    chatTextArea.setText(chatTextArea.getText() + message.substring(1, message.length()) + "\n");
                    for (ClientPOJO tempSc : clients) {
//                        if (!tempSc.getUsername().equals(username)) {
                        try {
                            tempSc.getOut().writeUTF(message);
                        } catch (SocketException ex) {
                            // broken pipe (someone exited) so remove from list
                            if (ex.getMessage().equals("Broken pipe (Write failed)"))
                                listOfClientToRemove.push(tempSc);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
//                        }
                    }
                    size = listOfClientToRemove.size();
                    for (int i = 0; i < size; i++)
                        clients.remove(listOfClientToRemove.pop());
                }
            } catch (SocketException ex) {
//                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static Thread getReceiveClientThread() {
        return receiveClientThread;
    }
}
