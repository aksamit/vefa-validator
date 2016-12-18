package no.difi.vefa.validator.declaration;

import com.typesafe.config.Config;
import no.difi.vefa.validator.lang.ValidatorException;
import no.difi.vefa.validator.util.XmlUtils;

public class SimpleXmlDeclaration extends AbstractXmlDeclaration {

    protected String namespace;
    protected String localName;

    public SimpleXmlDeclaration(Config config) {
        this.namespace = config.getString("namespace");

        if (config.hasPath("localName"))
            this.localName = config.getString("localName");
    }

    public SimpleXmlDeclaration(String namespace, String localName) {
        this.namespace = namespace;
        this.localName = localName;
    }

    @Override
    public boolean verify(byte[] content, String parent) throws ValidatorException {
        String c = new String(content);
        return namespace.equals(XmlUtils.extractRootNamespace(c)) &&
                (localName == null || localName.equals(XmlUtils.extractLocalName(c)));
    }

    @Override
    public String detect(byte[] content, String parent) throws ValidatorException {
        return String.format("%s::%s", namespace, localName == null ? XmlUtils.extractLocalName(new String(content)) : localName);
    }
}
