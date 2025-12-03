-- =============================================================
-- STAGE 5 QUERIES (MSSQL)
-- =============================================================

-- Query 1: Actor/Director Collaborations 45
WITH roles AS (
    SELECT wa.titleID, wa.personID, pfn.professionName
    FROM WorksAs wa
    JOIN Profession pfn ON pfn.professionID = wa.professionID
),
actors AS (
    SELECT titleID, personID
    FROM PlayedIn
),
directors AS (
    SELECT titleID, personID
    FROM roles
    WHERE professionName = 'director'
),
pair_titles AS (
    SELECT a.personID AS actorID,
           d.personID AS directorID,
           a.titleID AS titleID
    FROM actors a
    JOIN directors d ON d.titleID = a.titleID
),
pair_counts AS (
    SELECT actorID, directorID, COUNT(DISTINCT titleID) AS times_worked
    FROM pair_titles
    GROUP BY actorID, directorID
    HAVING COUNT(DISTINCT titleID) >= 3
)
SELECT pa.primaryName AS actorName,
       pd.primaryName AS directorName,
       pc.times_worked
FROM pair_counts pc
JOIN Person pa ON pa.personID = pc.actorID
JOIN Person pd ON pd.personID = pc.directorID
ORDER BY pc.times_worked DESC;


-- Query 2: Most Prolific Actors (Top 10) 11 sec
WITH actor_counts AS (
    SELECT personID, COUNT(DISTINCT titleID) AS title_showed_times
    FROM PlayedIn
    GROUP BY personID
)
SELECT TOP 10 
       p.primaryName AS actorName,
       ac.title_showed_times
FROM actor_counts ac
JOIN Person p ON ac.personID = p.personID
ORDER BY ac.title_showed_times DESC;


-- Query 3: Most Versatile Actors (Most Genres) ()
WITH actor_titles AS (
    SELECT personID, titleID
    FROM PlayedIn
),
actor_stats AS (
    SELECT personID,
           COUNT(DISTINCT genreID) AS genre_count,
           COUNT(DISTINCT at.titleID) AS title_count
    FROM actor_titles at
    LEFT JOIN HasGenre hg ON hg.titleID = at.titleID
    GROUP BY personID
)
SELECT TOP 20
       p.primaryName AS actorName,
       s.genre_count,
       s.title_count
FROM actor_stats s
JOIN Person p ON p.personID = s.personID
ORDER BY s.genre_count DESC, s.title_count DESC;


-- Query 4: Writer Count vs Ratings for a TV Show (instant)
WITH episodes AS (
    SELECT e.titleID AS episodeID,
           e.seasonNumber
    FROM Title t
    JOIN Episode e ON e.parentSeriesID = t.titleID
    WHERE t.titleType = 'tvSeries'
      AND t.primaryTitle = 'Breaking Bad' -- INPUT
),
writers AS (
    SELECT e.seasonNumber,
           COUNT(DISTINCT wa.personID) AS num_writers
    FROM episodes e
    JOIN WorksAs wa ON wa.titleID = e.episodeID
    JOIN Profession pfn ON pfn.professionID = wa.professionID
    WHERE pfn.professionName = 'writer'
    GROUP BY e.seasonNumber
)
SELECT e.seasonNumber,
       COUNT(r.titleID) AS num_episodes,
       w.num_writers,
       AVG(r.averageRating) AS avg_rating
FROM episodes e
JOIN Rating r ON r.titleID = e.episodeID
LEFT JOIN writers w ON w.seasonNumber = e.seasonNumber
GROUP BY e.seasonNumber, w.num_writers
ORDER BY e.seasonNumber;


-- Query 5: Hidden Gems vs Blockbusters (instant)
SELECT TOP 20
       t.primaryTitle AS movieTitle,
       r.averageRating,
       r.numVotes
FROM Title t
JOIN Rating r ON t.titleID = r.titleID
WHERE t.titleType = 'movie'
ORDER BY r.averageRating DESC, r.numVotes ASC;


-- Query 6: Regional Reach vs Ratings & Popularity (1s)
WITH TitleVariantCounts AS (
    SELECT titleID,
           COUNT(DISTINCT title) AS variant_count
    FROM AlternativeTitle
    GROUP BY titleID
),
TitleStrategies AS (
    SELECT titleID,
           CASE 
               WHEN variant_count = 1 THEN 'Global Title'
               ELSE 'Regional Variants'
           END AS title_strategy
    FROM TitleVariantCounts
),
MovieRatings AS (
    SELECT t.titleID, r.averageRating
    FROM Title t
    JOIN Rating r ON t.titleID = r.titleID
    WHERE t.titleType = 'movie'
),
StrategyRatings AS (
    SELECT ts.title_strategy, mr.averageRating
    FROM TitleStrategies ts
    JOIN MovieRatings mr ON ts.titleID = mr.titleID
)
SELECT title_strategy,
       COUNT(*) AS number_of_movies,
       AVG(averageRating) AS avg_rating
FROM StrategyRatings
GROUP BY title_strategy;


-- Query 7: Character played by most actors
WITH CharacterActorPairs AS (
    SELECT DISTINCT characterID, personID
    FROM PlayedIn
),
CharacterActorCounts AS (
    SELECT characterID, COUNT(personID) AS actor_count
    FROM CharacterActorPairs
    GROUP BY characterID
    HAVING COUNT(personID) > 1
),
NamedCharacters AS (
    SELECT c.characterName, cac.actor_count
    FROM CharacterActorCounts cac
    JOIN Character c ON cac.characterID = c.characterID
    WHERE c.characterName != '\N'
)

