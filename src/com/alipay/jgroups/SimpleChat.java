/**
 * hongxin.xu@alipay.com
 * 2011-10-15
 */
package com.alipay.jgroups;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

/**
 * 利用Jgroups编写的一个简单的聊天程序。
 * 
 * @author ibm
 * 
 */
public class SimpleChat extends ReceiverAdapter {
	public static void main(String[] args) throws Exception {
		/** 调用启动函数。 */
		new SimpleChat().start();
	}

	// 状态信息。
	private final List<String> state = new LinkedList<String>();

	/** 通道。 */
	JChannel channle;

	/** 用户名。 */
	String user_name = System.getProperty("user.name", "n/a");

	private void eventloop() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			try {
				System.out.println(">");
				System.out.flush();
				String line = in.readLine().toLowerCase();
				if (line.startsWith("quit") || line.startsWith("exit")) {
					break;
				}
				line = "[" + user_name + "]" + line;
				Message msg = new Message(null, null, line);
				channle.send(msg);
			} catch (Exception e) {

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgroups.ReceiverAdapter#getState(java.io.OutputStream)
	 */
	@Override
	public void getState(OutputStream output) throws Exception {
		// TODO Auto-generated method stub
		synchronized (state) {
			Util.objectToStream(state, new DataOutputStream(output));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgroups.ReceiverAdapter#receive(org.jgroups.Message)
	 */
	@Override
	public void receive(Message msg) {
		// TODO Auto-generated method stub
		System.out.println(msg.getSrc() + ":" + msg.getObject());

		synchronized (state) {
			state.add(msg.getSrc() + ":" + msg.getObject());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgroups.ReceiverAdapter#setState(java.io.InputStream)
	 */
	@Override
	public void setState(InputStream input) throws Exception {
		// TODO Auto-generated method stub
		List<String> stateLine;
		stateLine = (List<String>) Util.objectFromStream(new DataInputStream(
				input));
		synchronized (state) {
			state.clear();
			state.addAll(stateLine);
		}
		System.out.println(stateLine.size() + "message in chat history");
		for (String str : stateLine) {
			System.out.println(str);
		}
	}

	/**
	 * 启动连接。
	 * 
	 * @throws Exception
	 */
	private void start() throws Exception {
		channle = new JChannel();
		channle.setReceiver(this);
		channle.connect("ChatCluster");
		channle.getState(null, 10000);
		eventloop();
		channle.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgroups.ReceiverAdapter#viewAccepted(org.jgroups.View)
	 */
	@Override
	public void viewAccepted(View view) {
		// TODO Auto-generated method stub
		System.out.println("** view" + view);
	}

}
