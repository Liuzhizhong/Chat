package com.server;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.hibernate.Hibernate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import boaotong.dao.BoaoGroup;
import boaotong.dao.BoaoLixian;
import boaotong.dao.BoaoTemp;
import boaotong.dao.BoaoUser;
import boaotong.dao.IDao;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream; 
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class ServerMain extends JFrame {
    private Socket clientSocket;
    private ServerSocket serverSocket, serverSocket2; 
    private Vector<ForClientThread> socketList; 
    private Vector<ClientBean> clientList;
    private Vector<SendFileThread> threadVec; 

    private static final String FIRST_LOGIN = ":L";
    private static final String CLIENT_EXIT = ":E";
    private static final Integer MAX_LENGTH = 1024;
    private static final String TEXT_MESS_START = ":T";
    private static final String CHARSET = "UTF-8";
    private static final String END_TAG = "-]"; 
    private static final String END_ATTR = "_]"; 
    
    private final static String SERVERIP = "server.ip";
    private final static String MAINPORT = "main.port";
    private final static String MAIN_SEND_PORT = "main.send.port";
    private final static String MAIN_SHOU_PORT = "main.shou.port";
    private final static String CLIENT_VERSION = "client.version";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DAO_IMPL = "userDao";
    public static final String FENGEFU = ",";
    public static final String UPFILE = "/upfiles/";
    public static final String FG = "/";
    public static final int DELETE_TEMP_DAY_COUNT = 2; //超过2天的临时消息 记录删除
    public static final int DELETE_TEMP_HOUR = 2;//每天执行删除临时消息的时间（小时）（24小时制）
    public static final int DELETE_TEMP_MINUTE = 48;
    public static final int CLEAR_SLEEP_TIME = 59000;//每隔59秒执行一次在线用户检查
    
    public static final int LINE_COUNT = 12; //文本区中最多显示多少行数据，就自动清屏
   
    private String serverIp;
    private Integer mainPort;
    private Integer sendPort;
    private Integer shouPort;
    
    private String clientVersion;
     
    private static Integer lixianPort = 1980;//离线文件接收起始端口，一开始就自动+1
    private boolean isStop = false; 
    private boolean isReady = false;
    private IDao dao;
    private ApplicationContext springContext;
    private JTextArea area;
    private JMenuItem clearItem;
    private JPopupMenu popupMenu;

    public ServerMain(){ 
        File f = new File(System.getProperty("user.dir") + UPFILE);
        if ( ! f.exists() ){
            f.mkdir();
        }
        springContext = new ClassPathXmlApplicationContext("/appContext.xml"); 
        area = new JTextArea();
        area.setEditable(false);
        JScrollPane spane = new JScrollPane(area);
        popupMenu = new JPopupMenu();
        clearItem = new JMenuItem("清屏");
        popupMenu.add(clearItem);
        getContentPane().add(spane, BorderLayout.CENTER);
        setBounds(200,150,400,300);
        setTitle("服务器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clearItem.addActionListener(new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e) {
                area.setText("");
            }
        });
        area.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me){
                if ( me.getButton() == MouseEvent.BUTTON3 ){
                    popupMenu.show(ServerMain.this, me.getPoint().x, me.getPoint().y);
                }
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we){
                isReady = false; isStop = true;
            }
        });
        setVisible(true);
    }
	private void appInitIpAndPort(){
        InputStream in = null;
        Properties p = new Properties();
        try { 
            in = getClass().getResourceAsStream("config.properties");
            p.load(in);
            serverIp = p.getProperty(SERVERIP);
            mainPort = Integer.parseInt(p.getProperty(MAINPORT));
            sendPort = Integer.parseInt(p.getProperty(MAIN_SEND_PORT));
            shouPort = Integer.parseInt(p.getProperty(MAIN_SHOU_PORT));
            clientVersion = p.getProperty(CLIENT_VERSION);
        } catch (Exception ex) { 
            com.server.LogJT.LogMsg(ex.toString(),ex);
        } finally{
            if ( in != null ) try {
                in.close();
            } catch (IOException ex) {
                com.server.LogJT.LogMsg(ex.toString(),ex);
            }
        }
    }
    public void go() {
        appInitIpAndPort();
        try {
            serverSocket = new ServerSocket(mainPort); 
            serverSocket.setReceiveBufferSize(8192);
        } catch (BindException ex) {
            com.server.LogJT.LogMsg(ex.toString(),ex);
            System.exit(0);
        } catch (IOException ex) {
            com.server.LogJT.LogMsg(ex.toString(),ex);
        }
        try {
            isReady = true;
            clientList = new Vector<ClientBean>();
            socketList = new Vector<ForClientThread>();
            threadVec = new Vector<SendFileThread> (); 
            new Thread(new Runnable() { 
                @Override
                public void run() {
                    new ClientBean().go();
                }
            }).start();
            new CheckClientState().start(); 
            area.append("===  " + getDateTime() + "  === 服务器启动成功 ===\n");
            while (isReady) {
                clientSocket = serverSocket.accept();
                ForClientThread fct = new ForClientThread(clientSocket); 
                socketList.add(fct);
                new Thread(fct).start();
            }
        } catch (IOException ex) {
            com.server.LogJT.LogMsg(ex.toString(),ex);
        } finally {
            isStop = true;
            closeServerSocket();
        }
    }
	private String getDateTime(){
        Calendar cdr = Calendar.getInstance();
        int yy = cdr.get(Calendar.YEAR);
        int mM = cdr.get(Calendar.MONTH)+1;
        int dd = cdr.get(Calendar.DATE);
        int hh = cdr.get(Calendar.HOUR_OF_DAY);
        int mm = cdr.get(Calendar.MINUTE);
        int ss = cdr.get(Calendar.SECOND);
        String str = yy + "-" + mM + "-" + dd + " ";
        str += hh < 10 ? "0" + hh: "" + hh;
        str += ":";
        str += mm < 10 ? "0" + mm :mm;
        str += ":";
        str += ss < 10 ? "0" + ss: ss ;
        return str;
    }
    
