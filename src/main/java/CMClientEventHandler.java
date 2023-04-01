import kr.ac.konkuk.ccslab.cm.event.*;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import javax.swing.*;
import java.io.File;

public class CMClientEventHandler implements CMAppEventHandler {  // ??? CMAppEventHandler에게 상속받는 게 맞는지 확인하기
    private CMClientStub m_clientStub;  // CMClientStub 타입 레퍼런스 변수 m_clientStub 선언
    private boolean m_bDistFileProc;	// for distributed file processing
    private int m_nCurrentServerNum;	// for distributed file processing
    private String[] m_filePieces;		// for distributed file processing
    private int m_nRecvPieceNum;		// for distributed file processing
    private String m_strExt;			// for distributed file processing
    private long m_lStartTime;	// for delay of SNS content downloading, distributed file processing
    public CMClientEventHandler(CMClientStub stub) {  // CMClientEventHandler 생성자
        m_clientStub = stub;  // 인자로 넘어온 CMClientStub 객체를 변수 m_clientStub에 할당
        m_bDistFileProc = false;
        m_nCurrentServerNum = 0;
        m_filePieces = null;
        m_nRecvPieceNum = 0;
        m_strExt = null;
        m_lStartTime = 0;
    }

    public void setDistFileProc(boolean b)
    {
        m_bDistFileProc = b;
    }

    public boolean isDistFileProc()
    {
        return m_bDistFileProc;
    }

    public void setCurrentServerNum(int num)
    {
        m_nCurrentServerNum = num;
    }

    public int getCurrentServerNum()
    {
        return m_nCurrentServerNum;
    }

    public void setFilePieces(String[] pieces)
    {
        m_filePieces = pieces;
    }

    public String[] getFilePieces()
    {
        return m_filePieces;
    }

    public void setRecvPieceNum(int num)
    {
        m_nRecvPieceNum = num;
    }

    public int getRecvPieceNum()
    {
        return m_nRecvPieceNum;
    }

    public void setStartTime(long time)
    {
        m_lStartTime = time;
    }

    public void setFileExtension(String ext)
    {
        m_strExt = ext;
    }

    public String getFileExtension()
    {
        return m_strExt;
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
            case CMInfo.CM_DUMMY_EVENT:
                processDummyEvent(cme);
                break;
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
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
                    System.err.println("서버에 의해 인증이 실패했습니다.");
                    System.err.println("다시 시도하세요.");
                    // 로그인 실패 시 다시 로그인할 수 있는 방법 찾기
                }
                else if (se.isValidUser() == -1) {
                    System.err.println("이미 로그인되어 있습니다.");
                    System.err.println("다시 시도하세요.");
                    // 로그인 실패 시 다시 로그인할 수 있는 방법 찾기
                }
                else {
                    System.out.println("서버에 성공적으로 로그인했습니다.");
                }
                break;
            case CMSessionEvent.SESSION_ADD_USER:_USER:
                System.out.println("[" + se.getUserName() + "] 서버에 접속했습니다.");
                break;
            case CMSessionEvent.SESSION_REMOVE_USER:_USER:
                System.out.println("[" + se.getUserName() + "] 서버에 접속 해제했습니다.");
                break;
            case CMSessionEvent.CHANGE_SESSION:
                System.out.println("[" + se.getUserName() + "] " + se.getSessionName() + " 세선에 접속했습니다.");
                break;
//            case CMSessionEvent. // 세션에서 떠났을 때 통보
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

    private void processDummyEvent(CMEvent cme)
    {
        CMDummyEvent due = (CMDummyEvent) cme;
        System.out.println("세션(" + due.getHandlerSession() + "), 그룹(" + due.getHandlerGroup() + "), 유저(" + due.getSender() + ")가 메시지를 보냈습니다.");
        System.out.println("메시지: " + due.getDummyInfo());
        return;
    }

