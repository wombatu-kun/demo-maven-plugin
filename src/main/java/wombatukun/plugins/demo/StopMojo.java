package wombatukun.plugins.demo;

import com.jcraft.jsch.Session;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Stops running demo on remote by ssh.
 * @goal stop
 */
public class StopMojo extends DemoMojo {

	public void execute() throws MojoExecutionException {
		String command = "if [ -d " + directory + " ] ; then " +
				"cd " + directory + "; " + stop + "; " +
				"else echo 'NO SUCH DIRECTORY: <" + directory + ">'; fi;";
		try {
			Session session = makeSession();
			executeCommand(session, command);
			session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}