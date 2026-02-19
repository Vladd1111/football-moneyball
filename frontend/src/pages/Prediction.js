import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { predictionsAPI } from '../services/api';
import '../styles/Prediction.css';

function Prediction() {
  const location = useLocation();
  const navigate = useNavigate();
  const { match } = location.state || {};

  const [prediction, setPrediction] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [includeAI, setIncludeAI] = useState(false);

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
      const response = await predictionsAPI.predict(
        match.homeTeam.id,
        match.awayTeam.id,
        includeAI
      );
      setPrediction(response.data);
    } catch (err) {
      setError('Failed to generate prediction');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="prediction-container">
      <button className="back-btn" onClick={() => navigate('/')}>
        ← Back to Matches
      </button>

      <div className="match-header">
        <h2>{match.homeTeam.name}</h2>
        <span className="vs-large">VS</span>
        <h2>{match.awayTeam.name}</h2>
      </div>

      <div className="match-info">
        <p>{new Date(match.matchDate).toLocaleString()}</p>
      </div>

      {!prediction && (
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
            {loading ? 'Predicting...' : '🎯 Generate Prediction'}
          </button>
        </div>
      )}

      {error && <div className="error">{error}</div>}

      {prediction && (
        <div className="prediction-results">
          <h3>Match Prediction</h3>

          <div className="probabilities">
            <div className="prob-bar">
              <div className="prob-label">
                <span>{match.homeTeam.name} Win</span>
                <span className="prob-value">
                  {(prediction.homeWinProbability * 100).toFixed(1)}%
                </span>
              </div>
              <div className="bar">
                <div
                  className="fill home-fill"
                  style={{ width: `${prediction.homeWinProbability * 100}%` }}
                />
              </div>
            </div>

            <div className="prob-bar">
              <div className="prob-label">
                <span>Draw</span>
                <span className="prob-value">
                  {(prediction.drawProbability * 100).toFixed(1)}%
                </span>
              </div>
              <div className="bar">
                <div
                  className="fill draw-fill"
                  style={{ width: `${prediction.drawProbability * 100}%` }}
                />
              </div>
            </div>

            <div className="prob-bar">
              <div className="prob-label">
                <span>{match.awayTeam.name} Win</span>
                <span className="prob-value">
                  {(prediction.awayWinProbability * 100).toFixed(1)}%
                </span>
              </div>
              <div className="bar">
                <div
                  className="fill away-fill"
                  style={{ width: `${prediction.awayWinProbability * 100}%` }}
                />
              </div>
            </div>
          </div>

          <div className="expected-score">
            <h4>Expected Score</h4>
            <div className="score">
              <span className="score-value">
                {prediction.predictedHomeXg.toFixed(2)}
              </span>
              <span className="score-separator">-</span>
              <span className="score-value">
                {prediction.predictedAwayXg.toFixed(2)}
              </span>
            </div>
          </div>

          <div className="confidence">
            Confidence: <span className={`badge ${prediction.confidence.toLowerCase()}`}>
              {prediction.confidence}
            </span>
          </div>

          {prediction.aiAnalysis && (
            <div className="ai-analysis">
              <h4>🤖 AI Analysis</h4>
              <p>{prediction.aiAnalysis}</p>
            </div>
          )}

          <button className="new-prediction-btn" onClick={() => setPrediction(null)}>
            Generate New Prediction
          </button>
        </div>
      )}
    </div>
  );
}

export default Prediction;