package datagram.threadPooled;

import datagram.threadPooled.domain.RegisterAndJoinMessenger;
import datagram.threadPooled.domain.Node;
import datagram.threadPooled.domain.RoutingTableManager;
import datagram.threadPooled.workerThread.Searcher;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dileka on 9/29/18.
 */
public class Server extends Thread {
    
    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);
    
    private DatagramSocket UDPsocket;
    
    private boolean running;
    
    private byte[] buf = new byte[256];
    
    private ArrayList<Node> toJoinNodes;
    
    private ArrayList<Node> triedToJoinNodes;
    
    private ArrayList<Node> routingTable;
    
    private RegisterAndJoinMessenger registerAndJoinMessenger = null;
    
    private RoutingTableManager routingTableManager = null;
    
    public Server() throws SocketException {
        UDPsocket = new DatagramSocket(4445);
        toJoinNodes = new ArrayList<>();
        triedToJoinNodes = new ArrayList<>();
        toJoinNodes = new ArrayList<>();
        routingTable = new ArrayList<>();
        registerAndJoinMessenger = new RegisterAndJoinMessenger(UDPsocket, toJoinNodes, triedToJoinNodes, routingTable);
        
    }
    
    public void run() {
        
        try {
            running = this.registerAndJoinMessenger.start();
        }
        catch (ConnectException ce) {
            System.out.println("Bootstrap server unreachable");
            ce.printStackTrace();
            UDPsocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            UDPsocket.close();
        }
        if (running){
            routingTableManager = new RoutingTableManager(this.UDPsocket, this.routingTable);
            
        }
        while (running) {
            System.out.println("Server listening....");
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                UDPsocket.receive(packet);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            boolean isImportant = checkForJoinResponseMessage(packet);
            if (isImportant) {
                
            } else {
                System.out.println("worker Started");
                //DatagramPacket dp = packet;
                this.threadPool.execute(new Searcher(this.UDPsocket,routingTable,"searchString"));
            }
        }
        UDPsocket.close();
        System.out.println("Socket closed");
    }
    
    public String data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret.toString();
    }
    
    public boolean checkForJoinResponseMessage(DatagramPacket datagramPacket) {
        
        String received = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        System.out.println("in server received " + received);
        if (received.contains("JOINOK")) {
            int code = Integer.parseInt(received.substring(12));
            return code == 0;
            
        }
        return false;
    }
    
    public void addToNodeList(DatagramPacket datagramPacket) {
        InetAddress address = datagramPacket.getAddress();
        int port = datagramPacket.getPort();
        Node node = routingTable.stream().filter(s -> s.getIp() == datagramPacket.getAddress().getAddress()).findFirst().orElse(null);
        System.out.println("A node responded to the Join message:"+address+" "+port);
        node.setStatus(true);
    }
    
}
