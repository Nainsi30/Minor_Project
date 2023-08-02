import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.*;
interface FileUploadListener
{
public void fileUploadStatusChanged(FileUploadEvent fileUploadEvent);
}
class FileUploadEvent
{
private String uploaderId;
private File file;
private long numberOfBytesUploaded;
FileUploadEvent()
{
this.uploaderId=null;
this.file=null;
this.numberOfBytesUploaded=0;
}
public void setUploaderId(String uploaderId)
{
this.uploaderId=uploaderId;
}
public void setFile(File file)
{
this.file=file;
}
public void setNumberOfBytesUploaded(long numberOfBytesUploaded)
{
this.numberOfBytesUploaded=numberOfBytesUploaded;
}
public String getUploaderId()
{
return this.uploaderId;
}
public File getFile()
{
return this.file;
}
public long getNumberOfBytesUploaded()
{
return this.numberOfBytesUploaded;
}
}

class FileModel extends AbstractTableModel
{
private ArrayList<File> files;
public FileModel()
{
files=new ArrayList<>();
}
public int getRowCount()
{
return files.size();
}

public int getColumnCount()
{
return 2;
}

public String getColumnName(int c)
{
if(c==0) return "S.No";
return "Files";
}

public Class getColumnClass(int c)
{
if(c==0) return Integer.class;
return String.class;
}

public boolean isCellEditable(int rowIndex,int columnIndex)
{
return false;
}

public Object getValueAt(int rowIndex,int columnIndex)
{
if(columnIndex==0) return rowIndex+1;
return files.get(rowIndex).getAbsolutePath();
}

public void addFile(File file)
{
this.files.add(file);
fireTableDataChanged();
}
public ArrayList<File> getFiles()
{
return this.files;
}
}//FileModelClass ends

