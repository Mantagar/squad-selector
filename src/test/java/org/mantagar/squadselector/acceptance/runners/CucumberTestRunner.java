package org.mantagar.squadselector.acceptance.runners;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("features") // .feature files location in resources
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "org.mantagar.squadselector.acceptance.steps") // .java steps location
@ConfigurationParameter(key = "cucumber.plugin", value = "pretty,html:build/reports/cucumber.html")
class CucumberTestRunner {}
