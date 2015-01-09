package com.celements.ftpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.net.io.CopyStreamListener;
import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface ICelFTPClient {

  public List<String> listFileNames() throws IOException, FTPConnectionClosedException;

  public boolean connectAndLogin (String host, String userName, String password
      ) throws  IOException, UnknownHostException, FTPConnectionClosedException;

  public boolean connectAndLogin (String host, Integer port, String userName, String password
      ) throws  IOException, UnknownHostException, FTPConnectionClosedException;

  public boolean login(String username, String password) throws IOException;

  public boolean login(String username, String password, String account
      ) throws IOException;

  public boolean isConnectedAndReady();

  public boolean logout() throws IOException;

  public void disconnect() throws IOException;

  public void setPassiveMode(boolean isPassivMode);

  public boolean binary() throws IOException;

  public boolean ascii () throws IOException;

  public String printWorkingDirectory() throws IOException;

  public boolean changeWorkingDirectory(String pathname) throws IOException;

  public boolean changeToParentDirectory() throws IOException;

  public String getReplyString();

  public void setDataTimeout(int timeout);

  public void setParserFactory(FTPFileEntryParserFactory parserFactory);

  public void setRemoteVerificationEnabled(boolean enable);

  public boolean isRemoteVerificationEnabled();

  public boolean structureMount(String pathname) throws IOException;

  public void enterLocalActiveMode();

  public void enterLocalPassiveMode();

  public boolean enterRemoteActiveMode(InetAddress host, int port
      ) throws IOException;

  public boolean enterRemotePassiveMode() throws IOException;

  public String getPassiveHost();

  public int getPassivePort();

  public int getDataConnectionMode();

  public void setActivePortRange(int minPort, int maxPort);

  public void setActiveExternalIPAddress(String ipAddress) throws UnknownHostException;

  public void setPassiveLocalIPAddress(String ipAddress) throws UnknownHostException;

  public void setPassiveLocalIPAddress(InetAddress inetAddress);

  public InetAddress getPassiveLocalIPAddress();

  public void setReportActiveExternalIPAddress(String ipAddress) throws UnknownHostException;

  public boolean setFileType(int fileType) throws IOException;

  public boolean setFileType(int fileType, int formatOrByteSize
      ) throws IOException;

  public boolean setFileStructure(int structure) throws IOException;

  public boolean setFileTransferMode(int mode) throws IOException;

  public boolean remoteRetrieve(String filename) throws IOException;

  public boolean remoteStore(String filename) throws IOException;

  public boolean remoteStoreUnique(String filename) throws IOException;

  public boolean remoteStoreUnique() throws IOException;

  public boolean remoteAppend(String filename) throws IOException;

  public boolean completePendingCommand() throws IOException;

  public boolean retrieveFile(String remote, OutputStream local
      ) throws IOException;

  public InputStream retrieveFileStream(String remote) throws IOException;

  public boolean storeFile(String remote, InputStream local
      ) throws IOException;

  public OutputStream storeFileStream(String remote) throws IOException;

  public boolean appendFile(String remote, InputStream local
      ) throws IOException;

  public OutputStream appendFileStream(String remote) throws IOException;

  public boolean storeUniqueFile(String remote, InputStream local
      ) throws IOException;

  public OutputStream storeUniqueFileStream(String remote) throws IOException;

  public boolean storeUniqueFile(InputStream local) throws IOException;

  public OutputStream storeUniqueFileStream() throws IOException;

  public boolean allocate(int bytes) throws IOException;

  public boolean features() throws IOException;

  public String[] featureValues(String feature) throws IOException;

  public String featureValue(String feature) throws IOException;

  public boolean hasFeature(String feature) throws IOException;

  public boolean hasFeature(String feature, String value) throws IOException;

  public boolean allocate(int bytes, int recordSize) throws IOException;

  public boolean doCommand(String command, String params) throws IOException;

  public String[] doCommandAsStrings(String command, String params) throws IOException;

  public FTPFile mlistFile(String pathname) throws IOException;

  public FTPFile[] mlistDir() throws IOException;

  public FTPFile[] mlistDir(String pathname) throws IOException;

  public FTPFile[] mlistDir(String pathname, FTPFileFilter filter) throws IOException;

  public void setRestartOffset(long offset);

  public long getRestartOffset();

  public boolean rename(String from, String to) throws IOException;

  public boolean abort() throws IOException;

  public boolean deleteFile(String pathname) throws IOException;

  public boolean removeDirectory(String pathname) throws IOException;

  public boolean makeDirectory(String pathname) throws IOException;

  public boolean sendSiteCommand(String arguments) throws IOException;

  public String getSystemType() throws IOException;

  public String listHelp() throws IOException;

  public String listHelp(String command) throws IOException;

  public boolean sendNoOp() throws IOException;

  public String[] listNames(String pathname) throws IOException;

  public String[] listNames() throws IOException;

  public FTPFile[] listFiles() throws IOException;

  public FTPFile[] listFiles(String pathname, FTPFileFilter filter
      ) throws IOException;

  public FTPFile[] listDirectories() throws IOException;

  public FTPFile[] listDirectories(String parent) throws IOException;

  public FTPListParseEngine initiateListParsing(String pathname
      ) throws IOException;

  public FTPListParseEngine initiateListParsing(String parserKey,
      String pathname) throws IOException;

  public String getStatus() throws IOException;

  public String getStatus(String pathname) throws IOException;

  public String getModificationTime(String pathname) throws IOException;

  public boolean setModificationTime(String pathname, String timeval) throws IOException;

  public void setBufferSize(int bufSize);

  public int getBufferSize();

  public void setSendDataSocketBufferSize(int bufSize);

  public int getSendDataSocketBufferSize();

  public void setReceieveDataSocketBufferSize(int bufSize);

  public int getReceiveDataSocketBufferSize();

  public void configure(FTPClientConfig config);

  public void setListHiddenFiles(boolean listHiddenFiles);

  public boolean getListHiddenFiles();

  public boolean isUseEPSVwithIPv4();

  public void setUseEPSVwithIPv4(boolean selected);

  public void setCopyStreamListener(CopyStreamListener listener);

  public CopyStreamListener getCopyStreamListener();

  public void setControlKeepAliveTimeout(long controlIdle);

  public long getControlKeepAliveTimeout();

  public void setControlKeepAliveReplyTimeout(int timeout);

  public int getControlKeepAliveReplyTimeout();

  public void setPassiveNatWorkaround(boolean enabled);

  public void setAutodetectUTF8(boolean autodetect);

  public boolean getAutodetectUTF8();

  public boolean downloadFile (String serverFile, String localFile) throws IOException,
    FTPConnectionClosedException;

  public boolean uploadFile (String localFile, String serverFile) throws IOException,
    FTPConnectionClosedException;

  public List<String> listSubdirNames () throws IOException,
  FTPConnectionClosedException;

}
