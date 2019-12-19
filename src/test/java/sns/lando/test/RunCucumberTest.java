package sns.lando.test;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = { "de.monochromata.cucumber.report.PrettyReports:reports" }, features = "src/test/resources/cucumber")
//        @CucumberOptions(plugin = {"pretty",
//                "html:reports/cucumber",
//                "json:reports/cucumber.json",
//                "junit:reports/cucumber.xml"},
//        snippets = CAMELCASE,
//        features = "src/test/resources/cucumber")
public class RunCucumberTest {
}