Feature: Parent Object Sync With One To Many Association

  Scenario:
    Given I stub GET to "/customers/cu_100" with body
      | {"customerId": "cu_100", "firstName": "Matt", "lastName": "William", "age": 40, "salary": 240000.50, "addresses": [{"customerId": "cu_100", "street": "3rd Avenue", "city": "Seattle", "addressId": "ad_100"}, {"customerId": "cu_100", "street": "Pine Street", "city": "Seattle", "addressId": "ad_101"}] } |

    Then I raise a sync event for customer "cu_100"

    Then I verify that the table "customers" has the following entries
      | customer_id | first_name | last_name | age | salary    |
      | cu_100      | Matt       | William   | 40  | 240000.50 |

    Then I verify that the table "addresses" has the following entries
      | customer_id | address_id | street      | city    |
      | cu_100      | ad_100     | 3rd Avenue  | Seattle |
      | cu_100      | ad_101     | Pine Street | Seattle |