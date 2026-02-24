import React from 'react';
import '../styles/CorrectScoreGrid.css';

function CorrectScoreGrid({ scores }) {
  return (
    <div className="correct-score-section">
      <h3 className="section-title">📊 Most Likely Scores</h3>
      <div className="score-grid">
        {scores.map((score, idx) => (
          <div key={idx} className="score-item">
            <div className="score-rank">#{score.rank}</div>
            <div className="score-result">
              {score.homeGoals} - {score.awayGoals}
            </div>
            <div className="score-probability">
              {score.percentage}%
            </div>
            <div className="score-odds">
              {score.decimalOdds}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default CorrectScoreGrid;