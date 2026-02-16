-- Teams Table
CREATE TABLE IF NOT EXISTS teams (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    league VARCHAR(255) NOT NULL,
    goals_scored INTEGER DEFAULT 0,
    goals_conceded INTEGER DEFAULT 0,
    average_xg DECIMAL(4,2),
    average_xa DECIMAL(4,2),
    wins INTEGER DEFAULT 0,
    draws INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Matches Table
CREATE TABLE IF NOT EXISTS matches (
    id SERIAL PRIMARY KEY,
    home_team_id INTEGER NOT NULL REFERENCES teams(id),
    away_team_id INTEGER NOT NULL REFERENCES teams(id),
    home_score INTEGER,
    away_score INTEGER,
    home_xg DECIMAL(4,2),
    away_xg DECIMAL(4,2),
    home_xa DECIMAL(4,2),
    away_xa DECIMAL(4,2),
    home_possession DECIMAL(5,2),
    away_possession DECIMAL(5,2),
    home_shots INTEGER,
    away_shots INTEGER,
    match_date TIMESTAMP NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Predictions Table
CREATE TABLE IF NOT EXISTS predictions (
    id SERIAL PRIMARY KEY,
    home_team_id INTEGER NOT NULL,
    away_team_id INTEGER NOT NULL,
    home_win_prob DECIMAL(5,3),
    draw_prob DECIMAL(5,3),
    away_win_prob DECIMAL(5,3),
    predicted_home_xg DECIMAL(4,2),
    predicted_away_xg DECIMAL(4,2),
    ai_analysis TEXT,
    confidence VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_matches_home_team ON matches(home_team_id);
CREATE INDEX IF NOT EXISTS idx_matches_away_team ON matches(away_team_id);
CREATE INDEX IF NOT EXISTS idx_matches_date ON matches(match_date);
CREATE INDEX IF NOT EXISTS idx_predictions_teams ON predictions(home_team_id, away_team_id);

-- Sample Data: Premier League Teams
INSERT INTO teams (name, league, average_xg, average_xa, goals_scored, goals_conceded, wins, draws, losses) VALUES
('Manchester City', 'Premier League', 2.40, 1.80, 66, 28, 22, 5, 3),
('Arsenal', 'Premier League', 2.20, 1.70, 62, 30, 19, 8, 3),
('Liverpool', 'Premier League', 2.30, 1.75, 64, 32, 20, 6, 4),
('Manchester United', 'Premier League', 1.90, 1.50, 52, 38, 16, 8, 6),
('Chelsea', 'Premier League', 1.85, 1.55, 50, 40, 15, 9, 6),
('Tottenham', 'Premier League', 2.00, 1.60, 54, 42, 17, 7, 6),
('Newcastle', 'Premier League', 1.80, 1.40, 48, 36, 15, 7, 8),
('Brighton', 'Premier League', 1.75, 1.45, 46, 44, 14, 8, 8),
('Aston Villa', 'Premier League', 1.70, 1.35, 44, 46, 13, 9, 8),
('West Ham', 'Premier League', 1.60, 1.30, 42, 48, 12, 8, 10);

-- Sample User (password is 'admin123' - BCrypt hashed)
INSERT INTO users (username, password_hash, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');