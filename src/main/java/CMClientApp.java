import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class CMClientApp {
    private CMClientStub m_clientStub;  // CMClientStub 타입 레퍼런스 변수 m_clientStub 선언
    private CMClientEventHandler m_eventHandler;  // CMClientEventHandler 타입 레퍼런스 변수 m_eventHandler 선언
    private boolean m_bRun;
    private Scanner m_scan = null;


    public CMClientApp() {  // CMClientApp 생성자
        m_clientStub = new CMClientStub();  // CMClientStub 객체 생성
        m_eventHandler = new CMClientEventHandler(m_clientStub);  // CMClientEventHandler 객체 생성, CMClientStub 객체를 인자로 넘김
        m_bRun = true;
    }

    public CMClientStub getClientStub() {  // CMClientStub 타입 레퍼런스 변수 m_clientStub를 반환하는 메소드
        return m_clientStub;
    }

    public CMClientEventHandler getClientEventHandler() {  // CMClientEventHandler 타입 레퍼런스 변수 m_eventHandler를 반환하는 메소드
        return m_eventHandler;
    }

    public void testStartCM()
    {
        // 로컬 주소 가져오기
        List<String> localAddressList = CMCommManager.getLocalIPList();
        if(localAddressList == null) {
            System.err.println("로컬 주소를 찾을 수 없습니다.");
            return;
        }
        String strCurrentLocalAddress = localAddressList.get(0).toString();

        // set config home
        m_clientStub.setConfigurationHome(Paths.get("."));
        // set file-path home
        m_clientStub.setTransferedFileHome(m_clientStub.getConfigurationHome().resolve("client-file-path"));

//        // 저장된 서버 정보를 server configuration 파일에서 가져오기
//        String strSavedServerAddress = null;
//        int nSavedServerPort = -1;
//        String strNewServerAddress = null;
//        String strNewServerPort = null;

//        strSavedServerAddress = m_clientStub.getServerAddress();
//        nSavedServerPort = m_clientStub.getServerPort();
//
        // 서버 정보를 바꾸고 싶은지 묻기
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        System.out.println("========== start CM");
//        System.out.println("my current address: "+strCurrentLocalAddress);
//        System.out.println("saved server address: "+strSavedServerAddress);
//        System.out.println("saved server port: "+nSavedServerPort);
//
//        try {
//            System.out.print("new server address (enter for saved value): ");
//            strNewServerAddress = br.readLine().trim();
//            System.out.print("new server port (enter for saved value): ");
//            strNewServerPort = br.readLine().trim();
//
//            // update the server info if the user would like to do
//            if(!strNewServerAddress.isEmpty() && !strNewServerAddress.equals(strSavedServerAddress))
//                m_clientStub.setServerAddress(strNewServerAddress);
//            if(!strNewServerPort.isEmpty() && Integer.parseInt(strNewServerPort) != nSavedServerPort)
//                m_clientStub.setServerPort(Integer.parseInt(strNewServerPort));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        boolean bRet = m_clientStub.startCM();
        if(!bRet)
        {
            System.err.println("CM initialization error!");
            return;
        }
        startTest();
    }

    public void startTest()
    {
        System.out.println("클라이언트 시작");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        m_scan = new Scanner(System.in);
        String strInput = null;
        int nCommand = -1;
        while(m_bRun)
        {
            System.out.println("메뉴를 보려면 \"0\"을 입력하세요.");
            System.out.print("> ");
            try {
                strInput = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            try {
                nCommand = Integer.parseInt(strInput);
            } catch (NumberFormatException e) {
                System.out.println("잘못된 번호입니다.");
                continue;
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
                case 10: // 기본 서버에 비동기식으로 로그인
                    testLoginDS();
                    break;
//                case 11: // 기본 서버에 동기식으로 로그인
//                    testSyncLoginDS();
//                    break;
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

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_scan.close();
    }

    public void printAllMenus()
    {
        System.out.println("---------------------------------- 도움말");
        System.out.println("0: 모든 메뉴 보기");
        System.out.println("---------------------------------- 시작/종료");
        System.out.println("100: CM 시작, 999: CM 종료");
        System.out.println("---------------------------------- 연결");
        System.out.println("1: 기본 서버에 접속, 2: 기본 서버에 접속 해제");
//        System.out.println("3: connect to designated server, 4: disconnect from designated server");
        System.out.println("---------------------------------- 로그인");
        System.out.println("10: 기본 서버에 비동기식으로 로그인" /*, 11: 기본 서버에 동기식으로 로그인"*/);
        System.out.println("12: 기본 서버에 로그아웃");
//        System.out.println("13: login to designated server, 14: logout from designated server");
//        System.out.println("---------------------------------- Session/Group");
//        System.out.println("20: request session information from default server");
//        System.out.println("21: synchronously request session information from default server");
//        System.out.println("22: join session of default server, 23: synchronously join session of default server");
//        System.out.println("24: leave session of default server, 25: change group of default server");
//        System.out.println("26: print group members");
//        System.out.println("27: request session information from designated server");
//        System.out.println("28: join session of designated server, 29: leave session of designated server");
        System.out.println("---------------------------------- Event 전송");
//        System.out.println("40: chat, 41: multicast chat in current group");
        System.out.println("42: CMDummyEvent 테스트" /*, 43: test CMUserEvent, 44: test datagram event, 45: test user position"*/);
//        System.out.println("46: test sendrecv, 47: test castrecv");
//        System.out.println("48: test asynchronous sendrecv, 49: test asynchronous castrecv");
//        System.out.println("---------------------------------- Information");
//        System.out.println("50: show group information of default server, 51: show current user status");
//        System.out.println("52: show current channels, 53: show current server information");
//        System.out.println("54: show group information of designated server");
//        System.out.println("55: measure input network throughput, 56: measure output network throughput");
//        System.out.println("57: show all configurations, 58: change configuration");
//        System.out.println("59: show current thread information");
//        System.out.println("---------------------------------- Channel");
//        System.out.println("60: add channel, 61: remove channel, 62: test blocking channel");
        System.out.println("---------------------------------- 파일 전송");
        System.out.println(/*"70: set file path,*/ "71: 파일 요청, 72: 파일 전송");
//        System.out.println("73: cancel receiving file, 74: cancel sending file");
//        System.out.println("75: print sending/receiving file info");
//        System.out.println("---------------------------------- Social Network Service");
//        System.out.println("80: request content list, 81: request next content list, 82: request previous content list");
//        System.out.println("83: request attached file, 84: upload content");
//        System.out.println("---------------------------------- User");
//        System.out.println("90: register new user, 91: deregister user, 92: find registered user");
//        System.out.println("93: add new friend, 94: remove friend, 95: show friends, 96: show friend requesters");
//        System.out.println("97: show bi-directional friends");
//        System.out.println("---------------------------------- MQTT");
//        System.out.println("200: connect, 201: publish, 202: subscribe, 203: print session info");
//        System.out.println("204: unsubscribe, 205: disconnect");
//        System.out.println("---------------------------------- File Sync");
//        System.out.println("300: start file-sync with manual mode, 301: stop file-sync");
//        System.out.println("302: open file-sync folder");
//        System.out.println("303: request online mode, 304: request local mode");
//        System.out.println("305: print online mode files, 306: print local mode files");
//        System.out.println("307: start file-sync with auto mode, 308: print current file-sync mode");
//        System.out.println("---------------------------------- Other CM Tests");
//        System.out.println("101: test forwarding scheme, 102: test delay of forwarding scheme");
//        System.out.println("103: test repeated request of SNS content list");
//        System.out.println("104: pull/push multiple files, 105: split file, 106: merge files, 107: distribute and merge file");
//        System.out.println("108: send event with wrong # bytes, 109: send event with wrong type");
//        System.out.println("112: create test files for file-sync");
//        System.out.println("113: test file access for file-sync");
    }

    public void testConnectionDS()
    {
        System.out.println("====== 기본 서버에 접속합니다.");
        m_clientStub.connectToServer();
        System.out.println("======");
    }

    public void testDisconnectionDS()
    {
        System.out.println("====== 기본 서버에 접속을 해제합니다.");
        m_clientStub.disconnectFromServer();
        System.out.println("======");
    }

    public void testLoginDS()
    {
        String strUserName = null;
        String strPassword = null;
        boolean bRequestResult = false;
        Console console = System.console();
        if(console == null)
        {
            System.err.println("콘솔을 가져올 수 없습니다.");
        }

        System.out.println("====== 기본 서버에 비동기식으로 로그인");
        System.out.print("유저 ID: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            strUserName = br.readLine();
            if(console == null)
            {
                System.out.print("패스워드: ");
                strPassword = br.readLine();
            }
            else
                strPassword = new String(console.readPassword("패스워드: "));
        } catch (IOException e) {
            e.printStackTrace();
        }

        bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
        if(bRequestResult)
            System.out.println("로그인 요청 성공");
        else
            System.err.println("로그인 요청 실패");
        System.out.println("======");
    }

    // 동기식으로 로그인
//    public void testSyncLoginDS()
//    {
//        String strUserName = null;
//        String strPassword = null;
//        CMSessionEvent loginAckEvent = null;
//        Console console = System.console();
//        if(console == null)
//        {
//            System.err.println("콘솔을 가져올 수 없습니다.");
//        }
//
//        System.out.println("====== 기본 서버에 로그인");
//        System.out.print("유저 ID: ");
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        try {
//            strUserName = br.readLine();
//            if(console == null)
//            {
//                System.out.print("패스워드: ");
//                strPassword = br.readLine();
//            }
//            else
//                strPassword = new String(console.readPassword("패스워드: "));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);
//        if(loginAckEvent != null)
//        {
//            // 로그인 결과 출력
//            if(loginAckEvent.isValidUser() == 0)
//            {
//                System.err.println("서버에 의해 인증이 실패했습니다.");
//            }
//            else if(loginAckEvent.isValidUser() == -1)
//            {
//                System.err.println("이미 로그인되어 있습니다.");
//            }
//            else
//            {
//                System.out.println("서버에 성공적으로 로그인했습니다.");
//            }
//        }
//        else
//        {
//            System.err.println("로그인 요청에 실패했습니다.");
//        }
//
//        System.out.println("======");
//    }

    public void testLogoutDS()
    {
        boolean bRequestResult = false;
        System.out.println("====== 기본 서버에 로그아웃");
        bRequestResult = m_clientStub.logoutCM();
        if(bRequestResult)
            System.out.println("로그아웃 요청 성공");
        else
            System.err.println("로그아웃 요청 실패");
        System.out.println("======");
    }

    public void testDummyEvent()
    {
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        CMUser myself = interInfo.getMyself();

        if(myself.getState() != CMInfo.CM_SESSION_JOIN)
        {
            System.out.println("세선과 그룹에 속해있어야 합니다.");
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

    public void testRequestFile()
    {
        boolean bReturn = false;
        String strFileName = null;
        String strFileOwner = null;
        String strFileAppend = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== 파일 요청");
        try {
            System.out.print("파일 이름: ");
            strFileName = br.readLine();
            System.out.print("파일 소유자(서버면 enter 입력): ");
            strFileOwner = br.readLine();
            if(strFileOwner.isEmpty())
                strFileOwner = m_clientStub.getDefaultServerName();
            System.out.print("파일 추가 모드('y'(덧붙이기);'n'(덮어쓰기);''(공백은 기본값): ");
            strFileAppend = br.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(strFileAppend.isEmpty())
            bReturn = m_clientStub.requestFile(strFileName, strFileOwner);
        else if(strFileAppend.equals("y"))
            bReturn = m_clientStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_APPEND);
        else if(strFileAppend.equals("n"))
            bReturn = m_clientStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_OVERWRITE);
        else
            System.err.println("잘못된 파일 추가 모드입니다.");

        if(!bReturn)
            System.err.println("파일 요청 오류! 파일("+strFileName+"), 소유자("+strFileOwner+").");

        System.out.println("======");
    }

    public void testPushFile()
    {
        String strFilePath = null;
        String strReceiver = null;
        String strFileAppend = null;
        boolean bReturn = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== 파일 전송");

        try {
            System.out.print("파일 경로: ");
            strFilePath = br.readLine();
            System.out.print("파일 수신자 (서버면 enter 입력): ");
            strReceiver = br.readLine();
            if(strReceiver.isEmpty())
                strReceiver = m_clientStub.getDefaultServerName();
            System.out.print("파일 추가 모드('y'(덧붙이기);'n'(덮어쓰기);''(공백은 기본값): ");
            strFileAppend = br.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(strFileAppend.isEmpty())
            bReturn = m_clientStub.pushFile(strFilePath, strReceiver);
        else if(strFileAppend.equals("y"))
            bReturn = m_clientStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_APPEND);
        else if(strFileAppend.equals("n"))
            bReturn = m_clientStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_OVERWRITE);
        else
            System.err.println("잘못된 파일 추가 모드입니다.");

        if(!bReturn)
            System.err.println("파일 전송 오류! 파일("+strFilePath+"), 수신자("+strReceiver+")");

        System.out.println("======");
    }

    public void testTerminateCM()
    {
        m_clientStub.terminateCM();
        m_bRun = false;
    }

    public static void main(String[] args) {
        CMClientApp client = new CMClientApp();
        CMClientStub cmStub = client.getClientStub();
        cmStub.setAppEventHandler(client.getClientEventHandler());
        client.testStartCM();

        System.out.println("클라이언트를 종료합니다.");

//        // 레거시 CM server에 로그인
//        String strUserName = null;
//        String strPassword = null;
//        boolean bRequestResult = false;
//        Console console = System.console();
//
//        System.out.print("유저 ID : ");
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        try {
//            strUserName = br.readLine();
//            if(console == null) {
//                System.out.print("패스워드 : ");
//                strPassword = br.readLine();
//            } else {
//                strPassword = new String(console.readPassword("패스워드 : "));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        bRequestResult = cmStub.loginCM(strUserName, strPassword);
//        if(bRequestResult) {
//            System.out.println("로그인 요청 성공");
//        } else {
//            System.err.println("로그인 요청 실패");
//        }

        // 레거시 로그인 정보 받기
//      System.out.println("=== 로그인 정보 ===");
//      System.out.print("사용자 이름을 작성하세요 : ");
//      String userID = scanner.nextLine();
//      System.out.print("패스워드를 작성하세요 : ");
//      String userPW = scanner.nextLine();
//      System.out.println("사용자 이름 : " + userID);
//      System.out.println("패스워드 : " + userPW);
//      ret = cmStub.loginCM(userID, userPW);
//
//      if(ret) {
//          System.out.println("로그인 요청 성공");
//      } else {
//          System.out.println("로그인 요청 실패");
//          return;
//      }
//
//      if("로그인 성공 시") {
//          break;
//      }
//  }
        // 실행할 다음 API 기다리기
//        System.out.println("다음으로 실행할 API를 입력하세요.");
//        scanner.nextLine();

        // CM 종료
//        System.out.println("CM과 서버에 접속을 종료하려면 입력하세요.");
//        scanner.nextLine();
//        cmStub.terminateCM();
    }
}