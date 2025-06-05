// API Configuration
const API_BASE_URL = 'http://localhost:9090/api';
let currentUser = null;
const admins = ['0x742d35Cc6634C0532925a3b844Bc454e4438f44e'];

// DOM Elements
const sections = {
  home: document.getElementById('home'),
  search: document.getElementById('search'),
  'land-detail': document.getElementById('land-detail'),
  register: document.getElementById('register'),
  'my-lands': document.getElementById('my-lands'),
  admin: document.getElementById('admin'),
  signin: document.getElementById('signin'),
  signup: document.getElementById('signup')
};

// Initialize the app
document.addEventListener('DOMContentLoaded', () => {
  showSection('home');
  document.getElementById('connect-wallet').addEventListener('click', connectWallet);
  document.getElementById('search-form').addEventListener('submit', handleSearch);
  document.getElementById('register-form').addEventListener('submit', handleRegister);
  toggleAdminLink(false);
  toggleMyLandsLink(false);
});

// Navigation
function showSection(sectionId) {
  Object.values(sections).forEach(section => {
    section.classList.remove('active');
    section.style.display = 'none';
  });

  const section = sections[sectionId];
  if (section) {
    section.classList.add('active');
    section.style.display = 'block';
  }

  if (sectionId === 'my-lands') loadMyLands();
  if (sectionId === 'admin') loadAdminDashboard();
}

// Wallet Connection (mock)
function connectWallet() {
  currentUser = '0x742d35Cc6634C0532925a3b844Bc454e4438f44e';
  document.getElementById('wallet-display').querySelector('.wallet-address').textContent =
    `${currentUser.substring(0, 6)}...${currentUser.substring(currentUser.length - 4)}`;
  document.getElementById('connect-wallet').style.display = 'none';
  toggleAdminLink(admins.includes(currentUser));
  toggleMyLandsLink(true);
}

function toggleAdminLink(show) {
  document.getElementById('admin-link').style.display = show ? 'block' : 'none';
}

function toggleMyLandsLink(show) {
  document.getElementById('my-lands-link').style.display = show ? 'block' : 'none';
}

// API Functions
async function fetchLands(params = {}) {
  const queryString = new URLSearchParams(params).toString();
  const response = await fetch(`${API_BASE_URL}/lands?${queryString}`);
  if (!response.ok) throw new Error('Failed to fetch lands');
  return await response.json();
}

async function fetchLandById(id) {
  const response = await fetch(`${API_BASE_URL}/lands/${id}`);
  if (!response.ok) throw new Error('Failed to fetch land details');
  return await response.json();
}

async function registerLand(landData) {
  const response = await fetch(`${API_BASE_URL}/lands`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(landData)
  });
  if (!response.ok) throw new Error('Registration failed');
  return await response.json();
}

async function updateLandStatus(id, status, details = '') {
  const url = `${API_BASE_URL}/lands/${id}/status?status=${status}`;
  const fullUrl = details ? `${url}&disputeDetails=${encodeURIComponent(details)}` : url;

  const response = await fetch(fullUrl, { method: 'PUT' });
  if (!response.ok) throw new Error('Status update failed');
  return await response.json();
}

// Search Functionality
async function handleSearch(e) {
  e.preventDefault();
  try {
    const searchType = document.getElementById('search-type').value;
    const searchTerm = document.getElementById('search-term').value;
    const lands = await fetchLands({ [searchType]: searchTerm });
    displaySearchResults(lands);
  } catch (error) {
    console.error('Search error:', error);
    alert('Search failed. Please try again.');
  }
}

function displaySearchResults(lands) {
  const resultsContainer = document.getElementById('results-grid');
  resultsContainer.innerHTML = lands.length === 0
    ? '<p>No matching land records found</p>'
    : lands.map(land => `
        <div class="land-card ${land.status === 'disputed' ? 'has-dispute' : ''}">
          <h4>${land.surveyNumber}</h4>
          <p><strong>Location:</strong> ${land.location}</p>
          <p><strong>Owner:</strong> ${land.owner.substring(0, 6)}...${land.owner.substring(land.owner.length - 4)}</p>
          <p><strong>Status:</strong>
            <span class="badge ${land.status === 'clear' ? 'badge-success' : 'badge-danger'}">
              ${land.status === 'clear' ? 'Clear' : 'Disputed'}
            </span>
          </p>
          <button class="btn btn-secondary" onclick="viewLandDetail('${land.id}')">
            View Details
          </button>
        </div>
      `).join('');

  document.getElementById('search-results').style.display = 'block';
}

