# Zapp Project Scaffolding 🛠️

A **language/framework-agnostic CLI tool** to quickly bootstrap your project structure with customisable templates.


## Why This Exists

Starting new projects often involves repetitive setup:
- Creating the same folder structure
- Writing similar config files
- Copy-pasting boilerplate code

This CLI lets you create **reusable templates** for any tech stack (React, Python, Go, etc.), so you can focus on writing actual code from day one.

## Features ✨

- 📂 Generate project structures from templates
- 🔧 Supports variables (e.g., `<<<variable_name_here>>>`)
- 🌐 Works with any language/framework
- 🔄 Update existing projects from templates

## Quick Start 🚀

### Building from source
To build & run the CLI:
```cmd
	cd cli
	mvn package
    java -jar .\target\zapp-0.0.1-SNAPSHOT.jar
```

To run the API
```cmd
    cd project_scaffolding_server
    mvn spring-boot:run
```