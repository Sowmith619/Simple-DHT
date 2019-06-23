package edu.buffalo.cse.cse486586.simpledht;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import android.content.*;
import android.database.*;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;


public class SimpleDhtProvider extends ContentProvider {
        static ArrayList<String> portlist=new ArrayList<String>();
        static Comparator<String> comp= new Comparator<String>() {
        @Override
        public int compare(String a, String b) {
            return (a.compareTo(b));
        }
    };
    LinkedList<String> activePorts= new LinkedList<String>();
    static ArrayList<String> myFiles= new ArrayList<String>();
    static final int SERVER_PORT = 10000;
    static String nextPort="";
    static String prevPort="";
    static String portStr="";
    static  HashMap<String,LinkedList<String>> hm= new HashMap<String,LinkedList<String>>();
    static HashMap<String,String> hashPorts= new HashMap<String,String>();
    static Boolean min=false;
    static Boolean max= false;
    static String portno="";
    //ArrayList<String> foundAll=new ArrayList<String>();
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if(selection.contains("@") || selection.contains("*")){
            File[] files  = getContext().getFilesDir().listFiles();
            for(File file:files){
                file.delete();

            }
        }
        else{

            File dir =  getContext().getFilesDir();

            File f= new File(dir,selection);
            if(f.exists())
                f.delete();
        }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String msg_key = values.getAsString("key");
        String msg_value = values.getAsString("value");
        Context con = getContext();
        try
        {

            String keyHash = genHash(msg_key);
            if(prevPort.equals(portStr) && nextPort.equals(portStr)){
                FileOutputStream out = con.openFileOutput(msg_key, Context.MODE_PRIVATE);
                out.write(msg_value.getBytes());
                out.close();
                myFiles.add(msg_key);
                return uri;
            }
            if(genHash(nextPort).compareTo(portno)>0 && genHash(prevPort).compareTo(portno)>0 && !min){
                min=true;
                Log.e("min",portStr);
            }
            if(!min) {

                if (keyHash.compareTo(genHash(prevPort)) > 0 && (keyHash.compareTo(portno) <=0)) {
                    FileOutputStream out = con.openFileOutput(msg_key, Context.MODE_PRIVATE);
                    out.write(msg_value.getBytes());
                    out.close();
                    myFiles.add(msg_key);
        ;           return uri;

                } else {
                    Message m = new Message();
                    m.setType("Propagate");
                    m.setMsgValue(msg_key + "," + msg_value);
                    m.setRequestedPort(portStr);
                    Client c = new Client(m);
                    c.start();
                    return null;
                }
            }else {
                if((keyHash.compareTo(portno)<=0)|| ((keyHash.compareTo(portno)>0 && keyHash.compareTo(genHash(prevPort))>0))){
                    FileOutputStream out = con.openFileOutput(msg_key, Context.MODE_PRIVATE);
                    out.write(msg_value.getBytes());
                    out.close();
                    myFiles.add(msg_key);
                   // Log.e("Inserting at: ",portStr);
                    return uri;
                }
                else {
                    Message m = new Message();
                    m.setType("Propagate");
                    m.setMsgValue(msg_key + "," + msg_value);
                    m.setRequestedPort(portStr);
                    Client c = new Client(m);
                    c.start();
                    return null;
                }

            }
        }

        catch (IOException e)
        {
            Log.e("Insert:", "insert method failed");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return uri;

    }

