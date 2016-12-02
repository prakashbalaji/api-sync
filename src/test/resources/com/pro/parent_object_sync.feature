Feature: Parent Object Sync

  Scenario:
    Given I stub GET to "/customers/cu_100" with body
      | {"customerId": "cu_100", "firstName": "Matt", "lastName": "William"} |

    Then I raise a sync event for customer "cu_100"

    Then I verify that the table "customers" has the following entries
      | customer_id | first_name | last_name |
      | cu_100      | Matt       | William   |

  Scenario:
    Given I stub GET to "/providers/pro_100" with body
      | {"providerId": "pro_100", "firstName": "David", "lastName": "Crompton"} |

    Then I raise a sync event for provider "pro_100"

    Then I verify that the table "providers" has the following entries
      | provider_id | first_name | last_name |
      | pro_100     | David      | Crompton  |
