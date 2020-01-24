package network;

import java.net.Socket;

public interface SocketThreadListener {
    /**
     * Интерфейс слушателя потока Socket Thread имеет следующие события:
     */
    void onSocketThreadStart(SocketThread thread, Socket socket); //Socket Thread стартовал
    void onSocketThreadReady(SocketThread thread, Socket socket); //Socket Thread готов
    void onSocketThreadStop(SocketThread thread); //Socket Thread остановился
    void onReceiveString(SocketThread thread, Socket socket, String msg); //Принята строковое сообщение потоком Socket Thread
    void onSocketThreadException(SocketThread thread, Throwable throwable); //Возникло исключение на потоке Socket Thread
}