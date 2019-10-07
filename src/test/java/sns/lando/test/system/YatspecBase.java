package sns.lando.test.system;

import com.google.common.collect.ImmutableSet;
import com.googlecode.yatspec.junit.SpecResultListener;
import com.googlecode.yatspec.junit.SpecRunner;
import com.googlecode.yatspec.junit.WithCustomResultListeners;
import com.googlecode.yatspec.plugin.sequencediagram.ByNamingConventionMessageProducer;
import com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramGenerator;
import com.googlecode.yatspec.plugin.sequencediagram.SvgWrapper;
import com.googlecode.yatspec.rendering.html.DontHighlightRenderer;
import com.googlecode.yatspec.rendering.html.HtmlResultRenderer;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import org.junit.After;
import org.junit.runner.RunWith;

import java.util.function.BiConsumer;

import static com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramGenerator.getHeaderContentForModalWindows;

@RunWith(SpecRunner.class)
class YatspecBase extends TestState implements LivingDocumentation, WithCustomResultListeners {
    @Override
    public BiConsumer<String, Object> getGivenLogConsumer() {
        return (key, value) -> {
            interestingGivens.add(key, value);
        };
    }

    @Override
    public BiConsumer<String, Object> getWhenLogConsumer() {
        return (key, value) -> {
            log(key, value);
        };
    }

    @Override
    public BiConsumer<String, Object> getThenLogConsumer() {
        return (key, value) -> {
            log(key, value);
        };
    }

    @After
    public void tearDown() {
        addSequenceDiagram();
    }

    private void addSequenceDiagram() {
        super.log("Sequence diagram", new SequenceDiagramGenerator()
                .generateSequenceDiagram(new ByNamingConventionMessageProducer().messages(capturedInputAndOutputs)));
    }

    @Override
    public Iterable<SpecResultListener> getResultListeners() {
        return ImmutableSet.of(new HtmlResultRenderer().withCustomHeaderContent(getHeaderContentForModalWindows()).withCustomRenderer(SvgWrapper.class, new DontHighlightRenderer<>()));
    }
}
