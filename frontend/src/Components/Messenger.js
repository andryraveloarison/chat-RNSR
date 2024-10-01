import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Cookies from 'js-cookie';
import io from 'socket.io-client';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { FaChevronRight } from 'react-icons/fa';

const socket = io('http://localhost:5050');

const Messenger = () => {
    const navigate = useNavigate();
    const token = Cookies.get('token');
    const [users, setUsers] = useState([])
    const [user, setUser] = useState({
        username:'', id:0, profile:''
    })

    useEffect(() => {
        axios.get('http://localhost:5050/users')
        .then(response => {
            setUsers(response.data)
        })
        .catch(error => console.error(error));

        // get user info
        const getInfoUser = async () => {
            try {
                const rep = await axios.post('http://localhost:5050/getinfouser', {
                    token,
                });
                setUser((prevItem) => ({
                    ...prevItem,
                    username: rep.data.data.username,
                    id: rep.data.data.user,
                    profile: `https://i.pravatar.cc/150?img=${rep.data.data.username}`
                }));

            } catch (error) {
                console.error(error);
            }
        };
        getInfoUser()



       

    },[])

    useEffect(() => {

        socket.on('new_message', async (msg) => {
            if (msg.receiverId === user.id.toString()) {
                const rep = await axios.get('http://localhost:5050/getinfouser/'+msg.senderId);
                
                let msgText = `${rep.data.data.name} a envoye un message!`
                toast.info(msgText, {
                    position: toast.POSITION, 
                    autoClose: 5000, 
                    hideProgressBar: true,
                    closeOnClick: true,
                    pauseOnHover: true,
                    draggable: true,
                    progress: undefined,
                });
              }
        });

        

        socket.on('new_deconnection', async (userId) => {
            setUsers((prevUsers) =>
                prevUsers.map((user) =>
                    user.id === userId ? { ...user, status: 'Disconnected' } : user
                )
            );
        })


        return () => socket.off('new_message');

    },[user])
   

    useEffect(() => {
        const handleNewConnection = async (newUser) => {
            if (newUser.id === user.id) {
                alert("Zah ihany");
                return;
            }
    
            let msgText = `${newUser.name} est en ligne`
                toast.info(msgText, {
                    position: toast.POSITION, 
                    autoClose: 5000, 
                    hideProgressBar: true,
                    closeOnClick: true,
                    pauseOnHover: true,
                    draggable: true,
                    progress: undefined,
                });
                
    
            setUsers((prevUsers) =>
                prevUsers.map((user) =>
                    user.id === newUser.id ? { ...user, status: 'Connected' } : user
                )
            );
        };
    
        socket.on('new_connection', handleNewConnection);
    
        // Fonction de nettoyage
        return () => {
            console.log('Cleaning up socket listener');
            socket.off('new_connection', handleNewConnection);
        };
    }, [user, socket]);  // Ajouter des dépendances pertinentes
    

    
    const logout = async () => {
        try {
            await axios.post('http://localhost:5050/logout', {
                token
            });
            navigate('/');
        } catch (error) {
            alert('Failed to send message');
        }
      };

    return (
    <div className='flex flex-col justify-center items-center h-screen'>
        <div className="flex flex-col h-[90vh] bg-[#101010dc] rounded-lg p-4 w-[500px] text-white">
            <ToastContainer />
            <div className='flex flex-col'>
                {/* Header */}
                <div className="flex items-center justify-between p-2 bg-[#783CEF] text-white rounded-lg mb-3">
                    <h1 className="text-xl font-bold">Chat'Nay</h1>
                    <div className='flex flex-col justify-end items-end'>
                        <div><img src={`${user.profile}`} alt={user.username}   className="w-10 h-10 rounded-full"/></div>
                        <div><span className='text-[8px]'>{user.username}</span></div>
                    </div>
                    {/* <div className="relative">
                        <input
                            type="text"
                            placeholder="Rechercher"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:border-blue-300"
                        />
                    </div> */}
                </div>

                {/* User List */}
                <div className="flex flex-col gap-2 overflow-y-auto">
                    {users.map((user) => (
                    <Link to={`/message/${user.id}`}
                        key={user.id}
                        className="flex items-center justify-between bg-[#61616176] p-2 rounded-lg shadow-sm hover:bg-gray-50 hover:text-black cursor-pointer"
                    >
                        {/* User Details */}
                        <div className="flex items-center space-x-4">
                        <div className="relative">
                            {/* Online/Offline indicator */}
                            <span
                            className={`absolute bottom-0 right-0 w-4 h-4 rounded-full border-2 border-white ${
                                user.status == 'Connected'? 'bg-green-500' : 'bg-gray-400'
                            }`}
                            ></span>
                            <img
                            src={`https://i.pravatar.cc/150?img=${user.name}`}
                            alt={user.name}
                            className="w-10 h-10 rounded-full"
                            />
                        </div>
                        <div>
                            <h3 className="text-[12px] font-semibold">{user.name}</h3>
                            <p className="text-[10px] text-gray-500">
                            {user.status}
                            </p>
                        </div>
                        </div>
                    </Link>
                    ))}
                </div>
            </div>
            
            <div className='absolute bottom-[8vh] flex flex-row text-[10px] text-red-600 items-center gap-2 cursor-pointer' onClick={logout}>
                <div>Logout</div>
                <div className=''><i className='text-red-600'><FaChevronRight/></i></div>
            </div>
        </div>
    </div>
    );
};

export default Messenger;
