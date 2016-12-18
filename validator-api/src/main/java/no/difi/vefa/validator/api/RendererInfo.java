package no.difi.vefa.validator.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define file types used by an implementation of Presenter.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RendererInfo {

    /**
     * List of file extensions.
     *
     * @return List extensions.
     */
    String[] value();
}