    private void processFileEvent(CMEvent cme)
    {
        CMFileEvent fe = (CMFileEvent) cme;
        int nOption = -1;
        switch(fe.getID())
        {
            case CMFileEvent.REQUEST_PERMIT_PULL_FILE:
                String strReq = "["+fe.getFileReceiver()+"] 수신자가 ("+fe.getFileName()+
                        ") 파일을 요청했습니다.\n";
                System.out.print(strReq);
                // !!!아래가 무엇인지 확인하기
                nOption = JOptionPane.showConfirmDialog(null, strReq, "파일 요청",
                        JOptionPane.YES_NO_OPTION);
                if(nOption == JOptionPane.YES_OPTION)
                {
                    m_clientStub.replyEvent(fe, 1);
                }
                else
                {
                    m_clientStub.replyEvent(fe, 0);
                }
                break;
            case CMFileEvent.REPLY_PERMIT_PULL_FILE:
                if(fe.getReturnCode() == -1)
                {
                    System.err.print("["+fe.getFileName()+"] 소유자에게 파일이 없습니다.\n");
                }
                else if(fe.getReturnCode() == 0)
                {
                    System.err.print("["+fe.getFileSender()+"] 송신자가 (" + fe.getFileName() + ") 파일 전송을 거부했습니다.\n");
                }
                break;
            case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:
                StringBuffer strReqBuf = new StringBuffer();
                strReqBuf.append("["+fe.getFileSender()+"] 송신자가 파일을 보내려 합니다.\n");
                strReqBuf.append("파일 경로: "+fe.getFilePath()+"\n");
                strReqBuf.append("파일 크기: "+fe.getFileSize()+"\n");
                System.out.print(strReqBuf.toString());
                nOption = JOptionPane.showConfirmDialog(null, strReqBuf.toString(),
                        "파일 전송", JOptionPane.YES_NO_OPTION);
                if(nOption == JOptionPane.YES_OPTION)
                {
                    m_clientStub.replyEvent(fe, 1);
                }
                else
                {
                    m_clientStub.replyEvent(fe, 1);
                }
                break;
            case CMFileEvent.REPLY_PERMIT_PUSH_FILE:
                if(fe.getReturnCode() == 0)
                {
                    System.err.print("["+fe.getFileReceiver()+"] 수신자가 파일 수신을 거부했습니다.\n");
                    System.err.print("파일 경로("+fe.getFilePath()+"), 파일 크기("+fe.getFileSize()+").\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:
            case CMFileEvent.START_FILE_TRANSFER_CHAN:
                System.out.println("["+fe.getFileSender()+"] 송신자가 파일을 보냅니다. ("+fe.getFileName()+").");
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                System.out.println("["+fe.getFileSender()+"] 송신자가 파일 전송을 완료했습니다. ("+fe.getFileName()+", "
                        +fe.getFileSize()+" Bytes).");
                if(m_bDistFileProc)
                    processFile(fe.getFileName());
                break;
            case CMFileEvent.CANCEL_FILE_SEND:
            case CMFileEvent.CANCEL_FILE_SEND_CHAN:
                System.out.println("["+fe.getFileSender()+"] cancelled the file transfer.");
                break;
            case CMFileEvent.CANCEL_FILE_RECV_CHAN:
                System.out.println("["+fe.getFileReceiver()+"] cancelled the file request.");
                break;
        }
        return;
    }

    private void processFile(String strFile)
    {
        CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
        String strMergeName = null;

        // add file name to list and increase index
        if(m_nCurrentServerNum == 1)
        {
            m_filePieces[m_nRecvPieceNum++] = confInfo.getTransferedFileHome().toString()+ File.separator+strFile;
        }
        else
        {
            // Be careful to put a file into an appropriate array member (file piece order)
            // extract piece number from file name ('filename'-'number'.split )
            int nStartIndex = strFile.lastIndexOf("-")+1;
            int nEndIndex = strFile.lastIndexOf(".");
            int nPieceIndex = Integer.parseInt(strFile.substring(nStartIndex, nEndIndex))-1;

            m_filePieces[nPieceIndex] = confInfo.getTransferedFileHome().toString()+File.separator+strFile;
            m_nRecvPieceNum++;
        }


        // if the index is the same as the number of servers, merge the split file
        if( m_nRecvPieceNum == m_nCurrentServerNum )
        {
            if(m_nRecvPieceNum > 1)
            {
                // set the merged file name m-'file name'.'ext'
                int index = strFile.lastIndexOf("-");
                strMergeName = confInfo.getTransferedFileHome().toString()+File.separator+
                        strFile.substring(0, index)+"."+m_strExt;

                // merge split pieces
                CMFileTransferManager.mergeFiles(m_filePieces, m_nCurrentServerNum, strMergeName);
            }

            // calculate the total delay
            long lRecvTime = System.currentTimeMillis();
            System.out.println("total delay for ("+m_nRecvPieceNum+") files: "
                    +(lRecvTime-m_lStartTime)+" ms");

            // reset m_bDistSendRecv, m_nRecvFilePieceNum
            m_bDistFileProc = false;
            m_nRecvPieceNum = 0;
        }

        return;
    }
}
