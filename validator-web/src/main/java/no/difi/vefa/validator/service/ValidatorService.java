package no.difi.vefa.validator.service;

import no.difi.vefa.validator.Validation;
import no.difi.vefa.validator.Validator;
import no.difi.vefa.validator.ValidatorBuilder;
import no.difi.vefa.validator.api.Source;
import no.difi.vefa.validator.source.DirectorySource;
import no.difi.vefa.validator.source.RepositorySource;
import no.difi.xsd.vefa.validator._1.PackageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ValidatorService {

    private static Logger logger = LoggerFactory.getLogger(ValidatorService.class);

    @Autowired
    private WorkspaceService workspaceService;

    @Value("${source}")
    private String propSource;
    @Value("${repository}")
    private String propRepository;
    @Value("${dir.rules}")
    private String dirRules;

    private Validator validator;

    @PostConstruct
    public void postConstruct() {
        try {
            Source source;

            switch (propSource) {
                case "directory":
                    source = new DirectorySource(Paths.get(dirRules));
                    break;
                case "repository":
                    source = new RepositorySource(propRepository);
                    break;
                default:
                    throw new Exception("Type of source not recognized.");
            }

            validator = ValidatorBuilder.newValidator().setSource(source).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String validateWorkspace(InputStream inputStream) throws Exception {
        Validation validation = validator.validate(inputStream);
        return workspaceService.saveValidation(validation);
    }

    public List<PackageType> getPackages() {
        return validator.getPackages();
    }
}
