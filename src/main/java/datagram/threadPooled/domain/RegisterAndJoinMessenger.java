package datagram.threadPooled.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by dileka on 9/27/18.
 */
public class RegisterAndJoinMessenger {
    
    private String BSIP;
    
    private String myIP;
    
    private int myPort;
    
    private int BSPort;
    
    private String username;
    
    private DatagramSocket threadUDPSocket = null;
    
    private ArrayList<Node> toJoinNodes;
    
    private ArrayList<Node> triedToJoinNodes;
    
    private ArrayList<Node> routingTable;
    
    private Socket TCPSocket;
    
    private PrintWriter out;
    
    private BufferedReader in;
    
    public RegisterAndJoinMessenger(String BSIP, int BSPort, String myIP, int myPort, String username, DatagramSocket socket,
            ArrayList<Node> toJoinNodes, ArrayList<Node> triedToJoinNodes, ArrayList<Node> routingTable) {
        
        this.BSIP = BSIP;
        this.myIP = myIP;
        this.myPort = myPort;
        this.BSPort = BSPort;
        this.username = username;
        this.threadUDPSocket = socket;
        this.toJoinNodes = toJoinNodes;
        this.triedToJoinNodes = triedToJoinNodes;
        this.routingTable = routingTable;
    }
    
    public boolean start() throws IOException {
        System.out.println("Register and Join Messenger:Started");
        boolean isRegistered = Register(BSIP, BSPort, "0030 REG " + myIP + " " + myPort + " " + username);
        
        if (isRegistered) {
            System.out.println("Register and Join Messenger:Bootstrap Server Successfully Registered");
            boolean isJoinSent;
            System.out.println("Register and Join Messenger:Trying to send join messages to nodes");
            while (toJoinNodes.size() <= 1) {
                isJoinSent = sendJoin();
                if (isJoinSent)
                    return true;
            }
        }
        return false;
    }
    
