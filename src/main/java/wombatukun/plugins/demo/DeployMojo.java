package wombatukun.plugins.demo;

import com.jcraft.jsch.*;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;

/**
 * Deploys demo on remote server by ssh.
 * @goal deploy
 */
public class DeployMojo extends DemoMojo {

	public void execute() throws MojoExecutionException {
		try {
			Session session = makeSession();
			if (uploadArchive(session)) {
				getLog().info("UPDATE STARTED");
				updateDemo(session);
				getLog().info("UPDATE FINISHED");
				session.disconnect();
			} else {
				getLog().error("UPLOAD FAILED");
				session.disconnect();
				System.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateDemo(Session session) throws IOException, JSchException {
		String archName = getArchName();
		/* #!/bin/sh
	 		if [ -d directory ] ; then
				cd directory; ./stop.sh;
				cd ..; rm -Rf directory/*;
	 		else
				mkdir directory;
		 	fi
	 		unzip archName -d directory; rm -f archName;
	 		cd directory; ./start.sh;
		*/
		String command = "if [ -d " + directory + " ] ; then " +
				"cd " + directory + "; " + stop + "; cd ..; rm -Rf " + directory + "/*;" +
				"else mkdir " + directory + "; fi; " +
				"unzip " + archName + " -d " + directory + "; rm -f " + archName + "; cd " + directory + "; " + start;
		executeCommand(session, command);
	}

}
