import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.util.Scanner;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private CMClientEventHandler m_eventHandler;

    public CMClientApp() {
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler(m_clientStub);
    }

    public CMClientStub getClientStub() {
        return m_clientStub;
    }

    public CMClientEventHandler getClientEventHandler() {
        return m_eventHandler;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        CMClientApp client = new CMClientApp();
        CMClientStub cmStub = client.getClientStub();
        CMClientEventHandler eventHandler = client.getClientEventHandler();
        boolean ret = false;

        // initialize CM
        cmStub.setAppEventHandler(client.getClientEventHandler());
        ret = cmStub.startCM();

        if(ret) {
            System.out.println("초기화 성공");
        } else {
            System.err.println("초기화 오류");
            return;
        }

        // login CM server
        System.out.println("=== 로그인 정보 ===");
        System.out.print("사용자 이름을 작성하세요 : ");
        String userID = scanner.nextLine();
        System.out.print("패스워드를 작성하세요 : ");
        String userPW = scanner.nextLine();
        System.out.println("사용자 이름 : " + userID);
        System.out.println("패스워드 : " + userPW);
        ret = cmStub.loginCM(userID, userPW);

        if(ret) {
            System.out.println("로그인 요청 성공");
        } else {
            System.out.println("로그인 요청 실패");
            return;
        }

        // terminate CM
        System.out.println("CM과 서버에 접속을 종료하려면 입력하세요.");
        scanner.nextLine();
        cmStub.terminateCM();
    }
}