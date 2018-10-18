package datagram.threadPooled;

import datagram.threadPooled.domain.Node;
import datagram.threadPooled.domain.RegisterAndJoinMessenger;
import datagram.threadPooled.workerThread.RoutingTableManager;
import datagram.threadPooled.workerThread.SearchQueryAcceptor;
import datagram.threadPooled.workerThread.Searcher;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by dileka on 9/29/18.
 */
public class Server extends Thread {
    
    protected ExecutorService threadPool = Executors.newFixedThreadPool(20);
    
    private DatagramSocket UDPsocket;
    
    private boolean running;
    
    private byte[] buf = new byte[256];
    
    private ArrayList<Node> toJoinNodes;
    
    private ArrayList<Node> triedToJoinNodes;
    
    private ArrayList<Node> routingTable;
    
    private RegisterAndJoinMessenger registerAndJoinMessenger = null;
    
    private String myIP;
    
    private int myPort;
    
    private long programeStartedTime;
    
    public Server(String BSIp, int BSPort, String myIp, int myPort, String username) throws SocketException {
        programeStartedTime = System.currentTimeMillis();
        this.myIP = myIp;
        this.myPort = myPort;
        UDPsocket = new DatagramSocket(myPort);
        toJoinNodes = new ArrayList<>();
        triedToJoinNodes = new ArrayList<>();
        toJoinNodes = new ArrayList<>();
        routingTable = new ArrayList<>();
        registerAndJoinMessenger = new RegisterAndJoinMessenger(BSIp, BSPort, myIp, myPort, username, UDPsocket, toJoinNodes,
                triedToJoinNodes, routingTable);
    }
    
    public void run() {
        try {
            System.out.println("Server Thread: Before registering the server");
            running = this.registerAndJoinMessenger.start();
            System.out.println("Server Thread:This is the status 1st place"+running);
            System.out.println("Server Thread: Register and join messenger started");
            SearchQueryAcceptor searchQueryAcceptor = new SearchQueryAcceptor(UDPsocket, routingTable, threadPool);
            searchQueryAcceptor.start();
            System.out.println("Server Thread: Query acceptor started");
        }
        catch (ConnectException ce) {
            System.out.println("Server Thread:Bootstrap server unreachable");
            ce.printStackTrace();
            UDPsocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            UDPsocket.close();
        }
        long executionTime;
        System.out.println("Server Thread:This is the status "+running);
        System.out.println("Server Thread: Server Successfully Registered at " + myIP + " " + myPort);
        while (running) {
            System.out.println("Server Thread:Server inside the running loop");
            executionTime = System.currentTimeMillis();
            if ((executionTime - programeStartedTime) / 1000 >300) {
                System.out.println("Server Thread: More than 5 minutes since the start");
                List<Node> nodes = routingTable.stream().filter(Node::isJoined).collect(Collectors.toList());
                if (nodes.size() < 2) {
                    System.out.println(
                            "Server Thread: The 5 minute timeout has occurred and Server stopping due to failure to join with at least two nodes");
                    break;
                }
            }
            System.out.println("Server Thread:Server listening....");
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                UDPsocket.receive(packet);
                System.out.println("Server Thread: Server received a packet ");
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Server Thread: Server faced an error when receiving the packet, exiting");
                break;
            }
            
            String request = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Server Thread:In server received " + request);
            boolean whoseResponse = false;
            while (!whoseResponse) {
                System.out.println("Server Thread:Trying to identify the handler for the packet " + request);
                whoseResponse = checkForJoinResponseMessage(request);
                
                if (whoseResponse) {
                    System.out.println("Server Thread:JOINOK messeage Packet to be handled by Routing Table manager ");
                    this.threadPool.execute(new RoutingTableManager(this.UDPsocket, routingTable, packet));
                    System.out.println("Server Thread: One Routing table manager started");
                    break;
                    
                }
                System.out.println("Server Thread:Not JOINOK message");
                whoseResponse = checkForJoinRequestMessage(request);
                if (whoseResponse) {
                    
                    System.out.println("Server Thread:JOIN messeage Packet to be handled by Routing Table manager ");
                    this.threadPool.execute(new RoutingTableManager(this.UDPsocket, routingTable, packet));
                    System.out.println("Server Thread: One Routing table manager started");
                    break;
                    
                }
                System.out.println("Server Thread:Not JOIN messeage Packet ");
                whoseResponse = checkForSearchMessage(request);
                if (whoseResponse) {
                    System.out.println("Server Thread:worker Started");
                    //DatagramPacket dp = packet;
                    this.threadPool.execute(new Searcher(this.UDPsocket, routingTable, "searchString"));
                }
                
            }
            System.out.println("Server Thread: Figured Out the handler");
            
            System.out.println("Clear the buffer after every message");
            buf = new byte[256];
        }
        System.out.println("Server interrupted, Exiting");
        UDPsocket.close();
        System.out.println("Socket Closed");
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
    
    public boolean checkForJoinResponseMessage(String request) {
        
        System.out.println("Server Thread:checking for JOINOK response" + request);
        if (request.contains("JOINOK")) {
            return true;
            
        }
        System.out.println("Server Thread:checking for JOINOK response failed" + request);
        return false;
    }
    
    public boolean checkForJoinRequestMessage(String request) {
        
        System.out.println("Server Thread:checking for JOIN request" + request);
        if (request.contains("JOIN")) {
            return true;
        }
        System.out.println("Server Thread:checking for JOIN response failed" + request);
        return false;
    }
    
    public boolean checkForGossipMessage(String request) {
        
        System.out.println("Server Thread:checking for gossip resquest" + request);
        if (request.contains("GOSSIPOK")) {
            return true;
        }
        return false;
    }
    
    public boolean checkForSearchMessage(String request) {
        
        System.out.println("Server Thread:checking for search response" + request);
        if (request.contains("SEARCHOK")) {
            return true;
        }
        return false;
    }
}
