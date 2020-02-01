package online.server.gui;

import online.server.core.ChatServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Графическая оболочка сервера чата на основе фреймворка
 * Отвечает за запуск и остановку сервера чата
 * Данный класс имплементерует интерфейс ActionListener
 * и явлеется обработчиком событий и возгникающих исключений
 */
public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {

    private static final int POS_X = 1000;
    private static final int POS_Y = 550;
    private static final int HEIGHT = 100;
    private static final int WIDTH = 200;

    private final ChatServer chatServer = new ChatServer();
    private final JButton btnStart = new JButton("Start");
    private final JButton btnStop = new JButton("Stop");

    /**
     * Запуск графической оболочки с созданием экземпляра класса ServerGUI
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerGUI();
            }
        });
    }

    private ServerGUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Thread.setDefaultUncaughtExceptionHandler(this);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setTitle("Server GUI");
        setResizable(false);
        setAlwaysOnTop(true);
        setLayout(new GridLayout(1, 2));
        btnStop.addActionListener(this);
        btnStart.addActionListener(this);
        add(btnStart);
        add(btnStop);
        setVisible(true);
    }

    /**
     * События нажатия кнопок старт и стоп
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnStart) {
            chatServer.start(8189);
//            throw new RuntimeException("Hello from EDT!");
        } else if (src == btnStop) {
            chatServer.stop();
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    /**
     * Метод обработки возникших на графической части сервера исключений
     * @param t - поток, в котором возникло исключение
     * @param e - возникшее исключение
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
       e.printStackTrace();
       StackTraceElement[] ste = e.getStackTrace();
       String msg = String.format("Exception in thread %s: %s: %s\n\t at %s",
               t.getName(),
               e.getClass().getCanonicalName(),
               e.getMessage(),
               ste[0]
       );
       JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
    }
}
