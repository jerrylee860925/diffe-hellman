package iupui.zhijieli.trucker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jerry lee on 11/3/2016.
 */

public class SocketServerReplyThread extends Thread {



    private Socket hostThreadSocket;


    SocketServerReplyThread(Socket socket) {
        hostThreadSocket = socket;

    }

    @Override
    public void run() {
        OutputStream outputStream;
        String msgReply = "Hello from Server, you are #";
        String message = "";
        try {
            outputStream = hostThreadSocket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(msgReply);
            printStream.close();
            message += "replayed: " + msgReply + "\n";
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            message += "Something wrong! " + e.toString() + "\n";
        }
    }
}
