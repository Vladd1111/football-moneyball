import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api, { matchesAPI } from '../services/api';
import '../styles/Home.css';

function Home() {
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const navigate = useNavigate();
  const username = localStorage.getItem('username');

  useEffect(() => {
    fetchMatches();
  }, []);

  useEffect(() => {
    const role = localStorage.getItem('role');
    setIsAdmin(role === 'ADMIN');
  }, []);

  const fetchMatches = async () => {
    try {
      const response = await matchesAPI.getUpcoming();
      setMatches(response.data);
    } catch (err) {
      setError('Failed to load matches');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleMatchClick = (match) => {
    navigate(`/predict/${match.id}`, { state: { match } });
  };

  const handleRefreshFixtures = async () => {
    if (!window.confirm('Refresh fixtures from API-Football? This uses 1 API request.')) {
      return;
    }

    setRefreshing(true);
    try {
      const response = await api.post('/admin/refresh-fixtures');
      alert(`Success! ${response.data.count} fixtures loaded`);
      fetchMatches();
    } catch (err) {
      alert('Failed to refresh fixtures: ' + err.message);
    } finally {
      setRefreshing(false);
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return <div className="loading">Loading matches...</div>;
  }

  return (
    <div className="home-container">
      <header className="header">
        <h1>⚽ Football Moneyball</h1>
        <div className="user-info">
          <span>Welcome, {username}!</span>
          <button onClick={handleLogout} className="logout-btn">
            Logout
          </button>
        </div>
      </header>

      <div className="matches-section">
        <h2>Upcoming Matches</h2>
        <p className="subtitle">Tap any match to see AI prediction</p>

        {error && <div className="error">{error}</div>}

        {isAdmin && (
          <button
            className="refresh-btn"
            onClick={handleRefreshFixtures}
            disabled={refreshing}
          >
            {refreshing ? '⏳ Refreshing...' : '🔄 Refresh Fixtures from API'}
          </button>
        )}

        <div className="matches-grid">
          {matches.map((match) => (
            <div
              key={match.id}
              className="match-card"
              onClick={() => handleMatchClick(match)}
            >
              <div className="match-time">
                {formatDate(match.matchDate)}
              </div>

              <div className="teams">
                <div className="team home">
                  <span className="team-name">{match.homeTeam.name}</span>
                  <span className="team-stats">
                    {match.homeTeam.wins}W {match.homeTeam.draws}D{' '}
                    {match.homeTeam.losses}L
                  </span>
                </div>

                <div className="vs">VS</div>

                <div className="team away">
                  <span className="team-name">{match.awayTeam.name}</span>
                  <span className="team-stats">
                    {match.awayTeam.wins}W {match.awayTeam.draws}D{' '}
                    {match.awayTeam.losses}L
                  </span>
                </div>
              </div>

              <div className="predict-hint">
                Tap to predict →
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default Home;