import javax.swing.*;

import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.awt.*;
import java.awt.List;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Client {
	Frame f;
	TextArea content;
	TextField txt_message;
	Button send;
	List onLineList;
	TextField name;
	TextField hostIp;
	TextField hostPort;
	Button connect;
	Button disconnect;
	Panel top_left;
	Panel top_bottom;
	Panel top;
	Panel bottom;
	private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private MessageThread messageThread;
	boolean isconnected = false;
	
	public void send() {
		if (!isconnected) {
			JOptionPane.showMessageDialog(f, "Haven't connect to server and can't send message", "error", JOptionPane
					.ERROR_MESSAGE);
			return;
		}
		String message = txt_message.getText().trim();
		if (message == null || message == "") {
			JOptionPane.showMessageDialog(f, "can't send null message!", "error", JOptionPane
					.ERROR_MESSAGE);
			return;
		}
		sendMessage("Longfeng" + "@" + "ALL" + "@" + message);
		txt_message.setText(null);
	}
	public Client() {
		Frame f = new Frame("Client");
		f.setLayout(new BorderLayout());
		content = new TextArea();
		content.setEditable(false);
		txt_message = new TextField();
		send = new Button("send");
		onLineList = new List();
		name = new TextField("Longfeng");
		hostIp = new TextField("127.0.0.1");
		hostPort = new TextField("6666");
		connect = new Button("connect");
		disconnect = new Button("disconnect");
		top_left = new Panel();
		top_left.setLayout(new BorderLayout());
		top_bottom = new Panel();
		top_bottom.setLayout(new BorderLayout());
		top = new Panel();
		top.setLayout(new BorderLayout());
		bottom = new Panel();
		bottom.setLayout(new GridLayout(1, 5));
		top_bottom.add(txt_message, BorderLayout.CENTER);
		top_bottom.add(send, BorderLayout.EAST);
		top_left.add(content, BorderLayout.NORTH);
		top_left.add(top_bottom, BorderLayout.SOUTH);
		top.add(top_left, BorderLayout.WEST);
		top.add(onLineList, BorderLayout.EAST);
		bottom.add(name);
		bottom.add(hostIp);
		bottom.add(hostPort);
		bottom.add(connect);
		bottom.add(disconnect);
		f.add(top, BorderLayout.NORTH);
		f.add(bottom, BorderLayout.SOUTH);
		f.pack();
		f.setVisible(true);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		txt_message.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				send();
			}
		});
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port;
				if (isconnected) {
					JOptionPane.showMessageDialog(f, "Already connect to server", "error", JOptionPane
							.ERROR_MESSAGE);
					return;
				}
				try {
					try {
						port = Integer.parseInt(hostPort.getText().trim());
					} catch (NumberFormatException e2) {
						throw new Exception("Port number is not in right format");
					}
					String ip = hostIp.getText().trim();
					String username = name.getText().trim();
					if (username == "" || ip == "") {
						throw new Exception("ip and username can't be nothing");
					}
					boolean flag = connectServer(port, ip, username);
					if (flag == false) {
						throw new Exception("fail to connect to server");
					}
					//f.setTitle(username);
					JOptionPane.showMessageDialog(f, "Connect Sucessfully!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(f, exc.getMessage(), "error", JOptionPane
							.ERROR_MESSAGE);
				}
			}
		});
		disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isconnected) {
					JOptionPane.showMessageDialog(f, "didn't connect to server and don't need to disconnect", "error", JOptionPane
							.ERROR_MESSAGE);
					return;
				}
				try {
					boolean flag = closeConnection();
					if (flag == false) {
						throw new Exception("Error happened when try to disconnect");
					}
					JOptionPane.showMessageDialog(f, "disconnect successfully");
				} catch (Exception exc) {
                    JOptionPane.showMessageDialog(f, exc.getMessage(),
                            "error", JOptionPane.ERROR_MESSAGE);
                }
			}
		}); 
	}
	
	public synchronized boolean closeConnection() {
        try {
            sendMessage("CLOSE");// 发送断开连接命令给服务器
            messageThread.stop();// 停止接受消息线程
            // 释放资源
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            isconnected = false;
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
            isconnected = true;
            return false;
        }
    }
	
	public boolean connectServer(int port, String ip, String username) {
		try {
			socket = new Socket(ip, port);
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sendMessage(username + "@" + socket.getLocalAddress().toString());
			messageThread = new MessageThread(reader, content);
			messageThread.start();
			isconnected = true;
			return true;
		} catch (Exception e) {
			content.append("Server port: " + port + "  IP:  " + ip + "fail to connect to server" + "\r\n");
			isconnected = false;
			return false;
		}
	}
	public static void main(String[] args) {
		new Client();
	}
	
	public void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}
	
	class MessageThread extends Thread {
		private BufferedReader reader;
		private TextArea content;
		public MessageThread(BufferedReader reader, TextArea content) {
			this.reader = reader;
			this.content = content;
		}
		public synchronized void closeCon() throws Exception {
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isconnected = false;
		}
		public void run() {
			String message = "";
			while (true) {
				try {
					message = reader.readLine();
					StringTokenizer stringTokenizer = new StringTokenizer(message, "/@");
					String command = stringTokenizer.nextToken();
					if (command.equals("CLOSE")) {
						content.append("Sever is turned off!");
						closeCon();
						return;
					} else if (command.equals("ADD")) {
						String username = "";
						String userip = "";
					}else {// 普通消息
                        content.append(message + "\r\n");
                    }
				} catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
			}
		}
	}
}
