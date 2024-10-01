import express from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import {pool} from './db/db.js';  
import morgan from 'morgan';
import cors from 'cors';
import { Server } from 'socket.io';
import http from 'http';
import dotenv from 'dotenv';
import axios from 'axios';

dotenv.config();

const app = express();
const port = 5050;
const SECRET_KEY = process.env.JWT_SECRET; 
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: "http://localhost:3000", // Remplacez par l'URL de votre frontend
    methods: ["GET", "POST"]
  }
});


app.use(express.json());
app.use(morgan('dev'));
app.use(cors());


const API = "localhost"

/**
 * RECUPRER TOUT USER
 */
app.get('/users', async (req, res) => {
  try {
    const result = await axios.get('http://'+API+':9898/users')
    res.json(result.data);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Erreur serveur');
  }
});


/**
 * CREER USER
 */
app.post('/signup', async (req, res) => {
    const { username, email, password } = req.body;

    if (!username) return res.status(404).json({ messageError: "Veuillez ajouter une Email!" });
    if (!password) return res.status(404).json({ messageError: "Veuillez ajouter le mot de passe!" });

    // Vérifiez si un utilisateur existe
    const result = await pool.query('SELECT * FROM "user" WHERE username = $1', [username]);
    const userExists = result.rows.length > 0 ? result.rows[0] : null;
    if (userExists) return res.status(400).json({ messageError: "Cette email existe déjà!" });
    
     // Hashage du mot de passe
     const hashedPassword = await bcrypt.hash(password, 10);


    try {
        const result = await axios.post('http://'+API+':9898/users', {
          name: username,
          email: email,
          password: hashedPassword
      });
        res.json(result.data);
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Erreur serveur');
    }
    }
);


/**
 * LOGIN USER
 */app.post('/signin', async (req, res) => {
  try {
    const { email, password } = req.body;


    if (!email) return res.status(404).json({ messageError: "Veuillez ajouter une Email!" });
    if (!password) return res.status(404).json({ messageError: "Veuillez ajouter le mot de passe!" });


    // Appelez l'API Spring Boot pour vérifier l'utilisateur
    const response = await axios.post('http://'+API+':9898/users/login', {
        email: email,
        password: password
    });

    io.emit('new_connection', response.data);


      const token = jwt.sign({
        user: response.data.id,
        username: response.data.name
    }, SECRET_KEY, { expiresIn: '1h' });


    res.json({ token });

} catch (err) {
    if (err.response) {
        // Si l'erreur provient de l'API Spring Boot
        return res.status(err.response.status).json({ messageError: err.response.data });
    }
    console.error(err.message);
    res.status(500).send('Erreur serveur');
}
});


/**
 * LOGOUT USER
 */
app.post('/logout', async (req, res) => {
  const { token } = req.body;
  if (!token)   return res.status(400).json({ messageError: 'Token manquant' });
  
  try {
    const decodedToken = jwt.decode(token);
    if (decodedToken === null)  return res.status(400).json({ messageError: 'Token invalide' });  

    const userId = decodedToken.user
    
    await axios.get('http://'+API+':9898/users/logout/'+userId)

    io.emit('new_deconnection', userId);


    res.status(201).json({message : "Vous êtez déconnecté"});
  } catch (error) {
    //console.error('Error:', error);
    res.status(401).json({ error: 'Invalid token' });
  }
});


/**
 * GET INFO USER BY TOKEN
 */
app.post('/getinfouser', async (req, res) => {
  const { token } = req.body;
if (!token)   return res.status(400).json({ messageError: 'Token manquant' });
  
  try {
    const decodedToken = jwt.decode(token);
    if (decodedToken === null)  return res.status(400).json({ messageError: 'Token invalide' });

    const decoded = jwt.verify(token, SECRET_KEY);
    
    const user = decoded;    

    res.status(201).json({
      message : "Vos informations",
      data: user
    });
  } catch (error) {
    console.error('Error:', error);
    res.status(401).json({ error: 'Invalid token' });
  }
});


/**
 * ENVOYER UN MESSAGE
 */
app.post('/sendmessage', async (req, res) => {
  const { token, content, receiverId } = req.body;

  try {
    const decoded = jwt.verify(token, SECRET_KEY);
    const senderId = decoded.user;
    const dateNow = new Date()
    console.log(dateNow)

    const message = await axios.post('http://'+API+':9898/messages', {
      content: content,
      senderId:senderId,
      receiverId:receiverId,
  });

    // Send the message to the sender and the receiver using Socket.IO
    io.emit('new_message', message.data);

    console.log(dateNow)
    

    res.status(201).json(message.data);
  } catch (error) {
    //console.error('Error:', error);
    res.status(401).json({ error: 'Invalid token' });
  }
});



/**
 * GET INFO USER BY ID
 */
app.get('/getinfouser/:userId', async (req, res) => {
  const { userId } = req.params;
  
  try {
    const result = await axios.get('http://'+API+':9898/users/'+userId)
    res.status(201).json({
      message : "Vos informations",
      data: result.data
    });
  } catch (error) {
    console.error('Error:', error);
    res.status(401).json({ error: 'Invalid token' });
  }
});


/**
 * RECUPERER LES MESSAGES D'UNE UTILISATEUR
 */
app.post('/mymessage/:userId', async (req, res) => {
  const { userId } = req.params;
  const token = req.body.token; 
  if (!token) {
    return res.status(401).json({ messageError: 'Token manquante' });
  }

  try {
    const decodedToken = jwt.decode(token);
    if (decodedToken === null)  return res.status(401).json({ messageError: 'Token invalide' });

    const decoded = jwt.verify(token, SECRET_KEY);
    const currentUserId = decoded.user;

    const result = await axios.post('http://'+API+':9898/messages/user', {
      currentUserId: currentUserId,
      userId:userId,
  });



    res.json(result.data);
  } catch (error) {
    console.error(error.message);
    res.status(401).json({ error: 'Invalid token' });
  }
});



server.listen(port, () => {
  console.log(`Serveur Node.js démarré sur le port ${port}`);
});
