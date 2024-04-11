package com.mts.hackathon.service;

import com.api.igdb.apicalypse.APICalypse;
import com.api.igdb.exceptions.RequestException;
import com.api.igdb.request.IGDBWrapper;
import com.api.igdb.request.ProtoRequestKt;
import com.mts.hackathon.igdb_info.GameGenre;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import proto.Game;
import proto.Genre;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final IGDBWrapper igdbWrapper;
    private final APICalypse apiCalypse;

    public String getGameByName(String name) {
        List<Game> listOfGames;
        try {
            listOfGames = ProtoRequestKt.games(igdbWrapper, apiCalypse.fields("*").search(name).limit(1));
        } catch (RequestException e) {
            throw new RuntimeException(e);
        }
        return prepareResponse(listOfGames);
    }

    private String prepareResponse(List<Game> gameList) {
        StringBuilder response = new StringBuilder();
        for (Game game : gameList) {
            String str = String.format("""
                            Название игры: %s
                            Жанры: %s
                            Рейтинг пользователей: %s
                            Рейтинг IGDB: %s
                            Ссылка на IGDB: %s
                                
                            """, game.getName(),
                    genreIdToString(game.getGenresList()),
                    String.format("%.1f", game.getTotalRating()),
                    String.format("%.1f", game.getAggregatedRating()),
                    game.getUrl());
            response.append(str);
        }
        return response.toString();
    }

    private List<String> genreIdToString(List<Genre> genreList) {
        return genreList.stream()
                .map(game -> {
                    for (GameGenre genre : GameGenre.values()) {
                        if (genre.getId() == game.getId()) {
                            return genre.name();
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
