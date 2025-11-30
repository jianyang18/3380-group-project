-- ---------------------------------------------------------------------------
-- QUERY 1: Search Title by Name
-- Purpose: Find titles by partial name match
-- Tables Used: Title
-- User Input: title name (string, partial match)
-- ---------------------------------------------------------------------------
SELECT titleID, titleType, primaryTitle, startYear, runtimeMinutes
FROM Title
WHERE primaryTitle LIKE ?
ORDER BY startYear DESC, primaryTitle;

-- ---------------------------------------------------------------------------
-- QUERY 2: Search Person by Name
-- Purpose: Find people (actors, directors, etc.) by partial name match
-- Tables Used: Person
-- User Input: person name (string, partial match)
-- ---------------------------------------------------------------------------
SELECT personID, primaryName, birthYear, deathYear
FROM Person
WHERE primaryName LIKE ?
ORDER BY primaryName;

-- ---------------------------------------------------------------------------
-- QUERY 3: Search Character by Name
-- Purpose: Find characters by partial name match
-- Tables Used: Character
-- User Input: character name (string, partial match)
-- ---------------------------------------------------------------------------
SELECT characterID, characterName
FROM Character
WHERE characterName LIKE ?
ORDER BY characterName;

-- ---------------------------------------------------------------------------
-- QUERY 4: View All Genres
-- Purpose: List all genres with count of titles in each
-- Tables Used: Genre, HasGenre
-- User Input: None
-- ---------------------------------------------------------------------------
SELECT g.genreID, g.genreName, COUNT(hg.titleID) AS titleCount
FROM Genre g
LEFT JOIN HasGenre hg ON g.genreID = hg.genreID
GROUP BY g.genreID, g.genreName
ORDER BY g.genreName;

-- ---------------------------------------------------------------------------
-- QUERY 5: View All Professions
-- Purpose: List all professions with count of people in each
-- Tables Used: Profession, WorksAs
-- User Input: None
-- ---------------------------------------------------------------------------
SELECT p.professionID, p.professionName, COUNT(w.personID) AS peopleCount
FROM Profession p
LEFT JOIN WorksAs w ON p.professionID = w.professionID
GROUP BY p.professionID, p.professionName
ORDER BY p.professionName;

-- ---------------------------------------------------------------------------
-- QUERY 6: Get Title Details
-- Purpose: Retrieve comprehensive details for a specific title by ID
-- Tables Used: Title, Rating, HasGenre, Genre
-- User Input: titleID (string, exact match)
-- ---------------------------------------------------------------------------
-- Main title information with rating
SELECT t.titleID, t.titleType, t.primaryTitle, t.isAdult,
       t.startYear, t.endYear, t.runtimeMinutes,
       r.averageRating, r.numVotes
FROM Title t
LEFT JOIN Rating r ON t.titleID = r.titleID
WHERE t.titleID = ?;

-- Genres for this title (separate query)
SELECT g.genreName
FROM Genre g
JOIN HasGenre hg ON g.genreID = hg.genreID
WHERE hg.titleID = ?
ORDER BY g.genreName;

-- ---------------------------------------------------------------------------
-- QUERY 7: Get Person Details
-- Purpose: Retrieve comprehensive details for a specific person by ID
-- Tables Used: Person, WorksAs, Profession
-- User Input: personID (string, exact match)
-- ---------------------------------------------------------------------------
-- Main person information
SELECT personID, primaryName, birthYear, deathYear
FROM Person
WHERE personID = ?;

-- Professions for this person (separate query)
SELECT DISTINCT p.professionName
FROM Profession p
JOIN WorksAs w ON p.professionID = w.professionID
WHERE w.personID = ?
ORDER BY p.professionName;

-- ---------------------------------------------------------------------------
-- QUERY 8: View Alternative Titles for a Movie
-- Purpose: Show all regional/language variations of a title
-- Tables Used: AlternativeTitle
-- User Input: titleID (string, exact match)
-- ---------------------------------------------------------------------------
SELECT ordering, title, region, language, isOriginalTitle
FROM AlternativeTitle
WHERE titleID = ?
ORDER BY ordering;

