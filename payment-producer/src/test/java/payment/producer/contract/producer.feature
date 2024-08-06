Feature: Consumer Contract
  Background:
    #    * def port = karate.start('classpath:payment/consumer/payment-mock.feature').port
    * def paymentServiceUrl = karate.properties['payment.service.url']
    * url paymentServiceUrl + '/payments'
  Scenario: Consumer expects a specific response
    #    Given url 'http://localhost:'+ port+ '/payments'
    #    Given url paymentServiceUrl+ '/payments'
    Given request { amount: 5.67, description: 'test one' }
    When method post
    Then status 200
    And match response == { id: '#number', amount: 5.67, description: 'test one' }
    And def id = response.id

    #    Given url 'http://localhost:'+ port+ '/payments/' + id
    Given path id
    When method get
    Then status 200
    And match response == { id: '#(id)', amount: 5.67, description: 'test one' }

    Given path id
    And request { id: '#(id)', amount: 5.67, description: 'test two' }
    When method put
    Then status 200
    And match response == { id: '#(id)', amount: 5.67, description: 'test two' }

    When method get
    Then status 200
    And match response contains { id: '#(id)', amount: 5.67, description: 'test two' }

    Given path id
    When method delete
    Then status 200

    When method get
    Then status 200
    And match response !contains { id: '#(id)', amount: '#number', description: '#string' }

