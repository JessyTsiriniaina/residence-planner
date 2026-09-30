# Residence Planner

Residential property and space planning application built with Java.

Residence Planner is a Java desktop application for designing and managing residential layouts. It helps users define rooms, apply planning constraints, and visualize the arrangement of spaces in a graphical interface.

## Features

- Visual room and house planning interface
- Constraint management for residential design rules
- Drawing and rendering of floor-plan elements
- Support for doors, windows, openings, and room relationships
- Java Swing-based graphical user interface
- Maven-based project setup

## Tech Stack

- Java 25
- Maven
- Swing / IntelliJ GUI Designer
- JGoodies Forms

## Project Structure

```text
.
├── .mvn/
├── src/
│   └── main/
│       └── java/
│           └── io/github/jessytsiriniaina/
│               ├── gui/
│               ├── logic/
│               └── model/
├── .gitignore
├── pom.xml
├── mvnw
├── mvnw.cmd
├── TODO.md
└── README.md
```

## Getting Started

### Prerequisites

- Java 25 or newer
- Maven wrapper included, so no global Maven install is required

### Build

```bash
./mvnw clean compile
```

### Run

This project is primarily designed to be launched from an IDE such as IntelliJ IDEA:

1. Open the project in IntelliJ IDEA
2. Locate `io.github.jessytsiriniaina.Main`
3. Run the main entry point from the IDE

## Notes

The project includes a `TODO.md` file tracking planned improvements and remaining work items, including PDF export and several UI/logic refinements.

## License

This project currently does not specify a license.
