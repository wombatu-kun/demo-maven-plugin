package wombatukun.plugins.demo;

import com.jcraft.jsch.Session;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Uploads distributive zip-archive to remote by ssh.
 * @goal upload
 */
public class UploadMojo extends DemoMojo {

	public void execute() throws MojoExecutionException {
		try {
			Session session = makeSession();
			if (uploadArchive(session)) {
				getLog().info("ARCHIVE UPLOADED SUCCESSFULLY");
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

}