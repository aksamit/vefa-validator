package no.difi.vefa.validator.api;

import java.io.ByteArrayInputStream;

/**
 * Representation of validation document.
 */
public class Document {

    /**
     * Document content.
     */
    private byte[] bytes;

    /**
     * Declaration identifier used to recognize rules.
     */
    private String declaration;

    /**
     * Expectations when performing validation of triggered rules.
     */
    private Expectation expectation;

    /**
     * @param bytes InputStream containing the document used during validation.
     * @param declaration Declaration identifier used to recognize rules.
     * @param expectation Expectations when performing validation of triggered rules.
     */
    public Document(byte[] bytes, String declaration, Expectation expectation)  {
        this.bytes = bytes;
        this.declaration = declaration;
        this.expectation = expectation;
    }

    /**
     * Declaration detected in document for validation.
     *
     * @return Declaration
     */
    public String getDeclaration() {
        return declaration;
    }

    /**
     * Expectations detected in document for validation.
     *
     * @return Expectations
     */
    public Expectation getExpectation() {
        return expectation;
    }

    /**
     * Helper returning validated document as ByteArrayInputStream ready for use.
     *
     * @return Validated document
     */
    public ByteArrayInputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public byte[] getBytes() {
        return bytes;
    }
}