-- ---------------------------------------------------------------------------
-- QUERY 9: View All Episodes of a Series
-- Purpose: List all episodes for a TV series with ratings
-- Tables Used: Episode, Title, Rating
-- User Input: parentSeriesID (string, exact match)
-- ---------------------------------------------------------------------------
SELECT e.titleID, t.primaryTitle, e.seasonNumber, e.episodeNumber,
       r.averageRating, r.numVotes
FROM Episode e
JOIN Title t ON e.titleID = t.titleID
LEFT JOIN Rating r ON e.titleID = r.titleID
WHERE e.parentSeriesID = ?
ORDER BY e.seasonNumber, e.episodeNumber;

-- ---------------------------------------------------------------------------
-- QUERY 10: Search Titles by Genre
-- Purpose: Find all titles in a specific genre
-- Tables Used: Title, HasGenre, Genre, Rating
-- User Input: genre name (string, exact match)
-- ---------------------------------------------------------------------------
SELECT t.titleID, t.primaryTitle, t.titleType, t.startYear, r.averageRating
FROM Title t
JOIN HasGenre hg ON t.titleID = hg.titleID
JOIN Genre g ON hg.genreID = g.genreID
LEFT JOIN Rating r ON t.titleID = r.titleID
WHERE g.genreName = ?
ORDER BY r.averageRating DESC, t.primaryTitle;

-- ---------------------------------------------------------------------------
-- QUERY 11: Search Titles by Year Range
-- Purpose: Find titles released within a specific time period
-- Tables Used: Title, Rating
-- User Input: startYear (integer), endYear (integer)
-- ---------------------------------------------------------------------------
SELECT t.titleID, t.primaryTitle, t.titleType, t.startYear, 
       r.averageRating, r.numVotes
FROM Title t
LEFT JOIN Rating r ON t.titleID = r.titleID
WHERE t.startYear BETWEEN ? AND ?
ORDER BY t.startYear DESC, r.averageRating DESC;

-- ---------------------------------------------------------------------------
-- QUERY 12: Search People by Profession
-- Purpose: Find all people who work in a specific profession
-- Tables Used: Person, WorksAs, Profession
-- User Input: profession name (string, exact match)
-- ---------------------------------------------------------------------------
SELECT DISTINCT p.personID, p.primaryName, p.birthYear, p.deathYear
FROM Person p
JOIN WorksAs w ON p.personID = w.personID
JOIN Profession pr ON w.professionID = pr.professionID
WHERE pr.professionName = ?
ORDER BY p.primaryName;

-- ---------------------------------------------------------------------------
-- QUERY 13: Get Actor/Actress Age
-- Purpose: Calculate current age or age at death for actors/actresses
-- Tables Used: Person
-- User Input: actor/actress name (string, partial match)
-- ---------------------------------------------------------------------------
SELECT p.personID, p.primaryName, p.birthYear, p.deathYear,
       CASE 
           WHEN p.deathYear IS NOT NULL THEN p.deathYear - p.birthYear
           WHEN p.birthYear IS NOT NULL THEN YEAR(GETDATE()) - p.birthYear
           ELSE NULL
       END AS age,
       CASE 
           WHEN p.deathYear IS NOT NULL THEN 'Deceased'
           ELSE 'Living'
       END AS status
FROM Person p
WHERE p.primaryName LIKE ?
ORDER BY p.primaryName;

-- ---------------------------------------------------------------------------
-- QUERY 14: Who Has Played This Character?
-- Purpose: Find all actors who have portrayed a specific character
-- Tables Used: Character, PlayedIn, Person, Title
-- User Input: character name (string, partial match)
-- ---------------------------------------------------------------------------
SELECT c.characterID, c.characterName, p.personID, p.primaryName,
       t.titleID, t.primaryTitle, t.startYear, t.titleType
FROM Character c
JOIN PlayedIn pi ON c.characterID = pi.characterID
JOIN Person p ON pi.personID = p.personID
JOIN Title t ON pi.titleID = t.titleID
WHERE c.characterName LIKE ?
ORDER BY c.characterName, t.startYear DESC, p.primaryName;
