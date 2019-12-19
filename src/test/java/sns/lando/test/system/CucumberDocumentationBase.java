package sns.lando.test.system;

import java.util.function.BiConsumer;

public class CucumberDocumentationBase implements LivingDocumentation {
    @Override
    public BiConsumer<String, Object> getGivenLogConsumer() {
        return (key, value) -> {
//            interestingGivens.add(key, value);
        };
    }

    @Override
    public BiConsumer<String, Object> getWhenLogConsumer() {
        return (key, value) -> {
        };
    }

    @Override
    public BiConsumer<String, Object> getThenLogConsumer() {
        return (key, value) -> {
        };
    }
}
