package com.celements.ftpclient;

import static org.junit.Assert.*;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.stub.StubFtpServer;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class CelFTPClientTest extends AbstractComponentTest {

  private static final int _TEST_FTP_PORT = 23456;
  private CelFTPClient ftpClient;
  private StubFtpServer stubFtpServer;

  @Before
  public void setUp_FTPClientTest() throws Exception {
    ftpClient = (CelFTPClient) Utils.getComponent(ICelFTPClient.class);
    stubFtpServer = new StubFtpServer();
    stubFtpServer.setServerControlPort(_TEST_FTP_PORT);
  }

  @After
  public void tearDown_FTPClientTest() {
    if (stubFtpServer.isStarted()) {
      stubFtpServer.stop();
    }
  }

  @Test
  public void testPER_LOOKUP() {
    replayDefault();
    CelFTPClient secondFTPClient = (CelFTPClient) Utils.getComponent(ICelFTPClient.class);
    assertNotSame(ftpClient, secondFTPClient);
    verifyDefault();
  }

  @Test
  public void testConnectAndLogin() throws Exception {
    stubFtpServer.start();
    replayDefault();
    ftpClient.connectAndLogin("127.0.0.1", _TEST_FTP_PORT, "testUser", "test!pass");
    assertTrue(ftpClient.isConnectedAndReady());
    assertEquals(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE, ftpClient.getDataConnectionMode());
    verifyDefault();
  }

  @Test
  public void testIsConnectedAndReady() throws Exception {
    stubFtpServer.start();
    replayDefault();
    ftpClient.connect("127.0.0.1", _TEST_FTP_PORT);
    assertTrue(ftpClient.isConnectedAndReady());
    verifyDefault();
  }

  @Test
  public void testIsConnectedAndReady_connectionDropped() throws Exception {
    stubFtpServer.start();
    replayDefault();
    ftpClient.connect("127.0.0.1", _TEST_FTP_PORT);
    stubFtpServer.stop();
    assertFalse("Connection drop must result in isConnectedAndReady returing false",
        ftpClient.isConnectedAndReady());
    verifyDefault();
  }

  @Test
  public void testIsConnectedAndReady_connectionDropped_twoCalls() throws Exception {
    stubFtpServer.start();
    replayDefault();
    ftpClient.connect("127.0.0.1", _TEST_FTP_PORT);
    stubFtpServer.stop();
    assertFalse("[1] Connection drop must result in isConnectedAndReady returing false",
        ftpClient.isConnectedAndReady());
    assertFalse("[2] Connection drop must result in isConnectedAndReady returing false",
        ftpClient.isConnectedAndReady());
    verifyDefault();
  }

}
