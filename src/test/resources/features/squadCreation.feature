Feature: Squad Creation
  Having enough players to choose from
  Create a squad of players

  Background:
    Given players exist in the db

  Scenario Outline: Creating squad of <player_count> players
    When <player_count> players are selected
    Then return 400 with message that squad size must be 11
    Examples:
      | player_count |
      | 10           |
      | 12           |

