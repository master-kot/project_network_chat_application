package chat.client;

import chat.library.Library;
import network.SocketThread;
import network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

/**
 * Графический интерфейс клиент чата
 * Является слушателем сообщений SocketThreadListener
 */
public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;
    /**
     * Добавляем поля и кнопки
     */
    private final JTextArea log = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8189");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top", true);
    private final JTextField tfLogin = new JTextField("ivan");
    private final JPasswordField tfPassword = new JPasswordField("123");
    private final JButton btnLogin = new JButton("Login");
    /**
     * Размещаем элементы на окне
     */
    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private final JList<String> userList = new JList<>();
    private boolean shownIoErrors = false;
    private SocketThread socketThread;

    /**
     * Создаем новый экземпляр графической оболочки чат клиента
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI();
            }
        });
    }

    /**
     * Создаем новый экземпляр графической оболочки чат клиента
     */
    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle("Chat Client");
        setAlwaysOnTop(true);

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        JScrollPane scrollUsers = new JScrollPane(userList);
        String[] users = {"user1_with_an_exceptionally_long_nickname", "user2", "user3", "user4", "user5", "user6", "user7", "user8", "user9", "user10"};
        userList.setListData(users);
        scrollUsers.setPreferredSize(new Dimension(100, 0));
        /**
         * Добавляем слушателей для кнопок и полей
         * все события обрабатываюстя здесь же в методе actionPerformed
         */
        cbAlwaysOnTop.addActionListener(this);
        btnLogin.addActionListener(this);
        btnSend.addActionListener(this);
        btnLogin.addActionListener(this);
        tfMessage.addActionListener(this);
        panelBottom.setVisible(false);
        panelTop.setVisible(true);
        /**
         * Добавляем компоненты графической части клиента
         */
        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);
        /**
         * Добавляем панели
         */
        add(panelTop, BorderLayout.NORTH);
        add(scrollLog, BorderLayout.CENTER);
        add(panelBottom, BorderLayout.SOUTH);
        add(scrollUsers, BorderLayout.EAST);
        setVisible(true);
    }

    /**
     * Обработчик исключений
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        showException(t, e);
        System.exit(1);
    }

    /**
     * Подключиться с созданием нового сокета и сокет треда
     */
    private void connect() {
        try {
            Socket socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    /**
     * Обработчик событий
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        }  else if (src == btnSend || src == tfMessage) {
            sendMessage();
        } else if (src == btnLogin || src == tfLogin ||
                src == tfPassword || src == tfPort ||
                src == tfIPAddress) {
            connect();
        } else if (src == btnDisconnect) {
            socketThread.close();
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    private void sendMessage() {
        String msg = tfMessage.getText();
        if ("".equals(msg)) return;
        tfMessage.setText(null);
        tfMessage.requestFocusInWindow();
        socketThread.sendMessage(msg);
    }

    /**
     * Записываем сообщение в текстовый лог-файл
     */
    private void wrtMsgToLogFile(String msg, String username) {
        try (FileWriter out = new FileWriter("log.txt", true)) {
            out.write(username + ": " + msg + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
    }

    /**
     * Метод логирования сообщений
     */
    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private void showException(Thread t, Throwable e) {
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

    /**
     * Обработка событий SocketThread
     * */
    @Override
    public void onSocketThreadStart(SocketThread thread, Socket socket) {
        putLog("SocketThread start");
    }

    @Override
    public void onSocketThreadStop(SocketThread thread) {
        panelBottom.setVisible(false);
        panelTop.setVisible(true);
        putLog("Connection lost...");
    }

    @Override
    public void onSocketThreadReady(SocketThread thread, Socket socket) {
        putLog("Connection established");
        panelBottom.setVisible(true);
        panelTop.setVisible(false);
        String login = tfLogin.getText();
        String password = new String(tfPassword.getPassword());
        thread.sendMessage(Library.getAuthRequest(login, password));
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        putLog(msg);
    }

    /**
     * Обработка исключения, возникшего на потоке
     */
    @Override
    public void onSocketThreadException(SocketThread thread, Throwable throwable) {
        showException(thread, throwable);
    }
}
