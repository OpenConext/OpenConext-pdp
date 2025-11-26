package pdp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.openaz.xacml.pdp.policy.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import pdp.domain.PdpPolicy;
import pdp.domain.PdpPolicyDefinition;
import pdp.repositories.PdpPolicyRepository;
import pdp.xacml.PdpPolicyDefinitionParser;
import pdp.xacml.PolicyTemplateEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {})
public class PolicyHarnessTest {

    private final PolicyTemplateEngine policyTemplateEngine = new PolicyTemplateEngine();
    private final PdpPolicyDefinitionParser pdpPolicyDefinitionParser = new PdpPolicyDefinitionParser();

    @LocalServerPort
    protected int port;

    @Autowired
    protected PdpPolicyRepository policyRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    protected void beforeEach() {
        RestAssured.port = port;
    }

    @TestFactory
    Stream<DynamicTest> policyHarness() throws Exception {
        String policy = System.getProperty("policy");
        return Stream.concat(
                Stream.of(Objects.requireNonNull(new ClassPathResource("test-harness").getFile().listFiles())),
                Stream.of(Objects.requireNonNull(new ClassPathResource("test-harness-generated").getFile().listFiles()))
            )
            .filter(File::isDirectory)
            .filter(file -> (file.getName().matches("^[a-zA-Z].*")))
            .filter(file -> policy == null || file.getName().equalsIgnoreCase(policy))
            .map(directory -> DynamicTest.dynamicTest(
                "Policy harness: " + directory.getName(),
                () -> testPolicy(directory)
            ));
    }

    @SneakyThrows
    private void testPolicy(File policyDirectory) {
        policyRepository.deleteAll();
        List<File> files = List.of(Objects.requireNonNull(policyDirectory.listFiles()));
        String request = this.readFile(files, "request.json");
        File responseFile = files.stream()
            .filter(file -> file.getName().equalsIgnoreCase("response.json"))
            .findFirst().orElseThrow();
        Map<String, Object> responseMap = objectMapper.readValue(new FileInputStream(responseFile), new TypeReference<>() {
        });
        files.stream()
            .filter(file -> file.getName().toLowerCase().startsWith("policy"))
            .forEach(this::storePolicy);

        Map<String, Object> result = given()
            .auth().preemptive().basic("pdp_admin", "secret")
            .when()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(request)
            .post("/pdp/api/manage/decide")
            .as(new TypeRef<>() {
            });

        if (!responseMap.equals(result)) {
            ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
            String expected = objectWriter.writeValueAsString(responseMap);
            String actual = objectWriter.writeValueAsString(result);
            // Use java-diff-utils to produce a readable unified diff
            List<String> expectedLines = expected.lines().toList();
            List<String> actualLines = actual.lines().toList();
            Patch<String> patch = DiffUtils.diff(expectedLines, actualLines);
            List<String> unifiedDiff = UnifiedDiffUtils
                .generateUnifiedDiff("expected", "actual", expectedLines, patch, 3);
            // pretty-print the diff
            String message = """
                Response did not match expected JSON
                ===== Expected =====
                %s
                ===== Actual   =====
                %s
                ===== Unified Diff (expected vs actual) =====
                %s
                """.formatted(
                expected,
                actual,
                String.join("\n", unifiedDiff)
            );
            fail(message);
        }
    }

    @SneakyThrows
    private void storePolicy(File file) {
        PdpPolicyDefinition policyDefinition = objectMapper.readValue(new FileInputStream(file), PdpPolicyDefinition.class);
        String policyXml = policyTemplateEngine.createPolicyXml(policyDefinition);
        Policy parsedPolicy = pdpPolicyDefinitionParser.parsePolicy(policyXml, policyDefinition.getName());
        //If there are null's then something is wrong
        Assert.notNull(parsedPolicy, "ParsedPolicy is not valid");
        PdpPolicy policy = new PdpPolicy(
            policyXml,
            policyDefinition.getName(),
            true,
            "manage",
            "manage",
            "manage",
            true,
            policyDefinition.getType());
        policyRepository.save(policy);
    }

    private String readFile(List<File> files, String name) throws IOException {
        return IOUtils.toString(
            new FileInputStream(files.stream()
                .filter(file -> file.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow()), Charset.defaultCharset()
        );
    }
}
