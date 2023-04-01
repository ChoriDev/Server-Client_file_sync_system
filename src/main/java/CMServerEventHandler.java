import java.util.Iterator;
import java.io.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEventField;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEvent;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventDISCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBCOMP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREC;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREL;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventUNSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import javax.swing.*;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;
    private boolean m_bDistFileProc;	// for distributed file processing

    public CMServerEventHandler(CMServerStub serverStub) {
        m_serverStub = serverStub;
        m_bDistFileProc = false;
    }

    @Override
    public void processEvent(CMEvent cme) {
        switch(cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
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
        CMSessionEvent se = (CMSessionEvent) cme;
        switch(se.getID()) {
            case CMSessionEvent.LOGIN:
                System.out.println("[" + se.getUserName() + "] 로그인을 요청했습니다.");
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

    private void processDummyEvent(CMEvent cme)
    {
        CMDummyEvent due = (CMDummyEvent) cme;
        System.out.println("세션(" + due.getHandlerSession() + "), 그룹(" + due.getHandlerGroup() + "), 송신자(" + due.getSender() + ")가 메시지를 보냈습니다.");
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
                    m_serverStub.replyEvent(fe, 1);
                }
                else
                {
                    m_serverStub.replyEvent(fe, 0);
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
                    m_serverStub.replyEvent(fe, 1);
                }
                else
                {
                    m_serverStub.replyEvent(fe, 1);
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
                String strFile = fe.getFileName();
                if(m_bDistFileProc)
                {
                    processFile(fe.getFileSender(), strFile);
                    m_bDistFileProc = false;
                }
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

    private void processFile(String strSender, String strFile)
    {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        String strFullSrcFilePath = null;
        String strModifiedFile = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        byte[] fileBlock = new byte[CMInfo.FILE_BLOCK_LEN];

        long lStartTime = System.currentTimeMillis();

        // change the modified file name
        strModifiedFile = "m-"+strFile;
        strModifiedFile = confInfo.getTransferedFileHome().toString()+ File.separator+strSender+
                File.separator+strModifiedFile;

        // stylize the file
        strFullSrcFilePath = confInfo.getTransferedFileHome().toString()+File.separator+strSender+
                File.separator+strFile;
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

            while( lRemainBytes > 0 )
            {
                if( lRemainBytes >= CMInfo.FILE_BLOCK_LEN )
                {
                    readBytes = fis.read(fileBlock);
                }
                else
                {
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

        // add some process delay here
        for(long i = 0; i < lFileSize/50; i++)
        {
            for(long j = 0; j < lFileSize/50; j++)
            {
                //
            }
        }

        long lEndTime = System.currentTimeMillis();
        System.out.println("processing delay: "+(lEndTime-lStartTime)+" ms");

        // send the modified file to the sender
        CMFileTransferManager.pushFile(strModifiedFile, strSender, m_serverStub.getCMInfo());

        return;
    }
}
