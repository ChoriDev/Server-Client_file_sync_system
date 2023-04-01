import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import kr.ac.konkuk.ccslab.cm.entity.CMGroup;
import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMMessage;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMBlockingEventQueue;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.info.CMCommInfo;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMCommManager;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.manager.CMFileSyncManager;
import kr.ac.konkuk.ccslab.cm.manager.CMMqttManager;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSUserAccessSimulator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServerWinApp extends JFrame {
    private CMServerStub m_serverStub;  // CMServerStub 타입 레퍼런스 변수 m_serverStub 선언
    private CMServerWinEventHandler m_eventHandler;  // CMServerEventHandler 타입 레퍼런스 변수 m_eventHandler 선언
    private JTextPane m_outTextPane;
    private JTextField m_inTextField;
    private JButton m_startStopButton;



    public CMServerWinApp() {  // CMServerApp 생성자
        CMServerWinApp.MyKeyListener cmKeyListener = new MyKeyListener();
        CMServerWinApp.MyActionListener cmActionListener = new MyActionListener();
        setTitle("CMServerWinApp");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        m_outTextPane = new JTextPane();
        m_outTextPane.setEditable(false);

        StyledDocument doc = m_outTextPane.getStyledDocument();
        addStylesToDocument(doc);

        add(m_outTextPane, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane (m_outTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scroll);

        m_inTextField = new JTextField();
        m_inTextField.addKeyListener(cmKeyListener);
        add(m_inTextField, BorderLayout.SOUTH);

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new FlowLayout());
        add(topButtonPanel, BorderLayout.NORTH);

        m_startStopButton = new JButton("서버 시작");
        m_startStopButton.addActionListener(cmActionListener);
        m_startStopButton.setEnabled(false);
        topButtonPanel.add(m_startStopButton);

        setVisible(true);

        m_serverStub = new CMServerStub();  // CMServerStub 객체 생성 후 변수 m_serverStub에 할당
        m_eventHandler = new CMServerWinEventHandler(m_serverStub, this);  // CMServerEventHandler 객체 생성 후 변수 m_eventHandler에 할당

        startCM();
    }

    private void addStylesToDocument(StyledDocument doc)
    {
        Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regularStyle = doc.addStyle("regular", defStyle);
        StyleConstants.setFontFamily(regularStyle, "SansSerif");

        Style boldStyle = doc.addStyle("bold", defStyle);
        StyleConstants.setBold(boldStyle, true);
    }

    public void processInput(String strInput)
    {
        int nCommand = -1;
        try {
            nCommand = Integer.parseInt(strInput);
        } catch (NumberFormatException e) {
            printMessage("잘못된 입력입니다!\n");
            return;
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
//            case 1: // print session information
//                printSessionInfo();
//                break;
//            case 2: // print selected group information
//                printGroupInfo();
//                break;
//            case 3:	// test input network throughput
//                measureInputThroughput();
//                break;
//            case 4:	// test output network throughput
//                measureOutputThroughput();
//                break;
//            case 5:	// print current channels information
//                printCurrentChannelInfo();
//                break;
//            case 6: // print current login users
//                printLoginUsers();
//                break;
//            case 7: // print all current configurations
//                printConfigurations();
//                break;
//            case 8: // change a field value in the configuration file
//                changeConfiguration();
//                break;
//            case 9:	// show current thread information
//                printThreadInfo();
//                break;
            case 10:	// send CMDummyEvent
                sendCMDummyEvent();
                break;
//            case 20: // set file path
//                setFilePath();
//                break;
            case 21: // request a file
                requestFile();
                break;
            case 22: // push a file
                pushFile();
                break;
//            case 23:	// test cancel receiving a file
//                cancelRecvFile();
//                break;
//            case 24:	// test cancel sending a file
//                cancelSendFile();
//                break;
//            case 25:	// print sending/receiving file info
//                printSendRecvFileInfo();
//                break;
//            case 30: // request registration to the default server
//                requestServerReg();
//                break;
//            case 31: // request deregistration from the default server
//                requestServerDereg();
//                break;
//            case 32: // connect to the default server
//                connectToDefaultServer();
//                break;
//            case 33: // disconnect from the default server
//                disconnectFromDefaultServer();
//                break;
//            case 40: // set a scheme for attachement download of SNS content
//                setAttachDownloadScheme();
//                break;
//            case 50: 	// test add channel
//                addChannel();
//                break;
//            case 51: 	// test remove channel
//                removeChannel();
//                break;
//            case 60:	// find session info
//                findMqttSessionInfo();
//                break;
//            case 61:	// print all session info
//                printAllMqttSessionInfo();
//                break;
//            case 62:	// print all retain info
//                printAllMqttRetainInfo();
//                break;
//            case 70:	// open file-sync folder
//                openFileSyncFolder();
//                break;
//            case 101:	// configure variables of user access simulation
//                configureUserAccessSimulation();
//                break;
//            case 102: 	// start user access simulation
//                startUserAccessSimulation();
//                break;
//            case 103:	// start user access simulation and calculate prefetch precision and recall
//                startUserAccessSimulationAndCalPrecRecall();
//                break;
//            case 104: 	// configure, simulate and write recent history to CMDB
//                writeRecentAccHistoryToDB();
//                break;
//            case 105:	// send event with wrong # bytes
//                sendEventWithWrongByteNum();
//                break;
//            case 106:	// send event with wrong type
//                sendEventWithWrongEventType();
//                break;
            default:
                printStyledMessage("알 수 없는 번호입니다.\n", "bold");
                break;
        }
    }

    public void printMessage(String strText)
    {
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

    public class MyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e)
        {
            JButton button = (JButton) e.getSource();
            if(button.getText().equals("서버 시작"))
            {
                // start cm
                boolean bRet = m_serverStub.startCM();
                if(!bRet)
                {
                    printStyledMessage("서버 초기화 오류\n", "bold");
                }
                else
                {
                    printStyledMessage("서버 시작\n", "bold");
                    printMessage("메뉴를 보려면 \"0\"을 입력하세요.\n");
                    // change button to "stop CM"
                    button.setText("서버 종료");
                }
                // check if default server or not
                if(CMConfigurator.isDServer(m_serverStub.getCMInfo()))
                {
                    setTitle("CM Default Server (\"SERVER\")");
                }
                else
                {
                    setTitle("CM Additional Server (\"?\")");
                }
                m_inTextField.requestFocus();
            }
            else if(button.getText().equals("서버 종료"))
            {
                // stop cm
                m_serverStub.terminateCM();
                printMessage("서버를 종료합니다.\n");
                // change button to "start CM"
                button.setText("서버 시작");
            }
        }
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
        }

        public void keyReleased(KeyEvent e){}
        public void keyTyped(KeyEvent e){}
    }

    // CMServerStub 타입 m_serverStub을 반환하는 메소드
    public CMServerStub getServerStub() {
        return m_serverStub;
    }

    // CMServerEventHandler 타입 m_eventHandler를 반환하는 메소드
    public CMServerWinEventHandler getServerEventHandler() {
        return m_eventHandler;
    }

    public void sendCMDummyEvent()
    {
        String strMessage = null;
        String strTarget = null;
        String strSession = null;
        String strGroup = null;
        CMDummyEvent de = null;
        printMessage("====== 이벤트 전송 테스트\n");

        JTextField messageField = new JTextField();
        JTextField targetField = new JTextField();
        JTextField sessionField = new JTextField();
        JTextField groupField = new JTextField();

        Object[] msg = {
                "메시지: ", messageField,
                "수신 사용자 (send() 메소드 사용 시): ", targetField,
//                "수신 세션 (cast() 또는 broadcast() 메소드 이용 시): ", sessionField,
//                "수신 그룹 (cast() 또는 broadcast() 메소드 사용시): ", groupField
        };
        int option = JOptionPane.showConfirmDialog(null, msg, "더미 이벤트 전송",
                JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.OK_OPTION)
        {
            strMessage = messageField.getText().trim();
            strTarget = targetField.getText().trim();
            strSession = sessionField.getText().trim();
            strGroup = groupField.getText().trim();

            if(strMessage.isEmpty())
            {
                printStyledMessage("메시지를 입력하지 않았습니다.\n", "bold");
                return;
            }

            de = new CMDummyEvent();
            de.setDummyInfo(strMessage);
            de.setHandlerSession(strSession);
            de.setHandlerGroup(strGroup);

            if(!strTarget.isEmpty())
            {
                m_serverStub.send(de, strTarget);
            }
            else
            {
                if(strSession.isEmpty()) strSession = null;
                if(strGroup.isEmpty()) strGroup = null;
                m_serverStub.cast(de, strSession, strGroup);
            }
        }

    }

    // 파일 수신 완료 메시지 전달용으로 sendCMDummyEvent() 오버로딩
    public void sendCMDummyEvent(String constMsg, String fileSender)
    {
        String strMessage = constMsg;
        String strTarget = fileSender;
        CMDummyEvent de = null;

        de = new CMDummyEvent();
        de.setDummyInfo(strMessage);

        m_serverStub.send(de, strTarget);
    }

    public void startCM() {
        boolean ret = m_serverStub.startCM();

        if(ret) {
            printStyledMessage("서버 시작\n", "bold");
            printMessage("메뉴를 보려면 \"0\"을 입력하세요.\n");
            // 서버 종료로 버튼 바꾸기
            m_startStopButton.setEnabled(true);
            m_startStopButton.setText("서버 종료");
        } else {
            printMessage("서버 초기화 오류.\n");
        }
        m_inTextField.requestFocus();
    }

    public void printAllMenus()
    {
        printMessage("---------------------------------- 도움말\n");
        printMessage("0: 모든 메뉴 표시\n");
        printMessage("---------------------------------- 시작/종료\n");
        printMessage("100: CM 시작, 999: CM 종료\n");
//        printMessage("---------------------------------- Information\n");
//        printMessage("1: show session information, 2: show group information\n");
//        printMessage("3: test input network throughput, 4: test output network throughput\n");
//        printMessage("5: show current channels, 6: show login users\n");
//        printMessage("7: show all configurations, 8: change configuration\n");
//        printMessage("9: show current thread information\n");
//        printMessage("---------------------------------- Event Transmission\n");
        printMessage("10: 더미 이벤트 전송\n");
        printMessage("---------------------------------- 파일 전송\n");
        printMessage(/*"20: set file path, */"21: 파일 요청, 22: 파일 전송\n");
//        printMessage("23: cancel receiving file, 24: cancel sending file\n");
//        printMessage("25: print sending/receiving file info\n");
//        printMessage("---------------------------------- Multi-server\n");
//        printMessage("30: register to default server, 31: deregister from default server\n");
//        printMessage("32: connect to default server, 33: disconnect from default server\n");
//        printMessage("---------------------------------- Social Network Service\n");
//        printMessage("40: set attachment download scheme\n");
//        printMessage("---------------------------------- Channel\n");
//        printMessage("50: add channel, 51: remove channel\n");
//        printMessage("---------------------------------- MQTT\n");
//        printMessage("60: find session info, 61: print all session info, 62: print all retain info\n");
//        printMessage("---------------------------------- File Sync\n");
//        printMessage("70: open file-sync folder\n");
//        printMessage("---------------------------------- Other CM Tests\n");
//        printMessage("101: configure SNS user access simulation, 102: start SNS user access simulation\n");
//        printMessage("103: start SNS user access simulation and measure prefetch accuracy\n");
//        printMessage("104: start and write recent SNS access history simulation to CM DB\n");
//        printMessage("105: send event with wrong bytes, 106: send event with wrong type\n");
    }

    public void requestFile()
    {
        boolean bReturn = false;
        String strFileName = null;
        String strFileOwner = null;
        byte byteFileAppendMode = -1;

        printMessage("====== 파일 요청\n");
        JTextField fileNameField = new JTextField();
        JTextField fileOwnerField = new JTextField();
        String[] fAppendMode = {"기본값", "덮어쓰기", "덧붙이기"};
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

        Object[] message = {
                "파일 이름:", fileNameField,
                "파일 소유자:", fileOwnerField,
                "파일 추가 모드: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "파일 요청 입력", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
        {
            printMessage("취소했습니다.\n");
            return;
        }

        strFileName = fileNameField.getText().trim();
        if(strFileName.isEmpty())
        {
            printMessage("파일 이름을 입력하지 않았습니다.\n");
            return;
        }

        strFileOwner = fileOwnerField.getText().trim();
        if(strFileOwner.isEmpty())
        {
            printMessage("파일 소유자를 입력하지 않았습니다.\n");
            return;
        }

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

        bReturn = m_serverStub.requestFile(strFileName, strFileOwner, byteFileAppendMode);

        if(!bReturn)
            printMessage("파일 요청 오류. 파일("+strFileName+"), 소유자("+strFileOwner+").\n");

        printMessage("======\n");
    }

    public void pushFile()
    {
        String strFilePath = null;
        File[] files;
        String strReceiver = null;
        byte byteFileAppendMode = -1;
        boolean bReturn = false;

		/*
		strReceiver = JOptionPane.showInputDialog("Receiver Name: ");
		if(strReceiver == null) return;
		*/
        JTextField freceiverField = new JTextField();
        String[] fAppendMode = {"기본값", "덮어쓰기", "덧붙이기"};
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

        Object[] message = {
                "파일 수신자: ", freceiverField,
                "파일 추가 모드: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "파일 경로", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
        {
            printMessage("취소했습니다.\n");
            return;
        }

        strReceiver = freceiverField.getText().trim();
        if(strReceiver.isEmpty())
        {
            printMessage("파일 수신자를 입력하지 않았습니다.\n");
            return;
        }

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
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        File curDir = new File(confInfo.getTransferedFileHome().toString());
        fc.setCurrentDirectory(curDir);
        int fcRet = fc.showOpenDialog(this);
        if(fcRet != JFileChooser.APPROVE_OPTION) return;
        files = fc.getSelectedFiles();
        if(files.length < 1) return;
        for(int i=0; i < files.length; i++)
        {
            strFilePath = files[i].getPath();
            bReturn = m_serverStub.pushFile(strFilePath, strReceiver, byteFileAppendMode);
            if(!bReturn)
            {
                printMessage("파일 전송 오류. 파일("+strFilePath+"), 수신자("
                        +strReceiver+").\n");
            }
        }

        printMessage("======\n");
    }

    public void terminateCM()
    {
        m_serverStub.terminateCM();
        printMessage("서버를 종료합니다.\n");
        m_startStopButton.setText("서버 시작");
    }

    public static void main(String[] args) {
        CMServerWinApp server = new CMServerWinApp();  // CMServerApp 객체 생성
        CMServerStub cmStub = server.getServerStub();  // CMServerApp 객체의 getServerStub 메소드 실행, m_serverStub(CMServerStub 객체) 반환
        cmStub.setAppEventHandler(server.getServerEventHandler());  // ??? CM Stub에 Event Handler 설정
    }
}