class FTClientFrame extends JFrame 
{
private String host;
private int portNumber;
private FileSelectionPanel fileSelectionPanel;
private FileUploadViewPanel fileUploadViewPanel;
private Container container;
FTClientFrame(String host,int portNumber)
{
this.host="localhost";
this.portNumber=portNumber;
fileSelectionPanel=new FileSelectionPanel();
fileUploadViewPanel=new FileUploadViewPanel();
container=getContentPane();
container.setLayout(new GridLayout(1,2));
container.add(fileSelectionPanel);
container.add(fileUploadViewPanel);
setSize(500,400);
setLocation(100,200);
setVisible(true);
}
public void setBytesUploaded(String id,long bytes)
{
}
public void fileUploaded(String id)
{
}

class FileSelectionPanel extends JPanel implements ActionListener
{
private JLabel titleLabel;
private FileModel model;
private JTable table;
private JScrollPane jsp;
private JButton addFileButton;
FileSelectionPanel()
{
setLayout(new BorderLayout());
titleLabel=new JLabel("Selected Files");
model=new FileModel();
table=new JTable(model);
jsp=new JScrollPane(table,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
addFileButton=new JButton("Add File");
addFileButton.addActionListener(this);
add(titleLabel,BorderLayout.NORTH);
add(jsp,BorderLayout.CENTER);
add(addFileButton,BorderLayout.SOUTH);
}
public ArrayList<File> getFiles()
{
return this.model.getFiles();
}
public void actionPerformed(ActionEvent ev)
{
JFileChooser jfc=new JFileChooser();
jfc.setCurrentDirectory(new File("."));
int selectedOption=jfc.showOpenDialog(this);
if(selectedOption==jfc.APPROVE_OPTION)
{
File selectedFile=jfc.getSelectedFile();
model.addFile(selectedFile);
}
}
}//FileSelectionPanel ends


class FileUploadViewPanel extends JPanel implements ActionListener,FileUploadListener
{
private JButton uploadFileButton;
private JPanel progressPanelsContainer;
private ArrayList<ProgressPanel> progressPanels;
private ArrayList<File> files;
private ArrayList<FileUploadThread> fileUploaders;
private JScrollPane jsp;
FileUploadViewPanel()
{
uploadFileButton=new JButton("upload files");
setLayout(new BorderLayout());
add(uploadFileButton,BorderLayout.NORTH);
uploadFileButton.addActionListener(this);
}
public void actionPerformed(ActionEvent ev)
{
this.files=fileSelectionPanel.getFiles();
if(this.files.size()==0)
{
JOptionPane.showMessageDialog(FTClientFrame.this,"No files selected to upload");
return;
}
progressPanelsContainer=new JPanel();
progressPanelsContainer.setLayout(new GridLayout(files.size(),1));
ProgressPanel progressPanel;
progressPanels=new ArrayList<>();
fileUploaders=new ArrayList<>();
FileUploadThread fut;
String uploaderId;
for(File file:files)
{
uploaderId=UUID.randomUUID().toString();
progressPanel=new ProgressPanel(uploaderId,file);
progressPanels.add(progressPanel);
progressPanelsContainer.add(progressPanel);
fut=new FileUploadThread(this,uploaderId,file,host,portNumber);
fileUploaders.add(fut);
}
jsp=new JScrollPane(progressPanelsContainer,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
add(jsp,BorderLayout.CENTER);
this.revalidate();
this.repaint();
for(FileUploadThread fileUploadThread:fileUploaders)
{
fileUploadThread.start();
}

}//actionPerformed ends

public void fileUploadStatusChanged(FileUploadEvent fileUploadEvent)
{
String uploaderId=fileUploadEvent.getUploaderId();
long numberOfBytesUploaded=fileUploadEvent.getNumberOfBytesUploaded();
File file=fileUploadEvent.getFile();
for(ProgressPanel progressPanel:progressPanels)
{
if(progressPanel.getId().equals(uploaderId))
{
progressPanel.updateProgressBar(numberOfBytesUploaded);
this.revalidate();
this.repaint();
}
}
}

class ProgressPanel extends JPanel
{
private JProgressBar progressBar;
private JLabel fileNameLabel;
private String fileName;
private File file;
private long fileLength;
private String id;
ProgressPanel(String id,File file)
{
this.file=file;
this.id=id;
fileName=file.getAbsolutePath();
fileLength=file.length();
fileNameLabel=new JLabel("uploading : "+fileName);
progressBar=new JProgressBar(1,100);
setLayout(new GridLayout(2,3));
add(new JLabel("  "));
add(fileNameLabel);
add(new JLabel("  "));

add(new JLabel("  "));
add(progressBar);
add(new JLabel("  "));
}
public void updateProgressBar(long bytesUploaded)
{
int percentage;
if(bytesUploaded==fileLength) percentage=100;
else percentage=(int)((bytesUploaded*100)/fileLength);
if(percentage==100)
{
fileNameLabel.setText("Uploaded : "+fileName);
progressBar.setValue(percentage);
}
else
{
progressBar.setValue(percentage);
}
}
public String getId()
{
return this.id;
}
}//ProgressPanel  ends


}//FileUploadViewPanel ends

public static void main(String gg[])
{
FTClientFrame fcf=new FTClientFrame("169.254.93.232",5500);
}
}//FTClientFrame ends

class FileUploadThread extends Thread
{
private FTClientFrame fcf;
private String host;
private int portNumber;
private File file;
private String id;
private FileUploadListener fileUploadListener;
FileUploadThread(FileUploadListener fileUploadListener,String id,File file,String host,int portNumber)
{
this.fileUploadListener=fileUploadListener;
this.id=id;
this.file=file;
this.host=host;
this.portNumber=portNumber;
}
public void run()
{
try
{
long lengthOfFile=file.length();
byte header[]=new byte[1024];
long k;
k=lengthOfFile;
int i;
i=0;
while(k>0)
{
header[i]=(byte)(k%10);
i++;
k=k/10;
}
header[i]=(byte)',';
i++;
int j=0;
String name=file.getName();
while(j<name.length())
{
header[i]=(byte)name.charAt(j);
i++;
j++;
}
while(i<1023)
{
header[i]=32;
i++;
}
Socket socket=new Socket(host,portNumber);
OutputStream os=socket.getOutputStream();
os.write(header,0,1024);
os.flush();
System.out.println("Header is sent with file length :"+lengthOfFile+"and file name :"+name);
//header is sent
InputStream is=socket.getInputStream();
int bytesReadCount=0;
byte[] ack=new byte[1];
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
System.out.println("acknowledgement received from server");
int chunkSize=4096;
byte bytes[]=new byte[chunkSize];
k=0;
FileInputStream fis=new FileInputStream(file);
while(k<lengthOfFile)
{
bytesReadCount=fis.read(bytes);
if(bytesReadCount==-1) continue;
os.write(bytes,0,bytesReadCount);
os.flush();
k=k+bytesReadCount;
long brc=k;
SwingUtilities.invokeLater(()->{
FileUploadEvent fue=new FileUploadEvent();
fue.setFile(file);
fue.setUploaderId(id);
fue.setNumberOfBytesUploaded(brc);
fileUploadListener.fileUploadStatusChanged(fue);
});
}
//data is sent or request is sent
System.out.println("file is send");
while(true)
{
bytesReadCount=is.read(ack);
if(bytesReadCount==-1) continue;
break;
}
socket.close();
System.out.println("acknowledgement received from server");
}catch(Exception exception)
{
System.out.println(exception);
}
}
}