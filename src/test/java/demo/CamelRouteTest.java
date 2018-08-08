package demo;

import static com.google.common.io.Files.asCharSink;
import static org.awaitility.Awaitility.await;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.camel.LoggingLevel;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Charsets;

public class CamelRouteTest extends CamelTestSupport {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    File inputDirectory;
    File outputDirectory;

    @Override
    protected void doPreSetup() throws Exception {
        inputDirectory = temporaryFolder.newFolder();
        outputDirectory = temporaryFolder.newFolder();
    }

    @Test
    public void filesAreMovedToDirectory() throws Exception {
        final String uniqueName = UUID.randomUUID().toString();

        String expectedValue = "Hello world";
        File file = new File(inputDirectory, uniqueName);
        asCharSink(file, Charsets.UTF_8).write(expectedValue);

        await().untilAsserted(new ThrowingRunnable() {
            public void run() {
                List<String> strings = Arrays.asList(outputDirectory.list());

                System.out.println("files: " + strings);

                org.assertj.core.api.Assertions.assertThat(strings).contains(uniqueName);
            }
        });
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {

        return new RouteBuilder() {
            public void configure() {
                from("file:" + inputDirectory.getAbsolutePath() + "?readLock=none")
                        .log(LoggingLevel.INFO, "moving file")
                        .to("file:" + outputDirectory.getAbsolutePath());
            }
        };
    }
}