// Land Detail View
async function viewLandDetail(landId) {
  try {
    const land = await fetchLandById(landId);
    const isOwner = currentUser && currentUser.toLowerCase() === land.owner.toLowerCase();
    const isAdminUser = admins.includes(currentUser);

    const html = `
      <div class="container">
        <div class="card">
          <div class="land-header">
            <h2 class="card-title">${land.surveyNumber}</h2>
            <span class="badge ${land.status === 'clear' ? 'badge-success' : 'badge-danger'}">
              ${land.status === 'clear' ? 'Clear' : 'Disputed'}
            </span>
          </div>

          <div class="land-grid">
            <div class="land-info">
              <div class="info-item"><strong>Location:</strong> ${land.location}</div>
              <div class="info-item"><strong>Coordinates:</strong> ${land.coordinates}</div>
              <div class="info-item"><strong>Size:</strong> ${land.size}</div>
              <div class="info-item"><strong>Registered:</strong> ${land.registrationDate}</div>
              <div class="info-item">
                <strong>Owner:</strong>
                <span class="${isOwner ? 'owner-you' : ''}">
                  ${land.owner.substring(0, 6)}...${land.owner.substring(land.owner.length - 4)}
                  ${isOwner ? ' (You)' : ''}
                </span>
              </div>

              ${isOwner && land.status === 'clear' ? `
                <button class="btn btn-primary" onclick="initiateTransfer('${land.id}')">
                  Initiate Transfer
                </button>
              ` : ''}
            </div>

            <div class="land-qr">
              <h4>Land Verification QR</h4>
              <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${
                encodeURIComponent(JSON.stringify({
                  id: land.id,
                  surveyNumber: land.surveyNumber,
                  owner: land.owner,
                  status: land.status
                }))
              }" alt="QR Code" />
              <p>Scan to verify</p>
            </div>
          </div>
        </div>

        ${land.status === 'disputed' ? `
          <div class="card dispute-alert">
            <h3 class="card-title">⚠️ Dispute Details</h3>
            <p>${land.disputeDetails || 'No additional details available'}</p>
          </div>
        ` : ''}

        ${isAdminUser ? `
          <div class="card admin-actions">
            <h3>Admin Actions</h3>
            <button onclick="showStatusUpdateForm('${land.id}')" class="btn btn-primary">
              Update Status
            </button>
          </div>
        ` : ''}
      </div>
    `;

    document.getElementById('land-detail').innerHTML = html;
    showSection('land-detail');
  } catch (error) {
    console.error('Failed to load land details:', error);
    alert('Failed to load land details. Please try again.');
  }
}

// Status Update Functions
function showStatusUpdateForm(landId) {
  const container = document.querySelector('#land-detail .container');
  container.insertAdjacentHTML('beforeend', `
    <div class="card status-update-form">
      <h3>Update Land Status</h3>
      <form onsubmit="handleStatusUpdate(event, '${landId}')">
        <div class="form-group">
          <label>Status</label>
          <select id="status-select" class="form-control" required>
            <option value="clear">Clear</option>
            <option value="disputed" selected>Disputed</option>
          </select>
        </div>
        <div class="form-group" id="dispute-fields">
          <label>Dispute Details</label>
          <textarea id="dispute-details" rows="3" placeholder="Enter dispute details" required></textarea>
        </div>
        <button type="submit" class="btn btn-primary">Update Status</button>
      </form>
    </div>
  `);

  // Show/hide dispute details based on selection
  const statusSelect = document.getElementById('status-select');
  const disputeFields = document.getElementById('dispute-fields');
  statusSelect.addEventListener('change', () => {
    if (statusSelect.value === 'disputed') {
      disputeFields.style.display = 'block';
      disputeFields.querySelector('textarea').required = true;
    } else {
      disputeFields.style.display = 'none';
      disputeFields.querySelector('textarea').required = false;
    }
  });
}

