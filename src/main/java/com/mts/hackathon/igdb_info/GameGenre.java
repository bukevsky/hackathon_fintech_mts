package com.mts.hackathon.igdb_info;

import lombok.Getter;

@Getter
public enum GameGenre {
    POINT_AND_CLICK(2),
    FIGHTING(4),
    SHOOTER(5),
    MUSIC(7),
    PLATFORM(8),
    PUZZLE(9),
    RACING(10),
    REAL_TIME_STRATEGY(11),
    ROLE_PLAYING(12),
    SIMULATOR(13),
    SPORT(14),
    STRATEGY(15),
    TURN_BASED_STRATEGY(16),
    TACTICAL(24),
    HACK_AND_SLASH(25),
    QUIZ_TRIVIA(26),
    PINBALL(30),
    ADVENTURE(31),
    INDIE(32),
    ARCADE(33),
    VISUAL_NOVEL(34),
    CARD_AND_BOARD_GAME(35),
    MOBA(36);

    private final int id;

    GameGenre(int id) {
        this.id = id;
    }
}