    //Function to register in BS
    public boolean Register(String BSIp, int BSPort, String msg) throws IOException {
        System.out.println("Register and Join Messenger:Trying to create a TCP connection to send REG");
        TCPSocket = new Socket(BSIp, BSPort);
        out = new PrintWriter(TCPSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
        
        out.println(msg);
        char[] chars = new char[8192];
        int read = in.read(chars);
        String inMesssage = String.valueOf(chars, 0, read);
        System.out.println("Register and Join Messenger:Reply from BS server:" + inMesssage);
        String REGOK = inMesssage.substring(5, 10);
        int lengthOfMessage = Integer.parseInt(inMesssage.substring(0, 4));
        System.out.println("Register and Join Messenger:REGOK message:" + REGOK);
        int code = 0;
        if (lengthOfMessage > 12) {
            code = Integer.parseInt(inMesssage.substring(11, 13).trim());
            System.out.println("Register and Join Messenger:Code" + code);
        }
        
        if ("REGOK".equals(REGOK))
            if (lengthOfMessage == 12) {
                System.out.println("Register and Join Messenger:No hosts connected already");
                return true;
            } else if (98 > code) {
                System.out.println("Register and Join Messenger:No of Hosts connected already- " + inMesssage.substring(13));
                System.out.println("Register and Join Messenger:Connected to Bootstrap server successfully");
                String[] hostList;
                if (code < 10) {
                    hostList = inMesssage.substring(13).trim().split("\n");
                } else {
                    hostList = inMesssage.substring(14).trim().split("\n");
                }
                
                System.out.println("Register and Join Messenger:host string [0]:" + hostList[0]);
                for (int i = 0; i < hostList.length; i++) {
                    String[] data = hostList[i].trim().split(" ");
                    
                    String[] ips = data[0].replace(".", " ").split(" ");
                    System.out.println(
                            "Register and Join Messenger:" + Integer.parseInt(ips[0]) + " " + Integer.parseInt(ips[1]) + " "
                                    + Integer.parseInt(ips[2]) + " " + Integer.parseInt(ips[3]));
                    toJoinNodes.add(new Node(new byte[] { (byte) Integer.parseInt(ips[0]), (byte) Integer.parseInt(ips[1]),
                            (byte) Integer.parseInt(ips[2]), (byte) Integer.parseInt(ips[3]) }, data[2],
                            Integer.parseInt(data[1])));
                    System.out.println("Register and Join Messenger:" + toJoinNodes.get(i).toString());
                }
                for (Node node : toJoinNodes) {
                    System.out.println("Register and Join Messenger:Node created" + node.toString());
                }
                this.routingTable = toJoinNodes;
                return true;
                
            } else {
                String errorCode = inMesssage.substring(11);
                switch (errorCode) {
                    case "9999":
                        System.out.println("failed, there is some error in the command");
                        break;
                    case "9998":
                        System.out.println("failed, already registered to you, unregister first");
                        break;
                    case "9997":
                        System.out.println(" failed, registered to another user, try a different IP and port");
                        break;
                    case "9996":
                        System.out.println("failed, can’t register. BS full.");
                    default:
                        System.out.println("Invalid command");
                }
            }
        
        in.close();
        out.close();
        TCPSocket.close();
        return false;
        
    }
    
    public boolean sendJoin() throws IOException {
        System.out.println("Register and Join Messenger:Inside Send Join method");
        
        if (toJoinNodes.size() <= 4) {
            System.out.println("Register and Join Messenger:Inside Send Join method and sending Join message to all nodes "
                    + toJoinNodes.size());
            for (Node node : toJoinNodes) {
                toJoinNodes.remove(node);
                triedToJoinNodes.add(node);
                try {
                    System.out.println(
                            "Register and Join Messenger: Trying to send join message for node" + node.toString() + " "
                                    + node.getIp().toString());
                    byte[] bufToSend = ("0022 JOIN " + Arrays.toString(node.getIp()) + " " + node.getPort()).getBytes();
                    DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                            InetAddress.getByAddress(node.getIp()), node.getPort());
                    node.setRetries(1);
                    threadUDPSocket.send(nodeDatagramPacket);
                    System.out.println("Register and Join Messenger: Successfully sent the join message");
                }
                catch (UnknownHostException e) {
                    System.out.println("Register and Join Messenger:Node unreachable");
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Register and Join Messenger:Error in socket");
                }
            }
            return true;
        } else if (toJoinNodes.size() <= 2) {
            Node node;
            for (int i = 0; i < 2; i++) {
                node = toJoinNodes.get(ThreadLocalRandom.current().nextInt(0, toJoinNodes.size()));
                toJoinNodes.remove(node);
                triedToJoinNodes.add(node);
                try {
                    System.out.println(
                            "Register and Join Messenger: Trying to send join message for node" + node.toString() + " "
                                    + node.getIp().toString());
                    byte[] bufToSend = ("0022 JOIN " + Arrays.toString(node.getIp()) + " " + node.getPort()).getBytes();
                    DatagramPacket nodeDatagramPacket = new DatagramPacket(bufToSend, bufToSend.length,
                            InetAddress.getByAddress(node.getIp()), node.getPort());
                    node.setRetries(1);
                    threadUDPSocket.send(nodeDatagramPacket);
                    System.out.println("Register and Join Messenger: Successfully sent the join message");
                }
                catch (UnknownHostException e) {
                    System.out.println("Register and Join Messenger:Node unreachable");
                    e.printStackTrace();
                    if (toJoinNodes.size() < (2 - i)) {
                        return false;
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Register and Join Messenger:Error in socket");
                    if (toJoinNodes.size() < (2 - i)) {
                        return false;
                    }
                }
            }
            System.out.println("Register and Join Messenger:Successfully sent the Join Message to 2 nodes");
            return true;
        }
        System.out.println("Register and Join Messenger:No nodes to send the JOIN" + toJoinNodes.size());
        return false;
    }
    
}
