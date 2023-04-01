import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.*;
import javax.swing.text.*;

import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMGroupInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMPosition;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.info.enums.CMFileSyncMode;
import kr.ac.konkuk.ccslab.cm.info.enums.CMTestFileModType;
import kr.ac.konkuk.ccslab.cm.manager.*;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientWinApp3 extends CMClientWinApp{
    private CMClientStub m_clientStub;  // CMClientStub 타입 레퍼런스 변수 m_clientStub 선언
    private CMClientWinEventHandler m_eventHandler;  // CMClientEventHandler 타입 레퍼런스 변수 m_eventHandler 선언
    private boolean m_bRun;
    private JTextPane m_outTextPane;
    private JTextField m_inTextField;
    private JButton m_startStopButton;
    private JButton m_loginLogoutButton;
    private CMWinClient.MyMouseListener cmMouseListener;

    public CMClientWinApp3() {  // CMClientApp 생성자
        MyKeyListener cmKeyListener = new MyKeyListener();
        MyActionListener cmActionListener = new MyActionListener();
//        cmMouseListener = new MyMouseListener();
        setTitle("CMClientWinApp");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        m_outTextPane = new JTextPane();
        m_outTextPane.setBackground(new Color(245,245,245));
        m_outTextPane.setEditable(false);

        StyledDocument doc = m_outTextPane.getStyledDocument();
        addStylesToDocument(doc);
        add(m_outTextPane, BorderLayout.CENTER);
        JScrollPane centerScroll = new JScrollPane (m_outTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //add(centerScroll);
        getContentPane().add(centerScroll, BorderLayout.CENTER);

        m_inTextField = new JTextField();
        m_inTextField.addKeyListener(cmKeyListener);
        add(m_inTextField, BorderLayout.SOUTH);

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setBackground(new Color(220,220,220));
        topButtonPanel.setLayout(new FlowLayout());
        add(topButtonPanel, BorderLayout.NORTH);

        m_startStopButton = new JButton("클라이언트 시작");
        //m_startStopButton.setBackground(Color.LIGHT_GRAY);	// not work on Mac
        m_startStopButton.addActionListener(cmActionListener);
        m_startStopButton.setEnabled(false);
        //add(startStopButton, BorderLayout.NORTH);
        topButtonPanel.add(m_startStopButton);

        m_loginLogoutButton = new JButton("로그인");
        m_loginLogoutButton.addActionListener(cmActionListener);
        m_loginLogoutButton.setEnabled(false);
        topButtonPanel.add(m_loginLogoutButton);

        setVisible(true);

        m_clientStub = new CMClientStub();  // CMClientStub 객체 생성
        m_eventHandler = new CMClientWinEventHandler(m_clientStub, this);  // CMClientWinEventHandler 객체 생성, CMClientStub 객체를 인자로 넘김

        testStartCM();

        m_inTextField.requestFocus();
    }

    public class MyKeyListener implements KeyListener {
        public void keyPressed(KeyEvent e)
        {
            int key = e.getKeyCode();
            if(key == KeyEvent.VK_ENTER)
            {
                JTextField input = (JTextField)e.getSource();
                String strText = input.getText();
                printMessage(strText+"\n");
                // parse and call CM API
                processInput(strText);
                input.setText("");
                input.requestFocus();
            }
            else if(key == KeyEvent.VK_ALT)
            {

            }
        }

        public void keyReleased(KeyEvent e){}
        public void keyTyped(KeyEvent e){}
    }

    public class MyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e)
        {
            JButton button = (JButton) e.getSource();
            if(button.getText().equals("클라이언트 시작"))
            {
                testStartCM();
            }
            else if(button.getText().equals("클라이언트 종료"))
            {
                testTerminateCM();
            }
            else if(button.getText().equals("로그인"))
            {
                // login to the default cm server
                testSyncLoginDS();
            }
            else if(button.getText().equals("로그아웃"))
            {
                // logout from the default cm server
                testLogoutDS();
            }
//            else if(button.equals(m_composeSNSContentButton))
//            {
//                testSNSContentUpload();
//            }
//            else if(button.equals(m_readNewSNSContentButton))
//            {
//                testDownloadNewSNSContent();
//            }
//            else if(button.equals(m_readNextSNSContentButton))
//            {
//                testDownloadNextSNSContent();
//            }
//            else if(button.equals(m_readPreviousSNSContentButton))
//            {
//                testDownloadPreviousSNSContent();
//            }
//            else if(button.equals(m_findUserButton))
//            {
//                testFindRegisteredUser();
//            }
//            else if(button.equals(m_addFriendButton))
//            {
//                testAddNewFriend();
//            }
//            else if(button.equals(m_removeFriendButton))
//            {
//                testRemoveFriend();
//            }
//            else if(button.equals(m_friendsButton))
//            {
//                testRequestFriendsList();
//            }
//            else if(button.equals(m_friendRequestersButton))
//            {
//                testRequestFriendRequestersList();
//            }
//            else if(button.equals(m_biFriendsButton))
//            {
//                testRequestBiFriendsList();
//            }

            m_inTextField.requestFocus();
        }
    }

    private void addStylesToDocument(StyledDocument doc)
    {
        Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regularStyle = doc.addStyle("regular", defStyle);
        StyleConstants.setFontFamily(regularStyle, "SansSerif");

        Style boldStyle = doc.addStyle("bold", defStyle);
        StyleConstants.setBold(boldStyle, true);

        Style linkStyle = doc.addStyle("link", defStyle);
        StyleConstants.setForeground(linkStyle, Color.BLUE);
        StyleConstants.setUnderline(linkStyle, true);
    }

    public CMClientStub getClientStub() {  // CMClientStub 타입 레퍼런스 변수 m_clientStub를 반환하는 메소드
        return m_clientStub;
    }

    public CMClientWinEventHandler getClientEventHandler() {  // CMClientWinEventHandler 타입 레퍼런스 변수 m_eventHandler를 반환하는 메소드
        return m_eventHandler;
    }

    private void initializeButtons()
    {
        m_startStopButton.setText("클라이언트 시작");
        m_loginLogoutButton.setText("로그인");
        revalidate();
        repaint();
    }

    public void setButtonsAccordingToClientState()
    {
        int nClientState;
        nClientState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();

        // nclientState: CMInfo.CM_INIT, CMInfo.CM_CONNECT, CMInfo.CM_LOGIN, CMInfo.CM_SESSION_JOIN
        switch(nClientState)
        {
            case CMInfo.CM_INIT:
                m_startStopButton.setText("클라이언트 종료");
                m_loginLogoutButton.setText("로그인");
                //m_leftButtonPanel.setVisible(false);
                //m_westScroll.setVisible(false);
                break;
            case CMInfo.CM_CONNECT:
                m_startStopButton.setText("클라이언트 종료");
                m_loginLogoutButton.setText("로그인");
                //m_leftButtonPanel.setVisible(false);
                //m_westScroll.setVisible(false);
                break;
            case CMInfo.CM_LOGIN:
                m_startStopButton.setText("클라이언트 종료");
                m_loginLogoutButton.setText("로그아웃");
                //m_leftButtonPanel.setVisible(false);
                //m_westScroll.setVisible(false);
                break;
            case CMInfo.CM_SESSION_JOIN:
                m_startStopButton.setText("클라이언트 종료");
                m_loginLogoutButton.setText("로그아웃");
                //m_leftButtonPanel.setVisible(true);
                //m_westScroll.setVisible(true);
                break;
            default:
                m_startStopButton.setText("클라이언트 시작");
                m_loginLogoutButton.setText("로그인");
                //m_leftButtonPanel.setVisible(false);
                //m_westScroll.setVisible(false);
                break;
        }
        revalidate();
        repaint();
    }

    public void printMessage(String strText)
    {
		/*
		m_outTextArea.append(strText);
		m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());
		*/
        StyledDocument doc = m_outTextPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), strText, null);
            m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return;
    }

    public void printStyledMessage(String strText, String strStyleName)
    {
        StyledDocument doc = m_outTextPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
            m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return;
    }

    private void processInput(String strInput)
    {
        int nCommand = -1;
        try {
            nCommand = Integer.parseInt(strInput);
        } catch (NumberFormatException e) {
            printMessage("알 수 없는 번호입니다.\n");
            return;
        }

        switch (nCommand) {
            case 0:
                printAllMenus();
                break;
            case 100:
                testStartCM();
                break;
            case 999:
                testTerminateCM();
                break;
            case 1: // 기본 서버에 접속
                testConnectionDS();
                break;
            case 2: // 기본 서버에 접속 해제
                testDisconnectionDS();
                break;
//                case 3: // connect to a designated server
//                    testConnectToServer();
//                    break;
//                case 4: // disconnect from a designated server
//                    testDisconnectFromServer();
//                    break;
//            case 10: // 기본 서버에 비동기식으로 로그인
//                testLoginDS();
//                break;
            case 11: // 기본 서버에 동기식으로 로그인
                testSyncLoginDS();
                break;
            case 12: // 기본 서버에 로그아웃
                testLogoutDS();
                break;
//                case 13: // log in to a designated server
//                    testLoginServer();
//                    break;
//                case 14: // log out from a designated server
//                    testLogoutServer();
//                    break;
//                case 20: // request session info from default server
//                    testSessionInfoDS();
//                    break;
//                case 21: // synchronously request session info from default server
//                    testSyncSessionInfoDS();
//                    break;
//                case 22: // join a session
//                    testJoinSession();
//                    break;
//                case 23: // synchronously join a session
//                    testSyncJoinSession();
//                    break;
//                case 24: // leave the current session
//                    testLeaveSession();
//                    break;
//                case 25: // change current group
//                    testChangeGroup();
//                    break;
//                case 26: // print group members
//                    testPrintGroupMembers();
//                    break;
//                case 27: // request session information from a designated server
//                    testRequestSessionInfoOfServer();
//                    break;
//                case 28: // join a session of a designated server
//                    testJoinSessionOfServer();
//                    break;
//                case 29: // leave a session of a designated server
//                    testLeaveSessionOfServer();
//                    break;
//                case 40: // chat
//                    testChat();
//                    break;
//                case 41: // test multicast chat in current group
//                    testMulticastChat();
//                    break;
            case 42: // CMDummyEvent 테스트
                testDummyEvent();
                break;
//                case 43: // test CMUserEvent
//                    testUserEvent();
//                    break;
//                case 44: // test datagram message
//                    testDatagram();
//                    break;
//                case 45: // user position
//                    testUserPosition();
//                    break;
//                case 46: // test sendrecv
//                    testSendRecv();
//                    break;
//                case 47: // test castrecv
//                    testCastRecv();
//                    break;
//                case 48: // test asynchronous sendrecv
//                    testAsyncSendRecv();
//                    break;
//                case 49: // test asynchronous castrecv
//                    testAsyncCastRecv();
//                    break;
//                case 50: // print group info
//                    testPrintGroupInfo();
//                    break;
//                case 51: // print current information about the client
//                    testCurrentUserStatus();
//                    break;
//                case 52:    // print current channels information
//                    testPrintCurrentChannelInfo();
//                    break;
//                case 53: // request additional server info
//                    testRequestServerInfo();
//                    break;
//                case 54: // print current group info of a designated server
//                    testPrintGroupInfoOfServer();
//                    break;
//                case 55: // test input network throughput
//                    testMeasureInputThroughput();
//                    break;
//                case 56: // test output network throughput
//                    testMeasureOutputThroughput();
//                    break;
//                case 57: // print all configurations
//                    testPrintConfigurations();
//                    break;
//                case 58: // change configuration
//                    testChangeConfiguration();
//                    break;
//                case 59: // show current thread information
//                    printThreadInfo();
//                    break;
//                case 60: // add additional channel
//                    testAddChannel();
//                    break;
//                case 61: // remove additional channel
//                    testRemoveChannel();
//                    break;
//                case 62: // test blocking channel
//                    testBlockingChannel();
//                    break;
//                case 70: // set file path
//                    testSetFilePath();
//                    break;
            case 71: // 파일 요청
                testRequestFile();
                break;
            case 72: // 파일 전송
                testPushFile();
                break;
//                case 73:    // test cancel receiving a file
//                    cancelRecvFile();
//                    break;
//                case 74:    // test cancel sending a file
//                    cancelSendFile();
//                    break;
//                case 75:    // print sending/receiving file info
//                    printSendRecvFileInfo();
//                    break;
//                case 80: // test SNS content download
//                    testDownloadNewSNSContent();
//                    break;
//                case 81:
//                    testDownloadNextSNSContent();
//                    break;
//                case 82:
//                    testDownloadPreviousSNSContent();
//                    break;
//                case 83: // request an attached file of SNS content
//                    testRequestAttachedFileOfSNSContent();
//                    break;
//                case 84: // test SNS content upload
//                    testSNSContentUpload();
//                    break;
//                case 90: // register user
//                    testRegisterUser();
//                    break;
//                case 91: // deregister user
//                    testDeregisterUser();
//                    break;
//                case 92: // find user
//                    testFindRegisteredUser();
//                    break;
//                case 93: // add a new friend
//                    testAddNewFriend();
//                    break;
//                case 94: // remove a friend
//                    testRemoveFriend();
//                    break;
//                case 95: // request current friends list
//                    testRequestFriendsList();
//                    break;
//                case 96: // request friend requesters list
//                    testRequestFriendRequestersList();
//                    break;
//                case 97: // request bi-directional friends
//                    testRequestBiFriendsList();
//                    break;
//                case 101: // test forwarding schemes (typical vs. internal)
//                    testForwarding();
//                    break;
//                case 102: // test delay of forwarding schemes
//                    testForwardingDelay();
//                    break;
//                case 103: // test repeated downloading of SNS content
//                    testRepeatedSNSContentDownload();
//                    break;
//                case 104: // pull or push multiple files
//                    testSendMultipleFiles();
//                    break;
//                case 105: // split a file
//                    testSplitFile();
//                    break;
//                case 106: // merge files
//                    testMergeFiles();
//                    break;
//                case 107: // distribute a file and merge
//                    testDistFileProc();
//                    break;
//                case 108: // send an event with wrong # bytes
//                    testSendEventWithWrongByteNum();
//                    break;
//                case 109: // send an event with wrong event type
//                    testSendEventWithWrongEventType();
//                    break;
//                case 112: // create test files for file-sync
//                    testCreateTestFileForSync();
//                    break;
//                case 113: // test file access for file-sync
//                    testFileAccessForSync();
//                    break;
//                case 200: // MQTT connect
//                    testMqttConnect();
//                    break;
//                case 201: // MQTT publish
//                    testMqttPublish();
//                    break;
//                case 202: // MQTT subscribe
//                    testMqttSubscribe();
//                    break;
//                case 203: // print MQTT session info
//                    testPrintMqttSessionInfo();
//                    break;
//                case 204: // MQTT unsubscribe
//                    testMqttUnsubscribe();
//                    break;
//                case 205: // MQTT disconnect
//                    testMqttDisconnect();
//                    break;
//                case 300:    // start file-sync with manual mode
//                    testStartFileSyncWithManualMode();
//                    break;
//                case 301:    // stop file-sync
//                    testStopFileSync();
//                    break;
//                case 302:    // open file-sync folder
//                    testOpenFileSyncFolder();
//                    break;
//                case 303:    // request file-sync online mode
//                    testRequestFileSyncOnlineMode();
//                    break;
//                case 304:    // request file-sync local mode
//                    testRequestFileSyncLocalMode();
//                    break;
//                case 305:    // print online mode files
//                    testPrintOnlineModeFiles();
//                    break;
//                case 306:    // print local mode files
//                    testPrintLocalModeFiles();
//                    break;
//                case 307:	// start file-sync with auto mode
//                    testStartFileSyncWithAutoMode();
//                    break;
//                case 308:	// print current file-sync mode
//                    testPrintCurrentFileSyncMode();
//                    break;
            default:
                System.err.println("없는 번호입니다.");
                break;
        }
    }

    private void printAllMenus()
    {
        printMessage("---------------------------------- 도움말\n");
        printMessage("0: 모든 메뉴 보기\n");
        printMessage("---------------------------------- 시작/종료\n");
        printMessage("100: CM 시작, 999: CM 종료\n");
        printMessage("---------------------------------- 연결\n");
        printMessage("1: 기본 서버에 접속, 2: 기본 서버에 접속 해제\n");
//        printMessage("3: connect to designated server, 4: disconnect from designated server");
        printMessage("---------------------------------- 로그인\n");
        printMessage(/*10: 기본 서버에 비동기식으로 로그인, */"11: 기본 서버에 동기식으로 로그인");
        printMessage("12: 기본 서버에 로그아웃\n");
//        printMessage("13: login to designated server, 14: logout from designated server\n");
//        printMessage("---------------------------------- Session/Group\n");
//        printMessage("20: request session information from default server\n");
//        printMessage("21: synchronously request session information from default server\n");
//        printMessage("22: join session of default server, 23: synchronously join session of default server\n");
//        printMessage("24: leave session of default server, 25: change group of default server\n");
//        printMessage("26: print group members\n");
//        printMessage("27: request session information from designated server\n");
//        printMessage("28: join session of designated server, 29: leave session of designated server\n");
        printMessage("---------------------------------- Event 전송\n");
//        printMessage("40: chat, 41: multicast chat in current group\n");
        printMessage("42: CMDummyEvent 테스트\n" /*, 43: test CMUserEvent, 44: test datagram event, 45: test user position"*/);
//        printMessage("46: test sendrecv, 47: test castrecv\n");
//        printMessage("48: test asynchronous sendrecv, 49: test asynchronous castrecv\n");
//        printMessage("---------------------------------- Information\n");
//        printMessage("50: show group information of default server, 51: show current user status\n");
//        printMessage("52: show current channels, 53: show current server information\n");
//        printMessage("54: show group information of designated server\n");
//        printMessage("55: measure input network throughput, 56: measure output network throughput\n");
//        printMessage("57: show all configurations, 58: change configuration\n");
//        printMessage("59: show current thread information\n");
//        printMessage("---------------------------------- Channel\n");
//        printMessage("60: add channel, 61: remove channel, 62: test blocking channel\n");
        printMessage("---------------------------------- 파일 전송\n");
        printMessage(/*"70: set file path,*/ "71: 파일 요청, 72: 파일 전송\n");
//        printMessage("73: cancel receiving file, 74: cancel sending file\n");
//        printMessage("75: print sending/receiving file info\n");
//        printMessage("---------------------------------- Social Network Service\n");
//        printMessage("80: request content list, 81: request next content list, 82: request previous content list\n");
//        printMessage("83: request attached file, 84: upload content\n");
//        printMessage("---------------------------------- User\n");
//        printMessage("90: register new user, 91: deregister user, 92: find registered user\n");
//        printMessage("93: add new friend, 94: remove friend, 95: show friends, 96: show friend requesters\n");
//        printMessage("97: show bi-directional friends\n");
//        printMessage("---------------------------------- MQTT\n");
//        printMessage("200: connect, 201: publish, 202: subscribe, 203: print session info\n");
//        printMessage("204: unsubscribe, 205: disconnect \n");
//        printMessage("---------------------------------- File Sync\n");
//        printMessage("300: start file-sync with manual mode, 301: stop file-sync\n");
//        printMessage("302: open file-sync folder\n");
//        printMessage("303: request online mode, 304: request local mode\n");
//        printMessage("305: print online mode files, 306: print local mode files\n");
//        printMessage("307: start file-sync with auto mode, 308: print current file-sync mode\n");
//        printMessage("---------------------------------- Other CM Tests\n");
//        printMessage("101: test forwarding scheme, 102: test delay of forwarding scheme\n");
//        printMessage("103: test repeated request of SNS content list\n");
//        printMessage("104: pull/push multiple files, 105: split file, 106: merge files, 107: distribute and merge file\n");
//        printMessage("108: send event with wrong # bytes, 109: send event with wrong type\n");
//        printMessage("110: test csc file transfer, 111: test c2c file transfer\n");
//        printMessage("112: create test files for file-sync\n");
//        printMessage("113: test file access for file-sync\n");
    }

    private void testStartCM()
    {
        boolean bRet = m_clientStub.startCM();
        if(!bRet)
        {
            printStyledMessage("CM 초기화 오류.\n", "bold");
        }
        else
        {
            m_startStopButton.setEnabled(true);
            m_loginLogoutButton.setEnabled(true);
            printStyledMessage("클라이언트 시작\n", "bold");
            printStyledMessage("메뉴를 보려면 \"0\"을 입력하세요.\n", "regular");
            // change the appearance of buttons in the client window frame
            setButtonsAccordingToClientState();
        }
    }

    public void testConnectionDS()
    {
        printMessage("====== 기본 서버에 접속합니다.\n");
        boolean ret = m_clientStub.connectToServer();
        if(ret)
        {
            printMessage("성공적으로 기본 서버에 접속했습니다.\n");
        }
        else
        {
            printMessage("기본 서버에 접속할 수 없습니다.\n");
        }
        printMessage("======\n");

        setButtonsAccordingToClientState();
    }

    public void testDisconnectionDS()
    {
        printMessage("====== 기본 서버에서 접속을 해제합니다.\n");
        boolean ret = m_clientStub.disconnectFromServer();
        if(ret)
        {
            printMessage("성공적으로 기본 서버에서 접속을 해제했습니다.\n");
        }
        else
        {
            printMessage("기본 서버에서 접속을 해제하던 도중 오류가 발생했습니다.\n");
        }
        printMessage("======\n");

        setButtonsAccordingToClientState();
        setTitle("CMClientWinApp");
    }

