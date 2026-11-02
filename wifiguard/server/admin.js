// Fitaovana fitantanana: node admin.js <baiko>
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const FILE = path.join(__dirname, 'data.json');

function load() {
  try { return JSON.parse(fs.readFileSync(FILE, 'utf8')); }
  catch { return { wifi: null, users: {} }; }
}
function save(d) { fs.writeFileSync(FILE, JSON.stringify(d, null, 2), { mode: 0o600 }); }

const [cmd, a, b] = process.argv.slice(2);
const d = load();
const name = a && a.trim().toLowerCase();

switch (cmd) {
  case 'set-wifi': // node admin.js set-wifi <SSID> <motdepasse>
    if (!a || !b || b.length < 8) { console.log('Mila SSID sy mot de passe (8+ litera)'); process.exit(1); }
    d.wifi = { ssid: a, password: b }; save(d); console.log('WiFi voatahiry:', a); break;
  case 'add': // node admin.js add <anarana> <motdepasse>
    if (!a || !b || b.length < 6) { console.log('Mila anarana sy mot de passe (6+ litera)'); process.exit(1); }
    { const salt = crypto.randomBytes(16);
      d.users[name] = { salt: salt.toString('hex'), hash: crypto.scryptSync(b, salt, 64).toString('hex'), disabled: false };
      save(d); console.log('Mpampiasa voasoratra:', name); }
    break;
  case 'disable': // esorina ny fidiran'ny olona iray
    if (!d.users[name]) { console.log('Tsy hita'); process.exit(1); }
    d.users[name].disabled = true; save(d); console.log('Voasakana:', name); break;
  case 'enable':
    if (!d.users[name]) { console.log('Tsy hita'); process.exit(1); }
    d.users[name].disabled = false; save(d); console.log('Navoaka:', name); break;
  case 'remove':
    delete d.users[name]; save(d); console.log('Voafafa:', name); break;
  case 'list':
    console.log('WiFi:', d.wifi ? d.wifi.ssid : '(tsy voalamina)');
    for (const [u, r] of Object.entries(d.users)) console.log(' -', u, r.disabled ? '(voasakana)' : '');
    break;
  default:
    console.log(`Baiko:
  node admin.js set-wifi <SSID> <motdepasse>
  node admin.js add <anarana> <motdepasse>
  node admin.js disable <anarana>     (manala ny fidirana, avy eo ovao ny mot de passe WiFi)
  node admin.js enable <anarana>
  node admin.js remove <anarana>
  node admin.js list`);
}
