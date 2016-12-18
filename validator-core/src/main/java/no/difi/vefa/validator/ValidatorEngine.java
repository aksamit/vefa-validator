package no.difi.vefa.validator;

import no.difi.vefa.validator.api.SourceInstance;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.lang.UnknownDocumentTypeException;
import no.difi.vefa.validator.util.JAXBHelper;
import no.difi.xsd.vefa.validator._1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * This class handles all raw configurations detected in source of validation artifacts and preserves links
 * between source and configurations.
 */
class ValidatorEngine implements Closeable {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(ValidatorEngine.class);

    /**
     * JAXBContext
     */
    private static JAXBContext jaxbContext = JAXBHelper.context(Configurations.class);

    private Map<String, Path> configurationSourceMap = new HashMap<>();

    /**
     * Map containing raw configurations indexed by both 'identifier' and 'identifier#build'.
     */
    private Map<String, ConfigurationType> identifierMap = new HashMap<>();

    /**
     * Map containing raw configurations indexed by document declaration.
     */
    private Map<String, ConfigurationType> declarationMap = new HashMap<>();

    /**
     * Stylesheet declarations found in configurations indexed by identifier of stylesheet declaration.
     */
    private Map<String, StylesheetType> stylesheetMap = new HashMap<>();

    /**
     * List of package declarations detected.
     */
    private List<PackageType> packages = new ArrayList<>();

    private SourceInstance sourceInstance;

