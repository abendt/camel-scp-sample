package demo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.file.FileSystemView;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.nativefs.NativeFileSystemView;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.rules.ExternalResource;

public class EmbeddedSshdRule extends ExternalResource {

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    private SshServer sshd;

    public void setupSSHServer(final File testFolder) throws IOException {
        sshd = SshServer.setUpDefaultServer();
        sshd.setFileSystemFactory(new NativeFileSystemFactory() {
            @Override
            public FileSystemView createFileSystemView(final Session session) {
                return new NativeFileSystemView(session.getUsername(), Collections.singletonMap("/", "/"), testFolder.getAbsolutePath(), '/', false);
            };
        });
        sshd.setPort(18001);
        sshd.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(new SftpSubsystem.Factory()));
        sshd.setCommandFactory(new ScpCommandFactory());
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(testFolder, "hostkey.ser").getAbsolutePath()));
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            public boolean authenticate(final String username, final String password, final ServerSession session) {
                return USERNAME.equals(username) && PASSWORD.equals(password);
            }
        });
        sshd.start();
    }

    @Override
    protected void after() {
        if (sshd != null) {
            try {
                sshd.stop(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
