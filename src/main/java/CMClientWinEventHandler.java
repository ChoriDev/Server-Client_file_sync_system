import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import java.io.*;
import java.awt.*;

import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEventField;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEvent;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEventCompleteNewFile;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEventCompleteUpdateFile;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEventSkipUpdateFile;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEvent;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventCONNACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBCOMP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREC;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREL;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventSUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventUNSUBACK;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContent;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContentList;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.util.CMUtil;

public class CMClientWinEventHandler implements CMAppEventHandler {  // ??? CMAppEventHandler에게 상속받는 게 맞는지 확인하기
    private CMClientWinApp m_client;
    private CMClientStub m_clientStub;  // CMClientStub 타입 레퍼런스 변수 m_clientStub 선언
    private boolean m_bDistFileProc;	// for distributed file processing
    private int m_nCurrentServerNum;	// for distributed file processing
    private String[] m_filePieces;		// for distributed file processing
    private int m_nRecvPieceNum;		// for distributed file processing
    private String m_strExt;			// for distributed file processing
    private long m_lStartTime;	// for delay of SNS content downloading, distributed file processing

    public CMClientWinEventHandler(CMClientStub clientStub, CMClientWinApp client)
    {
        m_client = client;
        //m_outTextArea = textArea;
        m_clientStub = clientStub;
        m_lStartTime = 0;
        m_nCurrentServerNum = 0;
        m_nRecvPieceNum = 0;
        m_bDistFileProc = false;
        m_strExt = null;
        m_filePieces = null;
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
                    printMessage("서버에 의해 인증이 실패했습니다.\n");
                    printMessage("다시 시도하세요.\n");
                    // 로그인 실패 시 다시 로그인할 수 있는 방법 찾기
                }
                else if (se.isValidUser() == -1) {
                    printMessage("이미 로그인되어 있습니다.\n");
                    printMessage("다시 시도하세요.\n");
                    // 로그인 실패 시 다시 로그인할 수 있는 방법 찾기
                }
                else {
                    printMessage("서버에 성공적으로 로그인했습니다.\n");
                }
                break;
            case CMSessionEvent.SESSION_ADD_USER:_USER:
                printMessage("[" + se.getUserName() + "] 서버에 접속했습니다.\n");
                break;
            case CMSessionEvent.SESSION_REMOVE_USER:_USER:
                printMessage("[" + se.getUserName() + "] 서버에 접속 해제했습니다.\n");
                break;
            case CMSessionEvent.CHANGE_SESSION:
                printMessage("[" + se.getUserName() + "] " + se.getSessionName() + " 세선에 접속했습니다.\n");
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
                printMessage("[" + de.getUserName() + "] " + de.getHandlerSession() + " 세션의 " + de.getHandlerGroup() + " 그룹에 입장했습니다.\n");
                break;
            case CMDataEvent.REMOVE_USER:
                printMessage("[" + de.getUserName() + "] " + de.getHandlerSession() + " 세션의 " + de.getHandlerGroup() + " 그룹에 퇴장했습니다.\n");
                break;
            default:
                return;
        }
    }

    private void processDummyEvent(CMEvent cme)
    {
        CMDummyEvent due = (CMDummyEvent) cme;
        printMessage("세션(" + due.getHandlerSession() + "), 그룹(" + due.getHandlerGroup() + "), 유저(" + due.getSender() + ")가 메시지를 보냈습니다.\n");
        printMessage("메시지: " + due.getDummyInfo() + "\n");
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
                printMessage(strReq);
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
                    printMessage("["+fe.getFileName()+"] 소유자에게 파일이 없습니다.\n");
                }
                else if(fe.getReturnCode() == 0)
                {
                    printMessage("["+fe.getFileSender()+"] 송신자가 (" + fe.getFileName() + ") 파일 전송을 거부했습니다.\n");
                }
                break;
            case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:
                StringBuffer strReqBuf = new StringBuffer();
                strReqBuf.append("["+fe.getFileSender()+"] 송신자가 파일을 보내려 합니다.\n");
                strReqBuf.append("파일 경로: "+fe.getFilePath()+"\n");
                strReqBuf.append("파일 크기: "+fe.getFileSize()+"\n");
                printMessage(strReqBuf.toString());
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
                    printMessage("["+fe.getFileReceiver()+"] 수신자가 파일 수신을 거부했습니다.\n");
                    printMessage("파일 경로("+fe.getFilePath()+"), 파일 크기("+fe.getFileSize()+").\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:
            case CMFileEvent.START_FILE_TRANSFER_CHAN:
                printMessage("["+fe.getFileSender()+"] 송신자가 파일을 보냅니다. ("+fe.getFileName()+").\n");
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                printMessage("["+fe.getFileSender()+"] 송신자가 파일 전송을 완료했습니다. ("+fe.getFileName()+", "
                        +fe.getFileSize()+" Bytes).\n");
                if(m_bDistFileProc)
                    processFile(fe.getFileName());
                break;
            case CMFileEvent.CANCEL_FILE_SEND:
            case CMFileEvent.CANCEL_FILE_SEND_CHAN:
                printMessage("["+fe.getFileSender()+"] cancelled the file transfer.\n");
                break;
            case CMFileEvent.CANCEL_FILE_RECV_CHAN:
                printMessage("["+fe.getFileReceiver()+"] cancelled the file request.\n");
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
            printMessage("total delay for ("+m_nRecvPieceNum+") files: "
                    +(lRecvTime-m_lStartTime)+" ms\n");

            // reset m_bDistSendRecv, m_nRecvFilePieceNum
            m_bDistFileProc = false;
            m_nRecvPieceNum = 0;
        }

        return;
    }

    private void printMessage(String strText)
    {
		/*
		m_outTextArea.append(strText);
		m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());
		*/
		/*
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText, null);
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		*/
        m_client.printMessage(strText);

        return;
    }
}
