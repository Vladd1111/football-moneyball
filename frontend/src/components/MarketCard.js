import React from 'react';
import '../styles/MarketCard.css';

function MarketCard({ title, markets, type = 'single' }) {

  if (type === 'triple') {
    // For match outcome (3 options)
    return (
      <div className="market-card">
        <h3 className="market-title">{title}</h3>
        <div className="market-options-triple">
          {markets.map((market, idx) => (
            <div key={idx} className="market-option">
              <div className="option-label">{market.label}</div>
              <div className="option-probability">{market.percentage}%</div>
              <div className="option-odds">{market.decimalOdds}</div>
              <div className="probability-bar">
                <div
                  className="probability-fill"
                  style={{ width: `${market.percentage}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (type === 'dual') {
    // For BTTS, Over/Under (2 options)
    return (
      <div className="market-card">
        <h3 className="market-title">{title}</h3>
        <div className="market-options-dual">
          {markets.map((market, idx) => (
            <div key={idx} className="market-option-dual">
              <div className="option-header">
                <span className="option-label">{market.label}</span>
                <span className="option-odds">{market.decimalOdds}</span>
              </div>
              <div className="option-probability">{market.percentage}%</div>
              <div className="probability-bar">
                <div
                  className="probability-fill"
                  style={{ width: `${market.percentage}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  // Grid type for multiple markets
  return (
    <div className="market-card">
      <h3 className="market-title">{title}</h3>
      <div className="market-grid">
        {markets.map((market, idx) => (
          <div key={idx} className="market-item">
            <div className="market-label">{market.label}</div>
            <div className="market-stats">
              <span className="market-percentage">{market.percentage}%</span>
              <span className="market-odds">{market.decimalOdds}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default MarketCard;