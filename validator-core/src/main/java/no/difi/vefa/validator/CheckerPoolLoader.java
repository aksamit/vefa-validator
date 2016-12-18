package no.difi.vefa.validator;

import com.google.common.cache.CacheLoader;
import no.difi.vefa.validator.api.Checker;
import no.difi.vefa.validator.api.CheckerInfo;
import no.difi.vefa.validator.lang.ValidatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pool of prepared checkers. Size if configured using properties.
 */
@Deprecated
class CheckerPoolLoader extends CacheLoader<String, Checker> {

    private static Logger logger = LoggerFactory.getLogger(CheckerPoolLoader.class);

    private ValidatorEngine validatorEngine;
    private Class<? extends Checker>[] implementations;

    CheckerPoolLoader(ValidatorEngine validatorEngine, Class<? extends Checker>[] implementations) {
        this.validatorEngine = validatorEngine;
        this.implementations = implementations;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Checker load(String key) throws Exception {
        try {
            for (Class cls : implementations) {
                try {
                    for (String extension : ((CheckerInfo) cls.getAnnotation(CheckerInfo.class)).value()) {
                        if (key.toLowerCase().endsWith(extension)) {
                            logger.debug("Checker '{}'", key);
                            Checker checker = (Checker) cls.getConstructor().newInstance();
                            checker.prepare(validatorEngine.getResource(key));
                            return checker;
                        }
                    }
                } catch (Exception e) {
                    throw new ValidatorException(String.format("Unable to use %s for checker.", cls), e);
                }
            }
        } catch (Exception e) {
            throw new ValidatorException(String.format("Unable to load checker for '%s'.", key), e);
        }

        throw new ValidatorException(String.format("No checker found for '%s'", key));
    }
}
