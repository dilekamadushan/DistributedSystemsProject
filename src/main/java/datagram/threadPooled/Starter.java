package datagram.threadPooled;

import java.net.BindException;
import java.net.SocketException;

/**
 * Created by dileka on 9/29/18.
 */
public class Starter {
    
    public static void main(String[] args) throws SocketException {
        Server server = null;
        
        try {
            server = new Server();
            System.out.println("Server started successfully");
            new Thread(server).start();
            Thread.sleep(20 * 100000);
        }
        catch (BindException be) {
            be.printStackTrace();
            System.out.println("Try a different port");
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Stopping Server");
        }
        if (server != null) {
            server.stop();
        }
        
    }
}
