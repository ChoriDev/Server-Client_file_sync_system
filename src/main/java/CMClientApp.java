import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class CMClientApp {
    private CMClientStub m_clientStub;  // CMClientStub 타입 레퍼런스 변수 m_clientStub 선언
    private CMClientEventHandler m_eventHandler;  // CMClientEventHandler 타입 레퍼런스 변수 m_eventHandler 선언

    public CMClientApp() {  // CMClientApp 생성자
        m_clientStub = new CMClientStub();  // CMClientStub 객체 생성
        m_eventHandler = new CMClientEventHandler(m_clientStub);  // CMClientEventHandler 객체 생성, CMClientStub 객체를 인자로 넘김
    }

    public CMClientStub getClientStub() {  // CMClientStub 타입 레퍼런스 변수 m_clientStub를 반환하는 메소드
        return m_clientStub;
    }

    public CMClientEventHandler getClientEventHandler() {  // CMClientEventHandler 타입 레퍼런스 변수 m_eventHandler를 반환하는 메소드
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
        String strUserName = null;
        String strPassword = null;
        boolean bRequestResult = false;
        Console console = System.console();

        System.out.print("유저 ID : ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            strUserName = br.readLine();
            if(console == null) {
                System.out.print("패스워드 : ");
                strPassword = br.readLine();
            } else {
                strPassword = new String(console.readPassword("패스워드 : "));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        bRequestResult = cmStub.loginCM(strUserName, strPassword);
        if(bRequestResult) {
            System.out.println("로그인 요청 성공");
        } else {
            System.err.println("로그인 요청 실패");
        }

        // 기존 로그인 정보 받기
//      System.out.println("=== 로그인 정보 ===");
//      System.out.print("사용자 이름을 작성하세요 : ");
//      String userID = scanner.nextLine();
//      System.out.print("패스워드를 작성하세요 : ");
//      String userPW = scanner.nextLine();
//      System.out.println("사용자 이름 : " + userID);
//      System.out.println("패스워드 : " + userPW);
//      ret = cmStub.loginCM(userID, userPW);
//
//      if(ret) {
//          System.out.println("로그인 요청 성공");
//      } else {
//          System.out.println("로그인 요청 실패");
//          return;
//      }
//
//      if("로그인 성공 시") {
//          break;
//      }
//  }

        // 실행할 다음 API 기다리기
        System.out.println("다음으로 실행할 API를 입력하세요.");
        scanner.nextLine();

        // terminate CM
//        System.out.println("CM과 서버에 접속을 종료하려면 입력하세요.");
//        scanner.nextLine();
//        cmStub.terminateCM();
    }
}