# IMDb Database System

Java command-line application for querying an IMDb movie database.

### Compile and Run

- Make sure sqlite-jdbc.jar file is in the lib/ directory. 
- Make sure auth.cfg credentials are correct.

```bash
make compile
make run
```

## Project Structure

```
src/
 Main.java           # Entry point
 database/           # DatabaseConfig, DatabaseManager, DatabaseLoader
 logic/              # TitleQueries, PersonQueries, CharacterQueries, GenreQueries  
 ui/                 # Interface, MenuSystem, StatisticsDisplay
 utils/              # ResultFormatter, QueryUtils, InputValidator
```

## Features

- Search movies, TV shows, actors, characters
- Complex analytical queries
- Database statistics and management

---
**COMP 3380 Group 40**
- Jian Yang
- Mohammad Mohid Afzal
- Mohammad Mujahidul Islam

