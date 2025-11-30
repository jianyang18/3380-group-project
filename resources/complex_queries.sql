-- ===========================================================================
-- COMPLEX QUERIES FOR IMDb DATABASE SYSTEM
-- ===========================================================================
-- This file contains all 15 complex queries that demonstrate:
-- - GROUP BY operations
-- - ORDER BY operations
-- - Aggregate functions (COUNT, AVG, SUM, MAX, MIN, STDEVP)
-- - User parameterization
-- - Multi-table joins
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 1: Actor/Director Pairs with Most Collaborations (3+)
-- ---------------------------------------------------------------------------
-- Purpose: Find actor/director pairs who have worked together at least 3 times
-- Why Interesting: Reveals recurring professional partnerships that may
--                  influence film style and success
-- Tables Used: Person, WorksAs, Profession, PlayedIn (or similar structure)
-- User Input: None (hardcoded minimum of 3 collaborations)
-- Aggregates: COUNT(DISTINCT titleID)
-- Grouping: actorID, directorID
-- Ordering: collaboration count descending
-- ---------------------------------------------------------------------------

WITH roles AS (
    SELECT wa.titleID,
           wa.personID,
           pfn.professionName
    FROM WorksAs wa
    JOIN Profession pfn ON pfn.professionID = wa.professionID
),
actors AS (
    SELECT titleID, personID
    FROM roles
    WHERE professionName IN ('actor', 'actress')
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


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 2: Most Prolific Actors/Actresses (Top N)
-- ---------------------------------------------------------------------------
-- Purpose: List actors who have appeared in the most distinct titles
-- Why Interesting: Identifies the most active and hardworking performers
-- Tables Used: Person, WorksAs, Profession, Title
-- User Input: N (number of top actors to return)
-- Aggregates: COUNT(DISTINCT titleID)
-- Grouping: personID, personName
-- Ordering: title count descending
-- ---------------------------------------------------------------------------

WITH actor_roles AS (
    SELECT wa.personID,
           wa.titleID
    FROM WorksAs wa
    JOIN Profession pfn ON wa.professionID = pfn.professionID
    JOIN Title t ON t.titleID = wa.titleID
    WHERE pfn.professionName IN ("actor", "actress")
),
actor_counts AS (
    SELECT personID,
           COUNT(DISTINCT titleID) AS title_showed_times
    FROM actor_roles
    GROUP BY personID
)
SELECT p.primaryName AS actorName,
       ac.title_showed_times
FROM actor_counts ac
JOIN Person p ON ac.personID = p.personID
ORDER BY ac.title_showed_times DESC
LIMIT ?;  -- N is the input from user doing the query


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 3: Most Versatile Actors (By Genre Diversity)
-- ---------------------------------------------------------------------------
-- Purpose: Find actors who have appeared in the greatest number of distinct genres
-- Why Interesting: Shows which actors have the most diverse acting portfolio
-- Tables Used: Person, WorksAs, Profession, Title, HasGenre, Genre
-- User Input: None
-- Aggregates: COUNT(DISTINCT genreID), COUNT(DISTINCT titleID)
-- Grouping: personID, personName
-- Ordering: genre count descending (or ascending for specialists)
-- ---------------------------------------------------------------------------

WITH actor_titles AS (
    SELECT wa.personID,
           wa.titleID
    FROM WorksAs wa
    JOIN Profession pfn ON wa.professionID = pfn.professionID
    JOIN Title t ON t.titleID = wa.titleID
    WHERE pfn.professionName IN ("actor", "actress")
),
actor_stats AS (
    SELECT personID,
           COUNT(DISTINCT genreID) AS genre_count,
           COUNT(DISTINCT titleID) AS title_count
    FROM actor_titles at
    LEFT JOIN HasGenre hg ON hg.titleID = at.titleID
    GROUP BY personID
)
SELECT p.primaryName as actorName,
       s.genre_count,
       s.title_count
FROM actor_stats s
JOIN Person p ON p.personID = s.personID
ORDER BY s.genre_count DESC, s.title_count;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 4: TV Series Season Ratings vs Writer Count
-- ---------------------------------------------------------------------------
-- Purpose: Show each season's average rating and number of different writers
-- Why Interesting: Explores whether writer count correlates with season quality
-- Tables Used: Title, Episode, Rating, WorksAs, Profession
-- User Input: series_title (name of TV series)
-- Aggregates: COUNT(titleID), COUNT(DISTINCT personID), AVG(averageRating)
-- Grouping: seasonNumber
-- Ordering: seasonNumber ascending
-- ---------------------------------------------------------------------------

WITH episodes AS (
    SELECT e.titleID AS episodeID,
           e.seasonNumber
    FROM Title t
    JOIN Episode e ON e.parentSeriesID = t.titleID
    WHERE t.titleType = "tvSeries"
        AND t.primaryTitle = ?  -- series_title is the input from the user
),
writers AS (
    SELECT e.seasonNumber,
           COUNT(DISTINCT wa.personID) AS num_writers
    FROM episodes e
    JOIN WorksAs wa ON wa.titleID = e.episodeID
    JOIN Profession pfn ON pfn.professionID = wa.professionID
    WHERE pfn.professionName = "writer"
    GROUP BY e.seasonNumber
)
SELECT e.seasonNumber,
       COUNT(r.titleID) AS num_episodes,
       w.num_writers,
       AVG(r.averageRating)
FROM episodes e
JOIN Rating r ON r.titleID = episodeID
LEFT JOIN writers w ON w.seasonNumber
GROUP BY e.seasonNumber
ORDER BY e.seasonNumber;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 5: Hidden Gems vs Blockbusters
-- ---------------------------------------------------------------------------
-- Purpose: Find high-rated movies with few votes (hidden gems) OR
--          movies with many votes (blockbusters, can be low or high rated)
-- Why Interesting: Uncovers lesser-known quality films and popular crowd-pleasers
-- Tables Used: Title, Rating
-- User Input: None (but can be modified to add thresholds)
-- Aggregates: None (uses ORDER BY for sorting)
-- Ordering: 
--   - Hidden Gems: rating DESC, numVotes ASC
--   - Blockbusters: numVotes DESC
-- ---------------------------------------------------------------------------

-- Hidden Gems version (high ratings, few votes):
SELECT t.primaryTitle AS movitTitle,
       r.averageRating,
       r.numVotes
FROM Title t
JOIN Rating r ON t.titleID = r.titleID
WHERE t.titleType = "movie"
ORDER BY r.averageRating DESC, r.numVotes ASC
LIMIT 20;

-- Blockbusters version (many votes, sorted by votes):
SELECT t.primaryTitle AS movitTitle,
       r.averageRating,
       r.numVotes
FROM Title t
JOIN Rating r ON t.titleID = r.titleID
WHERE t.titleType = "movie"
ORDER BY r.numVotes DESC, r.averageRating ASC
LIMIT 20;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 6: Global Title vs Regional Title Performance
-- ---------------------------------------------------------------------------
-- Purpose: Compare average ratings of films with single global titles
--          vs films with multiple regional title variants
-- Why Interesting: Reveals if consistent global branding impacts audience reception
-- Tables Used: AlternativeTitle, Title, Rating
-- Aggregates: COUNT(DISTINCT title), COUNT(*), AVG(averageRating)
-- Grouping: Titling strategy (Global vs Regional)
-- ---------------------------------------------------------------------------

/* Step 1: Count the number of distinct title variants (names)
   for each titleID from the AlternativeTitle table */
WITH TitleVariantCounts AS (
    SELECT titleID,
           COUNT(DISTINCT title) AS variant_count
    FROM AlternativeTitle
    GROUP BY titleID
),
/* Step 2: Categorize each title based on its variant count.
   1 = 'Global Title', >1 = 'Regional Variants' */
TitleStrategies AS (
    SELECT titleID,
           CASE
               WHEN variant_count = 1 THEN 'Global Title'
               ELSE 'Regional Variants'
           END AS title_strategy
    FROM TitleVariantCounts
),
/* Step 3: Get the ratings for all titles that are 'movie' type.
   This filters out TV series, episodes, etc */
MovieRatings AS (
    SELECT t.titleID,
           r.averageRating
    FROM Title t
    JOIN Rating r ON t.titleID = r.titleID
    WHERE t.titleType = 'movie'
),
/* Step 4: Join the title strategy with the movie ratings.
   This ensures we are only analyzing movies */
StrategyRatings AS (
    SELECT ts.title_strategy,
           mr.averageRating
    FROM TitleStrategies ts
    JOIN MovieRatings mr ON ts.titleID = mr.titleID
)
/* Step 5: Calculate the final average rating and count
   for each strategy */
SELECT title_strategy,
       COUNT(*) AS number_of_movies,
       AVG(averageRating) AS avg_rating
FROM StrategyRatings
GROUP BY title_strategy;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 7: Most Portrayed Characters
-- ---------------------------------------------------------------------------
-- Purpose: Identify characters played by the most different actors
-- Why Interesting: Highlights iconic, frequently reinterpreted roles
-- Tables Used: Character, PlayedIn, Person
-- User Input: N (number of top characters to return)
-- Aggregates: COUNT(DISTINCT personID)
-- Grouping: characterID, characterName
-- Ordering: actor count descending
-- ---------------------------------------------------------------------------

/* Step 1: Get all unique pairs of characters and the actors
   who played them. */
WITH CharacterActorPairs AS (
    SELECT DISTINCT
        characterID,
        personID
    FROM PlayedIn
),
/* Step 2: Count the number of distinct actors (personID)
   for each character (characterID).
   Filter to include only characters played by more than one actor. */
CharacterActorCounts AS (
    SELECT characterID,
           COUNT(personID) AS actor_count
    FROM CharacterActorPairs
    GROUP BY characterID
    HAVING COUNT(personID) > 1
),
/* Step 3: Join with the Character table to get the human-readable
   character name. Filter out null or '\N' names. */
NamedCharacters AS (
    SELECT c.characterID,
           c.characterName,
           cac.actor_count
    FROM CharacterActorCounts cac
    JOIN Character c ON cac.characterID = c.characterID
    WHERE c.characterName != '\N'
)
/* Step 4: Order the results to show the most portrayed
   characters at the top, and limit the results to the top N. */
SELECT characterName,
       actor_count
FROM NamedCharacters
ORDER BY actor_count DESC
-- The number of top results to return (e.g., 10)
LIMIT ?;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 8: Genre Rating Trends by Decade
-- ---------------------------------------------------------------------------
-- Purpose: Track average ratings for a specific genre across decades
-- Why Interesting: Reveals long-term quality trends (e.g., Sci-Fi rise since 1970s)
-- Tables Used: Title, HasGenre, Genre, Rating
-- User Input: genre_name, start_decade, end_decade
-- Aggregates: COUNT(*), AVG(averageRating)
-- Grouping: genreName, decade
-- Ordering: decade ascending
-- ---------------------------------------------------------------------------

/* Step 1: Get the raw data for titles, linking genres
   and ratings. Filter for valid years and decade range. */
WITH TitleGenreData AS (
    SELECT hg.genreID,
           t.startYear,
           -- Calculate the decade (e.g., 1987 -> 1980)
           FLOOR(t.startYear / 10) * 10 AS decade,
           r.averageRating
    FROM Title t
    JOIN HasGenre hg ON t.titleID = hg.titleID
    JOIN Rating r ON t.titleID = r.titleID
    WHERE t.startYear IS NOT NULL
        -- ? = start_decade (e.g., 1980)
        AND FLOOR(t.startYear / 10) * 10 >= ?
        -- ? = end_decade (e.g., 2010)
        AND FLOOR(t.startYear / 10) * 10 <= ?
),
/* Step 2: Join with the Genre table to get genre names
   and filter for the user-specified genre. */
FilteredGenresWithDecades AS (
    SELECT g.genreName,
           tgd.decade,
           tgd.averageRating
    FROM TitleGenreData tgd
    JOIN Genre g ON tgd.genreID = g.genreID
    -- The specific genre to analyze (e.g., 'Sci-Fi')
    WHERE g.genreName = ?
)
/* Step 3: Group by genre and decade to calculate the
   average rating and title count for each period. */
SELECT genreName,
       decade,
       COUNT(*) AS title_count,
       AVG(averageRating) AS avg_rating
FROM FilteredGenresWithDecades
GROUP BY genreName, decade
ORDER BY genreName, decade;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 9: Genres by Total/Average Runtime
-- ---------------------------------------------------------------------------
-- Purpose: Calculate total and average runtime for each genre
-- Why Interesting: Shows which genres favor longer vs shorter storytelling
-- Tables Used: Title, HasGenre, Genre
-- Aggregates: SUM(runtimeMinutes), AVG(runtimeMinutes), COUNT(*)
-- Grouping: genreName
-- Ordering: average runtime descending
-- ---------------------------------------------------------------------------

/* Step 1: Get all titles that are 'movie' type and
   have a valid, non-null runtime */
WITH MovieRuntimes AS (
    SELECT titleID,
           runtimeMinutes
    FROM Title
    WHERE titleType = 'movie' AND runtimeMinutes IS NOT NULL
),
/* Step 2: Link these movies to their genres by
   joining with HasGenre and Genre tables */
GenreMovieRuntimes AS (
    SELECT g.genreName,
           mr.runtimeMinutes
    FROM MovieRuntimes mr
    JOIN HasGenre hg ON mr.titleID = hg.titleID
    JOIN Genre g ON hg.genreID = g.genreID
)
/* Step 3: Group by genreName to aggregate the runtimes.
   Calculate the SUM, AVG, and COUNT for each genre.
   Order by average runtime */
SELECT genreName,
       SUM(runtimeMinutes) AS total_runtime_minutes,
       AVG(runtimeMinutes) AS avg_runtime_minutes,
       COUNT(*) AS movie_count
FROM GenreMovieRuntimes
GROUP BY genreName
ORDER BY avg_runtime_minutes DESC;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 10: Language Ratings by Region
-- ---------------------------------------------------------------------------
-- Purpose: Analyze how films in a specific language are rated across regions
-- Why Interesting: Shows reception differences by country for same language
-- Tables Used: AlternativeTitle, Rating
-- User Input: language_code (e.g., 'en', 'fr', 'ja')
-- Aggregates: COUNT(*), AVG(averageRating)
-- Grouping: language, region
-- Ordering: region ascending
-- ---------------------------------------------------------------------------

/* Step 1: Get all titles that have a known region
   and match the user-specified language. */
WITH TitleLanguagesAndRegions AS (
    SELECT titleID,
           language,
           region
    FROM AlternativeTitle
    -- ? = The specific language code to analyze (e.g., 'en')
    WHERE language = ? AND region != '\N'
),
/* Step 2: Join the language/region data with the
   Rating table to get the averageRating for each title. */
LanguageRegionRatings AS (
    SELECT tlr.language,
           tlr.region,
           r.averageRating
    FROM TitleLanguagesAndRegions tlr
    JOIN Rating r ON tlr.titleID = r.titleID
)
/* Step 3: Group by both language and region to calculate
   the average rating and count for each combination. */
SELECT language,
       region,
       COUNT(*) AS title_count,
       AVG(averageRating) AS avg_rating
FROM LanguageRegionRatings
GROUP BY language, region
ORDER BY region;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 11: Most Popular Genres per Language
-- ---------------------------------------------------------------------------
-- Purpose: Find which genre appears most frequently in each language
-- Why Interesting: Reveals cultural storytelling preferences (e.g., Japanese animation)
-- Tables Used: AlternativeTitle, Title, HasGenre, Genre
-- Aggregates: COUNT(*) for titles per genre per language
-- Grouping: language, genreName
-- Ordering: language, count descending
-- ---------------------------------------------------------------------------

SELECT AlternativeTitle.language AS Language,
       Genre.genreName AS Genre,
       COUNT(*) AS TotalTitles
FROM AlternativeTitle
JOIN Title
    ON AlternativeTitle.titleId = Title.titleId
JOIN HasGenre
    ON Title.titleId = HasGenre.titleId
JOIN Genre
    ON HasGenre.genreID = Genre.genreID
GROUP BY AlternativeTitle.language, Genre.genreName
ORDER BY AlternativeTitle.language, TotalTitles DESC;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 12: TV Shows with Most Consistent Ratings
-- ---------------------------------------------------------------------------
-- Purpose: Find TV shows with consistent vs volatile episode ratings
-- Why Interesting: Shows quality consistency (e.g., Breaking Bad vs Game of Thrones)
-- Tables Used: Episode, Rating, Title
-- Aggregates: STDEVP(rating), AVG(rating), COUNT(episodes)
-- Grouping: series titleID, primaryTitle
-- Ordering: standard deviation ascending (consistent) or descending (volatile)
-- Notes: Volatility shows how consistent episode ratings were:
--        - Low (0-0.5): Episodes rated almost the same, steady season
--        - Medium (0.5-1.0): Some ups and downs
--        - High (1.0+): Big differences, inconsistent season
-- ---------------------------------------------------------------------------

SELECT Episode.parentSeriesID,
       Episode.seasonNumber,
       STDEVP(Rating.averageRating) AS seasonVolatility
FROM Episode
JOIN Rating
    ON Episode.titleID = Rating.titleID
JOIN Title
    ON Episode.parentSeriesID = Title.titleID
WHERE Title.primaryTitle = 'Breaking Bad'
GROUP BY Episode.parentSeriesID, Episode.seasonNumber
ORDER BY Episode.seasonNumber;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 13: Countries by Total Movie Releases
-- ---------------------------------------------------------------------------
-- Purpose: Rank countries by total film production output
-- Why Interesting: Shows size differences in national film industries
--                  E.g., US and India top the list with thousands of films,
--                  while smaller regions like Denmark rank much lower
-- Tables Used: AlternativeTitle, Title
-- Aggregates: COUNT(DISTINCT titleID)
-- Grouping: region (country)
-- Ordering: movie count descending
-- ---------------------------------------------------------------------------

SELECT AlternativeTitle.region AS Country,
       COUNT(DISTINCT Title.titleId) AS TotalMovies
FROM AlternativeTitle
JOIN Title
    ON AlternativeTitle.titleId = Title.titleId
GROUP BY AlternativeTitle.region
ORDER BY TotalMovies DESC;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 14: One-Hit Wonders
-- ---------------------------------------------------------------------------
-- Purpose: Find people known for one highly rated title but low average otherwise
-- Why Interesting: Identifies actors/directors who never repeated their early success
-- Tables Used: Person, KnownFor, Rating
-- User Input: min_rating (e.g., 7.1), min_difference (e.g., 1.5)
-- Aggregates: MAX(rating), AVG(rating), COUNT(*)
-- Grouping: personID, primaryName
-- Ordering: rating difference descending
-- ---------------------------------------------------------------------------

-- Temporary table to store aggregates
WITH RatingStats AS (
    SELECT knownFor.personID,
           MAX(Rating.averageRating) AS peakRating,
           AVG(Rating.averageRating) AS avgRating,
           COUNT(*) AS totalTitles
    FROM knownFor
    JOIN Rating
        ON knownFor.titleID = Rating.titleID
    GROUP BY knownFor.personID
)
SELECT Person.primaryName,
       RatingStats.peakRating,
       RatingStats.avgRating,
       -- Most find the difference between peak and average rating
       (RatingStats.peakRating - RatingStats.avgRating) AS ratingGap
FROM RatingStats
JOIN Person
    ON Person.personID = RatingStats.personID
    -- We don't consider people who only have one total title
WHERE RatingStats.totalTitles > 1
    -- Adjust our definition of "High rating" here
    AND RatingStats.peakRating >= ?  -- (Eg 7.1)
    -- Also adjust what we consider to be a "Big difference"
    -- between average and peak rating
    AND (RatingStats.peakRating - RatingStats.avgRating) >= ?  -- (Eg 1.5)
ORDER BY ratingGap DESC;


-- ---------------------------------------------------------------------------
-- COMPLEX QUERY 15: All Movies by Specific Actor
-- ---------------------------------------------------------------------------
-- Purpose: List all movies featuring a particular actor
-- Why Interesting: Quick lookup for exploring an actor's filmography
-- Tables Used: Person, PlayedIn, Title
-- User Input: actor_name (e.g., "Andrew Garfield")
-- Aggregates: None
-- Grouping: None
-- Ordering: year descending, title ascending
-- ---------------------------------------------------------------------------

SELECT Person.primaryName AS Person,
       Title.primaryTitle AS MovieTitle,
       Title.startYear AS ReleaseYear
FROM Person
JOIN PlayedIn
    ON Person.personID = PlayedIn.personID
JOIN Title
    ON PlayedIn.titleID = Title.titleID
WHERE Person.primaryName = ?;


-- ===========================================================================
-- END OF COMPLEX QUERIES
-- ===========================================================================
-- Note: These queries are templates. Actual implementation in Java will use
-- PreparedStatement with ? placeholders for user input parameters.
-- ===========================================================================
