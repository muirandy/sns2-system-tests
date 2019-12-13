package sns.lando.test.system;

import com.github.muirandy.living.artifact.main.ComponentDiagramApp;
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
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import static com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramGenerator.getHeaderContentForModalWindows;

@RunWith(SpecRunner.class)
public class YatspecBase extends TestState implements LivingDocumentation, WithCustomResultListeners {

    private ComponentDiagramApp componentDiagramApp;

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

    @Before
    public void resetComponentDiagram() {
        componentDiagramApp = new ComponentDiagramApp(new String[]{"http://jaeger:16686", "localhost:9092"});
        componentDiagramApp.initialise();
    }

    @After
    public void tearDown() {
        addSequenceDiagram();
        addComponentDiagram();
    }

    private void addSequenceDiagram() {
        super.log("Sequence diagram", new SequenceDiagramGenerator()
                .generateSequenceDiagram(new ByNamingConventionMessageProducer().messages(capturedInputAndOutputs)));
    }

    private void addComponentDiagram() {
        SvgWrapper componentDiagramSvgWrapper = generateComponentDiagram();
        super.log("Component Diagram", componentDiagramSvgWrapper);
    }

    private SvgWrapper generateComponentDiagram() {
        ByteArrayOutputStream byteArrayOutputStream = componentDiagramApp.drawComponentDiagram();
        String content = new String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8"));
        return new SvgWrapper(content);
    }

    @Override
    public Iterable<SpecResultListener> getResultListeners() {
        return ImmutableSet.of(new HtmlResultRenderer().withCustomHeaderContent(getHeaderContentForModalWindows()).withCustomRenderer(SvgWrapper.class, new DontHighlightRenderer<>()));
    }
}
