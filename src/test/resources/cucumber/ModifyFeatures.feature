Feature: Modify Features
  Customers decide they want their VOIP telephony to behave differently

  Scenario: VOIP Features are changed
    Given an existing VOIP Service
    When the operator issues a Modify Voice Features Request
    Then the VOIP features are changed on the Telephony Switch