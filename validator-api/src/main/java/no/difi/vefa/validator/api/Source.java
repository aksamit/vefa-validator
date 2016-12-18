package no.difi.vefa.validator.api;

/**
 * Source for validation artifacts.
 */
@Deprecated
public interface Source {

    /**
     * Instance of source with validation artifacts ready for use.
     *
     * @throws ValidatorException
     * @return Instance containing validation artifacts.
     */
    SourceInstance createInstance(Properties properties) throws ValidatorException;

}
