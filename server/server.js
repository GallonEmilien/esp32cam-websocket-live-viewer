const express = require('express');
const http = require('http');
const fs = require('fs');
const WebSocket = require('ws');
const path = require('path');
const bodyParser = require('body-parser');
const crypto = require('crypto');
const AuthManager = require('./AuthManager');
const authManager = new AuthManager(process.env.PASSWORD, process.env.USERNAME, process.env.API_KEY);
const hbengine = require('express-handlebars');

const app = express();


app.engine('hbs', hbengine.engine({
  defaultLayout: 'main',
  layoutsDir: path.join(__dirname, 'public/views/layouts'),
  extname: '.hbs',
    cache: false
}));


app.set('views', path.join(__dirname, 'public/views'));
app.set('view engine', 'hbs');



const hashedPassword = authManager.hashedPassword;
const hashedUsername = authManager.hashedUsername;

const cameraPorts = [];
var cameraNumber = 0;
const cameraServers = {};


function indexCameraArray(i) {
  const myArray = [];
  for (let j = 0; j < i; j++) {
    myArray.push(j);
  }
  return myArray;
}


const server = http.createServer(app);

const wss = new WebSocket.Server({ server });
const clients = {};
const authKeyList = new Set();



function loadParameters() {
  try {
    const config = JSON.parse(fs.readFileSync('server-config.json', 'utf8'));
    const { 'camera-number': nbCamera, 'camera-start-port': startPort } = config;
    cameraNumber = nbCamera;

    for (let i = 0; i < nbCamera; i++) {
      const port = startPort + i;
      cameraPorts.push(port);
    }

    console.log('Paramètres chargés avec succès');
  } catch (err) {
    console.error('Erreur lors du chargement des paramètres:', err);
  }
}
loadParameters();


app.post('/login', (req, res) => {
  const { username, password } = req.body;
  if(authManager.checkCredentials(password,username)) {
    console.log('Login Succes');
    res.status(200).send({ success: true, message: 'Login successful' });
  } else {
    console.log('Login Fail');
    res.status(401).send({ success: false, message: 'Invalid credentials' });
  }
});


cameraPorts.forEach(port => {
  const cameraWss = new WebSocket.Server({ port });
  cameraServers[port] = cameraWss;
  clients[port] = [];

  cameraWss.on('connection', (ws, req) => {
    
    if(!authManager.checkAuth(req.url)) {
      ws.terminate(); // Termine la connexion si l'utilisateur n'est pas authentifié
      console.log(`Unauthorized connection attempt on port ${port}`);
      return;
    }

    // Ajoute le client à la liste des clients pour ce port
    clients[port].push(ws);

    ws.on('message', (message) => {
      // Diffuse le message (image JPEG) à tous les clients connectés sur le port 80
      clients[port].forEach(client => {
        if (client.readyState === WebSocket.OPEN) {
          client.send(message);
          // Supprime l'image de la mémoire une fois envoyée
          message = null;
        }
      });
    });

    ws.on('close', () => {
      // Supprime le client de la liste des clients pour ce port lorsqu'il se déconnecte
      clients[port] = clients[port].filter(client => client !== ws);
    });
  });

  console.log(`WebSocket server for camera on port ${port} is running.`);
});


  
// Utiliser le middleware d'authentification avant express.static
app.use(authManager.middleware);

// Servir les fichiers statiques depuis le répertoire 'public'
app.use(express.static(path.join(__dirname, '/public')));

app.get('/', (req, res) => {
  res.render('index', { cameraPorts: cameraPorts, cameraIndex: indexCameraArray(cameraNumber), cameraNumber: cameraNumber, hashedPassword: hashedPassword, hashedUsername: hashedUsername });
});




const PORT = 8080;
server.listen(PORT, () => {
  console.log(`Server is listening on port ${PORT}`);
});
