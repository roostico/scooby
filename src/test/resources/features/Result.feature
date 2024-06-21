Feature: Result update and aggregation

  Example: Update result with a new entry
    Given I have a Scraper and its result
    When The Scraper filters the document
    And Obtains a non-empty element
    Then The result must be updated adding the element

  Example: Aggregate results from multiple sources
    Given I have multiple results
    When Each Scraper end its work
    And Notifies each other
    Then The aggregated result must be the same as the aggregation of all results

  Rule: Result should support different types, such as String and Map

    Example: Result provided in different formats
      Given I have a Scraper that can be configured with different filtering strategies
      When It filters the document, new entries need to be added to actual result
      Then The Result class must manage different formats of entries

    Scenario Outline: Gather data in various format
      Given I have Scrapers that elaborate documents with different filtering policies which generates different data <type> from the document
      And There are different <documents>
      When The scraper starts filtering the document, obtaining data to aggregate
      Then It will obtain a <result>

      Examples:
        |           type            |                   documents                              |              result              |
        |         String            | <a>txt_match_filter</a><div>txt_not_match_filter</div>   |  <a>txt_match_filter</a>         |
        | Map[String, String]       | <a>txt_match_filter</a><div>txt_not_match_filter</div>   |   Map(a -> txt_match_filter)     |
        |         String            | <a>match</a><div>non_match</div><div>match</div>         |  <a>match</a>\n<div>match</div>  |