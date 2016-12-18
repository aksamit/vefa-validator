package no.difi.vefa.validator.declaration;

import no.difi.vefa.validator.lang.ValidatorException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class EspdDeclaration extends AbstractXmlDeclaration {

    private static List<String> validParents = Arrays.asList(
            "urn:grow:names:specification:ubl:schema:xsd:ESPDRequest-1::ESPDRequest",
            "urn:grow:names:specification:ubl:schema:xsd:ESPDResponse-1::ESPDResponse"
    );

    @Override
    public boolean verify(byte[] content, String parent) throws ValidatorException {
        return validParents.contains(parent);
    }

    @Override
    public String detect(byte[] content, String parent) throws ValidatorException {
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new ByteArrayInputStream(content));
            while (xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();

                if (xmlEvent.isStartElement()) {
                    if ("VersionID".equals(((StartElement) xmlEvent).getName().getLocalPart())) {
                        xmlEvent = xmlEventReader.nextEvent();
                        if (xmlEvent instanceof Characters)
                            return String.format("%s::%s", parent, ((Characters) xmlEvent).getData());
                    }
                }
            }
        } catch (Exception e) {
            // No action.
        }

        return parent;
    }
}
