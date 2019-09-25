package sns.lando.test.system;

public interface TestEnvironment {
    void givenExistingVoipService();

    void assertFeaturesChangedOnSwitch();
}
