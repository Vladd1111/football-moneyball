import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { predictionsAPI } from '../services/api';
import MarketCard from '../components/MarketCard';
import CorrectScoreGrid from '../components/CorrectScoreGrid';
import '../styles/Prediction.css';

function Prediction() {
  const location = useLocation();
  const navigate = useNavigate();
  const { match } = location.state || {};

  const [markets, setMarkets] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [includeAI, setIncludeAI] = useState(false);
  const [showFullMarkets, setShowFullMarkets] = useState(false);

  if (!match) {
    return (
      <div className="error-container">
        <p>No match selected</p>
        <button onClick={() => navigate('/')}>Go Back</button>
      </div>
    );
  }

  const handlePredict = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await predictionsAPI.getBettingMarkets(
        match.homeTeam.id,
        match.awayTeam.id,
        includeAI
      );
      setMarkets(response.data);
      setShowFullMarkets(true);
    } catch (err) {
      setError('Failed to generate prediction');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatMatchOutcome = (outcome) => {
    return [
      {
        label: `${match.homeTeam.name} Win`,
        ...outcome.homeWin
      },
      {
        label: 'Draw',
        ...outcome.draw
      },
      {
        label: `${match.awayTeam.name} Win`,
        ...outcome.awayWin
      }
    ];
  };

  const formatBTTS = (btts) => {
    return [
      {
        label: 'Both Teams Score (GG)',
        ...btts.yes
      },
      {
        label: 'One/Both Blank (NG)',
        ...btts.no
      }
    ];
  };

  const formatOverUnder = (ou) => {
    return [
      { label: 'Over 0.5', ...ou['OVER_0.5'] },
      { label: 'Over 1.5', ...ou['OVER_1.5'] },
      { label: 'Over 2.5', ...ou['OVER_2.5'] },
      { label: 'Over 3.5', ...ou['OVER_3.5'] },
      { label: 'Over 4.5', ...ou['OVER_4.5'] },
    ];
  };

  const formatDoubleChance = (dc) => {
    return [
      { label: '1X (Home or Draw)', ...dc['1X'] },
      { label: '12 (Home or Away)', ...dc['12'] },
      { label: 'X2 (Draw or Away)', ...dc['X2'] },
    ];
  };

  return (
    <div className="prediction-container">
      <button className="back-btn" onClick={() => navigate('/')}>
        ← Back to Matches
      </button>

      <div className="match-header">
        <div className="team-section">
          <h2>{match.homeTeam.name}</h2>
          <div className="team-stats">
            {match.homeTeam.wins}W {match.homeTeam.draws}D {match.homeTeam.losses}L
          </div>
        </div>
        <span className="vs-large">VS</span>
        <div className="team-section">
          <h2>{match.awayTeam.name}</h2>
          <div className="team-stats">
            {match.awayTeam.wins}W {match.awayTeam.draws}D {match.awayTeam.losses}L
          </div>
        </div>
      </div>

      <div className="match-info">
        <p>{new Date(match.matchDate).toLocaleString()}</p>
      </div>

      {!markets && (
        <div className="predict-controls">
          <label className="ai-toggle">
            <input
              type="checkbox"
              checked={includeAI}
              onChange={(e) => setIncludeAI(e.target.checked)}
            />
            Include AI Analysis (slower)
          </label>

          <button
            className="predict-btn"
            onClick={handlePredict}
            disabled={loading}
          >
            {loading ? 'Generating Predictions...' : '🎯 Generate Full Betting Markets'}
          </button>
        </div>
      )}

      {error && <div className="error">{error}</div>}

      {markets && (
        <div className="betting-markets">

          {/* Match Outcome */}
          <MarketCard
            title="⚽ Match Result"
            markets={formatMatchOutcome(markets.matchOutcome)}
            type="triple"
          />

          {/* Over/Under Goals */}
          <MarketCard
            title="📈 Total Goals"
            markets={formatOverUnder(markets.overUnder)}
            type="grid"
          />

          {/* Both Teams to Score */}
          <MarketCard
            title="🎯 Both Teams to Score"
            markets={formatBTTS(markets.bothTeamsToScore)}
            type="dual"
          />

          {/* Double Chance */}
          <MarketCard
            title="🎲 Double Chance"
            markets={formatDoubleChance(markets.doubleChance)}
            type="grid"
          />

          {/* Correct Score */}
          <CorrectScoreGrid scores={markets.topCorrectScores} />

          {/* Confidence & AI */}
          <div className="additional-info">
            <div className="confidence-badge">
              <span className="label">Confidence:</span>
              <span className={`badge ${markets.confidence.toLowerCase()}`}>
                {markets.confidence}
              </span>
            </div>

            {markets.aiAnalysis && (
              <div className="ai-analysis">
                <h4>🤖 AI Expert Analysis</h4>
                <p>{markets.aiAnalysis}</p>
              </div>
            )}
          </div>

          <button
            className="new-prediction-btn"
            onClick={() => {
              setMarkets(null);
              setShowFullMarkets(false);
            }}
          >
            Generate New Prediction
          </button>
        </div>
      )}
    </div>
  );
}

export default Prediction;