package no.difi.vefa.validator.plugin;

import no.difi.vefa.validator.api.Checker;
import no.difi.vefa.validator.api.Renderer;
import no.difi.vefa.validator.api.Trigger;
import no.difi.vefa.validator.api.ValidatorPlugin;
import no.difi.vefa.validator.checker.SchematronXsltChecker;
import no.difi.vefa.validator.checker.XsdChecker;
import no.difi.vefa.validator.renderer.XsltRenderer;
import no.difi.xsd.vefa.validator._1.Configurations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class UblPlugin implements ValidatorPlugin {

    @Override
    public List<Class<? extends Checker>> checkers() {
        return new ArrayList<Class<? extends Checker>>() {{
            add(XsdChecker.class);
            add(SchematronXsltChecker.class);
        }};
    }

    @Override
    public List<Class<? extends Trigger>> triggers() {
        return Collections.emptyList();
    }

    @Override
    public List<Class<? extends Renderer>> renderers() {
        return new ArrayList<Class<? extends Renderer>>() {{
            add(XsltRenderer.class);
        }};
    }

    @Override
    public List<Configurations> configurations() {
        return Collections.emptyList();
    }
}
