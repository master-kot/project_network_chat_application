package network.sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Тестовый эхо-сервер
 */
public class EchoServer {
    public static void main(String[] args) {
        System.out.println("Waiting for connection...");
        try (ServerSocket serverSocket = new ServerSocket(8189);
             Socket s = serverSocket.accept()) {
            System.out.println("К нам подключился клиент");
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            String b = in.readUTF();
            out.writeUTF("echo: " + b);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
