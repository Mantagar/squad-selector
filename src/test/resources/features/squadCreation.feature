Feature: Squad Creation
  Having enough players to choose from
  Create a squad of players

  Background:
    Given players exist in the db

  Scenario Outline: Creating a squad of <player_count> players
    When <player_count> players are selected
    Then return 400 with message: 'Squad size must be 11 (provided: <player_count>)'
    Examples:
      | player_count |
      | 10           |
      | 12           |

  Scenario: Creating a squad with players' positions not matching the formation
    When selected players' positions don't match the formation
    Then return 400 with message: 'Invalid squad composition'

  Scenario: Creating a squad with injured and suspended players
    When selected players contain injured and suspended ones
    Then return 400 with message: 'Players not available'

  Scenario: Creating a squad with duplicated players
    When selected players contain duplicates
    Then return 400 with message: 'Player ids aren't unique'

  Scenario: Creating a squad with non-existent players
    When selected player doesn't exist
    Then return 404 with message: 'Invalid player ids'

  Scenario: Creating a valid squad
    When selected players are valid
    Then return 201 CREATED




