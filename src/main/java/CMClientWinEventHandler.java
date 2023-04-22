import javax.swing.JOptionPane;
import java.io.*;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientWinEventHandler implements CMAppEventHandler {
    private CMClientWinApp m_client;  // CMClientWinapp 타입 레퍼런스 변수 m_client 선언
    private CMClientStub m_clientStub;  // CMClientStub 타입 레퍼런스 변수 m_clientStub 선언
    private boolean m_bDistFileProc;  // 분산 파일에 사용할 변수
    private int m_nCurrentServerNum;  // 분산 파일에 사용할 변수
    private String[] m_filePieces;  // 분산 파일에 사용할 변수
    private int m_nRecvPieceNum;  // 분산 파일에 사용할 변수
    private String m_strExt;  // 분산 파일에 사용할 변수
    private long m_lStartTime;  // 분산 파일에 사용할 변수

    public CMClientWinEventHandler(CMClientStub clientStub, CMClientWinApp client) {  // CMClientWinEventHandler 생성자
        // 변수 초기화
        m_client = client;
        m_clientStub = clientStub;
        m_lStartTime = 0;
        m_nCurrentServerNum = 0;
        m_nRecvPieceNum = 0;
        m_bDistFileProc = false;
        m_strExt = null;
        m_filePieces = null;
    }

    public void setStartTime(long time) {  // 시작 시간 설정 메소드
        m_lStartTime = time;
    }

    @Override
    public void processEvent(CMEvent cme) {  // event를 받는 processEvent 메소드 오버라이드
        switch(cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:  // 로그인 이벤트의 경우
                processSessionEvent(cme);  // 로그인 이벤트 메소드 실행
                break;
            case CMInfo.CM_DATA_EVENT:  // 그룹 이벤트의 경우
                processDataEvent(cme);  // 그룹 이벤트 메소드 실행
                break;
            case CMInfo.CM_DUMMY_EVENT:  // 메시지 전송 이벤트의 경우
                processDummyEvent(cme);  // 메시지 전송 이벤트 메소드 실행
                break;
            case CMInfo.CM_FILE_EVENT:  // 파일 이벤트의 경우
                processFileEvent(cme);  // 파일 이벤트 메소드
                break;
            default:
                return;
        }
    }

    private void processSessionEvent(CMEvent cme) {  // 로그인 이벤트 관련 메소드
        CMSessionEvent se = (CMSessionEvent)cme;
        switch (se.getID()) {
            case CMSessionEvent.SESSION_ADD_USER:_USER:  // 서버에 접속한 경우
                printMessage("[" + se.getUserName() + "] 서버에 접속했습니다.\n");
                break;
            case CMSessionEvent.SESSION_REMOVE_USER:_USER:  // 서버에 접속 해제한 경우
                printMessage("[" + se.getUserName() + "] 서버에 접속 해제했습니다.\n");
                break;
            case CMSessionEvent.CHANGE_SESSION:  // 서버를 바꾼 경우
                printMessage("[" + se.getUserName() + "] " + se.getSessionName() + " 세션에 접속했습니다.\n");
                break;
            default:
                return;
        }
    }

    private void processDataEvent(CMEvent cme) {  // 그룹 관련 메소드
        CMDataEvent de = (CMDataEvent) cme;
        switch(de.getID()) {
            case CMDataEvent.NEW_USER:  // 그룹에 새로운 사용자가 입장한 경우
                printMessage("[" + de.getUserName() + "] " + de.getHandlerSession() + " 세션의 " + de.getHandlerGroup() + " 그룹에 입장했습니다.\n");
                m_client.displayMember();  // 접속한 사용자 표시 패널 재설정
                break;
            case CMDataEvent.REMOVE_USER:  // 그룹에 사용자가 퇴장한 경우
                printMessage("[" + de.getUserName() + "] " + de.getHandlerSession() + " 세션의 " + de.getHandlerGroup() + " 그룹에 퇴장했습니다.\n");
                m_client.displayMember();  // 접속한 사용자 표시 패널 재설정
                break;
            default:
                return;
        }
    }

    private void processDummyEvent(CMEvent cme) {  // 메시지 전송 관련 메소드
        CMDummyEvent due = (CMDummyEvent) cme;
        printMessage("세션(" + due.getHandlerSession() + "), 그룹(" + due.getHandlerGroup() + "), 유저(" + due.getSender() + ")가 메시지를 보냈습니다.\n");
        printMessage("메시지: " + due.getDummyInfo() + "\n");
        return;
    }

    private void processFileEvent(CMEvent cme) {  // 파일 전송 관련 메소드
        CMFileEvent fe = (CMFileEvent) cme;
        int nOption = -1;
        switch(fe.getID()) {
            case CMFileEvent.REQUEST_PERMIT_PULL_FILE:  // 파일을 요청을 받은 경우
                String strReq = "["+fe.getFileReceiver()+"] 수신자가 ("+fe.getFileName()+ ") 파일을 요청했습니다.\n";
                printMessage(strReq);
                nOption = JOptionPane.showConfirmDialog(null, strReq, "파일 요청", JOptionPane.YES_NO_OPTION);
                if(nOption == JOptionPane.YES_OPTION) {  // 파일 요청 허가
                    m_clientStub.replyEvent(fe, 1);
                }
                else {  // 파일 요청 거부
                    m_clientStub.replyEvent(fe, 0);
                }
                break;
            case CMFileEvent.REPLY_PERMIT_PULL_FILE:  // 파일 요청 응답을 받는 경우
                if(fe.getReturnCode() == -1) {  // 요청한 파일에 소유자에게 없을 경우
                    printMessage("["+fe.getFileName()+"] 소유자에게 파일이 없습니다.\n");
                }
                else if(fe.getReturnCode() == 0) {  // 파일 소유자가 파일 전송을 거부한 경우
                    printMessage("["+fe.getFileSender()+"] 송신자가 (" + fe.getFileName() + ") 파일 전송을 거부했습니다.\n");
                }
                break;
            case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:  // 파일 전송 요청을 받은 경우
                StringBuffer strReqBuf = new StringBuffer();
                strReqBuf.append("["+fe.getFileSender()+"] 송신자가 파일을 보내려 합니다.\n");
                strReqBuf.append("파일 경로: "+fe.getFilePath()+"\n");
                strReqBuf.append("파일 크기: "+fe.getFileSize()+"\n");
                printMessage(strReqBuf.toString());
                nOption = JOptionPane.showConfirmDialog(null, strReqBuf.toString(), "파일 전송", JOptionPane.YES_NO_OPTION);
                if(nOption == JOptionPane.YES_OPTION) {  // 파일 전송 요청 허가
                    m_clientStub.replyEvent(fe, 1);
                }
                else {
                    m_clientStub.replyEvent(fe, 0);  // 파일 전송 요청 거부
                }
                break;
            case CMFileEvent.REPLY_PERMIT_PUSH_FILE:  // 파일 전송 요청 응답을 받는 경우
                if(fe.getReturnCode() == 0) {  // 파일 수신을 거부한 경우
                    printMessage("["+fe.getFileReceiver()+"] 수신자가 파일 수신을 거부했습니다.\n");
                    printMessage("파일 경로("+fe.getFilePath()+"), 파일 크기("+fe.getFileSize()+").\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:  // 파일 전송을 시작한 경우
            case CMFileEvent.START_FILE_TRANSFER_CHAN:
                printMessage("["+fe.getFileSender()+"] 송신자가 파일을 보냅니다. ("+fe.getFileName()+").\n");
                break;
            case CMFileEvent.END_FILE_TRANSFER:  // 파일 전송이 끝난 경우
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                printMessage("["+fe.getFileSender()+"] 송신자가 파일 전송을 완료했습니다. ("+fe.getFileName()+", " +fe.getFileSize()+" Bytes).\n");

                m_client.testDummyEvent("수신자가 파일 수신을 완료했습니다.", fe.getFileSender());  // 수신자가 파일을 받았는지 메시지 출력

                if(m_bDistFileProc)
                    processFile(fe.getFileName());
                break;
            case CMFileEvent.CANCEL_FILE_SEND:  // 파일 전송을 취소한 경우
            case CMFileEvent.CANCEL_FILE_SEND_CHAN:
                printMessage("["+fe.getFileSender()+"] 파일 전송을 취소했습니다.\n");
                break;
            case CMFileEvent.CANCEL_FILE_RECV_CHAN:  // 파일 요청을 취소한 경우
                printMessage("["+fe.getFileReceiver()+"] 파일 요청을 취소했습니다.\n");
                break;
        }
        return;
    }

    private void processFile(String strFile) {  // 파일 처리 메소드
        CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
        String strMergeName = null;

        // 리스트에 수정된 파일 이름 추가
        if(m_nCurrentServerNum == 1) {
            m_filePieces[m_nRecvPieceNum++] = confInfo.getTransferedFileHome().toString()+ File.separator+strFile;
        }
        else {
            int nStartIndex = strFile.lastIndexOf("-")+1;
            int nEndIndex = strFile.lastIndexOf(".");
            int nPieceIndex = Integer.parseInt(strFile.substring(nStartIndex, nEndIndex))-1;

            m_filePieces[nPieceIndex] = confInfo.getTransferedFileHome().toString()+File.separator+strFile;
            m_nRecvPieceNum++;
        }


        // 인덱스가 서버의 개수와 동일하면 분할 파일 병합
        if( m_nRecvPieceNum == m_nCurrentServerNum )
        {
            if(m_nRecvPieceNum > 1) {
                // 병합된 파일 이름을 m-'file name'.'ext'로 설정
                int index = strFile.lastIndexOf("-");
                strMergeName = confInfo.getTransferedFileHome().toString()+File.separator+ strFile.substring(0, index)+"."+m_strExt;
                
                CMFileTransferManager.mergeFiles(m_filePieces, m_nCurrentServerNum, strMergeName); // 분할된 조각 병합
            }

            // 지연 시간 계산
            long lRecvTime = System.currentTimeMillis();
            printMessage("total delay for ("+m_nRecvPieceNum+") files: " +(lRecvTime-m_lStartTime)+" ms\n");

            m_bDistFileProc = false;
            m_nRecvPieceNum = 0;
        }

        return;
    }

    private void printMessage(String strText) {  // 메시지 출력 메소드
        m_client.printMessage(strText);
        return;
    }
}
