package no.difi.vefa.validator.util;

import com.typesafe.config.Config;
import no.difi.vefa.validator.lang.ValidatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeclarationDetector {

    private static Logger logger = LoggerFactory.getLogger(DeclarationDetector.class);

    public static final DeclarationIdentifier UNKNOWN = new DeclarationIdentifier(null, null, "unknown");

    private List<DeclarationWrapper> rootDeclarationWrappers = new ArrayList<>();

    @Inject
    public DeclarationDetector(Config config){
        Map<String, DeclarationWrapper> wrapperMap = new HashMap<>();
        for (String s : config.getObject("declaration").keySet()) {
            Config declarationConfig = config.getConfig("declaration").getConfig(s);

            if (!declarationConfig.hasPath("enabled") || declarationConfig.getBoolean("enabled"))
                wrapperMap.put(declarationConfig.getString("type"), new DeclarationWrapper(declarationConfig));
        }

        for (String key : wrapperMap.keySet()) {
            if (key.contains(".")) {
                String parent = key.substring(0, key.lastIndexOf("."));
                wrapperMap.get(parent).getChildren().add(wrapperMap.get(key));
            } else {
                rootDeclarationWrappers.add(wrapperMap.get(key));
            }
        }
    }

    public DeclarationIdentifier detect(byte[] content) {
        return detect(rootDeclarationWrappers, content, UNKNOWN);
    }

    private DeclarationIdentifier detect(List<DeclarationWrapper> wrappers, byte[] content, DeclarationIdentifier parent) {
        for (DeclarationWrapper wrapper : wrappers) {
            try {
                if (wrapper.verify(content, parent == null ? null : parent.getIdentifier())) {
                    String identifier = wrapper.detect(content, parent == null ? null : parent.getIdentifier());
                    if (identifier == null)
                        break;
                    logger.debug("Found: {} - {}", wrapper.getType(), identifier);

                    return detect(wrapper.getChildren(), content, new DeclarationIdentifier(parent, wrapper, identifier));
                }
            } catch (ValidatorException e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return parent;
    }
}
