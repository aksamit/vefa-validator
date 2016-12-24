package no.difi.vefa.validator.extractor;

import no.difi.vefa.validator.api.artifact.Extractor;
import no.difi.vefa.validator.util.AbstractArtifact;
import no.difi.xsd.vefa.validator._2.ExtractorType;

import java.nio.file.Path;

public class ValidatorTestSetExtractor extends AbstractArtifact<ExtractorType> implements Extractor {

    public ValidatorTestSetExtractor(ExtractorType definition, Path folder) {
        super(definition, folder);
    }
}
