package com.hyx.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.hyx.utils.IOUtil;
import com.hyx.utils.NetUtil;

public class chatJFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** ������Ϣ���߳� */
	private ReceiveMsgThread rcvmt;

	/** ��ǰ�û������� */
	private DataInputStream in;

	/** ��ǰ�û������ */
	private DataOutputStream out;

	/** �û�Socket */
	private Socket client;

	/** �����¼�ı��� */
	private JTextArea recordlistarea;

	/** �����û��б��ı��� */
	private JTextArea userlistarea;

	/** ������ť */
	private JButton stopButton;

	/** ���Ӱ�ť */
	private JButton connectButton;

	/** �ǳ��ı��� */
	private JTextField nicknameField;

	/** �ǳ� */
	private String nickName;

	/** �˿ں��ı��� */
	private JTextField serverPort;

	/** ������IP�ı��� */
	private JTextField serverIP;

	/** ����������Ϣ���ı��� */
	private JTextField sendField;

	/** ���Ͱ�ť */
	private JButton sendButton;

	/** �̹߳رձ�ʶ�� */
	private boolean BL = false;

	private class ReceiveMsgThread extends Thread {

		public void run() {
			while (true) {
				if (BL) {
					break;
				}

				if (client.isClosed()) {
					break;
				}

				try {
					// ���յ�����Ϣ
					String msg = in.readUTF();
					// �ж���Ϣ������������Ϣ
					if (msg.startsWith("ON@")) {// �û�����
						String usermsg = msg.substring(3);
						userlistarea.append(usermsg + "\n");
					} else if (msg.startsWith("OFF@")) {// �û�����
						userlistarea.setText("");
					} else if (msg.startsWith("MSG@")) {// ��ͨ������Ϣ
						String recordmsg = msg.substring(4);
						recordlistarea.append(recordmsg + "\n");
					} else if (msg.equals("STOP")) {// �������ر���Ϣ
						// ������Ҫ�ر��ˣ��ر� ��ǰsocket��Ӧ�������� �������socket �Լ� ������Ϣ���̡߳�
						// �ر����������
						recordlistarea.append("�������ѹر�\n");
						IOUtil.close(in);
						IOUtil.close(out);
						// �ر�socket
						NetUtil.close(client);
						client = null;
						// ��������û��б�
						userlistarea.setText("");
						// ���ð�ť
						stopButton.setEnabled(false);
						connectButton.setEnabled(true);
						break;
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			} // while

		}

	}

	/**
	 * chatJFrame�вι��캯��
	 * 
	 * @param title
	 *            ������
	 */
	public chatJFrame(String title) {
		super(title);
		init();
	}

	/**
	 * chatJFrame�޲ι��캯��
	 */
	public chatJFrame() {
		this("����");
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
	 * ��������������Ϣ���ı���
	 * 
	 * @return JTextField
	 */
	public JTextField getSendField() {
		sendField = new JTextField(20);
		return sendField;
	}

	/**
	 * �������Ͱ�ť�������ü���������÷�����Ϣ�������Ϣ�ı���
	 * 
	 * @return JButton
	 */
	public JButton getSendButton() {
		sendButton = new JButton("����");
		sendButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// �жϵ�ǰ�û��Ƿ����ӵ�������
				if (client == null) {
					JOptionPane.showMessageDialog(null, "������δ���ӣ������ӷ�����");
				}
				// ��ȡ��Ϣ�ı����е���Ϣ
				String msg = sendField.getText();
				try {
					// �ж��ı�����Ϣ�ǲ���Ϊ�գ���Ϊ�գ����װ��Ϣ�����͸�����������������Ϣ�ı���
					if (!msg.isEmpty()) {
						String sendmsg = "MSG@" + nickName + ":" + msg;
						// ��ӵ��Լ���recordlistarea,��Ϊ������ת����ͨ������Ϣֻ�������û����������Լ�
						recordlistarea.append(nickName + ":" + msg + "\n");
						out.writeUTF(sendmsg);
						sendField.setText("");
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		});
		return sendButton;
	}

	/**
	 * �ϱ�JPanel����,�ŷ���������Ϣ�ı��򣬺ͷ��Ͱ�ť
	 * 
	 * @return JPanel
	 */
	public JPanel SouthPanel() {
		// �����ϱ�JPanel
		JPanel south = new JPanel();
		south.setBorder(new TitledBorder("��Ϣ����"));
		// ���ñ߿򲼾�
		BorderLayout layout = new BorderLayout();
		south.setLayout(layout);
		// ��south��߷������ı���
		south.add(getSendField());
		// ��south��߷ŷ��Ͱ�ť,���ñ߿򲼾ֺ��ð�ť������
		south.add(getSendButton(), BorderLayout.EAST);
		return south;
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
	 * �����ǳ��ı���
	 * 
	 * @return JTextField
	 */
	public JTextField getNickName() {
		nicknameField = new JTextField(8);
		nicknameField.setText("����");
		nickName = nicknameField.getText();
		return nicknameField;
	}

	/**
	 * �������Ӱ�ť�������ü����������÷�����Ip�Ͷ˿ں����ӷ�����
	 * 
	 * @return JButton
	 */
	public JButton getConnectButton() {
		connectButton = new JButton("����");

		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// ��ȡ�˿ں�
				int port = Integer.parseInt(serverPort.getText());
				String host = serverIP.getText();
				nickName = nicknameField.getText();
				try {
					// �����û�Socket
					client = new Socket(host, port);
					recordlistarea.append("�����ӷ�����\n");
					// ��ʼ����ǰ�ͻ��˵����������
					in = new DataInputStream(client.getInputStream());
					out = new DataOutputStream(client.getOutputStream());
					// �����������������Ϣ
					out.writeUTF("ON@" + nickName);
					// ��ӵ�ǰ�û�����Ϣ��userlistarea
					// userlistarea.append(nickName+"["+client.getLocalSocketAddress()+"]");
					// ����������Ϣ���߳�
					BL = false;
					rcvmt = new ReceiveMsgThread();
					rcvmt.start();
					// ���ð�ť
					connectButton.setEnabled(false);
					stopButton.setEnabled(true);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		});
		return connectButton;
	}

	/**
	 * ����������ť�������ü������������Ͽ��������������
	 * 
	 * @return JButton
	 */
	public JButton getStopButton() {
		stopButton = new JButton("����");
		stopButton.setEnabled(false);

		stopButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// ��װ������Ϣ����ʽ��OFF@�ǳơ�
				String offmsg = "OFF@" + nickName;
				recordlistarea.append("�������\n");

				try {
					// �����������������Ϣ
					out.writeUTF(offmsg);
					// �رս�����Ϣ���߳�
					BL = true;
					// �ر����������
					IOUtil.close(in);
					IOUtil.close(out);
					// �ر�socket
					NetUtil.close(client);
					client = null;
					// ��������û��б�
					userlistarea.setText("");
					// ���ð�ť
					stopButton.setEnabled(false);
					connectButton.setEnabled(true);

					// chatJFrame.this.dispose();

				} catch (IOException e1) {
					e1.printStackTrace();
				}

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
		north.setBorder(new TitledBorder("��������"));
		// ��north��߷����
		// �ű�ǩ������IP
		north.add(new JLabel("������IP��"));
		// �ŷ�����IP�ı���
		north.add(getserverIP());
		// �Ŷ˿ںű�ǩ
		north.add(new JLabel("�˿ںţ�"));
		// �Ŷ˿ں��ı���
		north.add(getserverPort());
		// ���ǳƱ�ǩ���ǳ��ı���
		north.add(new JLabel("�ǳ�"));
		north.add(getNickName());
		// �����ӡ�������ť
		north.add(getConnectButton());
		north.add(getStopButton());
		return north;
	}

	/**
	 * ���������û��б��ı���
	 * 
	 * @return JTextArea
	 */
	public JScrollPane getUserListArea() {
		userlistarea = new JTextArea();
		JScrollPane userlistscroll = new JScrollPane(userlistarea);
		userlistscroll.setBorder(new TitledBorder("�����û�"));
		return userlistscroll;
	}

	/**
	 * ���������¼�ı���
	 * 
	 * @return JTextArea
	 */
	public JScrollPane getRecordListArea() {
		recordlistarea = new JTextArea();
		JScrollPane recordlistscroll = new JScrollPane(recordlistarea);
		recordlistscroll.setBorder(new TitledBorder("������Ϣ"));
		return recordlistscroll;
	}

	/**
	 * �����м�JSplitPane�ָ�����������������û��б�������¼
	 * 
	 * @return JSplitPane
	 */
	public JSplitPane CenterPane() {
		// ����һ���ָ���� ָ����������ˮƽ���� �ָ�Ϊ��������
		JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		center.setDividerLocation(160);
		center.setLeftComponent(getUserListArea());
		center.setRightComponent(getRecordListArea());
		return center;
	}

	/**
	 * ��ʼ�����촰��
	 */
	public void init() {

		this.setSize(700, 400);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setcenter(true);
		// ����������
		Container conPane = this.getContentPane();
		// ���������������
		// ���ϱߵ����
		conPane.add(SouthPanel(), BorderLayout.SOUTH);
		// �ű��ߵ����
		conPane.add(NorthPanel(), BorderLayout.NORTH);
		// ���м�����
		conPane.add(CenterPane(), BorderLayout.CENTER);

		this.setVisible(true);
	}

	public static void main(String[] args) {
		new chatJFrame();
	}

}