//    private String getCurrDate(){
//        Calendar cdr = Calendar.getInstance();
//        int yy = cdr.get(Calendar.YEAR);
//        int mM = cdr.get(Calendar.MONTH)+1;
//        int dd = cdr.get(Calendar.DATE);
//        String str = yy + "-" + mM + "-" + dd;
//        return str;
//    }
    
	private void closeServerSocket(){
        try{
            if ( serverSocket != null ){
                serverSocket.close();
            }
        }catch(IOException ex){
            com.server.LogJT.LogMsg(ex.toString(),ex);
        }
	}
	
	class ForClientThread implements Runnable, Serializable{
		private static final long serialVersionUID = -9194096328955415477L;
		private Socket socket;
		private Integer fromuserid;
		private Integer touserid;
		private Integer flag;
		private BufferedInputStream bis;
		private BufferedOutputStream bos;
		private boolean cont = false;  
		private Integer groupId;
		private String[] groupUsersId;
		private int byteCountTotal;
        private List<byte[]> byteList;        
		int firstLength = -1;
        
		private IDao dao;
		
		public ForClientThread(Socket s ) {
			this.socket = s; 
			try {
				bis = new BufferedInputStream(socket.getInputStream());
				bos = new BufferedOutputStream(socket.getOutputStream());
				cont = true;
			} catch (IOException ex) {
				com.server.LogJT.LogMsg(ex.toString(),ex);
			}
		}
		
		public int send(byte[] bs,int len) { 
			try { 
				bos.write(bs,0, len); 
				bos.flush();
			} catch (IOException e) {
				socketList.remove(this);
				return 0; 
			}
			return 1;
		}
		private boolean sendToClient(ClientBean cb, String flag){
            try {
                cb.sendToServer(flag);
                return true;
            } catch (Exception ex) {
                com.server.LogJT.LogMsg(ex.toString(),ex);
            }
            return false;
		}
        
        public void writeMsgToDB(byte[] bs){
            try{
                dao = dao == null ? (IDao) springContext.getBean(DAO_IMPL): dao;
                BoaoTemp boaoTemp = new BoaoTemp();
                boaoTemp.setFuid(fromuserid);
                boaoTemp.setTuid(touserid);
                boaoTemp.setFirstlength(firstLength);
                boaoTemp.setBlob(Hibernate.createBlob(bs));
                boaoTemp.setSendtime(getDateTime());
                dao.save(boaoTemp);
            }catch (Exception ex) {
                com.server.LogJT.LogMsg(ex.toString(),ex);
            }
        }
        public void writeGroupMsgToDB(byte[] bs, String uids){
            try{
                dao = dao == null ? (IDao) springContext.getBean(DAO_IMPL): dao;
                BoaoTemp boaoTemp = new BoaoTemp();
                boaoTemp.setFuid(fromuserid);
                boaoTemp.setGid(groupId);
                boaoTemp.setGuid(uids);
                boaoTemp.setFirstlength(firstLength);
                boaoTemp.setBlob(Hibernate.createBlob(bs));
                boaoTemp.setSendtime(getDateTime());
                dao.save(boaoTemp);
            }catch (Exception ex) {
                com.server.LogJT.LogMsg(ex.toString(),ex);
            }
        }
		
        /**
         * 若是私聊，就看接收者有没有正在聊，在聊的话就直接启动一个线程给他发送，如果发送失败，就将这消息写入临时表（他下次登录便会收到）
         * 若不在聊，就先写入数据表，再看他是否在线，若在线就启动个线程给他个私人新消息通知 5
         * 若是群聊，就先给所有在线用户发，发完判断已发用户数和群用户数有没有相等（除自己）若少于群用户数，则找出未发到的用户
         * 再给这些用户在临时表各写一条记录，然后判断是否需要给他发个群新消息通知 6
         * @param bs 
         */
		private void sendMessageToOne(final byte[] bs ){
			try{ 
				if ( flag == 0 ){
                    boolean isFind = false;
					for (int i = 0; i < socketList.size(); i++) {
						try{
							final ForClientThread c = (ForClientThread) socketList.get(i);
                            if ( c == null ){
                                continue;
                            }
							if ( touserid.equals(c.fromuserid) && fromuserid.equals(c.touserid) ){
                                isFind = true;
                                int x = c.send(bs, bs.length);
                                if ( x == 0 ){ //发送失败，就写入数据表
                                    writeMsgToDB(bs);
                                }
                                break;
							}
						}catch(Exception ex){
							com.server.LogJT.LogMsg(ex.toString(),ex);
						}
					}
                    if ( !isFind ){ //没找到接收者，即写入数据表                        
                        writeMsgToDB(bs);
                        for ( int i = 0; i < clientList.size(); i ++ ){
                            ClientBean cb = clientList.get(i);
                            Integer uid = cb.getUid();
                            if ( uid == null || cb.socket == null || cb.ip == null ) continue;
                            if ( uid.equals(touserid) ){ //若接收者在线就让他的托盘闪
                                sendToClient(cb,"5," + fromuserid);
                                break;
                            }
                        }
                    }
				}else{ 
					List<Integer> uList = new ArrayList<Integer>(); 
                    for ( int i = 0; i < socketList.size(); i ++ ){
                        final ForClientThread c = socketList.get(i);
						if ( c == null || c == this || c.groupId == null ) continue; 
						for ( int j = 0; j < groupUsersId.length; j ++ ){
							if ( groupUsersId[j].equals(c.fromuserid.toString()) && groupId.equals(c.groupId)) {
                                int x = c.send(bs, bs.length);
                                if ( x == 1 ){ //发送成功就加入已发名单
                                    uList.add(c.fromuserid);
                                }
							}
						}
					}  
					if ( uList.size() < groupUsersId.length - 1 ){//有的用户没在线,就找出是哪些没在线（除了自己）
						List<Integer> toList = new ArrayList<Integer>();
						for ( int i = 0; i < groupUsersId.length; i ++ ){
							Integer uid = Integer.parseInt(groupUsersId[i]);
							if ( !uList.contains(uid) && !uid.equals(fromuserid)){
								toList.add(uid);
							}
						}
                        String idString = listToString(toList);
                        writeGroupMsgToDB(bs, idString);
						for ( int x = 0; x < groupUsersId.length; x ++ ){
							Integer id = Integer.parseInt(groupUsersId[x]);
							if ( uList.contains(id) || id.equals(fromuserid) ) continue; 
							for ( int i = 0; i < clientList.size(); i ++ ){
                                ClientBean cb = clientList.get(i);
                                Integer uid = cb.getUid();
                                if ( uid == null || cb.socket == null ) continue;
								if ( uid.equals(fromuserid) ) continue; 
								if ( id.equals(uid) ){ //若接收者在线就让他的托盘闪
									sendToClient(cb,"6," + groupId); 
								}
							}
						}
					}
				}
			}catch(Exception ex){ 
                com.server.LogJT.LogMsg(ex.toString(),ex); 
            }
		}
		private String listToString(List<Integer> list){
            if (list == null || list.isEmpty() ) return "";
            String str = "";
            for ( Integer id : list){
                str += id + FENGEFU;
            }
            return str.substring(0, str.length()-1);
        }
		private int getFirstPackBytes( ){ 
	        byte[] xb = new byte[getCurrSize()];
	        int xi = 0;
	        for ( byte[] b : byteList ){
	            for ( int j = 0; j < b.length; j ++ ){
	                xb[xi++] = b[j];
	            }
	        }
	        String str = new String(xb);
	        int n = str.indexOf(END_TAG);
	        if ( n != -1 )
	            return str.substring(0,n+END_TAG.length()).getBytes().length;
	        return n;
	    }
		private String getFirstStr(){ 
	        byte[] xb = new byte[getCurrSize()];
	        int xi = 0;
	        for ( byte[] b : byteList ){
	            for ( int j = 0; j < b.length; j ++ ){
	                xb[xi++] = b[j];
	            }
	        }
	        String str = new String(xb);
	        return str;
	    }
		private int getCurrSize( ){ 
	        int length = 0;
	        for ( int i = 0; i < byteList.size(); i ++ ){
	            length += ((byte[]) byteList.get(i)).length;
	        }
	        return length;
	    }
		private byte[] getAllBytes(){
			byte[] bs = new byte[byteCountTotal]; 
            int ti = 0;
            for ( byte[] b : byteList ){
                for ( int j = 0; j < b.length; j ++ ){
                    bs[ti++] = b[j];
                }
            }
            return bs;
		} 
        @Override
		public void run() {
			try {
				byteList = new ArrayList<byte[]>();
				int len = 0;
				int messageBytes = 0;  
                boolean noBody = false;
	            boolean hasBegin = false;
				while (cont) {
					byte[] bs = new byte[MAX_LENGTH];
					len = bis.read(bs);
					if ( len == - 1 ) break; 
	                byte[] tb = new byte[len];
	                for ( int i = 0; i < len; i ++ ){
	                    tb[i] = bs[i];
	                }
	                byteList.add(tb);
	                int currLen = getCurrSize(); 
	                if ( firstLength == -1 )
	                    firstLength = getFirstPackBytes();
	                if ( firstLength == - 1 ){ 
	                    continue;
	                }
	                if ( !hasBegin ){ 
	                    String firstStr = getFirstStr();
                        if (firstStr.indexOf(FIRST_LOGIN) == 0 || firstStr.indexOf(CLIENT_EXIT) == 0 ){
                            messageBytes = 0;
                            noBody = true;
                        }else if ( firstStr.indexOf(TEXT_MESS_START) == 0 ){
	                        messageBytes = Integer.parseInt(firstStr.substring(TEXT_MESS_START.length(), firstStr.indexOf(END_ATTR))); 
	                    }else{ 
	                        messageBytes = Integer.parseInt(firstStr.substring(0, firstStr.indexOf(":"))); 
	                    } 
	                    byteCountTotal = firstLength + messageBytes; 
	                    hasBegin = true;
	                }
	                if ( currLen < byteCountTotal ){
                        continue;
                    }
                    if ( noBody ){
                        String str = getFirstStr();
                        str = str.substring(0, str.indexOf(END_TAG) );
                        if ( str.indexOf(CLIENT_EXIT) == 0){
                            break;
                        }else if ( str.indexOf(FIRST_LOGIN) == 0 ){
                            String id = str.substring(str.lastIndexOf(":")+1,str.lastIndexOf("-")); 
                            fromuserid = Integer.parseInt(id);
                            id = str.substring(str.lastIndexOf("-")+1,str.lastIndexOf("="));                            
                            String fs = str.substring(str.lastIndexOf("=")+1); 
                            flag = Integer.parseInt(fs);
                            if ( flag == 1 ){
                                groupId = Integer.parseInt(id);
                                dao = dao == null ? (IDao) springContext.getBean(DAO_IMPL): dao;
                                BoaoGroup group = (BoaoGroup)dao.get(BoaoGroup.class, groupId);
                                groupUsersId = group.getUsers().split(FENGEFU);
                            }else{
                                touserid = Integer.parseInt(id);
                            }
                        }
                    }else{
                        byte[] all = getAllBytes(); 
                        sendMessageToOne(all); 
                    }
                    byteList.clear();
                    firstLength = -1;
                    hasBegin = false;
                    noBody = false;
				}
                socketList.remove(this);
			} catch (Exception ex){ 
				socketList.remove(this); 
			} finally {
				try {
					if (bis != null)
						bis.close();
					if (bos != null)
						bos.close();
					if (socket != null) {
						socket.close();
						socket = null; 
					}
				} catch (IOException ex) {
					com.server.LogJT.LogMsg(ex.toString(),ex);
				}
			}
		}
	}
	
    //检查客户端是否在线，若是异常掉线，就将他的在线状态由1修改为0，若在线列表有他，但状态却为0，就设为1
	private class CheckClientState extends Thread{
        private int countDays(String begin,String end){
            int days = 0;
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            Calendar c_b = Calendar.getInstance();
            Calendar c_e = Calendar.getInstance();
            try{
                c_b.setTime(df.parse(begin));
                c_e.setTime(df.parse(end));
                while(c_b.before(c_e)){
                    days ++;
                    c_b.add(Calendar.DAY_OF_YEAR, 1);
                }
            }catch(Exception ex){
                com.server.LogJT.LogMsg(ex.toString(),ex);
            }
            return days;
        }
        
        @Override
		public void run(){ 
			while(!isStop){
				try {
                    dao = dao == null ? (IDao) springContext.getBean(DAO_IMPL): dao;
                    List onLineUserList = dao.getAllOnLineUsers();
                    int count = 0;
                    for ( int i = 0; i < onLineUserList.size(); i ++ ){
                        BoaoUser user = (BoaoUser) onLineUserList.get(i);
                        try{
                            if ( !containsClientList(user.getUid())){ //当前在线列表中没发现此客户端ID
                                user.setOnline(0); 
                                dao.update(user); count ++;
//                                com.server.LogJT.LogMsg("客户端(uid=" + user.getUid() + ")非法断线。通过服务器复位。", new Exception());
                            }
                        }catch (Exception ex) {
                            com.server.LogJT.LogMsg("恢复客户端(uid=" + user.getUid() + ")在线状态时异常：" + ex.toString(),ex);
                        }
                    }
                    if ( count > 0 ){ //有人被修改，就通知一下所有在线用户刷新
                        for ( int i = 0; i < clientList.size(); i ++ ){
                            final ClientBean cb = clientList.get(i); 
                            try{
                                if( cb.getUid() == null || cb.socket == null ) continue;
                                cb.sendToServer("0");
                            }
                            catch(Exception ex){
                                com.server.LogJT.LogMsg("服务器转发(0)给客户端(" + cb.getUid() + ")时异常:" + ex.toString(),ex);
                            }
                        }
                    }
                    //清除重复的用户
                    for ( int i = 0; i < clientList.size(); i ++ ){
                        ClientBean cb = clientList.get(i);
                        if ( cb == null || cb.myId == null || cb.socket == null ){
                            clientList.remove(i); i --; continue;
                        }
                        for ( int j = 0; j < clientList.size(); j ++ ){
                            if ( i == j ) continue;
                            ClientBean cc = clientList.get(j);
                            if ( cc == null || cc.myId == null || cc.socket == null ){
                                continue;
                            }
                            if ( cb.myId.equals(cc.myId) ){
                                if ( i < j ){
                                    clientList.remove(i); i--; break;
                                }else{
                                    clientList.remove(j); i--; break;
                                }
                            }
                        }
                    }
                    //到了清理临时表的时间 2:48
                    if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == DELETE_TEMP_HOUR 
                            && Calendar.getInstance().get(Calendar.MINUTE) == DELETE_TEMP_MINUTE){
                        String dt = getDateTime();
                        List<BoaoTemp> allTemp = dao.getAllGroupTemp();
                        for ( BoaoTemp temp : allTemp ){
                            if ( temp.getGuid().length() == 0 ){
                                dao.delete(temp); continue;
                            }
                            String ts = temp.getSendtime().substring(0, temp.getSendtime().indexOf(" "));
                            int days = countDays(ts, dt);
                            if ( days >= DELETE_TEMP_DAY_COUNT ){
                                dao.delete(temp);
                            }
                        }
                    }
					Thread.sleep(CLEAR_SLEEP_TIME);
//                    for ( int i = 0; i < clientList.size(); i ++ ){
//                        ClientBean cb = clientList.get(i);
//						Socket ss = null;
//						PrintWriter pw = null;
//						InetAddress ip = cb.getIp();
//						try{  
//	                    	ss = new Socket(ip, shouPort);
////	                        pw = new PrintWriter(ss.getOutputStream());
////	                        pw.write(""); 
////	                        pw.flush(); break;
//                    	}
//	                    catch(Exception ex){
//                            com.server.LogJT.LogMsg("客户端(" + ip.getHostAddress() + ")非法断线。" + ex.toString(),ex);
//                            dao = dao == null ? (IDao) springContext.getBean(DAO_IMPL): dao;
//                            BoaoUser user = (BoaoUser) dao.get(BoaoUser.class, cb.getUid()); 
//                            user.setOnline(0);
//                            clientList.remove(cb);
//                            dao.update(user); break;
//	                    }finally{
//	                    	if ( pw != null )
//	                    		pw.close();
//	                    	if ( ss != null )
//	                    		ss.close();
//	                        pw = null;ss = null;
//	                    }
//                    } 
				}catch (Exception ex) {
					com.server.LogJT.LogMsg(ex.toString(),ex);
                    isReady = false;
                    isStop = true; System.exit(0);
				}
			}
		}
	}
	
	//离线文件会发给服务器暂存，服务器接收完成以后，判断接收文件的客户端在线否，在线就立即通知他接收
    //直到他拒绝或接收完成以后，才会删除数据库记录(此类只完成接收动作，完成以后会开启一个线程让在线者接收)
	private class LixianWenJian extends Thread {
		private String readStr; 
		private InetAddress myIp;
		private FileOutputStream fos = null;
		private BufferedOutputStream bos = null;
		private BufferedInputStream bis = null;
		private Socket sos; 
        private File file;
		
		public LixianWenJian(String readStr,InetAddress myIp){
			this.readStr = readStr; 
			this.myIp = myIp;
		}
        @Override
		public void run(){
			try {
				Thread.sleep(500);
				String[] param = readStr.split(FG);
	        	Integer tuid = Integer.parseInt(param[2]);
	        	Integer fuid = Integer.parseInt(param[1]);
				Integer port = Integer.parseInt(param[5]);
                Long fileSize = Long.parseLong(param[3]);
				String fileName = param[4]; 
	        	fileName = jieShouLixian(readStr);
                BoaoLixian lixian = new BoaoLixian(fuid,tuid,fileSize.toString(),fileName,getDateTime(),false);
	        	String fn = writeToDB(lixian);
                if ( file != null && file.exists() && !fn.equals(fileName) ){
                    fileName = fn;
                    file.renameTo(new File(System.getProperty("user.dir") + UPFILE + fileName) );
                }
                //写入数据库后再看该用户在不在线，在线就通知个接收离线
                for ( int i = 0; i < clientList.size(); i ++ ){
                    ClientBean cb = clientList.get(i);
                    if ( cb.isFindUid(tuid) ){
                        SendFileThread t = new SendFileThread(cb.getIp(), fileName, fuid, tuid);
                    	threadVec.add(t);
                    	t.start();
                        break;
                    }
                }
			} catch (Exception ex) {
				com.server.LogJT.LogMsg(ex.toString(),ex);
			}
		}
        private String createNewFile(String fileName){
            int x = 1;
            String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "(" + x + ")"
                                    + fileName.substring(fileName.lastIndexOf("."));
            while( true ){
                File f = new File(System.getProperty("user.dir") + UPFILE + newFileName);
                if ( !f.exists() )
                    break;
                x ++;
                newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "(" + x + ")"
                                    + fileName.substring(fileName.lastIndexOf("."));
            }
            return newFileName;
        }
		private String jieShouLixian(String readStr){
			String[] param = readStr.split(FG);
			String fileName = param[4];
			try {
				sos = new Socket(myIp, Integer.parseInt(param[5]));
                                bis = new BufferedInputStream(sos.getInputStream());
				File f = new File(System.getProperty("user.dir") + UPFILE + fileName);
				try{
	                if ( f.exists() ){
                        fileName = createNewFile(fileName);
                        f = new File(System.getProperty("user.dir") + UPFILE + fileName);
                    }
                    f.createNewFile();
                    file = f;
	                fos = new FileOutputStream(f);
	                bos = new BufferedOutputStream(fos); 
	                if (1==1){
	                    byte[] bs = new byte[MAX_LENGTH];
	                    int len = bis.read(bs);
	                    while( len != -1 ){
	                        bos.write(bs,0, len);
	                        bos.flush();
	                        len = bis.read(bs);
	                    } 
	                }
	            }catch(Exception ex){
	                com.server.LogJT.LogMsg(ex.toString(),ex);
	            }finally { 
	                if ( bis != null ) bis.close();
	                if ( bos != null ) bos.close();
	                if ( fos != null ) fos.close();
	                if ( sos != null ) sos.close();
	            } 
			} catch (Exception e) {
				com.server.LogJT.LogMsg(e.toString(),e);
			}
            return fileName;
		}
		//"8" + FG + myId + FG + clientId + FG + fileBytes + FG + fileName + FG + port;
		private String writeToDB(BoaoLixian lixian){ //若在写时发现有同名的就换名称,返回新名称
			try{
				dao = dao == null ? (IDao) springContext.getBean(DAO_IMPL): dao;
                String fn = lixian.getFilename();
                if ( dao.checkFileNameChongFu(fn) ){
                    int n = (int)(Math.random()*999999) + 100000;
                    fn = fn.substring(0, fn.lastIndexOf(".")) + n + fn.substring(fn.lastIndexOf("."));
                }            
                lixian.setFilename(fn);
				dao.save(lixian);
                return fn;
			}catch(Exception e){
				com.server.LogJT.LogMsg(e.toString(),e);
			}
            return lixian.getFilename();
		}
	}
    
    
    
    
    
    
	
	private class SendFileThread extends Thread {  
		private String fileName;
		private BufferedInputStream bis;
	    private FileInputStream fis ;
	    private BufferedOutputStream bos;
	    private ServerSocket sst;
	    private Socket cs;
	    private Integer tuid, fuid;
	    private File file;
	    private boolean isWait = false; 
        private InetAddress clientIp;

        //当离线文件发到服务器后，接收者在线的情况下，服务器立即转给接收者(fuid赋给了tuid)
        public SendFileThread(InetAddress ip,String fileName, Integer fuid, Integer tuid){
	    	clientIp = ip;
			this.fileName = fileName;
			this.tuid = fuid;
            this.fuid = tuid;
			file = new File(System.getProperty("user.dir") + UPFILE + fileName); 
		}
	    public void deleteFile(){
            if ( file != null && file.exists() ){
                file.delete();
            }
        }
	    public void init(){
	    	try{ 
            	while(true){
	            	try {
	            		sst = new ServerSocket(lixianPort);
	                    break;
	                } catch (BindException ex) {  
	                	lixianPort ++; continue;
	                } catch (IOException ex) {
	                	com.server.LogJT.LogMsg(ex.toString(),ex);
	                    return;
	                } 
                }
            	String str = "F" + FG + tuid + FG + fuid  + FG + file.length() 
            		+ FG + fileName + FG + lixianPort + FG + serverIp + FG + this.getId();
    	    	sendLixianFileTongZhi(str);
                
    	    	lixianPort ++; 
    	    	if ( lixianPort > 7999 ) lixianPort = 1981;  
            	sst.setReceiveBufferSize(MAX_LENGTH);
                cs = sst.accept();
                isWait = true; 
	    	}catch(Exception e){
	    		com.server.LogJT.LogMsg(e.toString(),e);
	    	} 
	    }
	    private void sendLixianFileTongZhi(String readStr){
			try{
            	for ( int i = 0; i < clientList.size(); i ++ ){
                    ClientBean cb = clientList.get(i);
                    if ( cb.isFindUid(fuid) ){
                        cb.sendToServer(readStr);
                        break;
                    }
                }
			}catch(Exception e){
				com.server.LogJT.LogMsg(e.toString(),e);
			}
		}
        @Override
        public void run() {
            try{
            	if ( !file.exists() || clientIp == null ) return;
            	init();
            	while( true ){
            		if ( isWait ){
            			fis = new FileInputStream(file);
                    	bis = new BufferedInputStream(fis); 
            			bos = new BufferedOutputStream(cs.getOutputStream()); 
		                byte[] bs = new byte[MAX_LENGTH];
		                int len = bis.read(bs); 
		                while( len != -1 ){
		                    bos.write(bs, 0, len);
		                    bos.flush(); 
		                    len = bis.read(bs);
		                }
                        closeObj();
                        file.delete();
		                isWait = false;
		                break;
            		}
            		Thread.sleep(500);
            	} 
            }catch(Exception e){
                com.server.LogJT.LogMsg(e.toString(),e);
            }finally {
                closeObj();
                threadVec.remove(this);
            }
        }
        public void closeObj(){ 
        	try {
            	if ( bos != null ) bos.close(); bos = null;
				if ( bis != null ) bis.close(); bis = null;
				if ( fis != null ) fis.close(); fis = null;
				if ( cs != null ) cs.close(); cs = null;
				if ( sst != null ) sst.close(); sst = null;
			} catch (Exception ex) {
                com.server.LogJT.LogMsg(ex.toString(),ex);
			}
        }
    }
	
	private class ClientBean implements Runnable {
		private Socket socket; 
        private BufferedInputStream bis;
        private BufferedOutputStream bos;
        private boolean cont = false; 
        private List<byte[]> bytesList;
        
        private InetAddress ip;
        private Integer myId = 0;
        
        public InetAddress getIp(){
            return ip;
        }
        public Integer getUid(){
            return myId;
        }
        public ClientBean(InetAddress ip, Integer uid){
            this.ip = ip;
            this.myId = uid;
        }
        
        public boolean isFindUid(Integer uid){
            if ( myId == null && uid != null ) return false;
            return this.myId.equals(uid);
        }
        public boolean isFindIp(InetAddress ip){
            return this.ip.getHostAddress().equalsIgnoreCase(ip.getHostAddress());
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + myId;
            return result;
        }
        @Override
        public boolean equals(Object obj) { 
            if (obj == null || getClass() != obj.getClass() )
              return false;
            if (this == obj)
              return true;
            if ( myId.equals(((ClientBean) obj).getUid())
                    && ip.getHostAddress().equalsIgnoreCase( ((ClientBean) obj).getIp().getHostAddress() ) )
                return true;
            else
                return false;
        }
        
	    public ClientBean(Socket socket){
            this.socket = socket;
            try { 
                bis = new BufferedInputStream(socket.getInputStream());
                bos = new BufferedOutputStream(socket.getOutputStream());
                ip = socket.getInetAddress();
                cont = true;
            } catch (Exception e) {
                com.server.LogJT.LogMsg(e.toString(),e);
            } 
        }
        public ClientBean(){
            
        }
		public void go() {
            try { 
                serverSocket2 = new ServerSocket(sendPort); 
                serverSocket2.setReceiveBufferSize(MAX_LENGTH);
            } catch (BindException e) {
                com.server.LogJT.LogMsg(e.toString(),e);
                System.exit(0);
            } catch (IOException e) {
                com.server.LogJT.LogMsg(e.toString(),e);
            } 
            try { 
                while (isReady) {
                    Socket sk = serverSocket2.accept();
                    ClientBean cs = new ClientBean(sk);
                    int x = clientList.indexOf(cs);
                    if ( x == -1 )
                        clientList.add(cs);
                    else
                        clientList.set(x, cs);
                    cont = true;
                    new Thread(cs).start();
                }
            } catch (IOException e) {
                com.server.LogJT.LogMsg(e.toString(),e);
            } finally {
                closeServerSocket();
            }
		} 
		
        @Override
        public void run() { 
            try {
                int firstLength = -1;
                bytesList = new ArrayList<byte[]>();
                while (cont) {
                    byte[] bs = new byte[MAX_LENGTH];
                    int len = bis.read(bs);
                    if ( len == -1 ) break;
                    byte[] tb = new byte[len];
                    for ( int i = 0; i < len; i ++ ){
                        tb[i] = bs[i];
                    }
                    bytesList.add(tb);//保存本次接收的字节内容到集合
                    if ( firstLength == -1 )
                        firstLength = getFirstPackBytes();
                    if ( firstLength == - 1 ){ //没找到头结束的话，就继续收数据，直到找到 ChatMain.END_TAG为止
                        continue;
                    }
                    String readStr = getFirstStr();
//                    area.append("刚收到=" + readStr + "\n");
                    firstLength = -1;
                    bytesList.clear();
                    readStr = readStr.substring(0, readStr.indexOf(END_TAG) );
                    if ( readStr.charAt(0) == '0' ){
                        if ( readStr.lastIndexOf(FENGEFU) == 1 ){
                            readStr = "U";
                        }else{
                            String version = readStr.substring(readStr.lastIndexOf(FENGEFU) + FENGEFU.length());
                            if ( !version.equals( clientVersion ) ){
                                readStr = "U";
                            }else{
                                readStr = readStr.substring(0, readStr.lastIndexOf(FENGEFU));
                                Integer uid = Integer.parseInt(readStr.substring(2)); 
                                this.myId = uid;
                                BoaoUser user = (BoaoUser) dao.get(BoaoUser.class, uid);
                                user.setOnline(1);
                                dao.update(user);
                            }
                        }
                    }else if ( readStr.charAt(0) == '7' ){ 
                    	String[] param = readStr.split(FG);
                    	final Integer tuid = Integer.parseInt(param[2]);
                    	final String str = readStr + FG + ip.getHostAddress();
                        sendFileTongZhi(tuid, str);
                    	continue;
                    }else if ( readStr.charAt(0) == '8' ){ 
                        String[] param = readStr.split(FG);
                        String str = "C" + FG + param[1] + FG + param[2] + FG + param[5];
                        cancelSendFile(str);
                        readStr += FG + serverIp;
                    	new LixianWenJian(readStr, ip).start();
                    	continue;
                    }else if ( readStr.charAt(0) == '9' ){
                        clientList.remove(this);
                        String[] param = readStr.substring(2).split(FG);
                        for ( int k = 1; k < param.length; k ++ ){ //第一个是退出的用户id，从第2个参数开始是经程ID
                            Long tid = Long.parseLong(param[k]);
                            for ( int i = 0; i < threadVec.size(); i++ ){
                                SendFileThread t = threadVec.get(i);
                                if ( t.getId() == tid.longValue()){
                                    threadVec.remove(t); t.stop();
                                    t.closeObj(); t = null; i--;
                                }
                            }
                        }
                    }else if ( readStr.charAt(0) == 'C' || readStr.charAt(0) == 'A' ){  
                    	cancelSendFile(readStr); //取消在线文件发送
                    	continue;
                    }else if ( readStr.charAt(0) == 'L' ){ 
                    	String[] param = readStr.split(FG);
                    	Integer fuid = Integer.parseInt(param[1]);
                    	Integer tuid = Integer.parseInt(param[2]);
                    	String fileName = param[3];
                    	SendFileThread t = new SendFileThread(ip,fileName,fuid,tuid);
                    	threadVec.add(t); 
                    	t.start();
                    	continue;
                    }else if ( readStr.charAt(0) == 'T' ){ //客户端退了，让服务器清理线程
                    	String[] param = readStr.split(FG);
                        for ( int k = 1; k < param.length; k ++ ){
                            Long tid = Long.parseLong(param[k]);
                            for ( int i = 0; i < threadVec.size(); i++ ){
                                SendFileThread t = threadVec.get(i);
                                if ( t.getId() == tid.longValue()){
                                    threadVec.remove(t); t.stop(); 
                                    t.closeObj(); t = null; i--;
                                }
                            }
                        }
                    	continue;
                    }else if ( readStr.charAt(0) == 'D' ){ //客户端退了，让服务器清理线程，同时删除未收的文件
                    	String[] param = readStr.split(FG);
                        for ( int k = 1; k < param.length; k ++ ){
                            Long tid = Long.parseLong(param[k]);
                            for ( int i = 0; i < threadVec.size(); i++ ){
                                SendFileThread t = threadVec.get(i);
                                if ( t.getId() == tid.longValue()){
                                    t.deleteFile();
                                    threadVec.remove(t); t.stop(); 
                                    t.closeObj(); t = null; i--;
                                }
                            }
                        }
                    	continue;
                    }else if ( readStr.charAt(0) == 'S' ){ //发送一个闪屏
                    	String[] param = readStr.split(FG); 
                    	final Integer tuid = Integer.parseInt(param[2]);
                        final String str = readStr;
                        sendShanPingFlag(tuid, str);
                        continue;
                    }
                    else if ( readStr.charAt(0) == 'I' ){ //客户端发来求救信号 I,客户端id
                    	Integer uid = Integer.parseInt(readStr.substring(2));
                        readStr = "I";
                        dropClient(uid, readStr);
                        continue;
                    }
                    //开始向在线客户端（除自己外）发送更新消息标志
                    if ( readStr.equals("U") ){
                        sendToServer(readStr);
                    }else{
                        for ( int i = 0; i < clientList.size(); i ++ ){
                            final ClientBean cb = clientList.get(i);
                            if ( cb.equals( this ) ) continue;
                            if( cb.getUid() == null || cb.socket == null ) continue;
                            final String rstr = readStr;
                            new Thread(new Runnable(){ //每个客户端启动一个线程给发通知
                                @Override
                                public void run() {
                                    try{
                                        cb.sendToServer(rstr);
                                    }
                                    catch(Exception ex){
                                        com.server.LogJT.LogMsg("服务器转发(" + rstr + ")给客户端(" + cb.getUid() + ")时异常:" + ex.toString(),ex);
                                    }
                                }
                            }).start();
                        }
                        if ( area.getLineCount() > LINE_COUNT ){
                            area.setText("");
                        }
                        area.append("== " + getDateTime() + " ==当前在线人数== " + clientList.size() + " ==\n");
                    }
//                    0-9 A C D F L S T U K I
//                    area.append("===" + getDateTime() + "===当前在线人数===" + clientList.size() + "===\n"); 
                }
            } catch (Exception ex){
                //客户端主动断开的，不管
//                com.server.LogJT.LogMsg(ex.toString(),ex);
            } finally {
                if ( clientList.contains(this))
                    clientList.remove(this);
                try {
                    if ( bis != null )
                        bis.close();
                    if ( bos != null )
                        bos.close();
                    if (socket != null) {
                        socket.close();
                        socket = null; 
                    }
                    myId = null;
                } catch (IOException ex) {
                    com.server.LogJT.LogMsg(ex.toString(),ex);
                } 
            }
        }
        
        private int getFirstPackBytes( ){ 
            byte[] xb = new byte[getCurrSize()];
            int xi = 0;
            for ( int i = 0; i < bytesList.size(); i ++ ){
                byte[] b = (byte[]) bytesList.get(i);
                for ( int j = 0; j < b.length; j ++ ){
                    xb[xi++] = b[j];
                }
            }
            String str = new String(xb);
            int n = str.indexOf(END_TAG);
            if ( n != -1 )
                return str.substring(0,n+END_TAG.length()).getBytes().length;
            return n;
        }
        private String getFirstStr() throws UnsupportedEncodingException{ 
            byte[] xb = new byte[getCurrSize()];
            int xi = 0;
            for ( int i = 0; i < bytesList.size(); i ++ ){
                byte[] b = (byte[]) bytesList.get(i);
                for ( int j = 0; j < b.length; j ++ ){
                    xb[xi++] = b[j];
                }
            }
            String str = new String(xb, CHARSET);
            
            return str;
        }
        private int getCurrSize( ){ 
            int length = 0;
            for ( int i = 0; i < bytesList.size(); i ++ ){
                length += ((byte[]) bytesList.get(i)).length;
            }
            return length;
        }
        
        private void closeServerSocket(){
            try{
                if ( serverSocket != null ){
                    serverSocket.close();
                }
            }catch(IOException e){
                com.server.LogJT.LogMsg(e.toString(),e);
            }
		}
		private void sendShanPingFlag(Integer tuid, String readStr){
			try{
				ClientBean bean = null;
				for ( int i = 0; i < clientList.size(); i ++ ){
                    ClientBean cb = clientList.get(i);
                    if ( cb.isFindUid(tuid) ){
                        bean = cb; break;
                    }
                } 
                if ( bean != null ){
	            	bean.sendToServer(readStr);
                }
			}catch(Exception e){
				com.server.LogJT.LogMsg(e.toString(),e);
			}
		}
        //先将服务器中所有此客户端的信息删除，再向此客户端发送信息，让它断线，全新登录
        private void dropClient(Integer uid, String readStr){
            try{
				for ( int i = 0; i < clientList.size(); i ++ ){
                    ClientBean cb = clientList.get(i);
                    if ( cb != null && cb.isFindUid(uid) ){
                        clientList.remove(i); i --;
                    }
                }
                for ( int i = 0; i < socketList.size(); i ++ ){
                    ForClientThread c = (ForClientThread) socketList.get(i);
                    if ( c != null && c.fromuserid != null && c.fromuserid.equals(uid) ){
                        socketList.remove(i); i --;
                    }
                }
	            sendToServer(readStr);//让客户端断线重新登录
			}catch(Exception e){
				com.server.LogJT.LogMsg(e.toString(),e);
			}
        }
        public byte[] stringToByte(String str){
            try {
                return str.getBytes(CHARSET);
            } catch (UnsupportedEncodingException ex) {
                com.server.LogJT.LogMsg(ex.toString(),ex);
            }
            return null;
        } 
        public void sendToServer(String content){
            try {
                if( myId == null || socket == null || socket.isClosed() ) return;
                byte[] bs = stringToByte(content + END_TAG);
                bos.write(bs);
                bos.flush();
            }catch (Exception ex){
                area.append("=" + getDateTime() + "=给客户端[" + myId + "]发送[" + content + "]时异常。\n");
                com.server.LogJT.LogMsg("===给客户端[" + myId + "]发送[" + content + "]时异常。");
                com.server.LogJT.LogMsg(ex.toString(),ex);
                clientList.remove(this);
            }
        }   
		private void sendFileTongZhi(Integer tuid, String readStr){
			try{
				ClientBean bean = null;
				for ( int i = 0; i < clientList.size(); i ++ ){
                    ClientBean cb = clientList.get(i);
                    if ( cb.isFindUid(tuid) ){
                        bean = cb; break;
                    }
                } 
                if ( bean != null ){
	                bean.sendToServer(readStr);
                }
			}catch(Exception e){
				com.server.LogJT.LogMsg(e.toString(),e);
			}
		}
        
        private void cancelSendFile(final String readStr){
            final Integer tuid = Integer.parseInt(readStr.split(FG)[2]);
            for ( int i = 0; i < clientList.size(); i ++ ){
                ClientBean cb = clientList.get(i);
                if( cb.getUid() == null || cb.socket == null ) continue;
                if ( !cb.getUid().equals(tuid) ) continue;                 
                try{
                    cb.sendToServer(readStr);
                }
                catch(Exception ex){
                    com.server.LogJT.LogMsg("服务器转发(" + readStr + ")给客户端(" + tuid + ")时异常:" + ex.toString(),ex);
                }
                break;
            }
        }
    }
    
    
    
    
    
    private boolean containsClientList(Integer uid){ //检查客户端是否在线
        boolean isZai = false; 
        for ( int i = 0; i < clientList.size(); i ++ ){
            ClientBean cb = clientList.get(i);
            if ( cb.isFindUid(uid) ){
                isZai = true; break;
            }
        }
        return isZai;
    }
