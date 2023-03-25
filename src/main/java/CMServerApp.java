import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class CMServerApp {
    private CMServerStub m_serverStub;  // CMServerStub 타입 레퍼런스 변수 m_serverStub 선언
    private CMServerEventHandler m_eventHandler;  // CMServerEventHandler 타입 레퍼런스 변수 m_eventHandler 선언
    private boolean m_bRun;
    private Scanner m_scan = null;


    public CMServerApp() {  // CMServerApp 생성자
        m_serverStub = new CMServerStub();  // CMServerStub 객체 생성 후 변수 m_serverStub에 할당
        m_eventHandler = new CMServerEventHandler(m_serverStub);  // CMServerEventHandler 객체 생성 후 변수 m_eventHandler에 할당
        m_bRun = true;
    }

    // CMServerStub 타입 m_serverStub을 반환하는 메소드
    public CMServerStub getServerStub() {
        return m_serverStub;
    }

    // CMServerEventHandler 타입 m_eventHandler를 반환하는 메소드
    public CMServerEventHandler getServerEventHandler() {
        return m_eventHandler;
    }

    public void startTest()
    {
        System.out.println("서버 시작");
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

            switch(nCommand)
            {
                case 0:
                    printAllMenus();
                    break;
                case 100:
                    startCM();
                    break;
                case 999:
                    terminateCM();
                    return;
//                case 1: // print session information
//                    printSessionInfo();
//                    break;
//                case 2: // print selected group information
//                    printGroupInfo();
//                    break;
//                case 3:	// test input network throughput
//                    measureInputThroughput();
//                    break;
//                case 4:	// test output network throughput
//                    measureOutputThroughput();
//                    break;
//                case 5:	// print current channels information
//                    printCurrentChannelInfo();
//                    break;
//                case 6: // print current login users
//                    printLoginUsers();
//                    break;
//                case 7: // print all current configurations
//                    printConfigurations();
//                    break;
//                case 8: // change a field value in the configuration file
//                    changeConfiguration();
//                    break;
//                case 9:	// show current thread information
//                    printThreadInfo();
//                    break;
//                case 20: // set file path
//                    setFilePath();
//                    break;
                case 21: // request a file
                    requestFile();
                    break;
                case 22: // push a file
                    pushFile();
                    break;
//                case 23:	// test cancel receiving a file
//                    cancelRecvFile();
//                    break;
//                case 24:	// test cancel sending a file
//                    cancelSendFile();
//                    break;
//                case 25:	// print sending/receiving file info
//                    printSendRecvFileInfo();
//                    break;
//                case 30: // request registration to the default server
//                    requestServerReg();
//                    break;
//                case 31: // request deregistration from the default server
//                    requestServerDereg();
//                    break;
//                case 32: // connect to the default server
//                    connectToDefaultServer();
//                    break;
//                case 33: // disconnect from the default server
//                    disconnectFromDefaultServer();
//                    break;
//                case 40: // set a scheme for attachement download of SNS content
//                    setAttachDownloadScheme();
//                    break;
//                case 50: 	// test add channel
//                    addChannel();
//                    break;
//                case 51: 	// test remove channel
//                    removeChannel();
//                    break;
//                case 60:	// find session info
//                    findMqttSessionInfo();
//                    break;
//                case 61:	// print all session info
//                    printAllMqttSessionInfo();
//                    break;
//                case 62:	// print all retain info
//                    printAllMqttRetainInfo();
//                    break;
//                case 70:	// open file-sync folder
//                    openFileSyncFolder();
//                    break;
//                case 101:	// configure variables of user access simulation
//                    configureUserAccessSimulation();
//                    break;
//                case 102: 	// start user access simulation
//                    startUserAccessSimulation();
//                    break;
//                case 103:	// start user access simulation and calculate prefetch precision and recall
//                    startUserAccessSimulationAndCalPrecRecall();
//                    break;
//                case 104: 	// configure, simulate and write recent history to CMDB
//                    writeRecentAccHistoryToDB();
//                    break;
//                case 105:	// send event with wrong # bytes
//                    sendEventWithWrongByteNum();
//                    break;
//                case 106:	// send event with wrong type
//                    sendEventWithWrongEventType();
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

    public void startCM() {
        boolean ret = m_serverStub.startCM();

        if(ret) {
            System.out.println("CM 초기화 완료.");
        } else {
            System.out.println("CM 초기화 오류.");
        }
        startTest();
    }

    public void printAllMenus()
    {
        System.out.print("---------------------------------- 도움말\n");
        System.out.print("0: 모든 메뉴 표시\n");
        System.out.print("---------------------------------- 시작/종료\n");
        System.out.print("100: CM 시작, 999: CM 종료\n");
//        System.out.print("---------------------------------- Information\n");
//        System.out.print("1: show session information, 2: show group information\n");
//        System.out.print("3: test input network throughput, 4: test output network throughput\n");
//        System.out.print("5: show current channels, 6: show login users\n");
//        System.out.print("7: show all configurations, 8: change configuration\n");
//        System.out.print("9: show current thread information\n");
        System.out.print("---------------------------------- 파일 전송\n");
        System.out.print(/*"20: set file path, */"21: 파일 요청, 22: 파일 전송\n");
//        System.out.print("23: cancel receiving file, 24: cancel sending file\n");
//        System.out.print("25: print sending/receiving file info\n");
//        System.out.print("---------------------------------- Multi-server\n");
//        System.out.print("30: register to default server, 31: deregister from default server\n");
//        System.out.print("32: connect to default server, 33: disconnect from default server\n");
//        System.out.print("---------------------------------- Social Network Service\n");
//        System.out.print("40: set attachment download scheme\n");
//        System.out.print("---------------------------------- Channel\n");
//        System.out.print("50: add channel, 51: remove channel\n");
//        System.out.print("---------------------------------- MQTT\n");
//        System.out.print("60: find session info, 61: print all session info, 62: print all retain info\n");
//        System.out.print("---------------------------------- File Sync\n");
//        System.out.print("70: open file-sync folder\n");
//        System.out.print("---------------------------------- Other CM Tests\n");
//        System.out.print("101: configure SNS user access simulation, 102: start SNS user access simulation\n");
//        System.out.print("103: start SNS user access simulation and measure prefetch accuracy\n");
//        System.out.print("104: start and write recent SNS access history simulation to CM DB\n");
//        System.out.print("105: send event with wrong bytes, 106: send event with wrong type\n");
    }

    public void requestFile()
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
            System.out.print("파일 소유자(유저 이름): ");
            strFileOwner = br.readLine();
            System.out.print("파일 추가 모드('y'(덧붙이기);'n'(덮어쓰기);''(공백은 기본값): ");
            strFileAppend = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(strFileAppend.isEmpty())
            bReturn = m_serverStub.requestFile(strFileName, strFileOwner);
        else if(strFileAppend.equals("y"))
            bReturn = m_serverStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_APPEND);
        else if(strFileAppend.equals("n"))
            bReturn = m_serverStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_OVERWRITE);
        else
            System.err.println("잘못된 파일 추가 모드입니다");

        if(!bReturn)
            System.err.println("파일 요청 오류! 파일("+strFileName+"), 소유자("+strFileOwner+").");

        System.out.println("======");
    }

    public void pushFile()
    {
        boolean bReturn = false;
        String strFilePath = null;
        String strReceiver = null;
        String strFileAppend = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== 파일 전송");

        try {
            System.out.print("파일 경로: ");
            strFilePath = br.readLine();
            System.out.print("파일 수신자 (유저 이름): ");
            strReceiver = br.readLine();
            System.out.print("파일 추가 모드('y'(덧붙이기);'n'(덮어쓰기);''(공백은 기본값): ");
            strFileAppend = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(strFileAppend.isEmpty())
            bReturn = m_serverStub.pushFile(strFilePath, strReceiver);
        else if(strFileAppend.equals("y"))
            bReturn = m_serverStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_APPEND);
        else if(strFileAppend.equals("n"))
            bReturn = m_serverStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_OVERWRITE);
        else
            System.err.println("잘못된 파일 추가 모드입니다");

        if(!bReturn)
            System.err.println("파일 전송 오류! 파일("+strFilePath+"), 수신자("+strReceiver+")");

        System.out.println("======");
    }

    public void terminateCM()
    {
        m_serverStub.terminateCM();
        m_bRun = false;
    }

    public static void main(String[] args) {
        CMServerApp server = new CMServerApp();  // CMServerApp 객체 생성
        CMServerStub cmStub = server.getServerStub();  // CMServerApp 객체의 getServerStub 메소드 실행, m_serverStub(CMServerStub 객체) 반환
        cmStub.setAppEventHandler(server.getServerEventHandler());  // ??? CM Stub에 Event Handler 설정

        server.startCM();

      System.out.println("서버를 종료합니다.");
    }
}
