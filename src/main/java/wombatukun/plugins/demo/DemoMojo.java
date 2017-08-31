package wombatukun.plugins.demo;

import com.jcraft.jsch.*;
import org.apache.maven.plugin.AbstractMojo;

import java.io.*;

public abstract class DemoMojo extends AbstractMojo {
	private static final int CONNECT_TIMEOUT= 30000;
	private static final int DEFAULT_PORT = 22;
	/**
	 * SSH server address
	 * @parameter expression="${host}"
	 * @required
	 */
	protected String host;
	/**
	 * SSH connection port
	 * @parameter expression="${port}"
	 */
	protected Integer port;
	/**
	 * User name
	 * @parameter expression="${user}"
	 * @required
	 */
	protected String user;
	/**
	 * User password
	 * @parameter expression="${pass}"
	 * @required
	 */
	protected String pass;
	/**
	 * Distributive zip-archive made by assembly-plugin with option includeBaseDirectory=false
	 * @parameter expression="${archive}"
	 * @required
	 */
	protected String archive;
	/**
	 * Program directory on remote
	 * @parameter expression="${directory}"
	 * @required
	 */
	protected String directory;
	/**
	 * Stop command
	 * @parameter expression="${stop}"
	 * @required
	 */
	protected String stop;
	/**
	 * Start command
	 * @parameter expression="${start}"
	 * @required
	 */
	protected String start;

	protected String getArchName() {
		return archive.lastIndexOf('/')>0 ? archive.substring(archive.lastIndexOf('/')+1) : archive;
	}

	protected Session makeSession() throws JSchException {
		JSch jsch=new JSch();
		Session session=jsch.getSession(user, host, port==null ? DEFAULT_PORT : port);
		session.setPassword(pass);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect(CONNECT_TIMEOUT);
		return session;
	}

	protected void executeCommand(Session session, String command) throws JSchException, IOException {
		Channel channel=session.openChannel("exec");
		((ChannelExec)channel).setErrStream(System.err);
		channel.setInputStream(null);
		InputStream in = channel.getInputStream();
		((ChannelExec)channel).setCommand(command);
		channel.connect();
		printProcess(channel, in);
		channel.disconnect();
	}

	protected void printProcess(Channel channel, InputStream in) throws IOException {
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available()>0) {
				int i = in.read(tmp, 0, 1024);
				if (i<0) break;
				getLog().info(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				if (in.available()>0) continue;
				//getLog().info("exit-status: "+channel.getExitStatus());
				break;
			}
		}
	}

	protected boolean uploadArchive(Session session) throws JSchException, IOException {
		File archiveFile = new File(archive);
		if (!archiveFile.exists()) {
			getLog().error("LOCAL FILE NOT EXISTS: " + archive);
			return false;
		}

		getLog().info("UPLOAD STARTED");
		String archName = getArchName();
		/* #!/bin/sh
			if [ -e archName ] ; then
				rm -f archName;
			fi
			scp -t archName
		*/
		String command = "if [ -e " + archName + " ] ; then " +"rm -f " + archName + "; fi; scp -t " + archName;
		Channel channel=session.openChannel("exec");
		((ChannelExec)channel).setCommand(command);
		// get I/O streams for remote scp
		OutputStream out = channel.getOutputStream();
		InputStream in = channel.getInputStream();
		channel.connect();
		if (checkAck(in)!=0) { return false; }
		// send "C0644 filesize filename", where filename should not include '/'
		long fileSize = archiveFile.length();
		command = "C0644 " + fileSize + " " + archName + "\n";
		out.write(command.getBytes()); out.flush();
		if (checkAck(in)!=0) { return false; }
		getLog().info("UPLOAD IN PROGRESS........");
		// send a content of file
		FileInputStream fis = new FileInputStream(archiveFile);
		byte[] buf = new byte[1024];
		while (true) {
			int len = fis.read(buf, 0, buf.length);
			if(len<=0) break;
			out.write(buf, 0, len);
		}
		fis.close();
		// send '\0'
		buf[0]=0; out.write(buf, 0, 1); out.flush();
		if (checkAck(in)!=0) { return false; }
		out.close();
		channel.disconnect();
		getLog().info("UPLOAD FINISHED");
		return true;
	}

	private int checkAck(InputStream in) throws IOException{
		int b=in.read(); // b may be: 0 for success, 1 for error, 2 for fatal error, -1
		if(b==1 || b==2) {
			StringBuilder sb = new StringBuilder();
			int c;
			do {
				c = in.read();
				sb.append((char)c);
			} while (c!='\n');
			getLog().error(sb.toString());
		}
		return b;
	}

}
