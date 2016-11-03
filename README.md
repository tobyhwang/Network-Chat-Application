Networked Chat Application

Daniel Hajnos | dhajno2@uic.edu | 708-415-1454
Toby Hwang | hwang62@uic.edu | 847-915-2889
Lily Lu | llu25@uic.edu | 312-607-9531

To run the application you need to run ChatServer first, then you can run multiple ChatApps.


Information on how to receive and send data through a connection.

SENDTO:Userl;User;:MESSAGE

	(Varying Responses)
	(List of users) Send to all users in that list.
	(Single User) Send to that user
  
if it does match the number of connect user
PUBLIC:FROMUSER:MESSAGE

for each user: 
PRIVATE:FROMUSER:TOUSER:MESSAGE  (server must send to FROMUSER and TOUSER)

List of connected user
USERLIST: User1; User2;....UserN

CLOSE: closes the connection

