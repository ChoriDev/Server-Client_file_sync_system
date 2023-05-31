import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import javax.swing.*;
import java.io.*;
import java.lang.management.ManagementFactory;

public class CMServerWinEventHandler implements CMAppEventHandler {
    private CMServerWinApp m_server;  // CMServerWinapp 타입 레퍼런스 변수 m_server 선언
    private CMServerStub m_serverStub;  // CMServerSTub 타입 레퍼런스 변수 m_clientStub 선언
    private boolean m_bDistFileProc;	// 분산 파일에 사용할 변수

    public CMServerWinEventHandler(CMServerStub serverStub, CMServerWinApp server) {  // CMServerWinEventHandler 생성자
        // 변수 초기화
        m_server = server;
        m_serverStub = serverStub;
        m_bDistFileProc = false;
    }

    @Override
    public void processEvent(CMEvent cme) {  // event를 받는 processEvent 메소드 오버라이드
        m_server.clock.increment(Long.valueOf(m_server.pId).intValue());
        switch(cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:  // 로그인 이벤트의 경우
                processSessionEvent(cme);  // 로그인 이벤트 메소드 실행
                break;
            case CMInfo.CM_DUMMY_EVENT:  // 그룹 이벤트의 경우
                processDummyEvent(cme);  // 그룹 이벤트 메소드 실행
                break;
            case CMInfo.CM_FILE_EVENT:  // 파일 이벤트의 경우
                processFileEvent(cme);  // 파일 이벤트 메소드
                break;
            default:
                return;
        }
    }

    private void processSessionEvent(CMEvent cme) {  // 세션 관련 메소드
        CMSessionEvent se = (CMSessionEvent) cme;
        switch(se.getID()) {
            case CMSessionEvent.LOGIN:  // 클라이언트가 로그인을 요청한 경우
                printMessage("[" + se.getUserName() + "] 로그인을 요청했습니다.\n");
                m_server.displayLoginUsers();  // 접속한 사용자를 출력하는 패널 재설정
                break;
            case CMSessionEvent.LOGOUT:  // 클라이언트가 로그아웃을 요청한 경우
                printMessage("[" + se.getUserName() + "] 접속 해제했습니다.\n");
                m_server.displayLoginUsers();  // 접속한 사용자를 출력하는 패널 재설정
                break;
            case CMSessionEvent.JOIN_SESSION:  // 클라이언트가 세션에 접속한 경우
                printMessage("[" + se.getUserName() + "] " + se.getSessionName() + " 세션에 입장했습니다.\n");
                break;
            case CMSessionEvent.LEAVE_SESSION:  // 클라이언트가 세션에서 떠난 경우
                printMessage("[" + se.getUserName() + "] " + se.getSessionName() + " 세션에 퇴장했습니다.\n");
                break;
            default:
                return;
        }
    }

    private void processDummyEvent(CMEvent cme) {  // 메시지 전송 관련 메소드
        CMDummyEvent due = (CMDummyEvent) cme;
        printMessage("세션(" + due.getHandlerSession() + "), 그룹(" + due.getHandlerGroup() + "), 송신자(" + due.getSender() + ")가 메시지를 보냈습니다.\n");
        printMessage("메시지: " + due.getDummyInfo() + "\n");
        return;
    }

    private void processFileEvent(CMEvent cme) {  // 파일 전송 관련 메소드
        CMFileEvent fe = (CMFileEvent) cme;
        int nOption = -1;
        switch(fe.getID())
        {
            case CMFileEvent.REQUEST_PERMIT_PULL_FILE:  // 파일을 요청을 받은 경우
                String strReq = "["+fe.getFileReceiver()+"] 수신자가 ("+fe.getFileName()+
                        ") 파일을 요청했습니다.\n";
                printMessage(strReq);
                nOption = JOptionPane.showConfirmDialog(null, strReq, "파일 요청", JOptionPane.YES_NO_OPTION);
                if(nOption == JOptionPane.YES_OPTION) {  // 파일 요청 허가
                    m_serverStub.replyEvent(fe, 1);
                }
                else {  // 파일 요청 거부
                    m_serverStub.replyEvent(fe, 0);
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
                    m_serverStub.replyEvent(fe, 1);
                }
                else
                {
                    m_serverStub.replyEvent(fe, 0);  // 파일 전송 요청 거부
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
                String strFile = fe.getFileName();

                m_server.sendCMDummyEvent("수신자가 파일 수신을 완료했습니다.", fe.getFileSender());

                if(m_bDistFileProc)
                {
                    processFile(fe.getFileSender(), strFile);
                    m_bDistFileProc = false;
                }
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

    private void processFile(String strSender, String strFile) {  // 파일 처리 메소드
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        String strFullSrcFilePath = null;
        String strModifiedFile = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        byte[] fileBlock = new byte[CMInfo.FILE_BLOCK_LEN];

        long lStartTime = System.currentTimeMillis();

        // 리스트에 수정된 파일 이름 추가
        strModifiedFile = "m-"+strFile;
        strModifiedFile = confInfo.getTransferedFileHome().toString()+ File.separator+strSender+
                File.separator+strModifiedFile;

        // 파일 스타일 지정
        strFullSrcFilePath = confInfo.getTransferedFileHome().toString()+File.separator+strSender+ File.separator+strFile;
        File srcFile = new File(strFullSrcFilePath);
        long lFileSize = srcFile.length();
        long lRemainBytes = lFileSize;
        int readBytes = 0;

        try {
            fis = new FileInputStream(strFullSrcFilePath);
            fos = new FileOutputStream(strModifiedFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {
            while( lRemainBytes > 0 ) {
                if( lRemainBytes >= CMInfo.FILE_BLOCK_LEN )
                {
                    readBytes = fis.read(fileBlock);
                } else {
                    readBytes = fis.read(fileBlock, 0, (int)lRemainBytes);
                }

                fos.write(fileBlock, 0, readBytes);
                lRemainBytes -= readBytes;
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long lEndTime = System.currentTimeMillis();
        printMessage("processing delay: "+(lEndTime-lStartTime)+" ms\n");

        // 수신자에게 수정된 파일 전송
        CMFileTransferManager.pushFile(strModifiedFile, strSender, m_serverStub.getCMInfo());

        return;
    }

    private void printMessage(String strText) {  // 메시지 출력 메소드
        m_server.printMessage(strText);
    }
}