    /**
     * Loading a new validator engine loading configurations from current source.
     */
    ValidatorEngine(SourceInstance sourceInstance, Configurations... configurations) throws ValidatorException {
        this.sourceInstance = sourceInstance;

        // Load configurations from ValidatorBuilder.
        for (Configurations c : configurations)
            loadConfigurations("", c);

        try {
            List<String> configs = sourceInstance.getConfigs();
            if (configs == null) {

                // Matcher to find configuration files.
                final PathMatcher matcher = sourceInstance.getFileSystem().getPathMatcher("glob:**/config*.xml");

                Files.walkFileTree(sourceInstance.getFileSystem().getPath("/"), new HashSet<FileVisitOption>(), 3, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (matcher.matches(file)) {
                            String configurationSource = addResource(file.getParent());
                            try {
                                loadConfigurations(configurationSource, Files.newInputStream(file));
                            } catch (ValidatorException e) {
                                throw new IOException(e.getMessage(), e);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                for (String config : configs) {
                    Path path = sourceInstance.getFileSystem().getPath(config);
                    loadConfigurations(addResource(path.getParent()), Files.newInputStream(path));
                }
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            throw new ValidatorException("Unable to read all configurations from virtual disk.", e);
        }

        // Simply sort packages by value.
        Collections.sort(packages, new Comparator<PackageType>() {
            @Override
            public int compare(PackageType o1, PackageType o2) {
                return o1.getValue().compareToIgnoreCase(o2.getValue());
            }
        });
    }

    /**
     * Load configuration from stream of config.xml.
     *
     * @param configurationSource Identifier for resource.
     * @param inputStream         Stream of config.xml.
     */
    private void loadConfigurations(String configurationSource, InputStream inputStream) throws ValidatorException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            loadConfigurations(configurationSource, (Configurations) unmarshaller.unmarshal(inputStream));
        } catch (JAXBException e) {
            throw new ValidatorException("Unable to read configurations.", e);
        }
    }

    /**
     * Load configuration from content of config.xml.
     *
     * @param configurationSource Identifier for resource.
     * @param configurations      Configurations found in config.xml
     */
    private void loadConfigurations(String configurationSource, Configurations configurations) {
        // Add all declared packages to list of detected packages.
        packages.addAll(configurations.getPackage());

        // Write to log when loading new packages.
        for (PackageType pkg : configurations.getPackage())
            logger.info("Loaded '{}'", pkg.getValue());

        for (ConfigurationType configuration : configurations.getConfiguration()) {
            for (FileType fileType : configuration.getFile()) {
                if (fileType.getType() == null)
                    fileType.setType(fileType.getPath().endsWith(".xsd") ? "xml.xsd" : "xml.schematron.xslt");
                fileType.setPath(String.format("%s#%s", configurationSource, fileType.getPath()));
                fileType.setConfiguration(configuration.getIdentifier().getValue());
                fileType.setBuild(configuration.getBuild());
            }

            for (TriggerType triggerType : configuration.getTrigger()) {
                triggerType.setConfiguration(configuration.getIdentifier().getValue());
                triggerType.setBuild(configuration.getBuild());
            }

            if (configuration.getStylesheet() != null) {
                StylesheetType stylesheet = configuration.getStylesheet();
                stylesheet.setPath(String.format("%s#%s", configurationSource, configuration.getStylesheet().getPath()));
                if (stylesheet.getType() == null)
                    stylesheet.setType("xml.xslt");

                stylesheetMap.put(stylesheet.getIdentifier(), stylesheet);
            }

            // Add by identifier if not registered or weight is higher
            if (!identifierMap.containsKey(configuration.getIdentifier().getValue()) || identifierMap.get(configuration.getIdentifier().getValue()).getWeight() < configuration.getWeight())
                identifierMap.put(configuration.getIdentifier().getValue(), configuration);

            if (configuration.getBuild() != null) {
                String identifierBuild = String.format("%s#%s", configuration.getIdentifier(), configuration.getBuild());
                if (!identifierMap.containsKey(identifierBuild) || identifierMap.get(identifierBuild).getWeight() < configuration.getWeight())
                    identifierMap.put(identifierBuild, configuration);
            }

            if (configuration.getDeclaration().size() == 0) {
                if (configuration.getStandardId() == null) {
                    if (configuration.getProfileId() != null && configuration.getCustomizationId() != null) {
                        DeclarationType declaration = new DeclarationType();
                        declaration.setType("xml.ubl");
                        declaration.setValue(configuration.getProfileId() + "#" + configuration.getCustomizationId());
                        configuration.getDeclaration().add(declaration);
                    }
                }

                if (configuration.getStandardId() != null) {
                    DeclarationType declarationType = new DeclarationType();
                    if (configuration.getStandardId().startsWith("SBDH:")) {
                        declarationType.setType("xml.sbdh");
                        declarationType.setValue(configuration.getStandardId());
                    } else {
                        declarationType.setType("xml");
                        declarationType.setValue(configuration.getStandardId());
                    }
                    configuration.getDeclaration().add(declarationType);
                }
            }

            for (DeclarationType declaration : configuration.getDeclaration()) {
                String identifier = String.format("%s::%s", declaration.getType(), declaration.getValue());
                if (!declarationMap.containsKey(identifier) || declarationMap.get(identifier).getWeight() < configuration.getWeight())
                    declarationMap.put(identifier, configuration);
            }

            declarationMap.put(String.format("configuration::%s", configuration.getIdentifier().getValue()), configuration);
        }
    }

    /**
     * Fetch raw configuration by using identifier.
     *
     * @param identifier Configuration identifier
     * @return Configuration
     */
    public ConfigurationType getConfiguration(String identifier) {
        return identifierMap.get(identifier);
    }

    /**
     * Fetch raw configuration by using document declaration.
     *
     * @param declaration Document declaration.
     * @return Configuration
     * @throws UnknownDocumentTypeException Thrown if no configuration is found for the document declaration.
     */
    public ConfigurationType getConfigurationByDeclaration(String declaration) throws UnknownDocumentTypeException {
        if (!declarationMap.containsKey(declaration))
            throw new UnknownDocumentTypeException(String.format("Configuration for '%s' not found", declaration));

        return declarationMap.get(declaration);
    }

    /**
     * Fetch stylesheet declaration using stylesheet identifier (not necessarily the same as configuration
     * identifier containing stylesheet declaration).
     *
     * @param identifier Stylesheet identifier.
     * @return Stylesheet declaration.
     * @throws ValidatorException Thrown if no stylesheet declaration is found for the identifier.
     */
    public StylesheetType getStylesheet(String identifier) throws ValidatorException {
        if (!stylesheetMap.containsKey(identifier))
            throw new ValidatorException(String.format("Stylesheet for identifier '%s' not found.", identifier));

        return stylesheetMap.get(identifier);
    }

    /**
     * Fetch list of packages found in current configurations.
     *
     * @return List of packages.
     */
    public List<PackageType> getPackages() {
        return packages;
    }

    private String addResource(Path source) {
        String identifier = source.toString();
        configurationSourceMap.put(identifier, source);
        return identifier;
    }

    public Path getResource(String resource) throws IOException {
        String[] parts = resource.split("#", 2);
        return configurationSourceMap.get(parts[0]).resolve(parts[1]);
    }

    @Override
    public void close() throws IOException {
        if (sourceInstance instanceof Closeable)
            ((Closeable) sourceInstance).close();
    }
}
