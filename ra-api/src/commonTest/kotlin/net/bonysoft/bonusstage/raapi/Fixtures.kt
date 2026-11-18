package net.bonysoft.bonusstage.raapi

/**
 * Recorded responses, based on the samples in the RetroAchievements API docs
 * (https://github.com/RetroAchievements/api-docs, MIT), completed where the docs abbreviate them.
 */
object Fixtures {
    const val USER_PROFILE = """
{
  "User": "MaxMilyin",
  "ULID": "00003EMFWR7XB8SDPEHB3K56ZQ",
  "UserPic": "/UserPic/MaxMilyin.png",
  "MemberSince": "2016-01-02 00:43:04",
  "RichPresenceMsg": "Playing ~Hack~ 11th Annual Vanilla Level Design Contest, The",
  "LastGameID": 19504,
  "ContribCount": 0,
  "ContribYield": 0,
  "TotalPoints": 399597,
  "TotalSoftcorePoints": 0,
  "TotalTruePoints": 1599212,
  "Permissions": 1,
  "Untracked": 0,
  "ID": 16446,
  "UserWallActive": 1,
  "Motto": "Join me on Twitch! GameSquadSquad for live RA"
}
"""

    const val USER_SUMMARY = """
{
  "User": "xelnia",
  "ULID": "00003EMFWR7XB8SDPEHB3K56ZQ",
  "MemberSince": "2021-12-20 03:13:20",
  "LastActivity": {"ID": 0, "timestamp": null, "lastupdate": null, "activitytype": null, "User": "xelnia", "data": null, "data2": null},
  "RichPresenceMsg": "L=08-1 | 1 lives | 189300 points",
  "RichPresenceMsgDate": "2025-11-19 12:05:04",
  "LastGameID": 15758,
  "ContribCount": 0,
  "ContribYield": 0,
  "TotalPoints": 8317,
  "TotalSoftcorePoints": 0,
  "TotalTruePoints": 26760,
  "Permissions": 1,
  "Untracked": 0,
  "ID": 224958,
  "UserWallActive": 1,
  "Motto": "",
  "Rank": 4616,
  "RecentlyPlayedCount": 1,
  "RecentlyPlayed": [
    {
      "GameID": 15758,
      "ConsoleID": 27,
      "ConsoleName": "Arcade",
      "Title": "Crazy Kong",
      "ImageIcon": "/Images/068578.png",
      "ImageTitle": "/Images/068579.png",
      "ImageIngame": "/Images/068580.png",
      "ImageBoxArt": "/Images/068205.png",
      "LastPlayed": "2023-03-09 08:20:34",
      "AchievementsTotal": 43
    }
  ],
  "Awarded": [],
  "RecentAchievements": [],
  "UserPic": "/UserPic/xelnia.png",
  "TotalRanked": 45654,
  "Status": "Offline"
}
"""

    const val USER_COMPLETION_PROGRESS = """
{
  "Count": 2,
  "Total": 2,
  "Results": [
    {
      "GameID": 20246,
      "Title": "~Hack~ Knuckles the Echidna in Sonic the Hedgehog",
      "ImageIcon": "/Images/074560.png",
      "ConsoleID": 1,
      "ConsoleName": "Mega Drive / Genesis",
      "MaxPossible": 0,
      "NumAwarded": 0,
      "NumAwardedHardcore": 0,
      "MostRecentAwardedDate": "2023-10-27T02:52:34+00:00",
      "HighestAwardKind": "beaten-hardcore",
      "HighestAwardDate": "2023-10-27T02:52:34+00:00"
    },
    {
      "GameID": 1,
      "Title": "Sonic the Hedgehog",
      "ImageIcon": "/Images/067895.png",
      "ConsoleID": 1,
      "ConsoleName": "Mega Drive / Genesis",
      "MaxPossible": 23,
      "NumAwarded": 12,
      "NumAwardedHardcore": 10,
      "MostRecentAwardedDate": null,
      "HighestAwardKind": null,
      "HighestAwardDate": null
    }
  ]
}
"""

