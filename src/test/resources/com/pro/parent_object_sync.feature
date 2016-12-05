Feature: Parent Object Sync

  Scenario: Should sync a parent object
    Given I stub GET to "/customers/cu_100" with body
      | {"customerId": "cu_100", "firstName": "Matt", "lastName": "William", "age": 40, "salary": 240000.50, "addresses":[] } |

    Then I raise a sync event for customer "cu_100"

    Then I verify that the table "customers" has the following entries
      | customer_id | first_name | last_name | age | salary    |
      | cu_100      | Matt       | William   | 40  | 240000.50 |

  Scenario: Should sync a parent object
    Given I stub GET to "/providers/pro_100" with body
      | {"providerId": "pro_100", "firstName": "David", "lastName": "Crompton"} |

    Then I raise a sync event for provider "pro_100"

    Then I verify that the table "providers" has the following entries
      | provider_id | first_name | last_name |
      | pro_100     | David      | Crompton  |
