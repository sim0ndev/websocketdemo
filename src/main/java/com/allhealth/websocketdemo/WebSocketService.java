package com.allhealth.websocketdemo;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


@ServerEndpoint("/websocket/{info}")
@Component
public class WebSocketService {
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;
	//concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
	private static CopyOnWriteArraySet<WebSocketService> webSocketSet = new CopyOnWriteArraySet<WebSocketService>();

	private static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//创建时间格式对象

	//concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketService对象。
	//创建一个房间的集合，用来存放房间
	private static ConcurrentHashMap<String,ConcurrentHashMap<String, WebSocketService>> roomList = new  ConcurrentHashMap<String,ConcurrentHashMap<String, WebSocketService>>();
	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;
    //重新加入房间的标示；
    private int rejoin = 0;
    static {
    	roomList.put("room1", new ConcurrentHashMap<String, WebSocketService>());
    	roomList.put("room2", new ConcurrentHashMap<String, WebSocketService>());
    }
	/**
	 * 用户接入
	 * @param session 
	 */
	@OnOpen
	public void onOpen(@PathParam(value = "info") String param, Session session){
		this.session = session;
		webSocketSet.add(this);     //加入set中

		String flag = param.split("[|]")[0]; 		//标识
		String member = param.split("[|]")[1];		//成员名
		if(flag.equals("join")){
			String user = param.split("[|]")[2];
			joinRoom(member,user);
			
		}
	}
	
	//加入房间
	public void joinRoom(String member,String user){
		ConcurrentHashMap<String, WebSocketService> r =  roomList.get(member);
		if(r.get(user) != null){		//该用户有没有出
			this.rejoin = 1;
		}
		r.put(user, this);//将此用户加入房间中
	}
	public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
	/**
	 * 接收到来自用户的消息
	 * @param message
	 * @param session
	 * @throws IOException 
	 */
	@OnMessage
	public void onMessage(String message, Session session) throws IOException{
		//把用户发来的消息解析为JSON对象
		JSONObject obj = JSONObject.fromObject(message);
		if(obj.get("flag").toString().equals("exitroom")){		//退出房间操作
			String roomid = obj.get("roomid").toString();
			//将用户从聊天室中移除
			int f2 = 1;
			roomList.get(roomid).remove(obj.get("nickname").toString());//将用户直接移除
			if(roomList.get(roomid).size() == 0){//判断房间该房间是否还有用户，如果没有，则将此房间也移除
				f2 = 2;
			}
			if(f2 == 1){		//证明该房间还有其它成员，则通知其它成员更新列表
				obj.put("flag","exitroom");
				String m = obj.get("nickname").toString()+" 退出了房间";
				obj.put("message", m);
				ConcurrentHashMap<String, WebSocketService> r =roomList.get(roomid);
				List<String> uname = new ArrayList<String>();
				for(String u:r.keySet()){
					uname.add(u);
				}
				obj.put("uname", uname.toArray());
				for(String i:r.keySet()){  //遍历该房间
					r.get(i).sendMessage(obj.toString());//调用方法 将消息推送
				}
			}
		}else if(obj.get("flag").toString().equals("chatroom")){		//聊天室的消息 加入房间/发送消息
			//向JSON对象中添加发送时间
			obj.put("date", df.format(new Date()));
			//获取客户端发送的数据中的内容---房间�? 用于区别该消息是来自于哪个房间
			String roomid = obj.get("target").toString();
			//获取客户端发送的数据中的内容---用户
			String username = obj.get("nickname").toString();
			//从房间列表中定位到该房间
			ConcurrentHashMap<String, WebSocketService> r =roomList.get(roomid);
			List<String> uname = new ArrayList<String>();
			for(String u:r.keySet()){
				uname.add(u);
			}
			obj.put("uname", uname.toArray());
			if(r.get(username).rejoin == 0){			//证明不是退出重连
				for(String i:r.keySet()){  //遍历该房间
					obj.put("isSelf", username.equals(i));//设置消息是否为自己的
					r.get(i).sendMessage(obj.toString());//调用方法 将消息推送
				}
			}else{
				obj.put("isSelf", true);
				r.get(username).sendMessage(obj.toString());
			}
			r.get(username).rejoin = 0;
		}
		
	}
	
	/*
	 * @Author Simon
	 * @Description 用户连接断开
	 * @Date 18:26 2018/12/24
	 * @Param [session]
	 * @return void
	 **/
	@OnClose
	public void onClose(Session session){
		webSocketSet.remove(this);  //从set中删除
		subOnlineCount();           //在线数减1
		System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
	}
	
	/*
	 * @Author Simon
	 * @Description 用户连接异常
	 * @Date 18:25 2018/12/24
	 * @Param [session, error]
	 * @return void
	 **/
	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("发生错误");
		error.printStackTrace();
	}

	/*
	 * @Author Simon
	 * @Description //返回在线人数
	 * @Date 18:24 2018/12/24
	 * @Param []
	 * @return int
	 **/
	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	/*
	 * @Author Simon
	 * @Description //在线人数+1
	 * @Date 18:24 2018/12/24
	 * @Param []
	 * @return int
	 **/
	public static synchronized void addOnlineCount() {
		WebSocketService.onlineCount++;
	}

	/*
	 * @Author Simon
	 * @Description //在线人数-1
	 * @Date 18:24 2018/12/24
	 * @Param []
	 * @return int
	 **/
	public static synchronized void subOnlineCount() {
		WebSocketService.onlineCount--;
	}
}
