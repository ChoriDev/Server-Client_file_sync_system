import kr.ac.konkuk.ccslab.cm.*;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub; // ??? 위에 포함되는 것이 아닌지 확인 필요

public class CMServerApp {
    private CMServerStub m_serverStub;  // CMServerStub 타입 레퍼런스 변수 m_serverStub 선언
    private CMServerEventHandler m_eventHandler;  // CMServerEventHandler 타입 레퍼런스 변수 m_eventHandler 선언

    public CMServerApp() {  // CMServerApp 생성자
        m_serverStub = new CMServerStub();  // CMServerStub 객체 생성
        m_eventHandler = new CMServerEventHandler(m_serverStub);  // CMServerEventHandler 객체 생성
    }

    // CMServerStub 타입 m_serverStub을 반환하는 메소드
    public CMServerStub getServerStub() {
        return m_serverStub;
    }

    // CMServerEventHandler 타입 m_eventHandler를 반환하는 메소드
    public CMServerEventHandler getServerEventHandler() {
        return m_eventHandler;
    }

    public static void main(String[] args) {
        CMServerApp server = new CMServerApp();  // CMServerApp 객체 생성
        CMServerStub cmStub = server.getServerStub();  // CMServerApp 객체의 getServerStub 메소드 실행, m_serverStub(CMServerStub 객체) 반환
        cmStub.setAppEventHandler(server.getServerEventHandler());  // ???
        cmStub.startCM();
    }
}