    const val GAME_INFO_AND_USER_PROGRESS = """
{
  "ID": 1,
  "Title": "Sonic the Hedgehog",
  "ConsoleID": 1,
  "ForumTopicID": 112,
  "Flags": null,
  "ImageIcon": "/Images/067895.png",
  "ImageTitle": "/Images/054993.png",
  "ImageIngame": "/Images/000010.png",
  "ImageBoxArt": "/Images/051872.png",
  "Publisher": "",
  "Developer": "",
  "Genre": "",
  "Released": "1992-06-02 00:00:00",
  "ReleasedAtGranularity": "day",
  "IsFinal": false,
  "RichPresencePatch": "cce60593880d25c97797446ed33eaffb",
  "GuideURL": null,
  "ConsoleName": "Mega Drive",
  "ParentGameID": null,
  "NumDistinctPlayers": 27080,
  "NumAchievements": 2,
  "Achievements": {
    "9": {
      "ID": 9,
      "NumAwarded": 24273,
      "NumAwardedHardcore": 10831,
      "Title": "That Was Easy",
      "Description": "Complete the first act in Green Hill Zone",
      "Points": 3,
      "TrueRatio": 3,
      "Author": "Scott",
      "AuthorULID": "00003EMFWR7XB8SDPEHB3K56ZQ",
      "DateModified": "2023-08-08 00:36:59",
      "DateCreated": "2012-11-02 00:03:12",
      "BadgeName": "250336",
      "DisplayOrder": 1,
      "MemAddr": "22c9d5e2cd7571df18a1a1b43dfe1fea",
      "type": "progression",
      "DateEarnedHardcore": "2016-03-12 17:47:29",
      "DateEarned": "2016-03-12 17:47:29"
    },
    "10": {
      "ID": 10,
      "NumAwarded": 9100,
      "NumAwardedHardcore": 4200,
      "Title": "Ring Collector",
      "Description": "Collect 100 rings",
      "Points": 5,
      "TrueRatio": 7,
      "Author": "Scott",
      "AuthorULID": "00003EMFWR7XB8SDPEHB3K56ZQ",
      "DateModified": "2023-08-08 00:36:59",
      "DateCreated": "2012-11-02 00:03:12",
      "BadgeName": "250337",
      "DisplayOrder": 2,
      "MemAddr": "0123456789abcdef0123456789abcdef",
      "type": null
    }
  },
  "NumAwardedToUser": 1,
  "NumAwardedToUserHardcore": 1,
  "NumDistinctPlayersCasual": 27080,
  "NumDistinctPlayersHardcore": 27080,
  "UserCompletion": "50.00%",
  "UserCompletionHardcore": "50.00%",
  "UserTotalPlaytime": 60,
  "HighestAwardKind": null,
  "HighestAwardDate": null
}
"""

    /** A game without achievements: PHP sends the empty map as an array. */
    const val GAME_WITHOUT_ACHIEVEMENTS = """
{
  "ID": 30000,
  "Title": "~Homebrew~ Empty Set",
  "ConsoleID": 18,
  "ConsoleName": "Nintendo DS",
  "ImageIcon": "/Images/000001.png",
  "NumDistinctPlayers": 0,
  "NumAchievements": 0,
  "Achievements": [],
  "NumAwardedToUser": 0,
  "NumAwardedToUserHardcore": 0
}
"""

    const val USER_RECENT_ACHIEVEMENTS = """
[
  {
    "Date": "2023-12-27 16:04:50",
    "HardcoreMode": 1,
    "AchievementID": 98012,
    "Title": "Beginner I",
    "Description": "Clear stages 01 - 05 in Quest.",
    "BadgeName": "108302",
    "Points": 5,
    "TrueRatio": 25,
    "Type": null,
    "Author": "jos",
    "AuthorULID": "00003EMFWR7XB8SDPEHB3K56ZQ",
    "GameTitle": "Pokemon Pinball mini",
    "GameIcon": "/Images/028399.png",
    "GameID": 14715,
    "ConsoleName": "Pokemon Mini",
    "BadgeURL": "/Badge/108302.png",
    "GameURL": "/game/14715"
  },
  {
    "Date": "2023-12-27 15:58:01",
    "HardcoreMode": "0",
    "AchievementID": 98011,
    "Title": "Novice",
    "Description": "Clear stage 01 in Quest.",
    "BadgeName": "108301",
    "Points": 2,
    "TrueRatio": 3,
    "Type": "progression",
    "Author": "jos",
    "AuthorULID": "00003EMFWR7XB8SDPEHB3K56ZQ",
    "GameTitle": "Pokemon Pinball mini",
    "GameIcon": "/Images/028399.png",
    "GameID": 14715,
    "ConsoleName": "Pokemon Mini",
    "BadgeURL": "/Badge/108301.png",
    "GameURL": "/game/14715"
  }
]
"""

    const val USER_WANT_TO_PLAY_LIST = """
{
  "Count": 1,
  "Total": 1,
  "Results": [
    {
      "ID": 20246,
      "Title": "~Hack~ Knuckles the Echidna in Sonic the Hedgehog",
      "ImageIcon": "/Images/074560.png",
      "ConsoleID": 1,
      "ConsoleName": "Genesis/Mega Drive",
      "PointsTotal": 1500,
      "AchievementsPublished": 50
    }
  ]
}
"""

