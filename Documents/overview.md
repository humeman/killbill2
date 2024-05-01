### [Kill Bill 2](../README.md) → [Docs](README.md) → Technical Overview
---

# Technical Overview
This document details the plans for the frontend and backend of Kill Bill 2.

## Basics
The game is multiplayer-first, communicating to a central server hosted somewhere on the Internet. Clients will manage friends, send out chat messages, and create games in the frontend by communicating with a REST API. When a game is started and players join, communication switches to UDP for real-time events.

## [Frontend](frontend/README.md)
The game is developed for Android and desktop platforms (although it is mobile-first). It will be created using the libGDX library.

## [Backend](backend/README.md)
As mentioned before, the backend will have TCP and UDP components. These will be written in Java using Spring Boot with all data being stored in a backend MySQL database.