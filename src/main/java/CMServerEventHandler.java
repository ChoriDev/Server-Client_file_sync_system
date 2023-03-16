import kr.ac.konkuk.ccslab.cm.*;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;

    public CMServerEventHandler(CMServerStub serverStub) {
        m_serverStub = serverStub;
    }

    @Override
    public void processEvent(CMEvent cme)
}
