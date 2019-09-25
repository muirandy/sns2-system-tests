package sns.lando.test.system;

public interface TestEnvironment {
    void givenExistingVoipService();

    void writeMessageOntoActiveMq(String traceyId, int orderId);

    void assertFeaturesChangedOnSwitch();
}
