import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientEventHandler implements CMAppEventHandler {  // ??? CMAppEventHandler에게 상속받는 게 맞는지 확인하기
    private CMClientStub m_clientStub;
    public CMClientEventHandler(CMClientStub stub) {
        m_clientStub = stub;
    }

    @Override
    public void processEvent(CMEvent cme) {
        switch(cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
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
                }
                else if (se.isValidUser() == -1) {
                    System.err.println("이미 로그인되어 있습니다.");
                }
                else {
                    System.out.println("서버에 성공적으로 로그인했습니다.");
                }
                break;
            default:
                return;
        }
    }
}
