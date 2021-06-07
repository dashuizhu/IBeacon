package com.zby.corelib;


interface IConnectInterface {

	int type_bluetooth = 1;
	int type_usb =2;
	int type_ble = 3;

	/**
	 * 连接设备
	 * @param address 连接的地址，局域网内的ip 或者 蓝牙mac
	 * @param pwd 建立连接的密码
	 */
	boolean connect(String address, String pwd);// 连接
	/**
	 * 停止连接
	 */
	void stopConncet();// 停止连接
	/**
	 * 直接发送数据
	 * @param buffer
	 */
	boolean write(byte[] buffer);

	boolean write(byte[] buffer, boolean isNoResponse);

	/**
	 * 将命令生成协议后发送
	 * @param buffer
	 */
	boolean writeAgreement(byte[] buffer, String encryptKey);
	/**
	 * 读取数据，数据
	 * @param buffer
	 */
	//void read(byte[] buffer);//读取数据，处理一些和通信有关的数据
	/**
	 * 是否已经连接到设备
	 * @return
	 *   如果连上设备，返回true
	 */
	boolean isLink();//是否已连接

	boolean isLink(String mac);//是否已连接
	/**
	 * 是否正在连接中
	 * @return
	 */
	boolean isConnecting();

	int getConnectType();

	//void setCmdParse(ICmdParseInterface cmdParse);

	void requestConnectionPriority();

}
