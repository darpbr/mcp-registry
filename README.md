# MCP Registry

[![CI build](https://github.com/darpbr/mcp-registry/actions/workflows/ci.yml/badge.svg)](https://github.com/darpbr/mcp-registry/actions)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.29.3-blue)](https://quarkus.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Latest-green)](https://www.mongodb.com/)

Um registro centralizado e escalável para gerenciar servidores MCP (Model Context Protocol), desenvolvido com Quarkus e MongoDB.

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Uso](#uso)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [API REST](#api-rest)
- [Desenvolvimento](#desenvolvimento)
- [Testes](#testes)
- [Deploy](#deploy)
- [Contribuição](#contribuição)
- [Licença](#licença)

## 🎯 Visão Geral

O **MCP Registry** é uma aplicação RESTful que fornece um repositório centralizado para registro, gerenciamento e monitoramento de servidores MCP. Através de uma API simples e intuitiva, permite:

- ✅ Registrar novos servidores MCP
- ✅ Listar e filtrar servidores por status
- ✅ Atualizar informações e status de servidores
- ✅ Gerenciar ciclo de vida (Ativo, Inativo, Deprecado, Pendente)
- ✅ Armazenar metadados customizados
- ✅ Monitoramento com heartbeat duplo

## 🏗️ Arquitetura

```mermaid
graph TB
    subgraph "Camada Apresentação"
        REST["REST Endpoints<br/>API v1"]
        DEV_UI["Dev UI<br/>Quarkus Console"]
    end

    subgraph "Camada Aplicação"
        RESOURCE["Resource Layer<br/>McpServerResource"]
        SERVICE["Service Layer<br/>McpService"]
    end

    subgraph "Camada Domínio"
        DOMAIN["Domain Layer<br/>McpServer<br/>Status<br/>Validations"]
        DTO["DTOs<br/>McpServerDTO<br/>McpServerResponseDTO"]
        EXCEPTION["Exception Handler<br/>DomainException"]
    end

    subgraph "Camada Persistência"
        REPOSITORY["Repository Pattern<br/>McpServerRepository"]
        MONGODB["MongoDB<br/>NoSQL Database"]
    end

    subgraph "Utilitários"
        UTILS["Utils<br/>ValidarCamposNull<br/>Validators"]
    end

    subgraph "Infraestrutura"
        DOCKER["Containerization<br/>Docker & Docker Compose"]
        K8S["Orchestration<br/>Kubernetes"]
    end

    REST -->|Request| RESOURCE
    DEV_UI -->|Dev Mode| RESOURCE
    RESOURCE -->|Processing| SERVICE
    SERVICE -->|Business Logic| DOMAIN
    SERVICE -->|Validation| UTILS
    SERVICE -->|Mapping| DTO
    SERVICE -->|Data Access| REPOSITORY
    DOMAIN -->|Error Handling| EXCEPTION
    REPOSITORY -->|CRUD| MONGODB
    DOCKER -.->|Deploy| RESOURCE
    K8S -.->|Orchestrate| DOCKER

    style REST fill:#4A90E2
    style SERVICE fill:#7ED321
    style DOMAIN fill:#F5A623
    style REPOSITORY fill:#BD10E0
    style MONGODB fill:#50E3C2
    style DOCKER fill:#B8E986
    style K8S fill:#FF6B6B
```

### Padrões de Design Implementados

| Padrão | Localização | Propósito |
|--------|------------|----------|
| **REST** | `rest/` | Exposição de endpoints HTTP |
| **Service Layer** | `service/` | Lógica de negócio centralizada |
| **Repository Pattern** | `repository/` | Abstração de acesso a dados |
| **DTO Pattern** | `dto/` | Transferência de dados entre camadas |
| **Domain-Driven Design** | `domain/` | Isolamento de regras de negócio |
| **Exception Handling** | `exception/` | Tratamento unificado de erros |

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 21+**: [Download JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.9+**: Incluído via `./mvnw`
- **MongoDB 4.4+**: [Instalar MongoDB](https://docs.mongodb.com/manual/installation/)
- **Docker** (opcional): Para containerização
- **Git**: Para controle de versão

### Verificar Instalação

```bash
java -version
./mvnw --version
mongod --version
```

## 🚀 Instalação

### 1. Clonar o Repositório

```bash
git clone https://github.com/darpbr/mcp-registry.git
cd mcp-registry
```

### 2. Instalar Dependências

```bash
./mvnw clean install
```

## ⚙️ Configuração

### Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
# MongoDB
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_USER=root
MONGODB_PASSWORD=darp123
MONGODB_DATABASE=mcp_registry_db

# Application
APP_PORT=8080
APP_LOG_LEVEL=INFO
```

### Arquivo de Configuração

Edite `src/main/resources/application.properties`:

```properties
# HTTP Server
quarkus.http.port=8080
quarkus.http.root-path=/
quarkus.http.host=0.0.0.0

# MongoDB
quarkus.mongodb.connection-string=mongodb://root:darp123@localhost:27017/
quarkus.mongodb.database=mcp_registry_db

# Logging
quarkus.log.level=INFO
quarkus.log.console.format=%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{2.}] (%t) %s%e%n

# Swagger/OpenAPI (opcional)
quarkus.smallrye-openapi.path=/q/openapi
quarkus.swagger-ui.always-include=true
```

### MongoDB Local (Docker)

```bash
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=darp123 \
  mongo:latest
```

## 💻 Uso

### Modo Desenvolvimento

Para executar em modo desenvolvimento com live reload:

```bash
./mvnw quarkus:dev
```

A aplicação estará disponível em: **http://localhost:8080**

Dev UI: **http://localhost:8080/q/dev/**

### Modo Produção

#### Build JAR

```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

#### Build Über-JAR

```bash
./mvnw clean package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

#### Build Nativo (GraalVM)

```bash
./mvnw clean package -Dnative
./target/mcp-registry-1.0.0-SNAPSHOT-runner
```

#### Build Nativo (Container)

```bash
./mvnw clean package -Dnative -Dquarkus.native.container-build=true
./target/mcp-registry-1.0.0-SNAPSHOT-runner
```

## 📁 Estrutura do Projeto

```
mcp-registry/
├── src/
│   ├── main/
│   │   ├── java/br/com/darp/
│   │   │   ├── domain/                    # Entidades e interfaces
│   │   │   │   ├── McpServer.java        # Entidade principal
│   │   │   │   ├── Status.java           # Enum de status
│   │   │   │   └── McpServerRepository.java
│   │   │   ├── rest/                     # Endpoints REST
│   │   │   │   ├── McpServerResource.java
│   │   │   │   └── StatusRequest.java
│   │   │   ├── service/                  # Lógica de negócio
│   │   │   │   └── McpService.java
│   │   │   ├── repository/               # Implementação de persistência
│   │   │   │   └── McpServerRepositoryImp.java
│   │   │   ├── dto/                      # Objetos de transferência
│   │   │   │   ├── McpServerDTO.java
│   │   │   │   └── McpServerResponseDTO.java
│   │   │   ├── exception/                # Exceções customizadas
│   │   │   │   └── DomainException.java
│   │   │   ├── utils/                    # Utilitários
│   │   │   │   └── ValidarCamposNull.java
│   │   │   └── GreetingResource.java
│   │   ├── resources/
│   │   │   └── application.properties
│   │   └── docker/
│   │       ├── Dockerfile.jvm
│   │       ├── Dockerfile.native-micro
│   │       ├── Dockerfile.legacy-jar
│   │       └── Dockerfile.native
│   ├── test/
│   │   └── java/br/com/darp/
│   │       ├── GreetingResourceTest.java
│   │       └── GreetingResourceIT.java
├── .github/workflows/                    # CI/CD
├── .mvn/wrapper/
├── pom.xml
├── mvnw & mvnw.cmd
├── api.http                              # Testes HTTP
├── docker-compose.yml
└── README.md
```

## 🔌 API REST

### Base URL

```
http://localhost:8080/api/v1/mcp-servers
```

### Endpoints

#### 1. Listar Todos os Servidores

```http
GET /api/v1/mcp-servers
```

**Resposta (200 OK):**
```json
[
  {
    "id": "6918cf3676d9152f05fee4be",
    "nome": "MCP DARP",
    "descricao": "MCP para configurar repositório DARP",
    "versao": "1.0.0",
    "url": "https://mcp.darp.com.br/mcp",
    "responsavel": "Equipe DARP",
    "contato": "darp@email.com",
    "status": "ATIVO",
    "publicadoEm": "2024-01-15T10:30:00Z",
    "atualizadoEm": "2024-01-15T10:30:00Z"
  }
]
```

#### 2. Filtrar por Status

```http
GET /api/v1/mcp-servers?status=ATIVO
```

**Status válidos:** `ATIVO`, `INATIVO`, `DEPRECADO`, `PENDENTE_ATIVACAO`

#### 3. Criar Novo Servidor MCP

```http
POST /api/v1/mcp-servers
Content-Type: application/json

{
  "nome": "MCP DARP",
  "descricao": "MCP que configura repositório DARP",
  "versao": "1.0.0",
  "url": "https://mcp.darp.com.br/mcp",
  "responsavel": "Equipe DARP",
  "contato": "darp@email.com",
  "metadados": {
    "environment": "production",
    "region": "us-east-1"
  }
}
```

**Resposta (201 Created):**
```json
{
  "id": "6918cf3676d9152f05fee4be",
  "nome": "MCP DARP",
  "status": "PENDENTE_ATIVACAO",
  "publicadoEm": "2024-01-15T10:30:00Z"
}
```

#### 4. Atualizar Status

```http
PATCH /api/v1/mcp-servers/{id}
Content-Type: application/json

{
  "status": "ATIVO",
  "aprovador": "Diego Peixoto"
}
```

**Resposta (200 OK):** Sem conteúdo

### Códigos de Status HTTP

| Código | Significado |
|--------|-------------|
| 200 | OK - Requisição bem-sucedida |
| 201 | Created - Recurso criado |
| 400 | Bad Request - Requisição inválida |
| 404 | Not Found - Recurso não encontrado |
| 406 | Not Acceptable - Validação falhou |
| 500 | Server Error - Erro interno |

### Testar API

Use o arquivo [api.http](api.http) com a extensão REST Client do VS Code:

```bash
# Ou use cURL
curl -X GET http://localhost:8080/api/v1/mcp-servers
```

## 🛠️ Desenvolvimento

### Configurar IDE

#### Visual Studio Code

1. Instale as extensões:
   - Extension Pack for Java
   - Quarkus Tools
   - REST Client
   - Mermaid Diagram Previewer

2. Configurações recomendadas em `.vscode/settings.json`:

```json
{
  "java.server.launchMode": "Standard",
  "[java]": {
    "editor.defaultFormatter": "redhat.java",
    "editor.formatOnSave": true
  }
}
```

#### IntelliJ IDEA

1. Abra o projeto
2. Configure JDK 21: File → Project Structure → SDK
3. Maven será detectado automaticamente

### Padrões de Código

- **Linguagem:** Java 21
- **Framework:** Quarkus 3.29.3
- **Banco de Dados:** MongoDB
- **Style:** Seguir convenções do projeto

### Commits

```bash
# Format: type(scope): description
git commit -m "feat(mcp-server): add heartbeat monitoring"
git commit -m "fix(validation): improve field validation"
git commit -m "docs(api): update endpoint documentation"
```

## ✅ Testes

### Executar Testes Unitários

```bash
./mvnw test
```

### Executar Testes de Integração

```bash
./mvnw verify
```

### Cobertura de Testes

```bash
./mvnw clean test jacoco:report
```

### Exemplo de Teste

Veja [GreetingResourceTest.java](src/test/java/br/com/darp/GreetingResourceTest.java)

## 🐳 Deploy

### Docker Build

#### Modo JVM

```bash
./mvnw clean package
docker build -f src/main/docker/Dockerfile.jvm -t mcp-registry:latest .
docker run -p 8080:8080 mcp-registry:latest
```

#### Modo Nativo

```bash
./mvnw clean package -Dnative
docker build -f src/main/docker/Dockerfile.native-micro -t mcp-registry:native .
docker run -p 8080:8080 mcp-registry:native
```

### Docker Compose

```yaml
# docker-compose.yml
version: '3.8'
services:
  mongodb:
    image: mongo:latest
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: darp123
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

  mcp-registry:
    build: .
    ports:
      - "8080:8080"
    environment:
      QUARKUS_MONGODB_CONNECTION_STRING: mongodb://root:darp123@mongodb:27017/
    depends_on:
      - mongodb

volumes:
  mongodb_data:
```

Executar:

```bash
docker-compose up -d
```

### Kubernetes

```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

## 📚 Guias Relacionados

- [Guia REST - Quarkus](https://quarkus.io/guides/rest)
- [MongoDB Panache - Quarkus](https://quarkus.io/guides/mongodb-panache)
- [OpenAPI & Swagger - Quarkus](https://quarkus.io/guides/openapi-swaggerui)
- [Health Checks - Quarkus](https://quarkus.io/guides/smallrye-health)

## 🔗 Recursos Úteis

- [Quarkus Documentation](https://quarkus.io/)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [Jakarta REST (JAX-RS)](https://jakarta.ee/specifications/restful-web-services/)
- [Maven Guide](https://maven.apache.org/)

## 🤝 Contribuição

Contribuições são bem-vindas! Para contribuir:

1. Fork o repositório
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## ✉️ Suporte

Para reportar bugs ou solicitar features, abra uma [issue](https://github.com/darpbr/mcp-registry/issues).

---

**Desenvolvido com ❤️ por DARP Team**

Última atualização: Novembro de 2025
