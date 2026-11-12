package io.github.matii1942.tienda;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Entry point for {@code mvn test}.
 *
 * <p>Which scenarios run is decided by tags, not by this class. The default filter
 * ({@code not @known-bug}) lives in {@code junit-platform.properties} so that it can be
 * overridden from the command line:
 *
 * <pre>
 *   mvn test                                        # everything that must pass
 *   mvn test -Dcucumber.filter.tags="@api"          # API only
 *   mvn test -Dcucumber.filter.tags="@known-bug"    # reproduce the documented defects
 * </pre>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.github.matii1942.tienda")
public class RunCucumberTest {
}
