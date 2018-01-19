package ottifc.ott.semantics;

import helpers.FileHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ottifc.ott.Specification;

import java.io.File;

public class RuleTest {

    Specification spec;
    Rule rule;

    @BeforeEach
    void setUp() {
        String specificationFileContents = FileHelper.convertFileToString(new File("documents/examples/imperative-bigstep.ott"));

    }

    @AfterEach
    void tearDown() {
    }
}
