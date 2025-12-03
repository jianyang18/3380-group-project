# Architecture

## 3-Layer Design

### Presentation (`ui/`)

- Interface - Main UI coordinator
- MenuSystem - Menu displays
- StatisticsDisplay - Database stats

### Logic (`logic/`)

- TitleQueries - Movie/TV queries
- PersonQueries - Actor/director queries
- CharacterQueries - Character queries
- GenreQueries - Genre/statistics queries

### Data (`database/`)

- DatabaseConfig - Connection management
- DatabaseManager - DB operations
- DatabaseLoader - SQL loading

### Utilities (`utils/`)

- ResultFormatter - Pagination & formatting
- QueryUtils - Shared helpers
- InputValidator - Input validation

## Data Flow

```
User -> Interface -> MenuSystem
         \/
    Query Handler
         \/
    DatabaseConfig
         \/
    SQL Execution
         \/
    ResultFormatter
```

## Database Schema

11 tables: Title, Person, Character, Genre, Profession, Rating, Episode, AlternativeTitle, HasGenre, WorksAs, PlayedIn
