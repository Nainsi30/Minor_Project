import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
class RequestProcessor extends Thread
{
Socket socket;
String id;
ftServerFrame fsf;
RequestProcessor(Socket socket,String id,ftServerFrame fsf)
{
this.socket=socket;
this.id=id;
this.fsf=fsf;
start();
}
public void run()
{
try
{
SwingUtilities.invokeLater(new Runnable(){
public void run()
{
fsf.updateLog("Client is connected and id alloted is "+id);
}
});
OutputStream os=socket.getOutputStream();
InputStream is=socket.getInputStream();
int bytesToReceive=1024;
int chunkSize=4096;
byte[] tmp=new byte[chunkSize];
byte[] bytes=new byte[chunkSize];
byte[] header=new byte[1024];
int i,j,k;
j=0;
i=0;
int bytesReadCount;
while(j<bytesToReceive)
{
bytesReadCount=is.read(tmp);
if(bytesReadCount==-1) continue;
for(k=0;k<bytesReadCount;k++)
{
header[i]=tmp[k];
i++;
}
j=j+bytesReadCount;
}
System.out.println("Header is received");
long lengthOfFile=0;
i=0;
j=1;
while(header[i]!=',')
{
lengthOfFile=lengthOfFile+(header[i]*j);
j=j*10;
i++;
}
i++;
StringBuffer sb=new StringBuffer();
while(i<1023)
{
sb.append((char)header[i]);
i++;
}
System.out.println("length of file is : "+lengthOfFile);
String fileName=sb.toString().trim();
System.out.println("Receiving file is "+fileName);
long lof=lengthOfFile;
SwingUtilities.invokeLater(()->{
fsf.updateLog("Receiving file : "+fileName+" of length : "+lof);
});
//header Received
byte ack[]=new byte[1];
os.write(ack,0,1);
os.flush();
//ack send
System.out.println("acknowledgement is sent to client");
System.out.println("Uploads"+File.separator+fileName);
File file=new File("Uploads"+File.separator+fileName);
if(file.exists()) file.delete();
long m;
m=0;
FileOutputStream fos=new FileOutputStream(file);
while(m<lengthOfFile)
{
bytesReadCount=is.read(bytes);
if(bytesReadCount==-1) continue;
fos.write(bytes,0,bytesReadCount);
fos.flush();
m=m+bytesReadCount;
}
fos.close();
System.out.println("file is received");
ack[0]=1;
os.write(ack,0,1);
os.flush();
socket.close();
SwingUtilities.invokeLater(()->{
fsf.updateLog("File saved to"+file.getAbsolutePath());
fsf.updateLog("Connection with Client whos id is : "+id+" closed."+"\n");
});
System.out.println("acknowledgement is sent to client");
System.out.println("File Saved to "+file.getAbsolutePath());
}catch(Exception  e)
{
e.printStackTrace();
}
}
}
class ftServer extends Thread
{
private ServerSocket serverSocket;
ftServerFrame fsf;
ftServer(ftServerFrame fsf)
{
this.fsf=fsf;
}
public void run()
{
try
{
serverSocket=new ServerSocket(5500);
startListening();
}catch(Exception e)
{
System.out.println(e);
}
}
public void shutDown()
{
try
{
serverSocket.close();
}catch(Exception e)
{

}
}
public void startListening()
{
try
{
Socket socket;
RequestProcessor requestProcessor;
while(true)
{
System.out.println("Server is ready to accept request at port 5500");
SwingUtilities.invokeLater(new Thread(){
public void run()
{
fsf.updateLog("Server is ready to accept request at port 5500");
}
});
socket=serverSocket.accept();
requestProcessor=new RequestProcessor(socket,UUID.randomUUID().toString(),fsf);
}
}catch(Exception e)
{
System.out.println("Server is stopped listening");
System.out.println(e);
}
}

}


class ftServerFrame extends JFrame implements ActionListener
{
JTextArea jta;
JButton button;
Container container;
JScrollPane jsp;
boolean serverState=false;
ftServer server;
ftServerFrame()
{
container=getContentPane();
jta=new JTextArea();
jsp=new JScrollPane(jta,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

button=new JButton("Start");
container.setLayout(new BorderLayout());
container.add(jsp,BorderLayout.CENTER);
container.add(button,BorderLayout.SOUTH);
button.addActionListener(this);
setLocation(100,100);
setSize(400,400);
setVisible(true);
}
public void updateLog(String message)
{
jta.append(message+"\n");
}
public void actionPerformed(ActionEvent ev)
{
if(serverState==false)
{
server=new ftServer(this);
server.start();
serverState=true;
button.setText("Stop");
}
else
{
server.shutDown();
serverState=false;
button.setText("Start");
jta.append("Server Stopped\n");
}
}
public static void main(String gg[])
{
ftServerFrame server=new ftServerFrame();
}
}