package com.example.app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.example.client.Host;
import org.json.JSONObject;


public class MyApp {
  public static Host localHost;
  public static List<Host> allKnownHosts;



  public static void main(String args[]) throws Exception{

    if(args.length == 0){
        System.exit(0);
    }

    allKnownHosts = getKnownhosts();
    localHost = returnLocalhost(allKnownHosts, args[0]);

    Scanner scanner= new Scanner(System.in);

    //handleCtrlC();

    handleUserInput(scanner);
    handleReceiverMessagesAndRespond();
  }
  public static Host returnLocalhost(List<Host> allKnownhosts, String siteID){
    for(int i = 0; i < allKnownhosts.size(); ++i){
      if(allKnownhosts.get(i).siteID.equals(siteID)){
        return allKnownhosts.get(i);
      }
    }
    return new Host();
  }

  public static List<Host> getKnownhosts(){
    List<Host> knownhosts = new ArrayList<Host>();
    try{
      List<String> jsonFileList = Files.readAllLines(Paths.get("./knownhosts.json"));
      String jsonFileString = "";
      for(int i = 0; i < jsonFileList.size(); ++i){
        jsonFileString += jsonFileList.get(i);
      }

      JSONObject jsonObject = new JSONObject(jsonFileString);
      JSONObject allHosts = (JSONObject) jsonObject.get("hosts");

      for(String hostName : allHosts.keySet()){
        JSONObject host = (JSONObject) allHosts.get(hostName);
        Host localhost = new Host(hostName, (int) host.get("udp_start_port"),
                (int) host.get("udp_end_port"), (String) host.get("ip_address"));
        knownhosts.add(localhost);
        //System.out.println(localhost.siteID + " " + localhost.udpSenderPortNum
        //        + " " + localhost.udpReceiverPortNum + " " + localhost.ipAddress);
      }
    }
    catch(Exception e){
      System.out.println(e);

    }
    return knownhosts;
  }

  public static void handleUserInput(Scanner scanner) throws Exception {
  
    Runnable waitForUserInput = () -> {
      try {
        DatagramSocket datagramSocket = new DatagramSocket(localHost.udpSenderPortNum);

        while (true) {
          if (scanner.hasNextLine()) {

            String input = scanner.nextLine();

            if (input.equals("quit")) {
              scanner.close();
              System.exit(0);
            }
            System.out.println("You entered: " + input);

            for (int i = 0; i < allKnownHosts.size(); ++i) {
              if (allKnownHosts.get(i).siteID.equals(localHost.siteID)) {
                continue;
              }

              try {
                InetAddress inetAddress = InetAddress.getByName(allKnownHosts.get(i).ipAddress); //change to json value
                int portNumber = allKnownHosts.get(i).udpReceiverPortNum; //change to json value

                JSONObject inputJSON = new JSONObject();
                inputJSON.put("sender", localHost.siteID);
                inputJSON.put("message", input);

                DatagramPacket datagramSendingPacket = new DatagramPacket(inputJSON.toString().getBytes(),
                        inputJSON.toString().getBytes().length, inetAddress, portNumber);
                datagramSocket.send(datagramSendingPacket);

                byte[] replyMessageBytes = new byte[1024];

                DatagramPacket datagramReceivingPacket = new DatagramPacket(replyMessageBytes,
                        replyMessageBytes.length);
                datagramSocket.setSoTimeout(200);
                datagramSocket.receive(datagramReceivingPacket);

                String received = new String(datagramReceivingPacket.getData(), 0,
                        datagramReceivingPacket.getLength());

                JSONObject receivedJSON = new JSONObject(received);

                System.out.println("Reply received from " + receivedJSON.getString("sender") +
                  ": " + receivedJSON.getString("message"));

              } catch (SocketTimeoutException e) {
                continue;
              }
            }
          }
        }
      }
      catch(Exception e){
        System.out.println(e);
      }
    };

    Thread userInputThread = new Thread(waitForUserInput);
    userInputThread.start();
  }


  static void handleReceiverMessagesAndRespond(){
    Runnable waitAndSendEcho = () -> { 

      //InetAddress inetAddress = InetAddress.getByName("localhost"); //change to json value
      try {
        int portNumber = localHost.udpReceiverPortNum; //based on json file
        DatagramSocket datagramSocket = new DatagramSocket(portNumber); //maybe do last port number send from

        while (true) {
          try {
            byte[] requestMessageBytes = new byte[1024];
            DatagramPacket datagramRequestPacket = new DatagramPacket(requestMessageBytes,
                    requestMessageBytes.length);

            datagramSocket.receive(datagramRequestPacket);

            String requestMessage = new String(datagramRequestPacket.getData(), 0,
                    datagramRequestPacket.getLength());

            JSONObject requestJSON = new JSONObject(requestMessage);
            System.out.println(String.valueOf("Request received from " + requestJSON.getString("sender")
              + ": " + requestJSON.getString("message")));

            JSONObject responseJSON = new JSONObject();
            responseJSON.put("sender", localHost.siteID);
            responseJSON.put("message", requestJSON.getString("message"));
            DatagramPacket datagramRespondPacket = new DatagramPacket(responseJSON.toString().getBytes(),
                    responseJSON.toString().getBytes().length, datagramRequestPacket.getAddress(),
                    datagramRequestPacket.getPort());

            datagramSocket.send(datagramRespondPacket);
          } catch (Exception e) {
            continue;
          }
        }
      }
      catch(Exception e){
        System.out.println(e);
      }
    };
    
    Thread thread = new Thread(waitAndSendEcho);
    thread.start();
  }


  public static void handleCtrlC(){
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
          try {
            Thread.sleep(200);
            System.out.println("Shutting down ...");
          } 
          catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
          }
      }
    });
  }

}