package network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Класс потока сокетов
 */
public class SocketThread extends Thread {

    private SocketThreadListener listener;
    private Socket socket;
    private DataOutputStream out;

    /**
     * @param listener - слушатель событий
     */
    public SocketThread(SocketThreadListener listener, String name, Socket socket) {
        super(name);
        this.socket = socket;
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        listener.onSocketThreadStart(this, socket);
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            listener.onSocketThreadReady(this, socket);
            while (!isInterrupted()) {
                String msg = in.readUTF();
                listener.onReceiveString(this, socket, msg);
            }
        } catch (IOException e) {
            listener.onSocketThreadException(this, e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                listener.onSocketThreadException(this, e);
            }
        }
        listener.onSocketThreadStop(this);
    }

    /**
     * Отправить сообщение msg потоком вывода
     */
    public synchronized boolean sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
            return true;
        } catch (IOException e) {
            listener.onSocketThreadException(this, e);
            close();
            return false;
        }
    }

    /**
     * Закрываем сокет поток
     */
    public synchronized void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listener.onSocketThreadException(this, e);
        }
    }
}