    const val ACTIVE_CLAIMS = """
[
  {
    "ID": 11246,
    "User": "WanderingHeiho",
    "ULID": "00003EMFWR7XB8SDPEHB3K56ZQ",
    "GameID": 26971,
    "GameTitle": "~Homebrew~ No Place To Hide",
    "GameIcon": "/Images/084916.png",
    "ConsoleID": 18,
    "ConsoleName": "Nintendo DS",
    "ClaimType": 0,
    "SetType": 0,
    "Status": 0,
    "Extension": 0,
    "Special": 0,
    "Created": "2023-10-27 23:27:16",
    "DoneTime": "2024-01-27 23:27:16",
    "Updated": "2023-10-27 23:27:16",
    "UserIsJrDev": 0,
    "MinutesLeft": -41266
  }
]
"""

    const val USER_AWARDS = """
{
  "TotalAwardsCount": 1613,
  "HiddenAwardsCount": 0,
  "MasteryAwardsCount": 805,
  "CompletionAwardsCount": 0,
  "BeatenHardcoreAwardsCount": 807,
  "BeatenSoftcoreAwardsCount": 0,
  "EventAwardsCount": 2,
  "SiteAwardsCount": 0,
  "VisibleUserAwards": [
    {
      "AwardedAt": "2016-01-02T05:53:52+00:00",
      "AwardType": "Game Beaten",
      "AwardData": 1448,
      "AwardDataExtra": 1,
      "DisplayOrder": 0,
      "Title": "Mega Man",
      "ConsoleID": 7,
      "ConsoleName": "NES",
      "Flags": 0,
      "ImageIcon": "/Images/024519.png"
    }
  ]
}
"""

    const val CONSOLE_IDS = """
[
  {
    "ID": 1,
    "Name": "Mega Drive",
    "IconURL": "https://static.retroachievements.org/assets/images/system/md.png",
    "Active": true,
    "IsGameSystem": true
  },
  {
    "ID": 101,
    "Name": "Events",
    "IconURL": "https://static.retroachievements.org/assets/images/system/unknown.png",
    "Active": 0,
    "IsGameSystem": 0
  }
]
"""

    const val GAME_LIST = """
[
  {
    "Title": "Advanced Busterhawk: Gley Lancer",
    "ID": 3684,
    "ConsoleID": 1,
    "ConsoleName": "Mega Drive",
    "ImageIcon": "/Images/020895.png",
    "NumAchievements": 44,
    "NumLeaderboards": 33,
    "Points": 595,
    "DateModified": "2022-11-20 03:44:12",
    "ForumTopicID": 1936,
    "Hashes": ["8bd4a97783cda077c342173df0a9b51e", "a13ab653a20fb383337fab1e52ddb0df"]
  }
]
"""

    const val ACHIEVEMENT_OF_THE_WEEK = """
{
  "Achievement": {
    "ID": 178634,
    "Title": "Saved Summer",
    "Description": "Defeat the Flower and let Summer Get Busy",
    "Points": 10,
    "TrueRatio": 11,
    "Type": null,
    "Author": "StingX2",
    "AuthorULID": "00003EMFWR7XB8SDPEHB3K56ZQ",
    "DateCreated": "2021-10-16",
    "DateModified": "2021-10-17"
  },
  "Console": {"ID": 3, "Title": "SNES"},
  "ForumTopic": {"ID": 19685},
  "Game": {"ID": 2865, "Title": "~Hack~ Plumber For All Seasons, A"},
  "StartAt": "2023-10-23T00:00:00.000000Z",
  "TotalPlayers": 427,
  "Unlocks": [
    {
      "User": "Agnam",
      "ULID": "00003EMFWR7XB8SDPEHB3K56ZQ",
      "RAPoints": 56120,
      "RASoftcorePoints": 1267,
      "DateAwarded": "2023-10-26T22:13:34.000000Z",
      "HardcoreMode": 1
    }
  ],
  "UnlocksCount": 280,
  "UnlocksHardcoreCount": 268
}
"""

    const val USERS_I_FOLLOW = """
{
  "Count": 2,
  "Total": 2,
  "Results": [
    {"User": "zuliman92", "ULID": "00003EMFWR7XB8SDPEHB3K56ZQ", "Points": 1882, "PointsSoftcore": 258, "IsFollowingMe": true},
    {"User": "Scott", "ULID": "00003EMFWR7XB8SDPEHB3K56ZR", "Points": 51000, "PointsSoftcore": 0, "IsFollowingMe": false}
  ]
}
"""

    const val USERS_FOLLOWING_ME = """
{
  "Count": 1,
  "Total": 1,
  "Results": [
    {"User": "zuliman92", "ULID": "00003EMFWR7XB8SDPEHB3K56ZQ", "Points": 1882, "PointsSoftcore": 258, "AmIFollowing": true}
  ]
}
"""
}
