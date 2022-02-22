package io.quarkiverse.openapi.generator.deployment.wrapper;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.openapitools.codegen.CodegenConstants;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.GlobalSettings;

import io.quarkiverse.openapi.generator.deployment.SpecConfig;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Wrapper for the OpenAPIGen tool.
 * This is the same as calling the Maven plugin or the CLI.
 * We are wrapping into a class to generate code that meet our requirements.
 *
 * @see <a href="https://openapi-generator.tech/docs/generators/java">OpenAPI Generator Client for Java</a>
 */
public class OpenApiClientGeneratorWrapper {

    private static final String VERBOSE = "verbose";
    private static final String ONCE_LOGGER = "org.openapitools.codegen.utils.oncelogger.enabled";
    private static final String DEFAULT_PACKAGE = "org.openapi.quarkus";

    private final QuarkusCodegenConfigurator configurator;
    private final DefaultGenerator generator;

    private String basePackage = DEFAULT_PACKAGE;
    private String apiPackage = "";
    private String modelPackage = "";

    public OpenApiClientGeneratorWrapper(final Path specFilePath, final Path outputDir) {
        // do not generate docs nor tests
        GlobalSettings.setProperty(CodegenConstants.API_DOCS, FALSE.toString());
        GlobalSettings.setProperty(CodegenConstants.API_TESTS, FALSE.toString());
        GlobalSettings.setProperty(CodegenConstants.MODEL_TESTS, FALSE.toString());
        GlobalSettings.setProperty(CodegenConstants.MODEL_DOCS, FALSE.toString());
        // generates every Api and Models
        GlobalSettings.setProperty(CodegenConstants.APIS, "");
        GlobalSettings.setProperty(CodegenConstants.MODELS, "");
        GlobalSettings.setProperty(CodegenConstants.SUPPORTING_FILES, "");
        // logging
        GlobalSettings.setProperty(VERBOSE, FALSE.toString());
        GlobalSettings.setProperty(ONCE_LOGGER, TRUE.toString());

        this.configurator = new QuarkusCodegenConfigurator();
        this.configurator.setInputSpec(specFilePath.toString());
        this.configurator.setOutputDir(outputDir.toString());
        this.generator = new DefaultGenerator();
    }

    public OpenApiClientGeneratorWrapper withApiPackage(final String pkg) {
        this.apiPackage = pkg;
        return this;
    }

    public OpenApiClientGeneratorWrapper withModelPackage(final String pkg) {
        this.modelPackage = pkg;
        return this;
    }

    public OpenApiClientGeneratorWrapper withBasePackage(final String pkg) {
        this.basePackage = pkg;
        return this;
    }

    public OpenApiClientGeneratorWrapper withCircuitBreakerConfiguration(final CircuitBreakerConfiguration config) {
        configurator.addAdditionalProperty("circuit-breaker", config);
        return this;
    }

    public List<File> generate() {
        this.consolidatePackageNames();
        return generator.opts(configurator.toClientOptInput()).generate();
    }

    private void consolidatePackageNames() {
        if (basePackage.isEmpty()) {
            basePackage = DEFAULT_PACKAGE;
        }
        if (apiPackage.isEmpty()) {
            this.apiPackage = basePackage.concat(SpecConfig.API_PKG_SUFFIX);
        }
        if (modelPackage.isEmpty()) {
            this.modelPackage = basePackage.concat(SpecConfig.MODEL_PKG_SUFFIX);
        }
        this.configurator.setPackageName(basePackage);
        this.configurator.setApiPackage(apiPackage);
        this.configurator.setModelPackage(modelPackage);
        this.configurator.setInvokerPackage(apiPackage);
    }

}
