# MyIDE Project

## Overview
MyIDE is an integrated development environment that provides a modern, feature-rich coding experience. It combines a Java backend with a JavaScript/HTML frontend to create a versatile IDE that supports multiple programming languages including Java and C++.

## Project Structure
The project consists of several key components:

### Backend (JsToJava)
The Java backend provides core IDE functionality through a well-structured domain model:

- **Entity Layer**: Defines the core domain objects like Project, Node, Aspect, and Feature
- **Service Layer**: Implements services for managing projects and files
  - `ProjectService`: Handles project creation, loading, and aspect management
  - `NodeService`: Provides file operations (create, update, delete, move)
- **REST Layer**: Exposes backend functionality to the frontend through REST endpoints

### Frontend (UI)
The frontend provides a modern user interface with:

- Code editor based on Ace Editor with syntax highlighting and autocompletion
- File explorer for navigating project structure
- Command panel for executing various operations
- Support for multiple languages with specialized toolbars

### API Bridge
A Node.js Express server connects the frontend and backend:

- Serves the UI components
- Routes API requests to the Java backend
- Handles command execution

## Features

- **Multi-language Support**: Java, C++, and extensible to other languages
- **Project Management**: Create, open, and manage projects
- **File Operations**: Create, edit, delete, and move files
- **Code Editing**: Syntax highlighting, autocompletion, and other modern editor features
- **Build Tools Integration**: Support for Maven (Java) and compilation tools (C++)
- **Version Control**: Git integration for basic version control operations

## How to Run

### Prerequisites
- Node.js and npm
- Java 11 or higher
- Maven

### Starting the IDE

#### Windows
```
IDE.bat
```

#### Unix/Linux/macOS
```
./IDE.sh
```

Both scripts execute `npm run start` which:
1. Starts the Express server (`web` script)
2. Launches the Electron application (`elec` script)

## Architecture

The application follows a client-server architecture:

1. **Client**: Electron-based UI that provides the code editor and interface
2. **API Server**: Express.js server that handles HTTP requests
3. **Backend**: Java application that implements the core IDE functionality

Communication between components happens through REST API calls, allowing for a clean separation of concerns between the UI and the business logic.

## Configuration

The IDE supports configuration through JSON files (see `configex1.json` through `configex4.json` for examples). These configurations can specify project settings, indexing preferences, and other IDE behaviors.

## Development

To extend the IDE with new features:

1. Implement new Aspect types in the backend
2. Add corresponding UI components in the frontend
3. Connect them through the API layer

The modular architecture makes it straightforward to add support for new languages or tools.