//    private void testLoginDS()
//    {
//        String strUserName = null;
//        String strPassword = null;
//        boolean bRequestResult = false;
//
//        printMessage("====== 기본 서버에 로그인합니다.\n");
//        JTextField userNameField = new JTextField();
//        JPasswordField passwordField = new JPasswordField();
//        Object[] message = {
//                "사용자 이름:", userNameField,
//                "비밀번호:", passwordField
//        };
//        int option = JOptionPane.showConfirmDialog(null, message, "로그인 입력", JOptionPane.OK_CANCEL_OPTION);
//        if (option == JOptionPane.OK_OPTION)
//        {
//            strUserName = userNameField.getText();
//            strPassword = new String(passwordField.getPassword()); // security problem?
//
//            m_eventHandler.setStartTime(System.currentTimeMillis());
//            bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
////            long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
//            if(bRequestResult)
//            {
//                printMessage("성공적으로 로그인 요청을 보냈습니다.\n");
////                printMessage("return delay: "+lDelay+" ms.\n");
//            }
//            else
//            {
//                printStyledMessage("로그인 요청이 실패했습니다.\n", "bold");
//                m_eventHandler.setStartTime(0);
//            }
//        }
//        setButtonsAccordingToClientState();
//        printMessage("======\n");
//    }

    private void testSyncLoginDS()
    {
        String strUserName = null;
        String strPassword = null;
        CMSessionEvent loginAckEvent = null;

        printMessage("====== 기본 서버에 동기식으로 로그인합니다.\n");
        JTextField userNameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
                "사용자 이름:", userNameField,
                "비밀번호:", passwordField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "로그인 입력", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
        {
            strUserName = userNameField.getText();
            strPassword = new String(passwordField.getPassword()); // security problem?

            m_eventHandler.setStartTime(System.currentTimeMillis());
            loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);
//            long lDelay = System.currentTimeMillis() - m_eventHandler.getStartTime();
            if(loginAckEvent != null)
            {
                // print login result
                if(loginAckEvent.isValidUser() == 0)
                {
                    printMessage("기본 서버에 의해 인증에 실패했습니다.\n");
                }
                else if(loginAckEvent.isValidUser() == -1)
                {
                    printMessage("이미 로그인되어 있습니다.\n");
                }
                else
                {
//                    printMessage("return delay: "+lDelay+" ms.\n");
                    printMessage("성공적으로 기본 서버에 로그인했습니다.\n");
                    CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();

                    setTitle("CMClientWinApp ("+interInfo.getMyself().getName()+")");

                    // Set the appearance of buttons in the client frame window
                    setButtonsAccordingToClientState();
                }
            }
            else
            {
                printStyledMessage("로그인 요청에 실패했습니다.\n", "bold");
            }

        }

        printMessage("======\n");
    }

    private void testLogoutDS()
    {
        boolean bRequestResult = false;
        printMessage("====== 기본 서버에 로그아웃\n");
        bRequestResult = m_clientStub.logoutCM();
        if(bRequestResult)
            printMessage("성공적으로 로그아웃 요청을 보냈습니다.\n");
        else
            printStyledMessage("로그아웃 요청이 실패했습니다.\n", "bold");
        printMessage("======\n");

        // Change the title of the login button
        setButtonsAccordingToClientState();
        setTitle("CMClientWinApp");
    }

    private void testDummyEvent()
    {

        String strMessage = null;
        String strTarget = null;
        CMDummyEvent due = null;

        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        CMUser myself = interInfo.getMyself();

        if(myself.getState() != CMInfo.CM_SESSION_JOIN)
        {
            printMessage("세션과 그룹에 속해있어야 합니다.\n");
            return;
        }

        printMessage("====== 현재 그룹에서 CMDummyEvent 테스트\n");

        JTextField messageField = new JTextField();
        JTextField targetField = new JTextField();

        Object[] msg = {
                "메시지: ", messageField,
                "수신 사용자 (send() 메소드 사용 시): ", targetField,
        };
        int option = JOptionPane.showConfirmDialog(null, msg, "더미 이벤트 전송",
                JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.OK_OPTION)
        {
            strMessage = messageField.getText().trim();
            strTarget = targetField.getText().trim();

            if(strMessage.isEmpty())
            {
                printStyledMessage("메시지를 입력하지 않았습니다.\n", "bold");
                return;
            }

            due = new CMDummyEvent();
            due.setDummyInfo(strMessage);
            due.setHandlerSession(myself.getCurrentSession());
            due.setHandlerGroup(myself.getCurrentGroup());

            if(!strTarget.isEmpty())
            {
                m_clientStub.send(due, strTarget);
            }

            due = null;
            printMessage("======\n");
        }
    }

    /*
    public void testDummyEvent()
    {
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        CMUser myself = interInfo.getMyself();

        if(myself.getState() != CMInfo.CM_SESSION_JOIN)
        {
            System.out.println("세션과 그룹에 속해있어야 합니다.");
            return;
        }

        System.out.println("====== 현재 그룹에서 CMDummyEvent 테스트");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("수신자를 입력하세요: ");
        String strTarget = null;
        try {
            strTarget = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print("메시지를 입력하세요: ");
        String strInput = null;
        try {
            strInput = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CMDummyEvent due = new CMDummyEvent();
        due.setHandlerSession(myself.getCurrentSession());
        due.setHandlerGroup(myself.getCurrentGroup());
        due.setDummyInfo(strInput);
        m_clientStub.send(due, strTarget);
        due = null;

        System.out.println("======");
    }
     */

    private void testRequestFile()
    {
        boolean bReturn = false;
        String strFileName = null;
        String strFileOwner = null;
        byte byteFileAppendMode = -1;
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();

        printMessage("====== 파일 요청\n");

        JTextField fnameField = new JTextField();
        JTextField fownerField = new JTextField();
        String[] fAppendMode = {"기본값", "덮어쓰기", "덧붙이기"};
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

        Object[] message = {
                "파일 이름: ", fnameField,
                "파일 소유자(공백은 기본 서버): ", fownerField,
                "파일 추가 모드: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "파일 요청", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
        {
            printMessage("취소했습니다.\n");
            return;
        }

        strFileName = fnameField.getText().trim();
        if(strFileName.isEmpty())
        {
            printMessage("파일 이름을 입력하지 않았습니다.\n");
            return;
        }
        strFileOwner = fownerField.getText().trim();
        if(strFileOwner.isEmpty())
            strFileOwner = interInfo.getDefaultServerInfo().getServerName();

        switch(fAppendBox.getSelectedIndex())
        {
            case 0:
                byteFileAppendMode = CMInfo.FILE_DEFAULT;
                break;
            case 1:
                byteFileAppendMode = CMInfo.FILE_OVERWRITE;
                break;
            case 2:
                byteFileAppendMode = CMInfo.FILE_APPEND;
                break;
        }

        bReturn = m_clientStub.requestFile(strFileName, strFileOwner, byteFileAppendMode);

        if(!bReturn)
            printMessage("파일 요청 오류. 파일("+strFileName+"), 소유자("+strFileOwner+").\n");

        printMessage("======\n");
    }

    private void testPushFile()
    {
        String strFilePath = null;
        File[] files = null;
        String strReceiver = null;
        byte byteFileAppendMode = -1;
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        boolean bReturn = false;

        printMessage("====== 파일 전송\n");

		/*
		strReceiver = JOptionPane.showInputDialog("Receiver Name: ");
		if(strReceiver == null) return;
		*/
        JTextField freceiverField = new JTextField();
        String[] fAppendMode = {"기본값", "덮어쓰기", "덧붙이기"};
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

        Object[] message = {
                "파일 수신자(공백은 기본 서버): ", freceiverField,
                "파일 추가 모드: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "파일 전송", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
        {
            printMessage("취소했습니다.\n");
            return;
        }

        strReceiver = freceiverField.getText().trim();
        if(strReceiver.isEmpty())
            strReceiver = interInfo.getDefaultServerInfo().getServerName();

        switch(fAppendBox.getSelectedIndex())
        {
            case 0:
                byteFileAppendMode = CMInfo.FILE_DEFAULT;
                break;
            case 1:
                byteFileAppendMode = CMInfo.FILE_OVERWRITE;
                break;
            case 2:
                byteFileAppendMode = CMInfo.FILE_APPEND;
                break;
        }

        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
        File curDir = new File(confInfo.getTransferedFileHome().toString());
        fc.setCurrentDirectory(curDir);
        int fcRet = fc.showOpenDialog(this);
        if(fcRet != JFileChooser.APPROVE_OPTION) return;
        files = fc.getSelectedFiles();
        if(files.length < 1) return;
        for(int i=0; i < files.length; i++)
        {
            strFilePath = files[i].getPath();
            bReturn = m_clientStub.pushFile(strFilePath, strReceiver, byteFileAppendMode);
            if(!bReturn)
            {
                printMessage("파일 전송 오류 파일("+strFilePath+"), 수신자("
                        +strReceiver+")\n");
            }
        }

        printMessage("======\n");
    }

    public void testTerminateCM()
    {
        //m_clientStub.disconnectFromServer();
        m_clientStub.terminateCM();
        printMessage("클라이언트 종료\n");
        // change the appearance of buttons in the client window frame
        initializeButtons();
        setTitle("CMClientWinApp");
    }

    public static void main(String[] args) {
        CMClientWinApp client = new CMClientWinApp();
        CMClientStub cmStub = client.getClientStub();
        cmStub.setAppEventHandler(client.getClientEventHandler());
    }
}