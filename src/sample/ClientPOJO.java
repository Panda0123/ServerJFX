package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientPOJO {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String username;

    public ClientPOJO(Socket socket, DataOutputStream out, DataInputStream in, String username) {
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.username = username;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int hashCode() {
        return this.socket.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        ClientPOJO client = (ClientPOJO) o;
        return this.getSocket().equals(client.getSocket());
    }
}