async function handleStatusUpdate(event, landId) {
  event.preventDefault();
  const status = document.getElementById('status-select').value;
  const disputeDetails = document.getElementById('dispute-details').value;

  try {
    await updateLandStatus(landId, status, status === 'disputed' ? disputeDetails : '');
    alert('Status updated successfully');
    viewLandDetail(landId);
  } catch (error) {
    console.error('Status update failed:', error);
    alert('Failed to update status. Please try again.');
  }
}

// Load My Lands (owned by current user)
async function loadMyLands() {
  if (!currentUser) {
    alert('Please connect wallet or sign in first');
    return;
  }
  try {
    const lands = await fetchLands({ owner: currentUser });
    const container = document.getElementById('my-lands-list');
    container.innerHTML = lands.length === 0
      ? '<p>No lands owned by you.</p>'
      : lands.map(land => `
        <div class="land-card ${land.status === 'disputed' ? 'has-dispute' : ''}">
          <h4>${land.surveyNumber}</h4>
          <p><strong>Location:</strong> ${land.location}</p>
          <p><strong>Status:</strong> ${land.status}</p>
          <button onclick="viewLandDetail('${land.id}')" class="btn btn-secondary">View Details</button>
        </div>
      `).join('');
  } catch (error) {
    console.error(error);
    alert('Failed to load your lands.');
  }
}

// Admin Dashboard - show all lands
async function loadAdminDashboard() {
  if (!admins.includes(currentUser)) {
    alert('Access denied. Admins only.');
    showSection('home');
    return;
  }
  try {
    const lands = await fetchLands();
    const container = document.getElementById('admin-lands-list');
    container.innerHTML = lands.length === 0
      ? '<p>No lands found.</p>'
      : lands.map(land => `
        <div class="land-card ${land.status === 'disputed' ? 'has-dispute' : ''}">
          <h4>${land.surveyNumber}</h4>
          <p><strong>Owner:</strong> ${land.owner.substring(0, 6)}...${land.owner.substring(land.owner.length - 4)}</p>
          <p><strong>Status:</strong> ${land.status}</p>
          <button onclick="viewLandDetail('${land.id}')" class="btn btn-secondary">View Details</button>
        </div>
      `).join('');
  } catch (error) {
    console.error(error);
    alert('Failed to load admin dashboard.');
  }
}

// Land Registration
async function handleRegister(e) {
  e.preventDefault();
  if (!currentUser) {
    alert('Please connect wallet or sign in first');
    return;
  }
  const form = e.target;
  const landData = {
    surveyNumber: form.surveyNumber.value.trim(),
    location: form.location.value.trim(),
    coordinates: form.coordinates.value.trim(),
    size: form.size.value.trim(),
    owner: currentUser,
    registrationDate: new Date().toISOString().split('T')[0]
  };
  try {
    await registerLand(landData);
    alert('Land registered successfully!');
    form.reset();
    showSection('my-lands');
  } catch (error) {
    console.error(error);
    alert('Failed to register land. Please try again.');
  }
}

// Signup Form
document.getElementById('signup-form').addEventListener('submit', async function(e) {
  e.preventDefault();
  const username = document.getElementById('signup-username').value.trim();
  const password = document.getElementById('signup-password').value;
  try {
    const res = await fetch(`${API_BASE_URL}/auth/signup`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({ username, password })
    });
    if (res.ok) {
      alert('Signup successful! Please sign in.');
      showSection('signin');
    } else {
      const err = await res.json();
      alert('Signup failed: ' + err.message);
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
});

// Signin Form
document.getElementById('signin-form').addEventListener('submit', async function(e) {
  e.preventDefault();
  const username = document.getElementById('signin-username').value.trim();
  const password = document.getElementById('signin-password').value;
  try {
    const res = await fetch(`${API_BASE_URL}/auth/signin`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({ username, password })
    });
    if (res.ok) {
      const data = await res.json();
      currentUser = data.address; // Expected wallet address returned by backend
      alert('Sign in successful!');
      document.getElementById('connect-wallet').style.display = 'none';
      document.getElementById('wallet-display').querySelector('.wallet-address').textContent =
        `${currentUser.substring(0, 6)}...${currentUser.substring(currentUser.length - 4)}`;
      toggleAdminLink(admins.includes(currentUser));
      toggleMyLandsLink(true);
      showSection('home');
    } else {
      const err = await res.json();
      alert('Sign in failed: ' + err.message);
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
});
