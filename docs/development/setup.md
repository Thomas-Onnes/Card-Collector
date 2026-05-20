# Local Development Setup
This document explains how to set up the card Collector project for local development.

---

# Requirements
Before starting, make sure the following software is installed.

## Required Software

### Java development Kit (JDK)
The backend requires:
- Java 21 (LTS)

Verify installation:
```bash
java --version
```

Expected output:
```txt
java 21.x.x
```

### IntelliJ IDEA
Recommended IDE:
- IntelliJ IDEA Community Edition or Ultimate

Download:
https://www.jetbrains.com/idea/

---

### Git

Verify installation:

```bash
git --version
```

---

# Project Setup

## Clone Repository

Clone the repository:

```bash
git clone <repository-url>
```

Navigate into the project:

```bash
cd Card-Collector
```

---

# Backend Setup

The backend is written in Kotlin and uses Gradle.

## Navigate to Backend

```bash
cd backend
```

---

## Install Dependencies

Gradle Wrapper is included in the repository.

No global Gradle installation is required.

---

## Run Backend

### Windows

```powershell
.\gradlew.bat run
```

### macOS / Linux

```bash
./gradlew run
```

Expected output:

```txt
Card Collector Backend Running
```

---

## Verify Build

To test whether the backend compiles correctly:

### Windows

```powershell
.\gradlew.bat build
```

### macOS / Linux

```bash
./gradlew build
```

---