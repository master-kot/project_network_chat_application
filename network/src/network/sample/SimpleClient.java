package network.sample;

import java.io.*;
import java.net.Socket;

/**
 * Тестовый чат-клиент
 */
public class SimpleClient {
    public static void main(String[] args) {
        try (Socket s = new Socket("127.0.0.1", 8189)) {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeUTF("I'm a client");
            String b = in.readUTF();
            System.out.println(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
