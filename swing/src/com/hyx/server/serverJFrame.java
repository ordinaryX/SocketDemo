package com.hyx.server;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.hyx.utils.IOUtil;
import com.hyx.utils.NetUtil;

public class serverJFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** ���յ��Ŀͻ� */
	private Socket socket;

	/** ������ */
	private ServerSocket server;

	/** �û��б� */
	private List<ForwardThread> list = new ArrayList<ForwardThread>();

	/** �����û��б��ı��� */
	private JTextArea userlistarea;

	/** ������������ť */
	private JButton stopButton;

	/** ������������ť */
	private JButton openButton;

	/** �˿ں��ı��� */
	private JTextField serverPort;

	/** ������IP�ı��� */
	private JTextField serverIP;

	/** ѭ�������û����ߵ��߳� */
	private AcceptClientThread rcvt;

	/** �߳�ֹͣ��ʶ�� */
	private boolean BL = false;

	/**
	 * serverJFrame�вι��캯����
	 * 
	 * @param title���÷��������ڱ���
	 */
	public serverJFrame(String title) {
		super(title);
		init();
	}

	/**
	 * serverJFrame�޲ι��캯����Ĭ�ϱ���server��
	 */
	public serverJFrame() {
		this("server");
	}

	/**
	 * @author Administrator �����û����ӵ��߳�
	 */
	private class AcceptClientThread extends Thread {

		public void run() {
			try {
				while (true) {
					if (BL) {
						break;
					}
					// ���տͻ��˵�����
					socket = server.accept();
					// Ϊ���ܵ��û�����ת���߳�
					ForwardThread ft = new ForwardThread(socket);
					// �洢���յ����û���ת���߳�
					list.add(ft);
					// �����߳�
					ft.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @author Administrator ת���߳���
	 */
	private class ForwardThread extends Thread {
		private Socket socket;
		private DataOutputStream out;
		private DataInputStream in;
		private String nickname;

		/**
		 * ForwradThread�вι��캯��
		 * 
		 * @param socket
		 *            �������յ����û�socket
		 * @throws IOException
		 */
		public ForwardThread(Socket socket) throws IOException {
			this.socket = socket;
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}

		public void run() {
			try {
				while (true) {
					if (BL) {
						break;
					}

					if (socket.isClosed()) {
						break;
					}

					// ���յ���Ϣ
					String msg = in.readUTF();

					// �ж���Ϣ����
					if (msg.startsWith("ON@")) {// �û�����
						// �ж��������ߵ��û���Ȼ������û��������list��ߵ������û�,ʵ�������û��б�ͬ��
						for (ForwardThread ft : list) {
							if (ft != this) {
								String otherusermsg = "ON@" + ft.nickname + "["
										+ ft.socket.getRemoteSocketAddress().toString() + "]";
								out.writeUTF(otherusermsg);
							}
						}

						nickname = msg.substring(3);
						// �����IP����Ϣ�ǲ��ǿ��Է������������û�
						String usermsg = nickname + "[" + socket.getRemoteSocketAddress().toString() + "]";
						userlistarea.append(usermsg + "\n");
						for (ForwardThread ft : list) {
							// ���ﲻ�ж��ǲ����Լ������Լ�������Ȼ���û������Լ���ʱ��Ͳ�������userlistarea����Լ���
							ft.out.writeUTF("ON@" + usermsg);
						}

					} else if (msg.startsWith("OFF@")) {// �û�����
						// �����ߵ������û����͸��û���������Ϣ
						for (ForwardThread ft : list) {
							if (ft != this) {
								ft.out.writeUTF(msg);
							}
						}
						// ��list���Ƴ����û�
						list.remove(this);
						// �ӷ�����userlistarea���Ƴ����û�������ˢ��һ��
						userlistarea.setText("");
						for (ForwardThread ft : list) {
							String flushusermsg = ft.nickname + "[" + ft.socket.getRemoteSocketAddress().toString()
									+ "]";
							userlistarea.append(flushusermsg + "\n");
						}
						// Ϊ�����������߿ͻ��ٷ�һ�鵱ǰ���߿ͻ�����ˢ�����������û���userlistarea
						for (ForwardThread ft : list) {
							for (ForwardThread ft2 : list) {
								String flushusermsg = "ON@" + ft2.nickname + "["
										+ ft2.socket.getRemoteSocketAddress().toString() + "]";
								ft.out.writeUTF(flushusermsg);
							}
						}
						// ������ǰҪ���ߵ��û���Socket,in,out
						IOUtil.close(in);
						IOUtil.close(out);
						NetUtil.close(socket);

						// ��ǰҪ���ߵ��û���������Ϊ��������ת���߳̽�����
						break;

					} else if (msg.startsWith("MSG@")) {// ��ͨ������Ϣ
						// ת����ȥ�����ø��Լ���
						for (ForwardThread ft : list) {
							if (ft != this) {
								ft.out.writeUTF(msg);
							}
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * �Ѵ��ڷ�����Ļ����
	 * 
	 * @param bl�Ƿ�Ѵ��ڷ�����Ļ����
	 */
	public void setcenter(boolean bl) {
		if (bl == true) {
			Toolkit kit = Toolkit.getDefaultToolkit();
			Dimension screen = kit.getScreenSize();
			this.setLocation((screen.width - this.getWidth()) / 2, (screen.height - this.getHeight()) / 2);
		}
	}

	/**
	 * ����������IP�ı���
	 * 
	 * @return JTextField
	 */
	public JTextField getserverIP() {
		serverIP = new JTextField(12);
		serverIP.setText("127.0.0.1");
		return serverIP;
	}

	/**
	 * �����˿ں��ı���
	 * 
	 * @return JTextField
	 */
	public JTextField getserverPort() {
		serverPort = new JTextField(4);
		serverPort.setText("8000");
		return serverPort;
	}

	/**
	 * ����������������ť�������ü���������ȡ�˿ں��ı����еĶ˿ںſ���������
	 * 
	 * @return JButton
	 */
	public JButton getOpenButton() {
		openButton = new JButton("����");
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int port = Integer.parseInt(serverPort.getText());

				try {
					// ����������
					server = new ServerSocket(port);
					userlistarea.setText("������״̬������\n");
					// ����ѭ�������û����ӵ��߳�
					BL = false;
					rcvt = new AcceptClientThread();
					rcvt.start();
					// ���ð�ť
					openButton.setEnabled(false);
					stopButton.setEnabled(true);

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});
		return openButton;
	}

	/**
	 * ����������������ť�������ü�����������������
	 * 
	 * @return
	 */
	public JButton getStopButton() {
		stopButton = new JButton("ֹͣ");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// ���������߿ͻ�����STOP��Ϣ
					for (ForwardThread ft : list) {
						ft.out.writeUTF("STOP");
					}
					// �ر����ж�Ӧ�����߿ͻ���Socket,���������
					for (ForwardThread ft : list) {
						IOUtil.close(ft.in);
						IOUtil.close(ft.out);
						NetUtil.close(ft.socket);
					}
					// �ر�ѭ�������û������߳�
					BL = true;
					// �رշ�����
					NetUtil.close(server);

					list.clear();

					// ���ð�ť
					stopButton.setEnabled(false);
					openButton.setEnabled(true);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}

				userlistarea.setText("");
				userlistarea.append("�������ر�\n");
			}
		});
		return stopButton;
	}

	/**
	 * ��������JPanel�������߷ŷ�����IP��ǩ���ı��򣬶˿ںű�ǩ���ı������Ӱ�ť��������ť
	 * 
	 * @return
	 */
	public JPanel NorthPanel() {
		// ���山��JPanel
		JPanel north = new JPanel();
		north.setBorder(new TitledBorder("����������"));
		// ��north��߷����
		// �Ŷ˿ںű�ǩ
		north.add(new JLabel("�˿ںţ�"));
		// �Ŷ˿ں��ı���
		north.add(getserverPort());
		// �ſ�����������ť
		north.add(getOpenButton());
		north.add(getStopButton());

		return north;
	}

	public JScrollPane getUserListArea() {
		userlistarea = new JTextArea();
		JScrollPane userlistscroll = new JScrollPane(userlistarea);
		return userlistscroll;
	}

	/**
	 * �����м�JPanel����,����������û��б��ı���
	 * 
	 * @return JPanel
	 */
	public JPanel CenterPanel() {
		JPanel center = new JPanel();
		center.setBorder(new TitledBorder("�����û���Ϣ"));
		BorderLayout layout = new BorderLayout();
		center.setLayout(layout);
		// ���������û��б��ı���
		center.add(getUserListArea());

		return center;
	}

	/**
	 * ��ʼ������������
	 */
	public void init() {

		this.setSize(600, 400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setcenter(true);
		// ��ȡ�������
		Container conPane = this.getContentPane();
		// ���������������
		// ���뱱�����
		conPane.add(NorthPanel(), BorderLayout.NORTH);
		// �����м����
		conPane.add(CenterPanel(), BorderLayout.CENTER);

		this.setVisible(true);

	}

	public static void main(String[] args) {
		new serverJFrame();
	}

}
