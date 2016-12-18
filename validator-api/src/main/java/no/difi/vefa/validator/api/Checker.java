package no.difi.vefa.validator.api;

import no.difi.vefa.validator.lang.ValidatorException;

import java.nio.file.Path;

/**
 * Interface for classes performing validation of business documents.
 * <p/>
 * The constructor must contain no parameters.
 */
@Deprecated
public interface Checker extends Trigger {
    void prepare(Path path) throws ValidatorException;
}
