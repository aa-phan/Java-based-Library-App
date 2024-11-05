This repository contains the design and implementation of a Java-based app mimicking a library, written by Aaron Phan for ECE 422C: Software Design and Implementation II at The University of Texas at Austin. The app allows users to access a library of items such as books, DVDs, audiobooks, games, and comic books. 
The project consists of two main components: a server-side application (LibraryServer) and a client-side application (LibraryClient), which communicate over a network to enable users to interact with the library.

The server-side application (LibraryServer) is responsible for managing the library's inventory, handling client requests through the use of a helper class (ClientHandler), and facilitating communication between clients. It utilizes Java socket communication to establish a network connection with clients, allowing multiple users to access the library simultaneously. The server maintains a centralized database of library items and their availability status.

The client-side application (LibraryClient) provides users with a graphical interface (ClientApp) to interact with the library. Users can search for items, check out and return items, view their checked-out items, and communicate with other users. The client application communicates with the server to retrieve library information and update the library's status based on user actions.

The project utilizes Java for both the server and client applications, the GSON library for JSON object serialization and deserialization, JavaFX and Scene Builder for UI/UX elements, and MongoDB, an online database utilizing NoSQL that handles storage of user login information and checked-out item lists for each registered user.

Features include deployment of the LibraryServer on Heroku (cloud application hosting platform), password hashing/salting, usage of MongoDB, and a Maven build system.

This code is not to be used in any manner consistent with cheating, plagiarism, or other form of uncredited usage. For any questions related to implementation or usage, please contact me at atp2323@utexas.edu. This code is meant to be used for portfolio demonstration purposes with potential employers.
