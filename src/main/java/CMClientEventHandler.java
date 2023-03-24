import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientEventHandler implements CMAppEventHandler {  // ??? CMAppEventHandler에게 상속받는 게 맞는지 확인하기
    private CMClientStub m_clientStub;  // CMClientStub 타입 레퍼런스 변수 m_clientStub 선언
    public CMClientEventHandler(CMClientStub stub) {  // CMClientEventHandler 생성자
        m_clientStub = stub;  // 인자로 넘어온 CMClientStub 객체를 변수 m_clientStub에 할당 
    }

    @Override
    public void processEvent(CMEvent cme) {  // event를 받는 processEvent 메소드 오버라이드
        switch(cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:  // 로그인 이벤트의 경우
                processSessionEvent(cme);  // 로그인 이벤트 실행
                break;
            case CMInfo.CM_DATA_EVENT:  // 그룹 이벤트의 경우
                processDataEvent(cme);  // 그룹 이벤트 실행
                break;
            default:
                return;
        }
    }

    private void processSessionEvent(CMEvent cme) {
        CMSessionEvent se = (CMSessionEvent)cme;
        switch (se.getID()) {
            case CMSessionEvent.LOGIN_ACK:
                if(se.isValidUser() == 0) {
                    System.err.println("서버에 의해 인증이 실패되었습니다.");
                    // 로그인 실패 시 다시 로그인할 수 있는 방법 찾기
                }
                else if (se.isValidUser() == -1) {
                    System.err.println("이미 로그인되어 있습니다.");
                    // 로그인 실패 시 다시 로그인할 수 있는 방법 찾기
                }
                else {
                    System.out.println("서버에 성공적으로 로그인했습니다.");
                }
                break;
            case CMSessionEvent.SESSION_ADD_USER:_USER:
            System.out.println("[" + se.getUserName() + "] 접속했습니다.");
                break;
            case CMSessionEvent.SESSION_REMOVE_USER:_USER:
                System.out.println("[" + se.getUserName() + "] 접속 해제했습니다.");
                break;
            default:
                return;
        }
    }

    private void processDataEvent(CMEvent cme)
    {
        CMDataEvent de = (CMDataEvent) cme;
        switch(de.getID())
        {
            case CMDataEvent.NEW_USER:
                System.out.println("[" + de.getUserName() + "] " + de.getHandlerSession() + " 세션의 " + de.getHandlerGroup() + " 그룹에 입장했습니다.");
                break;
            case CMDataEvent.REMOVE_USER:
                System.out.println("[" + de.getUserName() + "] " + de.getHandlerSession() + " 세션의 " + de.getHandlerGroup() + " 그룹에 퇴장했습니다.");
                break;
            default:
                return;
        }
    }
}
