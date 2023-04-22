import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.text.*;
import kr.ac.konkuk.ccslab.cm.entity.CMMember;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientWinApp3 extends CMClientWinApp{
    private CMClientStub m_clientStub;  // CMClientStub 타입 레퍼런스 변수 m_clientStub 선언
    private CMClientWinEventHandler m_eventHandler;  // CMClientEventHandler 타입 레퍼런스 변수 m_eventHandler 선언
    private JPanel m_pnlCenter;  // Gui에 사용할 변수, 출력 메시지 패널과 접속 중인 다른 사용자 표시 패널을 담음
    private JTextPane m_outTextPane;  // Gui에 사용할 변수, 메시지를 출력할 패널
    private JTextPane m_memberPane;  // Gui에 사용할 변수, 현재 접속 중인 사용자를 출력할 패널
    private JTextField m_inTextField;  // Gui에 사용할 변수, 입력을 할 수 있는 텍스트 상자
    private JButton m_startStopButton;  // Gui에 사용할 변수, 클라이언트 시작과 종료를 할 수 있는 버튼
    private JButton m_loginLogoutButton;  // Gui에 사용할 변수, 로그인과 로그아웃을 할 수 있는 버튼

    public CMClientWinApp3() {  // CMClientApp 생성자
        // GUI 관련 설정
        MyKeyListener cmKeyListener = new MyKeyListener();  // 키 이벤트 리스너 객체 생성
        MyActionListener cmActionListener = new MyActionListener();  // 액션 이벤트 리스너 객체 생성
        setTitle("CMClientWinApp");  // 프레임의 타이틀 설정
        setSize(500, 500);  // 프레임 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 닫기 버튼을 클릭하면 프로그램 종료 설정
        setLayout(new BorderLayout());  // 배치를 BorderLayout으로 설정

        m_pnlCenter = new JPanel(new GridLayout());  // 배치를 GridLayout으로 설정
        getContentPane().add(m_pnlCenter, BorderLayout.CENTER);  // 프레임의 중앙에 배치

        m_outTextPane = new JTextPane();  // 메시지를 출력할 텍스트 패널 생성
        m_outTextPane.setBackground(new Color(236,235,227));  // 텍스트 패널의 바탕색 설정
        m_outTextPane.setEditable(false);  // 텍스트 패널은 편집이 불가하도록 설정
        JScrollPane centerScroll = new JScrollPane(m_outTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);  // 스크롤 패널 생성
        m_pnlCenter.add(centerScroll);  // pnlCenter 패널에 배치

        StyledDocument doc = m_outTextPane.getStyledDocument();  // 스타일 가져오기
        addStylesToDocument(doc);  // 스타일 적용

        m_memberPane = new JTextPane();  // 현재 접속 중인 사용자를 출력할 패널 생성
        m_memberPane.setEditable(false);  // 패널 편집이 불가능하도록 설정
        JScrollPane leftScroll = new JScrollPane(m_memberPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);  // 스크롤 패널 생성
        m_pnlCenter.add(leftScroll);  // pnlCenter 패널에 배치

        StyledDocument doc2 = m_outTextPane.getStyledDocument();  // 스타일 가져오기
        addStylesToDocument(doc2);  // 스타일 적용

        m_inTextField = new JTextField();  // 메시지를 입력할 텍스트 필드 생성
        m_inTextField.addKeyListener(cmKeyListener);  // 텍스트 필드에 키 이벤트 리스너 부착
        add(m_inTextField, BorderLayout.SOUTH);  // 텍스트 필드를 프레임의 하단에 배치

        JPanel topButtonPanel = new JPanel();  // 버튼을 담을 패널 생성
        topButtonPanel.setBackground(new Color(3,107,63));  // 패널의 배경색 설정
        topButtonPanel.setLayout(new FlowLayout());  // 패널의 배치를 FlowLayout으로 설정
        add(topButtonPanel, BorderLayout.NORTH);  // 패널을 프레임의 상단에 배치

        m_startStopButton = new JButton("클라이언트 시작");  // 클라이언트 작동 버튼 생성
        m_startStopButton.addActionListener(cmActionListener);  // 버튼에 액션 리스너 부착
        m_startStopButton.setEnabled(true);  // 버튼 활성화
        topButtonPanel.add(m_startStopButton);  // 버튼을 담을 패널에 버튼 추가

        m_loginLogoutButton = new JButton("로그인");  // 로그인 버튼 생성
        m_loginLogoutButton.addActionListener(cmActionListener);  // 버튼에 액션 리스너 부착
        m_loginLogoutButton.setEnabled(true);  // 버튼 활성화
        topButtonPanel.add(m_loginLogoutButton);  // 버튼을 담을 패널에 버튼 추가

        setVisible(true);  // 프레임 출력

        m_clientStub = new CMClientStub();  // CMClientStub 객체 생성
        m_eventHandler = new CMClientWinEventHandler(m_clientStub, this);  // CMClientWinEventHandler 객체 생성

        testStartCM();  // 프로그램을 실행하면 자동으로 클라이언트 시작

        m_inTextField.requestFocus(); // 텍스트 입력 상자에 포커스 설정
    }

    public class MyKeyListener implements KeyListener {  // 키 이벤트에 관한 리스너
        public void keyPressed(KeyEvent e) {  // 키가 눌렸을 때의 작동
            int key = e.getKeyCode();  // 어떤 키가 눌렸는지 정보 저장
            if(key == KeyEvent.VK_ENTER) { // 눌린 키가 enter일 경우
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

    public class MyActionListener implements ActionListener {  // 액션 이벤트에 관한 리스너
        public void actionPerformed(ActionEvent e) { // 액션을 감지했을 때 작동
            JButton button = (JButton)e.getSource();  // 클릭된 버튼의 정보 저장
            if(button.getText().equals("클라이언트 시작")) {  // 클릭된 버튼이 "클라이언트 시작"일 경우
                testStartCM();  // CM 시작
            }
            else if(button.getText().equals("클라이언트 종료")) {  // 클릭된 버튼이 "클라이언트 종료"일 경우
                testTerminateCM();  // CM 종료
            }
            else if(button.getText().equals("로그인")) {  // 클릭된 버튼이 "로그인"일 경우
                testSyncLoginDS();  // 서버에 동기식으로 로그인
            }
            else if(button.getText().equals("로그아웃")) {  // 클릭된 버튼이 "로그아웃"일 경우
                testLogoutDS();  // 서버에서 로그아웃
            }
            m_inTextField.requestFocus();  // 텍스트 필드에 포커스 설정
        }
    }

    private void addStylesToDocument(StyledDocument doc) {  // 스타일 설정 메소드
        Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);  //  기본 스타일 가져오기

        Style regularStyle = doc.addStyle("regular", defStyle);  // 일반 스타일
        StyleConstants.setFontFamily(regularStyle, "SansSerif");  // 글꼴은 산세리프

        Style boldStyle = doc.addStyle("bold", defStyle);  // 볼드 스타일
        StyleConstants.setBold(boldStyle, true);  // 글씨 굵게

        Style linkStyle = doc.addStyle("link", defStyle);  // 링크 스타일
        StyleConstants.setForeground(linkStyle, Color.BLUE);  // 글씨색을 파란색으로
        StyleConstants.setUnderline(linkStyle, true);  // 글씨에 밑줄
    }

    public CMClientStub getClientStub() {  // CMClientStub 타입 레퍼런스 변수 m_clientStub를 반환
        return m_clientStub;
    }

    public CMClientWinEventHandler getClientEventHandler() {  // CMClientWinEventHandler 타입 레퍼런스 변수 m_eventHandler를 반환
        return m_eventHandler;
    }

    private void initializeButtons() {  // 클라이언트 시작 시 버튼 초기화
        m_startStopButton.setText("클라이언트 시작");  // 시작, 종료 버튼을 "클라이언트 시작"으로 설정
        m_loginLogoutButton.setText("로그인");  // 로그인, 로그아웃 버튼을 "로그인"으로 설정
        revalidate();  // 컴포넌트 재배치
        repaint();  // 컴포넌트 다시 그리기
    }

    public void setButtonsAccordingToClientState() { // 클라이언트 상태에 따라 버튼 설정
        int nClientState;  // 클라이언트 상태를 담는 변수
        nClientState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();  // 클라이언트 정보 가져오기

        // 클라이언트 상태가 CMInfo.CM_INIT, CMInfo.CM_CONNECT, CMInfo.CM_LOGIN, CMInfo.CM_SESSION_JOIN일 경우
        switch(nClientState) {
            case CMInfo.CM_INIT:  // CM_INIT인 경우(CM을 새로 시작할 경우)
                m_startStopButton.setText("클라이언트 시작");  // 시작, 종료 버튼을 "클라이언트 시작"으로 설정
                m_loginLogoutButton.setText("로그인");  // 로그인, 로그아웃 버튼을 "로그인"으로 설정
                break;
            case CMInfo.CM_CONNECT:  // CM_CONNECT인 경우(클라이언트가 접속된 경우)
                m_startStopButton.setText("클라이언트 종료");  // 시작, 종료 버튼을 "클라이언트 종료"로 설정
                m_loginLogoutButton.setText("로그인");  // 로그인, 로그아웃 버튼을 "로그인"으로 설정
                break;
            case CMInfo.CM_LOGIN:  // CM_LOGIN인 경우(클라이언트가 로그인한 경우)
                m_startStopButton.setText("클라이언트 종료");  // 시작, 종료 버튼을 "클라이언트 종료"로 설정
                m_loginLogoutButton.setText("로그아웃");  // 로그인, 로그아웃 버튼을 "로그아웃"으로 설정
                break;
            case CMInfo.CM_SESSION_JOIN:  // CM_SESSION_JOIN인 경우(클라이언트가 세션에 접속한 경우)
                m_startStopButton.setText("클라이언트 종료");  // 시작, 종료 버튼을 "클라이언트 종료"로 설정
                m_loginLogoutButton.setText("로그아웃");  // 로그인, 로그아웃 버튼을 "로그아웃"으로 설정
                break;
            default:  // 위의 경우에 해당하지 않는 경우
                m_startStopButton.setText("클라이언트 시작");  // 시작, 종료 버튼을 "클라이언트 시작"으로 설정
                m_loginLogoutButton.setText("로그인");  // 로그인, 로그아웃 버튼을 "로그인"으로 설정
                break;
        }
        revalidate();  // 컴포넌트 재배치
        repaint();  // 컴포넌트 다시 그리기
    }

    public void displayMember() {  // 현재 접속 중인 사용자를 표시하는 메소드
        StyledDocument doc = m_memberPane.getStyledDocument();  // 스타일 정보 가져오기

        try {
            m_memberPane.setText("");  // 패널의 텍스트 초기화
            //  패널에 텍스트 출력
            doc.insertString(doc.getLength(), "현재 접속 중인 다른 사용자\n", null);
            m_memberPane.setCaretPosition(m_memberPane.getDocument().getLength());
        } catch (BadLocationException e) {  // 에러 처리
            e.printStackTrace();
        }

        CMMember groupMembers = m_clientStub.getGroupMembers();  // 접속 중인 사용자 가져오기
        if(groupMembers == null || groupMembers.isEmpty()) {  // 다른 사용자가 없을 경우
            try {  // 패널에 메시지 출력
                doc.insertString(doc.getLength(), "다른 사용자가 없습니다.\n", null);
                m_memberPane.setCaretPosition(m_memberPane.getDocument().getLength());
            } catch (BadLocationException e) {  // 에러 처리
                e.printStackTrace();
            }
            return;
        } else {  // 다른 사용자가 있을 경우
            try {  // 패널에 메시지 출력
                doc.insertString(doc.getLength(), groupMembers.toString()+"\n", null);
                m_memberPane.setCaretPosition(m_memberPane.getDocument().getLength());

            } catch (BadLocationException e) {  // 에러 처리
                e.printStackTrace();
            }
        }
    }

    public void clearMember() {  // 접속 중인 사용자 표시 패널 초기화
        m_memberPane.setText("");
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
        StyledDocument doc = m_outTextPane.getStyledDocument();  // 스타일 정보 가져오기
        try {  // 출력 텍스트 패널에 메시지 출력
            doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
            m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());
        } catch (BadLocationException e) {  // 에러 처리
            e.printStackTrace();
        }

        return;
    }

    private void processInput(String strInput) { // 입력한 값에 따라 각각의 기능을 호출하는 메소드
        int nCommand = -1;  // 입력한 값
        try {
            nCommand = Integer.parseInt(strInput);  // 입력한 값을 정수로 변환
        } catch (NumberFormatException e) {  // 기능을 수행할 수 있는 입력이 아닌 경우 에러 처리
            printMessage("알 수 없는 번호입니다.\n");
            return;
        }

        switch (nCommand) {  // 입력한 값에 따라 아래의 기능을 호출
            case 0:  // 사용 가능한 모든 기능 표시
                printAllMenus();
                break;
            case 100:  // 클라이언트 시작
                testStartCM();
                break;
            case 999:  // 클라이언트 종료
                testTerminateCM();
                break;
            case 1: // 기본 서버에 접속
                testConnectionDS();
                break;
            case 2: // 기본 서버에 접속 해제
                testDisconnectionDS();
                break;
            case 11: // 기본 서버에 동기식으로 로그인
                testSyncLoginDS();
                break;
            case 12: // 기본 서버에 로그아웃
                testLogoutDS();
                break;
            case 42: // 간단한 메시지 보내기
                testDummyEvent();
                break;
            case 71: // 파일 요청
                testRequestFile();
                break;
            case 72: // 파일 전송
                testPushFile();
                break;
            default:
                printMessage("없는 번호입니다.");
                break;
        }
    }

    private void printAllMenus() {  // 사용할 수 있는 기능과 입력할 값 안내
        printMessage("---------------------------------- 도움말\n");
        printMessage("0: 모든 메뉴 보기\n");
        printMessage("---------------------------------- 시작/종료\n");
        printMessage("100: CM 시작, 999: CM 종료\n");
        printMessage("---------------------------------- 연결\n");
        printMessage("1: 기본 서버에 접속, 2: 기본 서버에 접속 해제\n");
        printMessage("---------------------------------- 로그인\n");
        printMessage("11: 기본 서버에 동기식으로 로그인\n");
        printMessage("12: 기본 서버에 로그아웃\n");
        printMessage("---------------------------------- Event 전송\n");
        printMessage("42: 간단한 메시지 보내기\n");
        printMessage("---------------------------------- 파일 전송\n");
        printMessage(/*"70: set file path,*/ "71: 파일 요청, 72: 파일 전송\n");
    }

    private void testStartCM() {  // 클라이언트 시작 메소드
        boolean bRet = m_clientStub.startCM();  // 시작 여부
        if(!bRet) {  // 정상적으로 클라이언트 시작이 안될 시 에러 발생
            printStyledMessage("CM 초기화 오류.\n", "bold");
        } else {  // 정상적으로 클라이언트 시작 시
            printStyledMessage("클라이언트 시작\n", "bold");
            printStyledMessage("메뉴를 보려면 \"0\"을 입력하세요.\n", "regular");
            setButtonsAccordingToClientState();  // 클라이언트 상태에 맞게 버튼 설정
        }
    }

    public void testConnectionDS() {  // 서버 접속 메소드
        printMessage("====== 기본 서버에 접속합니다.\n");
        boolean ret = m_clientStub.connectToServer();  // 접속 여부
        if(ret) {  // 접속에 성공했을 경우
            printMessage("성공적으로 기본 서버에 접속했습니다.\n");
        } else {  // 접속에 실패했을 경우
            printMessage("기본 서버에 접속할 수 없습니다.\n");
        }
        printMessage("======\n");

        setButtonsAccordingToClientState();  // 클라이언트 상태에 맞게 버튼 설정
    }

    public void testDisconnectionDS() {  // 서버 접속 해제 메소드
        printMessage("====== 기본 서버에서 접속을 해제합니다.\n");
        boolean ret = m_clientStub.disconnectFromServer();  // 접속 해제 여부
        if(ret) {  // 접속 해제에 성공했을 경우
            printMessage("성공적으로 기본 서버에서 접속을 해제했습니다.\n");
        } else {  // 접속 해제에 실패했을 경우
            printMessage("기본 서버에서 접속을 해제하던 도중 오류가 발생했습니다.\n");
        }
        printMessage("======\n");

        setButtonsAccordingToClientState();  // 클라이언트 상태에 맞게 버튼 설정
        setTitle("CMClientWinApp");  // 프레임의 타이틀을 재설정
    }

    private void testSyncLoginDS() {  // 동기식으로 로그인하는 메소드
        String strUserName = null;  // 사용자 ID
        String strPassword = null;  // 사용자 PW
        CMSessionEvent loginAckEvent = null;  // 인증 여부

        printMessage("====== 기본 서버에 동기식으로 로그인합니다.\n");
        JTextField userNameField = new JTextField();  // 사용자 ID를 받는 필드 생성
        JPasswordField passwordField = new JPasswordField();  // 사용자 PW를 받는 필드 생성
        Object[] message = {  // 필드에 입력한 값을 배열로 저장
                "사용자 이름:", userNameField,
                "비밀번호:", passwordField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "로그인 입력", JOptionPane.OK_CANCEL_OPTION);  // 로그인을 하는 창
        if (option == JOptionPane.OK_OPTION) {  // 로그인을 하는 창에서 OK 클릭 시
            strUserName = userNameField.getText();  // 로그인을 하는 창에서 사용자 ID 가져오기
            strPassword = new String(passwordField.getPassword()); // 로그인을 하는 창에서 사용자 PW 가져오기

            m_eventHandler.setStartTime(System.currentTimeMillis());  // 접속 시작 시작 설정
            loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);  // 인증된 사용자 여부
            if(loginAckEvent != null) {  // 로그인 요청에 성공한 경우
                if(loginAckEvent.isValidUser() == 0) {  // 인증되지 않은 사용자인 경우
                    printMessage("기본 서버에 의해 인증에 실패했습니다.\n");
                } else if(loginAckEvent.isValidUser() == -1) {  // 이미 로그인되어 있는 경우
                    printMessage("이미 로그인되어 있습니다.\n");
                } else {  // 로그인에 성공한 경우
                    printMessage("성공적으로 기본 서버에 로그인했습니다.\n");
                    CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();

                    setTitle("CMClientWinApp ("+interInfo.getMyself().getName()+")");  // 프레임의 타이틀 재설정

                    setButtonsAccordingToClientState();  // 클라이언트 상태에 맞게 버튼 재설정
                }
            } else {  // 로그인 요청에 실패한 경우
                printStyledMessage("로그인 요청에 실패했습니다.\n", "bold");
            }
        }

        printMessage("======\n");
    }

    private void testLogoutDS() {  // 로그아웃 메소드
        boolean bRequestResult = false;  // 요청 결과
        printMessage("====== 기본 서버에 로그아웃\n");
        bRequestResult = m_clientStub.logoutCM(); // 로그아웃 요청 결과 저장
        if(bRequestResult) {  // 로그아웃 요청에 성공한 경우
            printMessage("성공적으로 로그아웃 요청을 보냈습니다.\n");
            clearMember();  // 접속 중인 사용자를 표시하는 패널 초기화
            setButtonsAccordingToClientState();  // 클라이언트 상태에 맞게 버튼 재설정
            setTitle("CMClientWinApp");  // 프레임의 타이틀 재설정
        }
        else {  // 로그아웃 요청에 실패한 경우
            printStyledMessage("로그아웃 요청이 실패했습니다.\n", "bold");
        }
        printMessage("======\n");
    }

    public void testDummyEvent() {  // 간단한 메시지를 보내는 메소드
        String strMessage = null;  // 메시지 내용
        String strTarget = null;  // 메시지 수신자
        CMDummyEvent due = null;  // 이벤트

        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        CMUser myself = interInfo.getMyself();  //송신자 정보 가져오기

        if(myself.getState() != CMInfo.CM_SESSION_JOIN) {  // 세션에 접속하지 않은 경우
            printMessage("세션과 그룹에 속해있어야 합니다.\n");
            return;
        }

        printMessage("====== 현재 그룹에서 간단한 메시지 보내기\n");

        JTextField messageField = new JTextField();  // 메시지 내용을 담는 필드
        JTextField targetField = new JTextField();  // 메시지 수신자를 담는 필드

        Object[] msg = {  // 메시지 내용과 메시지 수신자를 배열로 저장
                "메시지: ", messageField,
                "수신 사용자 (send() 메소드 사용 시): ", targetField,
        };
        int option = JOptionPane.showConfirmDialog(null, msg, "간단한 메시지 전송",
                JOptionPane.OK_CANCEL_OPTION);  // 메시지 전송 창
        if(option == JOptionPane.OK_OPTION) {  // 메시지 전송 창에서 OK를 눌렀을 경우
            strMessage = messageField.getText().trim();  // 입력한 메시지에서 좌우 띄어쓰기는 제거
            strTarget = targetField.getText().trim();  // 입력한 수신자에서 좌우 띄어쓰기는 제거

            if(strMessage.isEmpty()) {  // 입력한 메시지가 없는 경우
                printStyledMessage("메시지를 입력하지 않았습니다.\n", "bold");
                return;
            }

            // 이벤트 처리
            due = new CMDummyEvent();
            due.setDummyInfo(strMessage);
            due.setHandlerSession(myself.getCurrentSession());
            due.setHandlerGroup(myself.getCurrentGroup());

            if(!strTarget.isEmpty()) {  // 수신자를 입력한 경우
                m_clientStub.send(due, strTarget);  // 메시지 전송
            }
            printMessage("======\n");
        }
    }

    public void testDummyEvent(String constMsg, String fileSender)  // 파일 수신 완료 메시지 전달용으로 testDummyEvent() 오버로딩
    {
        String strMessage = constMsg;  // 파일 수신 완료 메시지
        String strTarget = fileSender;  // 파일 송신자
        CMDummyEvent due = null;  // 이벤트

        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        CMUser myself = interInfo.getMyself();  // 사용자 정보 저장

        // 이벤트 처리
        due = new CMDummyEvent();
        due.setDummyInfo(strMessage);
        due.setHandlerSession(myself.getCurrentSession());
        due.setHandlerGroup(myself.getCurrentGroup());

        m_clientStub.send(due, strTarget);  // 수신 완료 메시지 전달
    }

    private void testRequestFile() {  // 파일 요청 메소드
        boolean bReturn = false;  // 파일 요청 이상 여부
        String strFileName = null;  // 파일 이름
        String strFileOwner = null;  // 파일 소유자
        byte byteFileAppendMode = -1;  // 파일 전송 모드
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();

        printMessage("====== 파일 요청\n");

        JTextField fnameField = new JTextField();  // 파일 이름 필드
        JTextField fownerField = new JTextField();  // 파일 소유자 필드
        String[] fAppendMode = {"기본값", "덮어쓰기", "덧붙이기"};  // 파일 전송 모드 필드
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);  // 콤보 박스로 전송 모드 설정

        Object[] message = {  // 파일 이름, 파일 소유자, 파일 전송 모드를 담는 배열
                "파일 이름: ", fnameField,
                "파일 소유자(공백은 기본 서버): ", fownerField,
                "파일 추가 모드: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "파일 요청", JOptionPane.OK_CANCEL_OPTION);  // 요청 확인 다이얼로그
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION) {  // 요청 취소 시
            printMessage("취소했습니다.\n");
            return;
        }

        strFileName = fnameField.getText().trim();  // 입력한 파일 이름에서 좌우 띄어쓰기 제거
        if(strFileName.isEmpty()) {  // 파일 이름을 입력하지 않은 경우
            printMessage("파일 이름을 입력하지 않았습니다.\n");
            return;
        }
        strFileOwner = fownerField.getText().trim();  // 입력한 파일 소유자에서 좌우 띄어쓰기 제거
        if(strFileOwner.isEmpty())  // 파일 소유자를 입력하지 않은 경우
            strFileOwner = interInfo.getDefaultServerInfo().getServerName();  // 소유자를 서버로 설정

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

        bReturn = m_clientStub.requestFile(strFileName, strFileOwner, byteFileAppendMode);  // 파일 요청 이상 여부

        if(!bReturn)  // 파일 요청에 문제가 발생하면 에러 발생
            printMessage("파일 요청 오류. 파일("+strFileName+"), 소유자("+strFileOwner+").\n");

        printMessage("======\n");
    }

    private void testPushFile() {  // 파일 전송 메소드
        String strFilePath = null;  // 파일 경로
        File[] files = null;  // 파일 이름
        String strReceiver = null;  // 파일 수신자
        byte byteFileAppendMode = -1;  // 파일 전송 모드
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        boolean bReturn = false;  // 파일 전송 이상 여부

        printMessage("====== 파일 전송\n");

        JTextField freceiverField = new JTextField();  // 수신자 입력 필드
        String[] fAppendMode = {"기본값", "덮어쓰기", "덧붙이기"};  // 파일 전송 모드 필드
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);  // 콤보 박스로 파일 전송 모드 설정

        Object[] message = {  // 파일 수신자, 파일 전송 모드를 담는 배열
                "파일 수신자(공백은 기본 서버): ", freceiverField,
                "파일 추가 모드: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "파일 전송", JOptionPane.OK_CANCEL_OPTION);  // 파일 전송 확인 창
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION) {  // 파일 전송 취소 시
            printMessage("취소했습니다.\n");
            return;
        }

        strReceiver = freceiverField.getText().trim();  // 입력한 파일 수신자의 좌우 띄어쓰기 제거
        if(strReceiver.isEmpty())  // 파일 수신자를 입력하지 않은 경우
            strReceiver = interInfo.getDefaultServerInfo().getServerName();  // 파일 수신자를 서버로 설정

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
        CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();  // CM 설정 가져오기
        File curDir = new File(confInfo.getTransferedFileHome().toString());  // 파일 현재 경로 가져오기
        fc.setCurrentDirectory(curDir);  // 파일 현재 경로
        int fcRet = fc.showOpenDialog(this);  // 열기 창
        if(fcRet != JFileChooser.APPROVE_OPTION) return;
        files = fc.getSelectedFiles();  // 선택한 파일
        if(files.length < 1) return;  // 선택한 파일이 없을 경우 바로 리턴
        for(int i=0; i < files.length; i++) {  // 선택한 파일이 한 개 이상일 경우
            strFilePath = files[i].getPath();  // 각 파일마다 경로 가져오기
            bReturn = m_clientStub.pushFile(strFilePath, strReceiver, byteFileAppendMode);  // 파일 전송
            if(!bReturn) {  // 파일 전송이 실패한 발생한 경우
                printMessage("파일 전송 오류 파일("+strFilePath+"), 수신자(" +strReceiver+")\n");
            }
        }

        printMessage("======\n");
    }

    public void testTerminateCM() {  // 클라이언트 종료 메소드
        clearMember();  // 접속 중인 사용자 표시 패널 초기화
        m_clientStub.terminateCM();  // 클라이언트 종료
        printMessage("클라이언트 종료\n");
        initializeButtons();  // 버튼 재설정
        setTitle("CMClientWinApp");  // 프레임 타이틀 재설정
    }

    public static void main(String[] args) {
        CMClientWinApp client = new CMClientWinApp();  // CMClientWinApp 객체 생성
        CMClientStub cmStub = client.getClientStub();  // m_serverStub(CMServerStub 객체) 반환
        cmStub.setAppEventHandler(client.getClientEventHandler());
    }
}