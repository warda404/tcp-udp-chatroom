# tcp-udp-chatroom
udp -> basic chat
tcp -> file transfer

UDP server on port 8192
TCP server on port 8192 (to listen for incoming client connections and adds them to its list of clients)
TCP server on port 5717 (exclusively for opening sockets just for file transfer)

User names and passwords are stored in a MySQL database 
Messages sent are also stored in MySQL

New SERVER THREAD started:
Server started on PORT 8192, Datagram Socket created on this port ...
MANAGE, RECV AND SEND in 3 separate threads running inside the SERVRRUN THREAD

Client sign up ->  Stored in MYSQL database running on 3306 TCP
Client Log in -> Enter name, pw, IP of server , PORT of server (access MYSQL db to authenticate user)
On connection , 
Client is bound to a datagram socket with random port number + IP of host
a connection packet is sent to the SERVER
Server recvs this conn packet, assigns ID to user, stores it in list of clients
And sends a confirmation msg back to the client along with its ID



Message -> SERVER (logs message in MYSQL db) -> Broadcast to all clients (including the one who sent it, so its like a confirmation to the sender that the packet was sent as well)

> Client can VIEW number of online users 
> Client can VIEW message log (with date and time) by asking the MYSQL server

DISCONNECT (normal vs abnormal) -> send a disconnect packet to the server. it removes the client from its list + client socket (protected by a lock) is closed in a thread + tells all users that the user has left

manager thread:-
> Updates number of online users on client app evry 2 seconds
> NOT CLEAN DISCONNECT -> pings clients and gets responses... if doesnt get a response after a total
						of  5 ATTEMPTS. it drops the client... 
						thread sleeps  for 2 secs so its like a total of around 10 seconds

SERVER COMMANDS: -> servr can send message to all + 
					/clients to get list of all connectd clienst
				 -> /quit (remove all clients closes socket of srver as well)	
				 ->	/kick KICK CLIENTS based on id

-> file transfer as soon as done socket is closed
-> users choose the directory to save files to

to be implemented (or not? )
***BAN USER FOR SPECIFIC TIME (admin user?)
***Show names of all online + of all offline 