//    private boolean containsClientList(InetAddress ip, Integer uid){ //检查刚登录或准备退出的客户端是否在线
//        boolean isZai = false; 
//        for ( int i = 0; i < clientList.size(); i ++ ){
//            ClientBean cb = clientList.get(i);
//            if ( cb.isFindIp(ip) && cb.isFindUid(uid) ){
//                isZai = true; break;
//            }
//        } 
//        return isZai;
//    }
//	private class ClientBean implements java.io.Serializable{
//        private InetAddress ip;
//        private Integer uid;
//        public InetAddress getIp(){
//            return ip;
//        }
//        public Integer getUid(){
//            return uid;
//        }
//        public ClientBean(InetAddress ip, Integer uid){
//            this.ip = ip;
//            this.uid = uid;
//        }
//        public ClientBean(InetAddress ip ){
//            this.ip = ip; 
//        }
//        public ClientBean(Integer uid){
//            this.uid = uid;
//        }
//        public boolean isFindUid(Integer uid){
//            return this.uid.equals(uid);
//        }
//        public boolean isFindIp(InetAddress ip){
//            return this.ip.getHostAddress().equalsIgnoreCase(ip.getHostAddress());
//        }
//        
//        @Override
//        public int hashCode() {
//            final int prime = 31;
//            int result = 1;
//            result = prime * result + uid;
//            return result;
//        }
//        @Override
//        public boolean equals(Object obj) { 
//            if (obj == null || getClass() != obj.getClass() )
//              return false;
//            if (this == obj)
//              return true;
//            if ( uid.equals(((ClientBean) obj).getUid())
//                    && ip.getHostAddress().equalsIgnoreCase( ((ClientBean) obj).getIp().getHostAddress() ) )
//                return true;
//            else
//                return false;
//        }
//    }
//	public static void main(String[] args) {
//		new ServerMain().go();
//	} 
}

