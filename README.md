# Java-based Library App

This repository contains a Java-based application simulating a library system, developed as a final project for ECE 422C: Software Design and Implementation II at the University of Texas at Austin. The application enables users to interact with a collection of items, including books, DVDs, audiobooks, games, and comic books.

## Project Overview

The project is structured into two main components:

### 1. LibraryServer
The server-side application that manages the library's inventory and handles client requests. Key features include:

- **Centralized Inventory Management**: Manages a database of library items and their availability.
- **Java Socket Communication**: Establishes network connections with clients to support multiple simultaneous users.
- **Client Requests Handling**: Processes requests such as item checkout, returns, and account information retrieval.

### 2. LibraryClient
The client-side application provides users with an interface to interact with the library system. Users can:

- **Browse Inventory**: Search and view available items in the library.
- **Checkout and Return Items**: Borrow items and return them when done.
- **View Account Information**: Access user-specific information, including borrowed items and account status.

The client and server applications communicate over a network using Java sockets to allow real-time interaction with the library's system.

## Technologies Used

- **Java**: Core language for both server and client applications.
- **GSON**: For JSON object serialization and deserialization.
- **JavaFX and Scene Builder**: UI and UX elements for the client interface.
- **MongoDB**: An online NoSQL database for managing user login information and lists of checked-out items.
- **Maven**: Build automation tool for managing dependencies and compiling the project.

## Features

- **Multi-user Support**: Multiple clients can connect and interact with the library simultaneously.
- **Inventory Management**: Tracks availability and maintains records of all library items.
- **User Account Management**: Each user can access their account information, including borrowed items and due dates.
- **Secure User Authentication**: Passwords are securely hashed and salted before being stored in MongoDB.
- **Cloud Deployment**: Server is hosted on Heroku, a cloud hosting service used to run the server.

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Network configuration to enable client-server communication (default uses localhost)

## Usage and Restrictions

> **Important**: This code is intended solely for portfolio and demonstration purposes with potential employers. It is not to be used in any manner that constitutes cheating, plagiarism, or uncredited usage. 

For any questions regarding implementation or usage, please contact Aaron Phan at [atp2323@utexas.edu](mailto:atp2323@utexas.edu).
