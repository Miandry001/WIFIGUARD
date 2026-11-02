// WifiGuard serveur: manamarina ny mpampiasa dia mamerina ny SSID sy mot de passe WiFi.
// Tsy mila dépendance: Node.js 18+ ihany.
const http = require('http');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const FILE = path.join(__dirname, 'data.json');
const PORT = process.env.PORT || 3000;

// Fetran'ny fanandramana: 8 diso isaky ny 15 minitra isaky ny IP, ary 5 isaky ny anarana
const WINDOW_MS = 15 * 60 * 1000;
const MAX_FAILS_IP = 8;
const MAX_FAILS_USER = 5;
const fails = new Map(); // key -> [timestamps]

function load() {
  try { return JSON.parse(fs.readFileSync(FILE, 'utf8')); }
  catch { return { wifi: null, users: {} }; }
}

function tooMany(key, max) {
  const now = Date.now();
  const list = (fails.get(key) || []).filter(t => now - t < WINDOW_MS);
  fails.set(key, list);
  return list.length >= max;
}
function addFail(key) {
  const list = fails.get(key) || [];
  list.push(Date.now());
  fails.set(key, list);
}

function checkPassword(user, pass) {
  const rec = Object.prototype.hasOwnProperty.call(load().users, user) ? load().users[user] : null;
  // Mandany foana ny scrypt mba tsy ho hita amin'ny fotoana raha misy ny anarana
  const salt = rec ? Buffer.from(rec.salt, 'hex') : Buffer.alloc(16);
  const hash = crypto.scryptSync(pass, salt, 64);
  if (!rec) return false;
  const good = Buffer.from(rec.hash, 'hex');
  return crypto.timingSafeEqual(hash, good) && !rec.disabled;
}

function send(res, code, obj) {
  const body = JSON.stringify(obj);
  res.writeHead(code, { 'Content-Type': 'application/json', 'Cache-Control': 'no-store' });
  res.end(body);
}

const server = http.createServer((req, res) => {
  const ip = (req.headers['x-forwarded-for'] || req.socket.remoteAddress || '').split(',')[0].trim();

  if (req.method === 'GET' && req.url === '/health') return send(res, 200, { ok: true });
  if (req.method !== 'POST' || req.url !== '/wifi') return send(res, 404, { error: 'not_found' });

  let raw = '';
  req.on('data', c => { raw += c; if (raw.length > 4096) req.destroy(); });
  req.on('end', () => {
    let body;
    try { body = JSON.parse(raw); } catch { return send(res, 400, { error: 'bad_request' }); }
    const user = String(body.username || '').trim().toLowerCase();
    const pass = String(body.password || '');
    if (!user || !pass) return send(res, 400, { error: 'bad_request' });

    if (tooMany('ip:' + ip, MAX_FAILS_IP) || tooMany('u:' + user, MAX_FAILS_USER)) {
      return send(res, 429, { error: 'too_many_attempts' });
    }
    if (!checkPassword(user, pass)) {
      addFail('ip:' + ip); addFail('u:' + user);
      return send(res, 401, { error: 'invalid_credentials' });
    }
    const wifi = load().wifi;
    if (!wifi) return send(res, 503, { error: 'wifi_not_configured' });
    console.log(new Date().toISOString(), 'OK', user, ip);
    send(res, 200, { ssid: wifi.ssid, password: wifi.password, hidden: true });
  });
});

server.listen(PORT, () => console.log('WifiGuard serveur mandeha amin\'ny port ' + PORT));