SELECT TOP 10
       characterName, actor_count
FROM NamedCharacters
ORDER BY actor_count DESC;


-- Query 8: Genre Ratings by Decade (instant)
WITH TitleGenreData AS (
    SELECT hg.genreID,
           t.startYear,
           (t.startYear / 10) * 10 AS decade,
           r.averageRating
    FROM Title t
    JOIN HasGenre hg ON t.titleID = hg.titleID
    JOIN Rating r ON t.titleID = r.titleID
    WHERE t.startYear IS NOT NULL
      AND (t.startYear / 10) * 10 >= 1980 
      AND (t.startYear / 10) * 10 <= 2020 
),
FilteredGenres AS (
    SELECT g.genreName, tgd.decade, tgd.averageRating
    FROM TitleGenreData tgd
    JOIN Genre g ON tgd.genreID = g.genreID
    WHERE g.genreName = 'Sci-Fi' -- INPUT
)
SELECT genreName,
       decade,
       COUNT(*) AS title_count,
       AVG(averageRating) AS avg_rating
FROM FilteredGenres
GROUP BY genreName, decade
ORDER BY genreName, decade;


-- Query 9: Genre Runtimes (instant)
WITH MovieRuntimes AS (
    SELECT titleID, runtimeMinutes
    FROM Title
    WHERE titleType = 'movie' AND runtimeMinutes IS NOT NULL
),
GenreMovieRuntimes AS (
    SELECT g.genreName, mr.runtimeMinutes
    FROM MovieRuntimes mr
    JOIN HasGenre hg ON mr.titleID = hg.titleID
    JOIN Genre g ON hg.genreID = g.genreID
)
SELECT genreName,
       SUM(runtimeMinutes) AS total_runtime_minutes,
       AVG(runtimeMinutes) AS avg_runtime_minutes,
       COUNT(*) AS movie_count
FROM GenreMovieRuntimes
GROUP BY genreName
ORDER BY avg_runtime_minutes DESC;


-- Query 10: Language Ratings (instant)
WITH TitleLanguages AS (
    SELECT titleID, language, region
    FROM AlternativeTitle
    WHERE language = 'en' -- INPUT
      AND region != '\N'
),
LanguageRegionRatings AS (
    SELECT tir.language, tir.region, r.averageRating
    FROM TitleLanguages tir
    JOIN Rating r ON tir.titleID = r.titleID
)
SELECT language,
       region,
       COUNT(*) AS title_count,
       AVG(averageRating) AS avg_rating
FROM LanguageRegionRatings
GROUP BY language, region
ORDER BY region;


-- Query 11: Popular Genres by Language (1second)
SELECT TOP 50
       at.language AS Language,
       g.genreName AS Genre,
       COUNT(*) AS TotalTitles
FROM AlternativeTitle at
JOIN Title t ON at.titleID = t.titleID
JOIN HasGenre hg ON t.titleID = hg.titleID
JOIN Genre g ON hg.genreID = g.genreID
WHERE at.language IS NOT NULL AND at.language != '\N'
GROUP BY at.language, g.genreName
ORDER BY TotalTitles DESC;


-- Query 12: TV Show Volatility (instant)
-- NOTE: Using STDEVP which is native to MSSQL
SELECT e.parentSeriesID,
       e.seasonNumber,
       STDEVP(r.averageRating) AS seasonVolatility, -- Native MSSQL function
       AVG(r.averageRating) as avg_rating
FROM Episode e
JOIN Rating r ON e.titleID = r.titleID
JOIN Title t ON e.parentSeriesID = t.titleID
WHERE t.primaryTitle = 'Breaking Bad' -- INPUT
GROUP BY e.parentSeriesID, e.seasonNumber
ORDER BY e.seasonNumber;


-- Query 13: Movies per Country (2s)
SELECT TOP 20
       at.region AS Country,
       COUNT(DISTINCT t.titleID) AS TotalMovies
FROM AlternativeTitle at
JOIN Title t ON at.titleID = t.titleID
WHERE at.region != '\N'
GROUP BY at.region
ORDER BY TotalMovies DESC;


-- Query 14: One Hit Wonders (2s)
WITH RatingStats AS (
    SELECT kf.personID,
           MAX(r.averageRating) AS peakRating,
           AVG(r.averageRating) AS avgRating,
           COUNT(*) AS totalTitles
    FROM WorksAs kf 
    JOIN Rating r ON kf.titleID = r.titleID
    GROUP BY kf.personID
)
SELECT TOP 20
       p.primaryName,
       rs.peakRating,
       rs.avgRating,
       (rs.peakRating - rs.avgRating) AS ratingGap
FROM RatingStats rs
JOIN Person p ON p.personID = rs.personID
WHERE rs.totalTitles > 1
  AND rs.peakRating >= 8.0 
  AND (rs.peakRating - rs.avgRating) >= 2.0 
ORDER BY ratingGap DESC;


-- Query 15: Movies by Actor (instant)
SELECT p.primaryName AS Person,
       t.primaryTitle AS MovieTitle,
       t.startYear AS ReleaseYear
FROM Person p
JOIN PlayedIn pi ON p.personID = pi.personID
JOIN Title t ON pi.titleID = t.titleID
WHERE p.primaryName = 'Dwayne Johnson' -- INPUT
ORDER BY t.startYear DESC;
