package com.celements.ftpclient;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class CelFTPClient extends FTPClient implements ICelFTPClient {

  private static Logger _LOGGER = LoggerFactory.getLogger(CelFTPClient.class);

  /** A convenience method for connecting and logging in */
  @Override
  public boolean connectAndLogin (String host, Integer port, String userName, String password
      ) throws  IOException, UnknownHostException, FTPConnectionClosedException {
    boolean success = false;
    if (port != null) {
      connect(host, port);
    } else {
      connect(host);
    }
    int reply = getReplyCode();
    if (FTPReply.isPositiveCompletion(reply))
      enterLocalPassiveMode();
      success = login(userName, password);
    if (!success)
      disconnect();
    return success;
  }

  /** A convenience method for connecting and logging in */
  @Override
  public boolean connectAndLogin (String host, String userName, String password
      ) throws  IOException, UnknownHostException, FTPConnectionClosedException {
    return connectAndLogin(host, null, userName, password);
  }
  
  /** Turn passive transfer mode on or off. If Passive mode is active, a
    * PASV command to be issued and interpreted before data transfers;
    * otherwise, a PORT command will be used for data transfers. If you're
    * unsure which one to use, you probably want Passive mode to be on. */
  @Override
  public void setPassiveMode(boolean setPassive) {
    if (setPassive)
      enterLocalPassiveMode();
    else
      enterLocalActiveMode();
  }
  
  /** Use ASCII mode for file transfers */
  @Override
  public boolean ascii () throws IOException {
    return setFileType(FTP.ASCII_FILE_TYPE);
  }
  
  /** Use Binary mode for file transfers */
  @Override
  public boolean binary () throws IOException {
    return setFileType(FTP.BINARY_FILE_TYPE);
  }
  
  /** Download a file from the server, and save it to the specified local file */
  @Override
  public boolean downloadFile (String serverFile, String localFile)
      throws IOException, FTPConnectionClosedException {
    FileOutputStream out = new FileOutputStream(localFile);
    boolean result = retrieveFile(serverFile, out);
    out.close();
    return result;
  }
  
  /** Upload a file to the server */
  @Override
  public boolean uploadFile (String localFile, String serverFile) 
      throws IOException, FTPConnectionClosedException {
    FileInputStream in = new FileInputStream(localFile);
    boolean result = storeFile(serverFile, in);
    in.close();
    return result;
  }
  
  /** Get the list of files in the current directory as a Vector of Strings 
    * (excludes subdirectories) */
  @Override
  public List<String> listFileNames () 
      throws IOException, FTPConnectionClosedException {
    FTPFile[] files = listFiles();
    Vector<String> v = new Vector<String>();
    for (int i = 0; i < files.length; i++) {
      if (!files[i].isDirectory())
        v.addElement(files[i].getName());
    }
    return v;
  }
  
  /** Get the list of subdirectories in the current directory as a Vector of Strings 
    * (excludes files) */
  @Override
  public List<String> listSubdirNames () throws IOException,
      FTPConnectionClosedException {
    FTPFile[] files = listFiles();
    Vector<String> v = new Vector<String>();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory())
        v.addElement(files[i].getName());
    }
    return v;
  }

  @Override
  public boolean isConnectedAndReady() {
    try {
      if (isConnected() && sendNoOp()) {
        return true;
      }
    } catch(IOException exp) {
      _LOGGER.debug("isConnected failed with ioException.", exp);
    }
    return false;
  }

}