    @Override
    public boolean onCreate() {
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        portlist.add("11108");
        portlist.add("11112");
        portlist.add("11116");
        portlist.add("11120");
        portlist.add("11124");
        Message m = new Message();
        Server s= new Server();
        s.start();
        try {
            portno=genHash(portStr);
            if(portStr.equals("5554")){
                hashPorts.put(portno,portStr);
                activePorts.add(portno);
                nextPort=portStr;
                prevPort=portStr;
            }
            else{
                m.setType("Join");
                m.setCurrentPort(portStr);
                nextPort=portStr;
                prevPort=portStr;
                Client c= new Client(m);
                c.start();

            }


            //s.start();
           // Client c = new Client(m);

            Log.e(portStr,portno);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }



        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String given_key, String[] selectionArgs,
            String sortOrder) {
        String write_fields[] = {"key", "value"};
        MatrixCursor mc = new MatrixCursor(write_fields);
        Context context_obj = getContext();
        String reqPort=portStr;

        if( given_key.equals("*") || given_key.equals("@")  ){

            for(String s:myFiles){
                try {
                    //using FileInputStream class
                    FileInputStream fis = context_obj.openFileInput(s);
                    InputStreamReader inputStreamReader = new InputStreamReader(fis);
                    String retrieved_values = new BufferedReader(inputStreamReader).readLine();
                    String arr_content[] = {s, retrieved_values};
                    mc.addRow(arr_content);
                    fis.close();
                   // return mc;
                } catch (IOException e) {
                    //Log.e(TAG,e.getMessage());
                    Log.e("Qu", "Querying failed");
                }
            }

            if(given_key.equals("*") && !prevPort.equals(portStr) && !nextPort.equals(portStr)){
                //ArrayList<ArrayList<String>> as= new ArrayList<ArrayList<String>>();
                ArrayList<String> notHardcodedPorts=new ArrayList<String>();
                Message message= new Message();
                message.setType("getPorts");
                message.setCurrentPort(portStr);
                Client c= new Client(message);
                c.start();
                notHardcodedPorts=c.getPorts();
                Log.e("ports",notHardcodedPorts.size()+"");
                Log.e("ports",notHardcodedPorts.toString());
                for(String s:notHardcodedPorts){
                    int num=2*Integer.parseInt(portStr);
                    if(!(s).equals(num+"")) {
                       Message m = new Message();
                        Log.e("Called",s);
                        m.setType("all");
                        m.setCurrentPort(s);
                        Client c1= new Client(m);
                        c1.start();
                        ArrayList<String> ast=c1.allAns();
                        Log.e("Size in query",ast.size()+"");
                        if(ast!=null) {
                            for (String b : ast) {
                                String arr_content[] = {b.split(",")[0], b.split(",")[1]};
                                mc.addRow(arr_content);
                            }
                        }
                    }
                }
            }
        }else {
            if(myFiles.contains(given_key)){
                try {
                    //using FileInputStream class
                    FileInputStream fis = context_obj.openFileInput(given_key);
                    InputStreamReader inputStreamReader = new InputStreamReader(fis);
                    String retrieved_values = new BufferedReader(inputStreamReader).readLine();
                    String arr_content[] = {given_key, retrieved_values};
                    mc.addRow(arr_content);
                    fis.close();
                    //return mc;
                } catch (IOException e) {
                    //Log.e(TAG,e.getMessage());
                    Log.e("Qu", "Querying failed");
                }
            }
            else{
                try {
                   // String fileHash=genHash(given_key);
                    Message m = new Message();
                    m.setType("query");
                  //  Log.e("thnk:",given_key);
                    m.setRequestedPort(reqPort);
                    m.setMsgValue(given_key);
                    Client c = new Client(m);
                    c.start();
                    String[] found=c.ans();
                    //String[] fo=found.split(",");
                    mc.addRow(found);
                    return mc;

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        }
        // Log.v("query", given_key);
        return mc;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
   public class Server extends Thread {
        private Uri AUri = null;

        private Uri buildUri(String scheme, String authority)
        {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }
          public void run(){


              try {
                  ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                  Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
                  while (true) {
                      Socket client = serverSocket.accept();
                      DataInputStream ois = new DataInputStream(new BufferedInputStream(client.getInputStream()));

                      String m = ois.readUTF();
                      //Log.e("Server:",m);
                      if (m.contains("join")) {
                          Log.e("ent", "......");
                          DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                          String[] bong = m.split(",");
                          String b = bong[0];
                          String hashedValue = genHash(b);
                          hashPorts.put(hashedValue, b);
                          activePorts.add(hashedValue);
                          Collections.sort(activePorts);
                          for (int i = 0; i < activePorts.size(); i++) {
                              if (i == 0) {
                                  LinkedList<String> ls = new LinkedList<String>();
                                  ls.add(activePorts.get(activePorts.size() - 1));
                                  ls.add(activePorts.get(i + 1));
                                  hm.put(activePorts.get(0), ls);

                              } else if (i == activePorts.size() - 1) {
                                  LinkedList<String> ls = new LinkedList<String>();
                                  ls.add(activePorts.get(i - 1));
                                  ls.add(activePorts.get(0));
                                  hm.put(activePorts.get(activePorts.size() - 1), ls);
                              } else {
                                  LinkedList<String> ls = new LinkedList<String>();
                                  ls.add(activePorts.get(i - 1));
                                  ls.add(activePorts.get(i + 1));
                                  hm.put(activePorts.get(i), ls);
                              }
                          }
                          String mySucc = hashPorts.get(hm.get(portno).get(1));
                          String myPred = hashPorts.get(hm.get(portno).get(0));
                          nextPort = mySucc;
                          prevPort = myPred;
                          //hm.get(hashedValue);
                          String pred = hm.get(hashedValue).get(0);
                          String succ = hm.get(hashedValue).get(1);
                          dos.writeUTF(hashPorts.get(pred) + "," + hashPorts.get(succ));
                      } else if (m.contains("Propagate")) {
                          String[] insertMsgs = m.split(",");
                          DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                          //  String keyHash = genHash(insertMsgs[0]);
                          ContentValues mContentValues = new ContentValues();

                          mContentValues.put("key", insertMsgs[0]);
                          mContentValues.put("value", insertMsgs[1]);

                          Uri newUri = getContext().getContentResolver().insert(mUri, mContentValues);
                          if (newUri != null) {
                              Log.e("By: ", portStr);
                              dos.writeUTF("insertion done");
                          } else {
                              dos.writeUTF("still propagating");
                          }
                      } else if (m.contains("update")) {
                          if (m.contains("pred")) {
                              DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                              String[] portUpdate = m.split(",");
                              nextPort = portUpdate[2];
                              dos.writeUTF("updated");

                          } else {
                              DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                              String[] portUpdate = m.split(",");
                              prevPort = portUpdate[2];
                              dos.writeUTF("updated");
                          }
                      } else if (m.contains("query")) {
                          String[] mg = m.split(",");
                          String[] proj = {"key", "value"};
                          Cursor q = query(mUri, proj, mg[1], null, "");
                          DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                          if (q == null) {
                              dos.writeUTF("searching");
                          } else {
                              if (q.getCount() >= 1) {
                                  while (q.moveToNext()) {
                                      String key = q.getString(0);
                                      String value = q.getString(1);
                                      dos.writeUTF(key + "," + value);
                                  }


                                  //  Log.e("Update: ","Next Port: "+nextPort+" , Prev Port: "+prevPort);

                              }

                          }
                      }

                      else if(m.equals("all")) {
                          Log.e("entered","all in server"+portStr);
                          DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                          String[] proj = {"key", "value"};

                          Cursor q = query(mUri, proj, "@", null, "");
                          Log.e("Cursor",q.getCount()+" in server ");
                          String foundall="";
                          if (q.getCount() >= 1) {

                             // ArrayList<String> sd= new ArrayList<String>();
                              while (q.moveToNext()) {
                                  String key = q.getString(0);
                                  String value = q.getString(1);
                                  String keyValue=key+"&"+value;
                                  foundall=foundall+keyValue+",";

                                 // dos.writeUTF(key + "," + value);
                              }
//                             for(String s:sd){
//                                 dos.writeUTF(s);
//                                 sd.remove(s);
//                             }
                              Log.e("foundal",foundall);


                          }dos.writeUTF(foundall);
                       //   dos.writeUTF("done");
                      }else if(m.equals("getPorts")){
                          DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                          String p="";
                          for(Map.Entry<String,String> mp:hashPorts.entrySet()){
                              p=p+mp.getValue()+",";
                          }
                          dos.writeUTF(p);

                      }

                  }
              }catch (Exception e) {
                  e.printStackTrace();
              }

          }
    }
    public class Client extends Thread {
        Message m;

        public Client(Message m) {
            this.m = m;
        }

        public void run() {
            try {

                if (m.getType().equals("Join")) {

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt("11108"));
                    DataOutputStream oos = new DataOutputStream(socket.getOutputStream());

                    oos.writeUTF(m.getCurrentPort() + ",join");
                    DataInputStream ois = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    String msg = ois.readUTF();
                    String ports[] = msg.split(",");
                    prevPort = ports[0];
                    nextPort = ports[1];
                    //  Log.e("Client",msg);
                    if (!ports[0].equals("5554")) {

                        Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), 2 * Integer.parseInt(ports[0]));
                        DataOutputStream oos1 = new DataOutputStream(socket1.getOutputStream());
                        DataInputStream ois1 = new DataInputStream(new BufferedInputStream(socket1.getInputStream()));
                        oos1.writeUTF("update,pred," + portStr);
                        if (ois1.readUTF().equals("updated")) {
                            socket1.close();
                        }

                    }
                    if (!ports[1].equals("5554")) {

                        Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), 2 * Integer.parseInt(ports[1]));
                        DataOutputStream oos1 = new DataOutputStream(socket1.getOutputStream());
                        DataInputStream ois1 = new DataInputStream(new BufferedInputStream(socket1.getInputStream()));
                        oos1.writeUTF("update,succ," + portStr);
                        if (ois1.readUTF().equals("updated")) {
                            socket1.close();
                        }
                    }
                    //    Log.e("Update: ","Next Port: "+nextPort+" , Prev Port: "+prevPort);
                } else if (m.getType().equals("Propagate")) {

                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), 2 * Integer.parseInt(nextPort));
                    DataOutputStream oos1 = new DataOutputStream(socket1.getOutputStream());
                    DataInputStream ois1 = new DataInputStream(new BufferedInputStream(socket1.getInputStream()));
                    oos1.writeUTF(m.getMsgValue() + "," + m.getRequestedPort() + ",Propagate");
                    String s = ois1.readUTF();
                    if (s.equals("insertion done")) {
                        // Log.e("Inserted: ",m.getMsgValue().toString());

                        socket1.close();
                    } else if (s.equals("still propagating")) {
                        socket1.close();
                    }

                }

            } catch (StreamCorruptedException s) {

                s.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public String[] ans() {
            try {

                if (m.getType().equals("query")) {

                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), 2 * Integer.parseInt(nextPort));
                    DataOutputStream oos1 = new DataOutputStream(socket1.getOutputStream());
                    DataInputStream ois1 = new DataInputStream(new BufferedInputStream(socket1.getInputStream()));
                    oos1.writeUTF("query," + m.getMsgValue());
                    String msg = ois1.readUTF();
                    if (msg.equals("searching")) {
                        socket1.close();
                    } else {

                        socket1.close();
                        String[] trial = msg.split(",");
//                        m.setMsgValue(trial[0] + "," + trial[1]);
//                        return m.getMsgValue();
                        return trial;
//
//                        }

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        public ArrayList<String> allAns() {
          ArrayList<String> as = new ArrayList<String>();

            try {
                if(m.getType().equals("all")) {

                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(m.getCurrentPort()));
                    DataOutputStream oos1 = new DataOutputStream(socket1.getOutputStream());
                    DataInputStream ois1 = new DataInputStream(new BufferedInputStream(socket1.getInputStream()));
                    oos1.writeUTF("all");
                    String mes= ois1.readUTF();
                    String[] keyValue=mes.split(",");
                    for(String s:keyValue){
                        String[] star=s.split("&");
                        as.add(star[0]+","+star[1]);

                    }
                    socket1.close();



                }
            } catch (Exception e) {
                e.printStackTrace();
            }
           Log.e("allans",as.size()+" ..");
            return as;
    }


        public ArrayList<String> getPorts() {
            ArrayList<String> as = new ArrayList<String>();

            try {
                if(m.getType().equals("getPorts")) {

                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt("11108"));
                    DataOutputStream oos1 = new DataOutputStream(socket1.getOutputStream());
                    DataInputStream ois1 = new DataInputStream(new BufferedInputStream(socket1.getInputStream()));
                    oos1.writeUTF("getPorts");
                    String mes= ois1.readUTF();
                    if(mes.equals("")){
                        return null;
                    }
                    String[] keyValue=mes.split(",");
                    for(String s:keyValue){
                        as.add(2*Integer.parseInt(s)+"");

                    }
                    socket1.close();



                }
            } catch (Exception e) {
            }
            Log.e("client",as.size()+" ..");
            return as;
        }
    }
}
