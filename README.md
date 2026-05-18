# Fenix

Fenix es una aplicación de escritorio desarrollada en JavaFX con backend en Spring Boot.  
El objetivo del proyecto es crear una plataforma para publicar, descubrir, adquirir y gestionar novelas visuales o juegos narrativos.

El proyecto está organizado como una aplicación modular con tres partes principales:

- `client`: aplicación JavaFX.
- `server`: backend REST con Spring Boot.
- `common`: interfaces y DTO compartidos entre cliente y servidor.

---

## Tecnologías utilizadas

### General

- Java 21
- Maven
- Lombok

### Cliente

- JavaFX
- FXML
- CSS
- Spring RestClient
- Ikonli / Material Design Icons

### Servidor

- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- MySQL
- Apache Tika
- Commons IO

---

## Estructura del proyecto

```text
ModuledFenix/
├── client/
│   ├── src/main/java/org/ies/fenix/client/
│   │   ├── api/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── listener/
│   │   └── utils/
│   └── src/main/resources/
│       ├── fxml/
│       ├── graphics/
│       └── styles/
│
├── common/
│   └── src/main/java/org/ies/fenix/controller/
│       ├── dto/
│       ├── IClientController.java
│       ├── IGameController.java
│       ├── IPurchaseController.java
│       ├── ITagController.java
│       └── ITeaserController.java
│
├── server/
│   └── src/main/java/org/ies/fenix/server/
│       ├── config/
│       ├── controller/
│       ├── exception/
│       ├── models/
│       ├── repositories/
│       ├── services/
│       └── utils/
│
└── pom.xml