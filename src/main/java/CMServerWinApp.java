import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServerWinApp extends JFrame {
    private CMServerStub m_serverStub;  // CMServerStub 타입 레퍼런스 변수 m_serverStub 선언
    private CMServerWinEventHandler m_eventHandler;  // CMServerEventHandler 타입 레퍼런스 변수 m_eventHandler 선언
    private JPanel m_pnlCenter;  // Gui에 사용할 변수, 출력 메시지 패널과 접속 중인 사용자 표시 패널을 담음
    private JTextPane m_memberPane;  // Gui에 사용할 변수, 현재 접속 중인 사용자를 출력할 패널
    private JTextPane m_outTextPane;  // Gui에 사용할 변수, 메시지를 출력할 패널
    private JTextField m_inTextField;  // Gui에 사용할 변수, 입력을 할 수 있는 텍스트 상자
    private JButton m_startStopButton;  // Gui에 사용할 변수, 서버 시작과 종료를 할 수 있는 버튼

    public CMServerWinApp() {  // CMServerApp 생성자
        CMServerWinApp.MyKeyListener cmKeyListener = new MyKeyListener();  // 키 이벤트 리스너 객체 생성
        CMServerWinApp.MyActionListener cmActionListener = new MyActionListener();  // 액션 이벤트 리스너 객체 생성
        setTitle("CMServerWinApp");  // 프레임의 타이틀 설정
        setSize(500, 500);  // 프레임 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 닫기 버튼을 클릭하면 프로그램 종료 설정

        setLayout(new BorderLayout());  // 프레임의 배치를 BorderLayout으로 설정

        m_pnlCenter = new JPanel(new GridLayout());  // 배치를 GridLayout으로 설정
        getContentPane().add(m_pnlCenter, BorderLayout.CENTER);  // 프레임의 중앙에 배치

        m_outTextPane = new JTextPane();  // 메시지를 출력할 텍스트 패널 생성
        m_outTextPane.setBackground(new Color(236,235,227));  // 텍스트 패널의 바탕색 설정
        m_outTextPane.setEditable(false);  // 텍스트 패널은 편집이 불가하도록 설정

        StyledDocument doc = m_outTextPane.getStyledDocument();  // 스타일 가져오기
        addStylesToDocument(doc);  // 스타일 적용

        JScrollPane scroll = new JScrollPane (m_outTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);  // 스크롤 패널 생성
        m_pnlCenter.add(scroll);  // pnlCenter 패널에 배치

        m_memberPane = new JTextPane();  // 현재 접속 중인 사용자를 출력할 패널 생성
        m_memberPane.setEditable(false);  // 패널 편집이 불가능하도록 설정
        JScrollPane leftScroll = new JScrollPane(m_memberPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);  // 스크롤 패널 생성
        m_pnlCenter.add(leftScroll);  // pnlCenter 패널에 배치

        m_inTextField = new JTextField();  // 메시지를 입력할 텍스트 필드 생성
        m_inTextField.addKeyListener(cmKeyListener);  // 텍스트 필드에 키 이벤트 리스너 부착
        add(m_inTextField, BorderLayout.SOUTH);  // 텍스트 필드를 프레임의 하단에 배치

        JPanel topButtonPanel = new JPanel();  // 버튼을 담을 패널 생성
        topButtonPanel.setLayout(new FlowLayout());  // 패널의 배치를 FlowLayout으로 설정
        add(topButtonPanel, BorderLayout.NORTH);  // 패널을 프레임의 상단에 배치
        topButtonPanel.setBackground(new Color(3,107,63));  // 패널의 배경색 설정

        m_startStopButton = new JButton("서버 시작");  // 서버 작동 버튼 생성
        m_startStopButton.addActionListener(cmActionListener);  // 버튼에 액션 리스너 부착
        m_startStopButton.setEnabled(true);  // 버튼 활성화
        topButtonPanel.add(m_startStopButton);  // 버튼을 담을 패널에 버튼 추가

        setVisible(true);  // 프레임 출력

        m_serverStub = new CMServerStub();  // CMServerStub 객체 생성 후 변수 m_serverStub에 할당
        m_eventHandler = new CMServerWinEventHandler(m_serverStub, this);  // CMServerEventHandler 객체 생성 후 변수 m_eventHandler에 할당

        startCM();  // 프로그램을 실행하면 자동으로 서버 시작
    }

    private void addStylesToDocument(StyledDocument doc) {  // 스타일 설정 메소드
        Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);  // 기본 스타일 가져오기

        Style regularStyle = doc.addStyle("regular", defStyle);  // 일반 스타일
        StyleConstants.setFontFamily(regularStyle, "SansSerif");  // 글꼴은 산세리프

        Style boldStyle = doc.addStyle("bold", defStyle);  // 볼드 스타일
        StyleConstants.setBold(boldStyle, true);  // 글씨 굵게
    }

    public void processInput(String strInput) {  // 입력한 값에 따라 각각의 기능을 호출하는 메소드
        int nCommand = -1;  // 입력한 값
        try {  // 입력한 값을 정수로 변환
            nCommand = Integer.parseInt(strInput);
        } catch (NumberFormatException e) {  // 기능을 수행할 수 있는 입력이 아닌 경우 에러 처리
            printMessage("잘못된 입력입니다!\n");
            return;
        }

        switch(nCommand) {
            case 0:  // 사용 가능한 모든 기능 표시
                printAllMenus();
                break;
            case 100: // 서버 시작
                startCM();
                break;
            case 999:  // 서버 종료
                terminateCM();
                return;
            case 10:	// 간단한 메시지 보내기
                sendCMDummyEvent();
                break;
            case 21: // 파일 요청
                requestFile();
                break;
            case 22: // 파일 전송
                pushFile();
                break;
            case 70: // 파일 동기화 폴더 열기
                openFileSyncFolder();
                break;
            default:
                printStyledMessage("알 수 없는 번호입니다.\n", "bold");
                break;
        }
    }

    public void printMessage(String strText) {  // 텍스트를 출력하는 메소드
        StyledDocument doc = m_outTextPane.getStyledDocument();  // 스타일 정보 가져오기
        try {  // 출력 텍스트 패널에 메시지 출력
            doc.insertString(doc.getLength(), strText, null);
            m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());
        } catch (BadLocationException e) {  // 에러 처리
            e.printStackTrace();
        }

        return;
    }

    public void printStyledMessage(String strText, String strStyleName) {  // 스타일이 적용된 텍스트를 출력하는 메소드
        StyledDocument doc = m_outTextPane.getStyledDocument();
        try {  // 출력 텍스트 패널에 메시지 출력
            doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
            m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());
        } catch (BadLocationException e) {  // 에러 처리
            e.printStackTrace();
        }

        return;
    }

    public class MyActionListener implements ActionListener {  // 액션 이벤트에 관한 리스너
        public void actionPerformed(ActionEvent e) {  // 액션을 감지했을 때 작동
            JButton button = (JButton) e.getSource();  // 클릭된 버튼의 정보 저장
            if(button.getText().equals("서버 시작")) {  // 클릭된 버튼이 "서버 시작"일 경우
                boolean bRet = m_serverStub.startCM();  // CM 시작
                if(!bRet) {  // CM 시작에 실패한 경우
                    printStyledMessage("서버 초기화 오류\n", "bold");
                }
                else {  // CM 시작에 성공한 경우
                    printStyledMessage("서버 시작\n", "bold");
                    printMessage("메뉴를 보려면 \"0\"을 입력하세요.\n");
                    button.setText("서버 종료");  // 버튼을 "서버 종료"로 설정
                }
                if(CMConfigurator.isDServer(m_serverStub.getCMInfo())) {  // 기본 서버인 경우
                    setTitle("CM Default Server (\"SERVER\")");
                }
                else {  // 기본 서버가 아닌 경우
                    setTitle("CM Additional Server (\"?\")");
                }
                m_inTextField.requestFocus();  // 텍스트 필드에 포커스 설정
            }
            else if(button.getText().equals("서버 종료")) {  // 클릭된 버튼이 "서버 종료"일 경우
                m_serverStub.terminateCM();  // CM 종료
                printMessage("서버를 종료합니다.\n");
                button.setText("서버 시작");  // 버튼을 "서버 시작"으로 설정
            }
        }
    }

    public class MyKeyListener implements KeyListener {  // 키 이벤트에 관한 리스너
        public void keyPressed(KeyEvent e) {  // 키가 눌렸을 때의 작동
            int key = e.getKeyCode();  // 어떤 키가 눌렸는지 정보 저장
            if(key == KeyEvent.VK_ENTER) {  // 눌린 키가 enter일 경우
                JTextField input = (JTextField)e.getSource();  // 이벤트가 발생한 컴포넌트 정보 저장
                String strText = input.getText();  // 텍스트 필드에 입력된 값 저장
                printMessage(strText+"\n");  // 텍스트 필드의 값 출력
                processInput(strText);  // 입력한 값에 따라 기능을 수행하는 메소드에 입력한 값 전달
                input.setText("");  // 이벤트가 발생한 컴포넌트의 정보 초기화
                input.requestFocus();  // 이벤트가 발생한 컴포넌트에 포커스 설정
            }
        }

        public void keyReleased(KeyEvent e){
            // 미사용
        }
        public void keyTyped(KeyEvent e){
            // 미사용
        }
    }

    public CMServerStub getServerStub() {  // CMServerStub 타입 m_serverStub을 반환하는 메소드
        return m_serverStub;
    }

    public CMServerWinEventHandler getServerEventHandler() {  // CMServerEventHandler 타입 m_eventHandler를 반환하는 메소드
        return m_eventHandler;
    }

    public void sendCMDummyEvent() {  // 간단한 메시지를 보내는 메소드
        String strMessage = null;  // 메시지 내용
        String strTarget = null;  // 메시지 수신자
        CMDummyEvent de = null;  // 이벤트
        printMessage("====== 간단한 메시지 보내기\n");

        JTextField messageField = new JTextField();  // 메시지 내용을 담는 필드
        JTextField targetField = new JTextField();  // 메시지 수신자를 담는 필드

        Object[] msg = {  // 메시지 내용과 메시지 수신자를 배열로 저장
                "메시지: ", messageField,
                "수신 사용자 (send() 메소드 사용 시): ", targetField
        };
        int option = JOptionPane.showConfirmDialog(null, msg, "더미 이벤트 전송",
                JOptionPane.OK_CANCEL_OPTION);  // 메시지 전송 창
        if(option == JOptionPane.OK_OPTION) {   // 메시지 전송 창에서 OK를 눌렀을 경우
            strMessage = messageField.getText().trim();  // 입력한 메시지에서 좌우 띄어쓰기는 제거 
            strTarget = targetField.getText().trim();  // 입력한 수신자에서 좌우 띄어쓰기는 제거

            if(strMessage.isEmpty()) {  // 입력한 메시지가 없는 경우
                printStyledMessage("메시지를 입력하지 않았습니다.\n", "bold");
                return;
            }
            
            // 이벤트 처리
            de = new CMDummyEvent();
            de.setDummyInfo(strMessage);

            if(!strTarget.isEmpty()) {  // 수신자를 입력한 경우
                m_serverStub.send(de, strTarget);
            }
        }
    }

    public void sendCMDummyEvent(String constMsg, String fileSender) {  // 파일 수신 완료 메시지 전달용으로 sendCMDummyEvent() 오버로딩
        String strMessage = constMsg;  // 파일 수신 완료 메시지
        String strTarget = fileSender;  // 파일 송신자
        CMDummyEvent de = null;  // 이벤트

        // 이벤트 처리
        de = new CMDummyEvent();
        de.setDummyInfo(strMessage);

        m_serverStub.send(de, strTarget);  // 수신 완료 메시지 전달
    }

    public void startCM() {  // 서버 시작 메소드
        boolean ret = m_serverStub.startCM();  // 시작 여부
        if(ret) {  // 정상적으로 서버 시작이 된 경우
            printStyledMessage("서버 시작\n", "bold");
            printMessage("메뉴를 보려면 \"0\"을 입력하세요.\n");
            m_startStopButton.setText("서버 종료");  // 서버 종료로 버튼 바꾸기
        } else {  // 정상적으로 서버 시작이 안된 경우 에러 발생
            printMessage("서버 초기화 오류.\n");
        }
        m_inTextField.requestFocus();  // 텍스트 입력 상자에 포커스 설정
    }

    public void printAllMenus() {  // 사용할 수 있는 기능과 입력할 값 안내
        printMessage("---------------------------------- 도움말\n");
        printMessage("0: 모든 메뉴 표시\n");
        printMessage("---------------------------------- 시작/종료\n");
        printMessage("100: CM 시작, 999: CM 종료\n");
        printMessage("10: 간단한 메시지 보내기\n");
        printMessage("---------------------------------- 파일 전송\n");
        printMessage("21: 파일 요청, 22: 파일 전송\n");
        printMessage("---------------------------------- 파일 동기화\n");
        printMessage("70: 동기화 폴더 열기\n");
    }

    public void displayLoginUsers() {  // 현재 접속 중인 사용자를 표시하는 메소드
        StyledDocument doc = m_memberPane.getStyledDocument();  // 스타일 정보 가져오기
        try {
            m_memberPane.setText("");  // 패널의 텍스트 초기화
            // 패널에 텍스트 출력
            doc.insertString(doc.getLength(), "현재 접속 중인 사용자\n", null);
            m_memberPane.setCaretPosition(m_memberPane.getDocument().getLength());
        } catch (BadLocationException e) {  // 에러 처리
            e.printStackTrace();
        }

        CMMember loginUsers = m_serverStub.getLoginUsers();  // 접속 중인 사용자 가져오기
        if(loginUsers == null)  {  // 접속 중인 사용자가 없을 경우
            try {  // 패널에 메시지 출력
                doc.insertString(doc.getLength(), "현재 사용 중인 사용자가 없습니다.\n", null);
                m_memberPane.setCaretPosition(m_memberPane.getDocument().getLength());
            } catch (BadLocationException e) {  // 에러 처리
                e.printStackTrace();
            }
            return;
        } else {  // 접속 중인 사용자가 있을 경우
            try {  // 패널에 메시지 출력
                doc.insertString(doc.getLength(), "현재 ["+loginUsers.getMemberNum()+"] 사용자가 접속 중입니다.\n", null);
                m_memberPane.setCaretPosition(m_memberPane.getDocument().getLength());
                Vector<CMUser> loginUserVector = loginUsers.getAllMembers();  // 접속 중인 사용자 수
                Iterator<CMUser> iter = loginUserVector.iterator();
                int nPrintCount = 0;
                while(iter.hasNext()) {
                    CMUser user = iter.next();
                    try {  // 패널에 접속 중인 사용자 출력
                        doc.insertString(doc.getLength(), user.getName()+" ", null);
                        m_memberPane.setCaretPosition(m_memberPane.getDocument().getLength());
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                    nPrintCount++;
                    if((nPrintCount % 10) == 0) {  // 접속 중인 사용자가 10명이 된 경우
                        try {  // 패널에 접속 중인 사용자 출력
                            doc.insertString(doc.getLength(), "\n", null);
                            m_memberPane.setCaretPosition(m_memberPane.getDocument().getLength());
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                        nPrintCount = 0;
                    }
                }
            } catch (BadLocationException e) {  // 에러 처리
                e.printStackTrace();
            }
        }
    }

    public void requestFile() {  // 파일 요청 메소드
        boolean bReturn = false;  // 파일 요청 이상 여부
        String strFileName = null;  // 파일 이름
        String strFileOwner = null;  // 파일 소유자
        byte byteFileAppendMode = -1;  // 파일 전송 모드

        printMessage("====== 파일 요청\n");
        JTextField fileNameField = new JTextField();  // 파일 이름 필드
        JTextField fileOwnerField = new JTextField();  // 파일 소유자 필드
        String[] fAppendMode = {"기본값", "덮어쓰기", "덧붙이기"};  // 파일 전송 모드 필드
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);  // 콤보 박스로 전송 모드 설정

        Object[] message = {  // 파일 이름, 파일 소유자, 파일 전송 모드를 담는 배열
                "파일 이름:", fileNameField,
                "파일 소유자:", fileOwnerField,
                "파일 추가 모드: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "파일 요청 입력", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION) {  // 요청 취소 시
            printMessage("취소했습니다.\n");
            return;
        }

        strFileName = fileNameField.getText().trim();  // 입력한 파일 이름에서 좌우 띄어쓰기 제거
        if(strFileName.isEmpty()) {  // 파일 이름을 입력하지 않은 경우
            printMessage("파일 이름을 입력하지 않았습니다.\n");
            return;
        }

        strFileOwner = fileOwnerField.getText().trim();  // 입력한 파일 소유자에서 좌우 띄어쓰기 제거
        if(strFileOwner.isEmpty()) {  // 파일 소유자를 입력하지 않은 경우
            printMessage("파일 소유자를 입력하지 않았습니다.\n");
            return;
        }

        switch(fAppendBox.getSelectedIndex()) {  // 파일 전송 모드
            case 0:  // 기본값을 설정
                byteFileAppendMode = CMInfo.FILE_DEFAULT;
                break;
            case 1:  // 덮어쓰기로 설정
                byteFileAppendMode = CMInfo.FILE_OVERWRITE;
                break;
            case 2:  // 덧붙이기로 설정
                byteFileAppendMode = CMInfo.FILE_APPEND;
                break;
        }

        bReturn = m_serverStub.requestFile(strFileName, strFileOwner, byteFileAppendMode);  // 파일 요청 이상 여부

        if(!bReturn)  // 파일 요청에 문제가 발생하면 에러 발생
            printMessage("파일 요청 오류. 파일("+strFileName+"), 소유자("+strFileOwner+").\n");

        printMessage("======\n");
    }

    public void pushFile() {  // 파일 전송 메소드
        String strFilePath = null;  // 파일 경로
        File[] files;  // 파일 이름
        String strReceiver = null;  // 파일 수신자
        byte byteFileAppendMode = -1;  // 파일 전송 모드
        boolean bReturn = false;  // 파일 전송 이상 여부

        JTextField freceiverField = new JTextField();  // 수신자 입력 필드
        String[] fAppendMode = {"기본값", "덮어쓰기", "덧붙이기"};  // 파일 전송 모드 필드
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);  // 콤보 박스로 파일 전송 모드 설정

        Object[] message = {  // 파일 수신자, 파일 전송 모드를 담는 배열
                "파일 수신자: ", freceiverField,
                "파일 추가 모드: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "파일 경로", JOptionPane.OK_CANCEL_OPTION);  // 파일 전송 확인 창
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION) {  // 파일 전송 취소 시
            printMessage("취소했습니다.\n");
            return;
        }

        strReceiver = freceiverField.getText().trim();  // 입력한 파일 수신자의 좌우 띄어쓰기 제거
        if(strReceiver.isEmpty()) {  // 파일 수신자를 입력하지 않은 경우
            printMessage("파일 수신자를 입력하지 않았습니다.\n");
            return;
        }

        switch(fAppendBox.getSelectedIndex()) {  // 파일 전송 모드
            case 0:  // 기본값으로 설정
                byteFileAppendMode = CMInfo.FILE_DEFAULT;
                break;
            case 1:  // 덮어쓰기로 설정
                byteFileAppendMode = CMInfo.FILE_OVERWRITE;
                break;
            case 2:  // 덧붙이기로 설정
                byteFileAppendMode = CMInfo.FILE_APPEND;
                break;
        }

        JFileChooser fc = new JFileChooser();  // 파일 선택자 생성
        fc.setMultiSelectionEnabled(true);  // 여러 파일 선택 가능하도록 설정
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();  // CM 설정 가져오기
        File curDir = new File(confInfo.getTransferedFileHome().toString());  // 파일 현재 경로 가져오기
        fc.setCurrentDirectory(curDir);  // 파일 현재 경로
        int fcRet = fc.showOpenDialog(this);  // 열기 창
        if(fcRet != JFileChooser.APPROVE_OPTION) return;
        files = fc.getSelectedFiles();  // 선택한 파일
        if(files.length < 1) return;  // 선택한 파일이 없을 경우 바로 리턴
        for(int i=0; i < files.length; i++) {  // 선택한 파일이 한 개 이상일 경우
            strFilePath = files[i].getPath();  // 각 파일마다 경로 가져오기
            bReturn = m_serverStub.pushFile(strFilePath, strReceiver, byteFileAppendMode);  // 파일 전송
            if(!bReturn) {  // 파일 전송이 실패한 경우
                printMessage("파일 전송 오류. 파일("+strFilePath+"), 수신자(" +strReceiver+").\n");
            }
        }

        printMessage("======\n");
    }

    private void openFileSyncFolder() {  // 파일 동기화 폴더 열기 메소드
        printMessage("=========== 파일 동기화 폴더 열기\n");
        String userName = JOptionPane.showInputDialog("사용자 이름:");  // 클라이언트 이름 묻기
        if(userName != null) {
            Path syncHome = m_serverStub.getFileSyncHome(userName);  // 사용자의 동기화 폴더 가져오기
            if(syncHome == null) {  // 파일 동기화 폴더가 없을 경우
                printStyledMessage("파일 동기화 폴더가 없습니다.\n", "bold");
                printStyledMessage("더 자세한 정보는 콘솔 창의 에러 메시지를 참조하세요.\n", "bold");
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(syncHome.toFile());  // 파일 동기화 폴더 열기
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void terminateCM() {  // 서버 종료 메소드
        m_serverStub.terminateCM();  // 서버 종료
        printMessage("서버를 종료합니다.\n");
        m_startStopButton.setText("서버 시작");  // 버튼 재설정
    }

    public static void main(String[] args) {
        CMServerWinApp server = new CMServerWinApp();  // CMServerApp 객체 생성
        CMServerStub cmStub = server.getServerStub();  // m_serverStub(CMServerStub 객체) 반환
        cmStub.setAppEventHandler(server.getServerEventHandler());
    }
}
