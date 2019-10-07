package sns.lando.test.system;

import java.util.function.BiConsumer;

interface LivingDocumentation {
    BiConsumer<String, Object> getGivenLogConsumer();
    BiConsumer<String, Object> getWhenLogConsumer();
    BiConsumer<String, Object> getThenLogConsumer();

    default void logGiven(String key, Object value) {
        getGivenLogConsumer().accept(key, value);
    }

    default void logWhen(String key, Object value) {
        getWhenLogConsumer().accept(key, value);
    }

    default void logThen(String key, Object value) {
        getThenLogConsumer().accept(key, value);
    }
}
