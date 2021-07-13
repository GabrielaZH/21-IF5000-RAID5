
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ServerUDP {
   final int PORT = 16789;

   //UDP 
   public DatagramSocket udpSocket; // Socket open for UDP
   public ArrayList<InetAddress> clientIPs; //List of IP addresses communicating via UDP
   public ArrayList<Integer> clientPorts; //List of Ports used for communicating via UDP
   public HashSet<String> connectedClients;//List of Unique clients connected and communicating on our server

   //Get the IP and Port of where the message came from
   InetAddress currentClientIP;
   int currentClientPort ;

   DatagramPacket udpPacket;

   public ServerUDP() {

      try {
         System.out.println(InetAddress.getLocalHost());
      } catch (UnknownHostException uhe) {
         System.err.println(uhe.getMessage());
      }

      try {

         //UDP
         udpSocket = new DatagramSocket(PORT);
         clientIPs = new ArrayList();
         clientPorts = new ArrayList();
         connectedClients = new HashSet();


         UDPThread udpThread = new UDPThread();
         udpThread.start();
         System.out.println("Waiting for UDP Client");

      } catch (IOException ioe) {
         System.err.println(ioe.getMessage());
      }

   }//end of ChatServer Class - The GUI

   //Execute Chat Server
   public static void main(String[] args) {
      ServerUDP cs = new ServerUDP();
   }


  public class UDPThread extends Thread {

      //Buffer
      byte[] byteBuffer;

      public void run() {

         while (true) {
            try {

               // sets/resets buffer size
               byteBuffer = new byte[1024];

               //create new Packet
               udpPacket = new DatagramPacket(byteBuffer, byteBuffer.length);

               //Receive
               udpSocket.receive(udpPacket);
               //New Timestamp of when the message was received;
               String newTimeStamp = "[" + getCurrentTime() + "]";



               //Create string with received message from bytes
               String receivedMsg = new String(byteBuffer, byteBuffer.length);



               //Get the IP and Port of where the message came from
                currentClientIP = udpPacket.getAddress();
                currentClientPort = udpPacket.getPort();
               String outgoingMsg ="";
               if(!receivedMsg.contains("receive")){
                  FileWriter fw = new FileWriter(new File("text1.txt"));
                  fw.write(receivedMsg.trim());
                  fw.flush();
                  fw.close();
                  outgoingMsg = "[" + newTimeStamp + "] IP:" + currentClientIP + " : File Receive from server" ;
                  sendMessageToClients( (outgoingMsg).getBytes());
               }else{
                  String filename = receivedMsg.replace("receive","");
                  FileInputStream source = new FileInputStream("C:\\21-IF5000-RAID5\\Server\\text1.txt");
                  byte buf[]=new byte[1024];
                  int i=0;
                  while(source.available()!=0)
                  {
                     buf[i]=(byte)source.read();
                     i++;
                  }
                  source.close();
                  sendMessageToClients(buf);
               }


            }//end of try
            catch (IOException ioe) {
               System.out.println(ioe.getMessage());
            }

         }//end of while

      }//end of run 
   }//end of UDP thread

   public void sendMessageToClients(byte[] outgoingByte)  {
    try {

       //Unique string to keep track of clients
       String currentClient = "Client IP: " + currentClientIP.toString() + " | Port: " + currentClientPort;

       //If the client that just sent the
       //Received message has not connected before
       //Add the client to list of connected clients,
       //And store their IP and PORT
       if (!connectedClients.contains(currentClient)) {
          connectedClients.add(currentClient);
          clientIPs.add(currentClientIP);
          clientPorts.add(currentClientPort);
          System.out.println("Added new client | " + currentClient);
       }

       //For every client connected
       for (int i = 0; i < clientIPs.size(); i++) {
          //get their IP and Port
          InetAddress clientIp = clientIPs.get(i);
          int clientPort = clientPorts.get(i);

          //construct Packet
          udpPacket = new DatagramPacket(outgoingByte, outgoingByte.length, clientIp, clientPort);
          //send message out
          udpSocket.send(udpPacket);

          System.out.println("Message sent to client #" + i);
       }//end of for

       System.out.println("\nSuccessfully sent message to all Clients!\n\n");

    }catch (IOException ioe) {
         System.out.println(ioe.getMessage());
      }
   }





   public String getCurrentTime() {
      String time =
              new SimpleDateFormat("hh:mm:ss").format(Calendar.getInstance().getTime());
      return time;
   }
}
