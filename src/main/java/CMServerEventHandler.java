// import kr.ac.konkuk.ccslab.cm.*;
import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;

    public CMServerEventHandler(CMServerStub serverStub) {
        m_serverStub = serverStub;
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
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        CMSessionEvent se = (CMSessionEvent) cme;
        switch(se.getID()) {
            case CMSessionEvent.LOGIN:
                System.out.println("[" + se.getUserName() + "] 로그인을 요청했습니다.");

                // 로그인 정보 확인
//                if(confInfo.isLoginScheme()) {
//                    boolean ret = CMDBManager.authenticateUser(se.getUserName(), se.getPassword(), m_serverStub.getCMInfo());
//                    if(!ret) {
//                        System.out.println("[" + se.getUserName() + "] 인증 실패");
//                        m_serverStub.replyEvent(se, 0);
//                    } else {
//                        System.out.println("[" + se.getUserName() + "] 인증 성공");
//                        m_serverStub.replyEvent(se, 1);
//                    }
//                }

                break;
            case CMSessionEvent.LOGOUT:
                System.out.println("[" + se.getUserName() + "] 접속 해제했습니다.");
                break;
            case CMSessionEvent.JOIN_SESSION:
                System.out.println("[" + se.getUserName() + "] " + se.getSessionName() + " 세션에 입장했습니다.");
                break;
            case CMSessionEvent.LEAVE_SESSION:
                System.out.println("[" + se.getUserName() + "] " + se.getSessionName() + " 세션에 퇴장했습니다.");
                break;
            default:
                return;
        }
    }
}
