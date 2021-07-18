
import huffman.HuffmanEncoding;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ServerUDP {
   final int PORT = 16789;
   public DatagramSocket udpSocket; // Socket open for UDP
   public ArrayList<InetAddress> clientIPs; //List of IP addresses
   public ArrayList<Integer> clientPorts; //List of Ports
   public HashSet<String> connectedClients;//List of clients

   InetAddress currentClientIP;
   int currentClientPort ;
   DatagramPacket udpPacket;
   String filename="";
   int numNodos;
   byte[] byteBuffer;
   String pathFiles="C:\\21-IF5000-RAID5\\Controller_Node_Java\\filesReceive\\";
   public ServerUDP() {
      try {
         System.out.println(InetAddress.getLocalHost().getHostAddress());
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

   }

   public static void main(String[] args) {
      ServerUDP cs = new ServerUDP();
   }


  public class UDPThread extends Thread {



      public void run() {

         while (true) {
            try {

               // sets/resets buffer size
               byteBuffer = new byte[60000];

               //create new Packet
               udpPacket = new DatagramPacket(byteBuffer, byteBuffer.length);

               //Receive
               udpSocket.receive(udpPacket);
               //New Timestamp of when the message was received;
               String newTimeStamp = "[" + getCurrentTime() + "]";

               //Get the IP and Port of where the message came from
               currentClientIP = udpPacket.getAddress();
               currentClientPort = udpPacket.getPort();
               String response = "";
               String receivedMsg = "";


               receivedMsg = readFile(decodeFile(pathFiles + "fileEncode.txt", pathFiles + "fileDecode.txt"));//read data in file

               //TODO Recibir archivo con numero de nodos
               String nodos="";
               // verify if the message is nodes quantity
               if (receivedMsg.contains("nodes:")) {
                  nodos = receivedMsg.replaceAll("nodes:","");
                  nodos = nodos.replaceAll("\uFFFF","");
                  numNodos =  Integer.parseInt(nodos);

               // verify if the message is the name file
               }else if(receivedMsg.contains(".txt")){
                  filename=receivedMsg.trim();
                  filename = filename.replaceAll(".txt","");
                  filename = filename.replaceAll("\uFFFF","");
                  //verify if the message is the content file for save
               } else if(!receivedMsg.contains("receiveRequest")){
                  decodeFile(pathFiles+"fileEncode.txt",pathFiles+filename+".txt");
                  response = "[" + newTimeStamp + "] IP:" + currentClientIP + " : File Receive from server" ;

                  Send send= new Send();
                  RAID5 raid5 = new RAID5();

                  List<String> disks = new ArrayList<>();
                  for (int i = 0; i < numNodos; i++) {
                     disks.add("DISK"+i);
                  }

                  raid5.createDisks(disks,pathFiles);
                  byte[] fileLikeByte = raid5.parseFileToByte(new File(pathFiles+filename+".txt"));
                  raid5.saveFileWithRAID5(fileLikeByte,disks.size(),pathFiles);

                  for (int i = 0; i < numNodos; i++) {
                     send.sendFile(new File(pathFiles+"DISK"+i,"file"+i+".txt"));
                     Thread.sleep(1000);
                     send.sendFile(new File(pathFiles+"DISK"+i,"fileParity"+i+".txt"));

                  }

                  sendMessageToClients( (response).getBytes());

                  //verify if the message is send the file
               }else if (receivedMsg.contains("receiveRequest")){
                  File finalFile= new File(encodeFile(pathFiles+"fileEncode.txt",pathFiles+filename+".txt"));
                  FileInputStream source = new FileInputStream(finalFile);
                  byte buf[]=new byte[60000];
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
            catch (IOException | InterruptedException i) {
               System.out.println(i.getMessage());
            }

         }//end of while

      }//end of run 
   }//end of UDP thread


   public void sendMessageToClients(byte[] outgoingByte)  {
    try {

       //Unique string to keep track of clients
       String currentClient = "Client IP: " + currentClientIP.toString() + " | Port: " + currentClientPort;

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

public String decodeFile(String pathFileEncode, String pathFileDecode){
   File fileEncode = new File(pathFileEncode);
   try (OutputStream os = new FileOutputStream(fileEncode)) {
      os.write(byteBuffer);
   } catch (Exception e) {
      throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
   }
   File file = new File(pathFileDecode);
   HuffmanEncoding huffman = new HuffmanEncoding();
   huffman.decode(pathFileEncode,pathFileDecode);
   return pathFileDecode;
}

public String encodeFile(String pathFileEncoded, String pathFileDecodeNameFile){
   File fileSaved= new File(pathFileDecodeNameFile);
   HuffmanEncoding huffman = new HuffmanEncoding();//encode the file
   String fileOutPath=(pathFileEncoded);
   File fileOut= new File(fileOutPath);
   huffman.encoding(fileSaved.getPath(),fileOutPath);
   return pathFileEncoded;
}
   public String getCurrentTime() {
      String time =
              new SimpleDateFormat("hh:mm:ss").format(Calendar.getInstance().getTime());
      return time;
   }

   public String readFile(String path) {
    String text="";
      try {
         FileReader file=new FileReader(path);
         int c=0;
         while(c!=-1) {
            c=file.read();
            char content=(char)c;
            text+=content;
         }
         file.close();
      } catch (IOException e) {

         System.out.println("No se ha encontrado el archivo");
      }
      return text;
   }
}
