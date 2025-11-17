# MCP Registry

[![CI build](https://github.com/darpbr/mcp-registry/actions/workflows/ci.yml/badge.svg)](https://github.com/darpbr/mcp-registry/actions)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.29.3-blue)](https://quarkus.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Latest-green)](https://www.mongodb.com/)

Um registro centralizado e escalável para gerenciar servidores MCP (Model Context Protocol), desenvolvido com Quarkus e MongoDB.

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Fluxo de Ciclo de Vida](#fluxo-de-ciclo-de-vida)
- [Monitoramento com Heartbeat](#monitoramento-com-heartbeat)
- [Automações e CronJobs](#automações-e-cronjobs)
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

## 🔄 Fluxo de Ciclo de Vida

O MCP Registry implementa um fluxo completo de gerenciamento de servidores MCP, desde o registro inicial até a inativação por falta de atividade. Este fluxo garante que apenas servidores ativos e monitorados estejam disponíveis no registro.

### 1. Registro e Aprovação de MCP Servers

O fluxo começa quando uma equipe de desenvolvimento deseja registrar um novo servidor MCP.

```mermaid
sequenceDiagram
    participant DevTeam as Equipe Dev
    participant Registry as MCP Registry
    participant ApprovalTeam as Time Aprovação
    participant Notifications as Sistema Alertas

    DevTeam->>Registry: POST /api/v1/mcp-servers<br/>(dados do MCP)
    Registry->>Registry: Validar dados<br/>Salvar com status PENDENTE_APROVACAO
    Registry-->>DevTeam: 201 Created (id, status)
    
    Registry->>Notifications: Notificar fila de aprovação
    Notifications-->>ApprovalTeam: Alerta: Novo MCP para aprovação

    ApprovalTeam->>Registry: PATCH /api/v1/mcp-servers/{id}<br/>status: ATIVO
    Registry->>Registry: Atualizar status<br/>Registrar aprovador
    Registry-->>ApprovalTeam: 200 OK
    
    Registry->>Notifications: MCP Server ativado
    Notifications-->>DevTeam: Alerta: Seu MCP foi ativado
```

#### Exemplo de Solicitação de Registro

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
    "region": "us-east-1",
    "team": "darp-team"
  }
}
```

#### Resposta de Registro (201 Created)

```json
{
  "id": "6918cf3676d9152f05fee4be",
  "nome": "MCP DARP",
  "status": "PENDENTE_APROVACAO",
  "publicadoEm": "2024-11-17T10:30:00Z",
  "atualizadoEm": "2024-11-17T10:30:00Z"
}
```

#### Exemplo de Aprovação

```http
PATCH /api/v1/mcp-servers/6918cf3676d9152f05fee4be
Content-Type: application/json

{
  "status": "ATIVO",
  "aprovador": "Diego Peixoto"
}
```

---

## 💓 Monitoramento com Heartbeat

Após a ativação, cada servidor MCP deve enviar sinais de vida (heartbeat) em dois momentos separados por 12 horas, indicando que está funcionando corretamente.

### 2. Envio de Heartbeat pelos MCP Servers

O servidor MCP ativo deve se registrar enviando heartbeats periodicamente para o registry.

```mermaid
sequenceDiagram
    participant MCPServer as MCP Server Ativo
    participant Registry as MCP Registry
    participant Database as MongoDB

    loop A cada 12 horas
        MCPServer->>Registry: POST /api/v1/mcp-servers/{id}/heartbeat
        Registry->>Database: Registrar heartbeat com timestamp
        Registry-->>MCPServer: 200 OK (heartbeat aceito)
    end

    Note over MCPServer,Registry: Servidor continua ativo
    Note over Registry,Database: Registro atualizado em tempo real
```

#### Endpoint de Heartbeat

```http
POST /api/v1/mcp-servers/{id}/heartbeat
Content-Type: application/json

{
  "versao": "1.0.0",
  "metadados": {
    "uptime": "730h45m",
    "metricas": {
      "cpu": 45.2,
      "memoria": 62.8,
      "requisicoes": 15420
    }
  }
}
```

#### Resposta de Sucesso (200 OK)

```json
{
  "success": true,
  "message": "Heartbeat registrado com sucesso",
  "proximoHeartbeatEm": "2024-11-17T22:30:00Z"
}
```

#### Configuração de Heartbeat no MCP Server

O servidor MCP deve ser configurado para enviar heartbeats em intervalos regulares. Exemplo de configuração:

```properties
# application.properties do MCP Server
mcp.registry.url=https://registry.darp.com.br
mcp.registry.server-id=6918cf3676d9152f05fee4be
mcp.heartbeat.interval=12h
mcp.heartbeat.enabled=true
mcp.heartbeat.timeout=30s
```

---

## 🤖 Automações e CronJobs

O MCP Registry executa rotinas automatizadas para monitorar a saúde dos servidores registrados e gerenciar seu ciclo de vida.

### 3. Monitoramento de Heartbeat (CronJob - A cada 6 horas)

Esta rotina verifica quais servidores enviaram heartbeats e atualiza seus status.

```mermaid
graph TD
    A["CronJob Executa<br/>a cada 6 horas"] -->|Executa| B["Job: Verificar Heartbeats"]
    B --> C["1. Listar todos os<br/>MCPs ativos"]
    C --> D["2. Consultar último<br/>heartbeat de cada MCP"]
    D --> E{Tem heartbeat<br/>nos últimos 12h?}
    
    E -->|SIM| F["Manter status ATIVO"]
    E -->|NÃO| G["Alterar para DEPRECADO"]
    
    F --> H["Nenhuma ação"]
    G --> I["Registrar mudança"]
    G --> J["Enviar alerta ao time<br/>responsável"]
    
    I --> K["CronJob Finalizado"]
    H --> K
    J --> K

    style A fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style B fill:#7ED321,stroke:#5AA020,color:#fff
    style E fill:#F5A623,stroke:#D68910,color:#fff
    style G fill:#FF6B6B,stroke:#E63946,color:#fff
    style J fill:#FF6B6B,stroke:#E63946,color:#fff
```

#### Pseudocódigo da Rotina

```java
@Scheduled(cron = "0 */6 * * * ?") // A cada 6 horas
public void verificarHeartbeats() {
    // 1. Listar todos os MCPs com status ATIVO ou DEPRECADO
    List<McpServer> servidores = mcpServerRepository.findByStatusIn(
        Arrays.asList(Status.ATIVO, Status.DEPRECADO)
    );
    
    for (McpServer servidor : servidores) {
        LocalDateTime ultimoHeartbeat = servidor.getUltimoHeartbeat();
        LocalDateTime agora = LocalDateTime.now();
        
        // 2. Se último heartbeat foi há mais de 12h
        if (ultimoHeartbeat != null && 
            ChronoUnit.HOURS.between(ultimoHeartbeat, agora) >= 12) {
            
            // 3. Atualizar status para DEPRECADO
            servidor.setStatus(Status.DEPRECADO);
            mcpServerRepository.update(servidor);
            
            // 4. Enviar alerta à equipe
            notificationService.notificarStatusAlterado(
                servidor.getId(),
                Status.ATIVO,
                Status.DEPRECADO,
                "Nenhum heartbeat recebido nos últimos 12h"
            );
            
            logger.warn("MCP Server {} marcado como DEPRECADO", servidor.getNome());
        }
        // Se tem heartbeat recente e estava DEPRECADO, restaurar para ATIVO
        else if (Status.DEPRECADO.equals(servidor.getStatus()) && 
                 ultimoHeartbeat != null &&
                 ChronoUnit.HOURS.between(ultimoHeartbeat, agora) < 12) {
            
            servidor.setStatus(Status.ATIVO);
            mcpServerRepository.update(servidor);
            
            notificationService.notificarStatusAlterado(
                servidor.getId(),
                Status.DEPRECADO,
                Status.ATIVO,
                "Heartbeat recebido - servidor restaurado"
            );
            
            logger.info("MCP Server {} restaurado para ATIVO", servidor.getNome());
        }
    }
}
```

### 4. Verificação de Inatividade Prolongada (CronJob - Diário)

Esta rotina identifica servidores sem heartbeat por mais de 30 dias e notifica as equipes responsáveis sobre possível inativação.

```mermaid
graph TD
    A["CronJob Executa<br/>diariamente às 08:00"] -->|Executa| B["Job: Verificar Inatividade"]
    B --> C["1. Listar MCPs DEPRECADOS"]
    C --> D["2. Calcular dias sem<br/>heartbeat"]
    D --> E{Sem heartbeat<br/>há 30+ dias?}
    
    E -->|NÃO| F["Nenhuma ação"]
    E -->|SIM| G["1. Notificar time<br/>responsável"]
    
    G --> H["2. Notificar time<br/>administrador"]
    H --> I["3. Registrar tentativa<br/>de contato"]
    I --> J{Resposta do time<br/>em 7 dias?}
    
    J -->|SIM| K["Avaliar situação"]
    J -->|NÃO| L["CronJob Finalizado<br/>com status pendente"]
    
    K --> L

    style A fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style B fill:#7ED321,stroke:#5AA020,color:#fff
    style E fill:#F5A623,stroke:#D68910,color:#fff
    style G fill:#FF6B6B,stroke:#E63946,color:#fff
    style H fill:#FF6B6B,stroke:#E63946,color:#fff
    style L fill:#9B59B6,stroke:#6C3483,color:#fff
```

#### Pseudocódigo da Rotina

```java
@Scheduled(cron = "0 0 8 * * ?") // Diariamente às 08:00
public void verificarInatividadeProlongada() {
    // 1. Listar MCPs DEPRECADOS
    List<McpServer> servidoresDeprecados = mcpServerRepository
        .findByStatus(Status.DEPRECADO);
    
    LocalDateTime agora = LocalDateTime.now();
    
    for (McpServer servidor : servidoresDeprecados) {
        LocalDateTime ultimoHeartbeat = servidor.getUltimoHeartbeat();
        
        if (ultimoHeartbeat != null) {
            long diasSemHeartbeat = ChronoUnit.DAYS
                .between(ultimoHeartbeat, agora);
            
            // 2. Se sem heartbeat há 30+ dias
            if (diasSemHeartbeat >= 30) {
                
                // 3. Enviar alerta ao time responsável
                notificationService.alertarInativacaoProlongada(
                    servidor.getId(),
                    servidor.getNome(),
                    servidor.getResponsavel(),
                    servidor.getContato(),
                    diasSemHeartbeat
                );
                
                // 4. Enviar alerta ao time administrador
                notificationService.alertarAdministrador(
                    String.format(
                        "MCP Server '%s' sem heartbeat há %d dias. " +
                        "Avaliação de inativação necessária.",
                        servidor.getNome(),
                        diasSemHeartbeat
                    )
                );
                
                // 5. Registrar tentativa de contato
                servidor.setUltimaTentativaContato(agora);
                servidor.setDiasSemHeartbeat(diasSemHeartbeat);
                mcpServerRepository.update(servidor);
                
                logger.warn(
                    "MCP Server {} sem heartbeat há {} dias - " +
                    "Notificações enviadas",
                    servidor.getNome(),
                    diasSemHeartbeat
                );
            }
        }
    }
}
```

### Fluxo Completo de Estados

```mermaid
stateDiagram-v2
    [*] --> PENDENTE_APROVACAO: Novo registro

    PENDENTE_APROVACAO --> ATIVO: Aprovação manual
    PENDENTE_APROVACAO --> REJEITADO: Rejeição manual
    
    ATIVO --> DEPRECADO: Sem heartbeat 12h+<br/>(CronJob)
    DEPRECADO --> ATIVO: Heartbeat recebido<br/>(CronJob)
    
    DEPRECADO --> INATIVO: Sem heartbeat 30+ dias<br/>(Decisão manual após alerta)
    
    ATIVO --> INATIVO: Desativação manual
    DEPRECADO --> INATIVO: Desativação manual
    
    INATIVO --> [*]
    REJEITADO --> [*]
```

### Tabela de Estados

| Estado | Descrição | Transições | Trigger |
|--------|-----------|-----------|---------|
| **PENDENTE_APROVACAO** | Novo MCP aguardando aprovação | → ATIVO, REJEITADO | Manual (time aprovação) |
| **ATIVO** | MCP operacional e recebendo heartbeats | → DEPRECADO, INATIVO | CronJob (12h sem HB) ou Manual |
| **DEPRECADO** | MCP sem heartbeat recente | → ATIVO, INATIVO | CronJob (HB recebido) ou Manual |
| **INATIVO** | MCP desativado | Final | Manual (após 30+ dias ou por decisão) |
| **REJEITADO** | Solicitação rejeitada | Final | Manual (time aprovação) |



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

# Heartbeat Configuration
HEARTBEAT_TIMEOUT_HOURS=12
HEARTBEAT_CHECK_INTERVAL=6h

# Inactivity Configuration
INACTIVITY_THRESHOLD_DAYS=30
INACTIVITY_CHECK_INTERVAL=1d

# Notifications
NOTIFICATION_ENABLED=true
NOTIFICATION_EMAIL_FROM=registry@darp.com.br
NOTIFICATION_SMTP_HOST=smtp.gmail.com
NOTIFICATION_SMTP_PORT=587
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

# CronJobs - Configuração de Agendamento
quarkus.scheduler.enabled=true
quarkus.scheduler.start-mode=halted

# CronJob 1: Verificação de Heartbeats (a cada 6 horas)
# Verifica se MCPs ativos receberam heartbeats e muda para DEPRECADO se não
scheduler.heartbeat.cron=0 */6 * * * ?
scheduler.heartbeat.enabled=true
scheduler.heartbeat.name=Verificar Heartbeats

# CronJob 2: Verificação de Inatividade (diariamente às 08:00)
# Identifica MCPs sem heartbeat há 30+ dias e notifica equipes
scheduler.inactivity.cron=0 0 8 * * ?
scheduler.inactivity.enabled=true
scheduler.inactivity.name=Verificar Inatividade Prolongada

# Limites de Negócio
mcp.heartbeat.timeout.hours=12
mcp.inactivity.threshold.days=30
mcp.max.notification.retries=3
```

### Configuração de CronJobs

O MCP Registry utiliza duas rotinas automatizadas para gerenciar o ciclo de vida dos servidores:

#### CronJob 1: Verificação de Heartbeats

**Expressão:** `0 */6 * * * ?` (A cada 6 horas)

**Responsabilidades:**
- ✅ Verificar último heartbeat de todos os MCPs ATIVO
- ✅ Se sem heartbeat > 12h → alterar para DEPRECADO
- ✅ Se DEPRECADO com heartbeat recente → restaurar para ATIVO
- ✅ Enviar notificações quando status mudar

**Horários de Execução (exemplo):**
- 00:00, 06:00, 12:00, 18:00 UTC

#### CronJob 2: Verificação de Inatividade Prolongada

**Expressão:** `0 0 8 * * ?` (Diariamente às 08:00)

**Responsabilidades:**
- ✅ Listar MCPs DEPRECADO sem heartbeat há 30+ dias
- ✅ Notificar equipe responsável sobre possível inativação
- ✅ Notificar administradores do registry
- ✅ Registrar tentativa de contato
- ✅ Preparar dados para possível inativação manual

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
https://registry.darp.com.br/api/v1/mcp-servers
```

### Endpoints - Gerenciamento de Servidores

#### 1. Listar Todos os Servidores

```http
GET /api/v1/mcp-servers
```

**Parâmetros de Query (opcionais):**
- `status`: Filtrar por status (ATIVO, INATIVO, DEPRECADO, PENDENTE_APROVACAO)
- `responsavel`: Filtrar por equipe responsável
- `pagina`: Número da página (padrão: 1)
- `tamanho`: Itens por página (padrão: 20)

**Resposta (200 OK):**

```json
[
  {
    "id": "6918cf3676d9152f05fee4be",
    "nome": "MCP DARP",
    "descricao": "MCP que configura repositório DARP",
    "versao": "1.0.0",
    "url": "https://mcp.darp.com.br/mcp",
    "responsavel": "Equipe DARP",
    "contato": "darp@email.com",
    "status": "ATIVO",
    "publicadoEm": "2024-11-15T10:30:00Z",
    "atualizadoEm": "2024-11-17T10:30:00Z",
    "ultimoHeartbeat": "2024-11-17T10:15:00Z",
    "metadados": {
      "environment": "production",
      "region": "us-east-1"
    }
  }
]
```

#### 2. Filtrar por Status

```http
GET /api/v1/mcp-servers?status=ATIVO
```

**Status válidos:** `ATIVO`, `INATIVO`, `DEPRECADO`, `PENDENTE_APROVACAO`, `REJEITADO`

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
  "status": "PENDENTE_APROVACAO",
  "publicadoEm": "2024-11-17T10:30:00Z"
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

#### 5. Obter Detalhes de um Servidor

```http
GET /api/v1/mcp-servers/{id}
```

**Resposta (200 OK):**

```json
{
  "id": "6918cf3676d9152f05fee4be",
  "nome": "MCP DARP",
  "descricao": "MCP que configura repositório DARP",
  "versao": "1.0.0",
  "url": "https://mcp.darp.com.br/mcp",
  "responsavel": "Equipe DARP",
  "contato": "darp@email.com",
  "status": "ATIVO",
  "publicadoEm": "2024-11-15T10:30:00Z",
  "atualizadoEm": "2024-11-17T10:30:00Z",
  "ultimoHeartbeat": "2024-11-17T10:15:00Z",
  "diasSemHeartbeat": 0,
  "metadados": {
    "environment": "production",
    "region": "us-east-1"
  }
}
```

#### 6. Deletar Servidor MCP

```http
DELETE /api/v1/mcp-servers/{id}
```

**Resposta (204 No Content):** Sem conteúdo

### Endpoints - Heartbeat (Monitoramento)

#### 7. Registrar Heartbeat

```http
POST /api/v1/mcp-servers/{id}/heartbeat
Content-Type: application/json

{
  "versao": "1.0.0",
  "metadados": {
    "uptime": "730h45m",
    "metricas": {
      "cpu": 45.2,
      "memoria": 62.8,
      "requisicoes": 15420
    }
  }
}
```

**Resposta (200 OK):**

```json
{
  "success": true,
  "message": "Heartbeat registrado com sucesso",
  "ultimoHeartbeat": "2024-11-17T22:30:00Z",
  "proximoHeartbeatEm": "2024-11-18T10:30:00Z"
}
```

**Códigos de Erro:**
- `400`: MCP Server não encontrado
- `409`: MCP Server não está ATIVO (heartbeat rejeitado)

#### 8. Listar Heartbeats de um Servidor

```http
GET /api/v1/mcp-servers/{id}/heartbeats
```

**Parâmetros de Query (opcionais):**
- `ultimas`: Número de últimos heartbeats (padrão: 10)
- `desde`: Data/hora mínima (ISO 8601)

**Resposta (200 OK):**

```json
[
  {
    "timestamp": "2024-11-17T10:15:00Z",
    "status": "ok",
    "versao": "1.0.0",
    "metadados": {
      "cpu": 45.2,
      "memoria": 62.8
    }
  },
  {
    "timestamp": "2024-11-16T22:15:00Z",
    "status": "ok",
    "versao": "1.0.0",
    "metadados": {
      "cpu": 42.1,
      "memoria": 61.5
    }
  }
]
```

### Resumo de Endpoints

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| GET | `/api/v1/mcp-servers` | Listar todos os MCPs | ✅ |
| GET | `/api/v1/mcp-servers?status={status}` | Filtrar por status | ✅ |
| POST | `/api/v1/mcp-servers` | Registrar novo MCP | ✅ |
| GET | `/api/v1/mcp-servers/{id}` | Obter detalhes | ✅ |
| PATCH | `/api/v1/mcp-servers/{id}` | Atualizar status | ✅ |
| DELETE | `/api/v1/mcp-servers/{id}` | Deletar MCP | ✅ |
| POST | `/api/v1/mcp-servers/{id}/heartbeat` | Enviar heartbeat | ✅ |
| GET | `/api/v1/mcp-servers/{id}/heartbeats` | Listar heartbeats | ✅ |

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

---

## 📖 Guia de Uso Prático - Fluxo Completo

Este guia demonstra o fluxo completo de um novo MCP Server, desde o registro até o monitoramento contínuo.

### Cenário: Registrar e Monitorar um Novo MCP Server

**Participantes:**
- 👨‍💻 **Equipe de Desenvolvimento:** Equipe DARP
- ✅ **Time de Aprovação:** Responsável por revisar e aprovar novos MCPs
- 🤖 **MCP Server:** Aplicação que será registrada
- 📡 **MCP Registry:** Sistema de registro e monitoramento

#### Passo 1: Equipe Dev Registra um Novo MCP (T0)

A equipe de desenvolvimento envia uma requisição POST com os dados do novo servidor MCP:

```bash
curl -X POST http://localhost:8080/api/v1/mcp-servers \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "MCP DARP",
    "descricao": "MCP para integração com repositório DARP",
    "versao": "1.0.0",
    "url": "https://mcp.darp.com.br:8443/mcp",
    "responsavel": "Equipe DARP",
    "contato": "darp-team@company.com",
    "metadados": {
      "environment": "production",
      "region": "us-east-1",
      "team": "darp-team",
      "slack_channel": "#mcp-darp"
    }
  }'
```

**Resposta (201 Created):**

```json
{
  "id": "507f1f77bcf86cd799439011",
  "nome": "MCP DARP",
  "status": "PENDENTE_APROVACAO",
  "publicadoEm": "2024-11-17T09:00:00Z",
  "atualizadoEm": "2024-11-17T09:00:00Z"
}
```

**O que acontece internamente:**
1. ✅ Dados são validados
2. ✅ MCP é criado com status `PENDENTE_APROVACAO`
3. ✅ Notificação é enviada para o time de aprovação
4. ✅ Email/Slack: "Novo MCP 'MCP DARP' aguardando aprovação"

#### Passo 2: Time de Aprovação Revisa e Aprova (T1 - 1 dia depois)

O time de aprovação revisa a solicitação e aprova:

```bash
curl -X PATCH http://localhost:8080/api/v1/mcp-servers/507f1f77bcf86cd799439011 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ATIVO",
    "aprovador": "Diego Peixoto"
  }'
```

**Resposta (200 OK):** Sem conteúdo (ou com confirmação)

**O que acontece internamente:**
1. ✅ Status muda de `PENDENTE_APROVACAO` → `ATIVO`
2. ✅ Registra nome do aprovador e timestamp
3. ✅ Notificação é enviada para a Equipe Dev: "Seu MCP 'MCP DARP' foi aprovado!"

#### Passo 3: MCP Server Registra Primeiro Heartbeat (T2 - Primeiras 24h)

Após ser ativado, o MCP Server deve estar configurado para enviar heartbeats a cada 12 horas:

```bash
curl -X POST http://localhost:8080/api/v1/mcp-servers/507f1f77bcf86cd799439011/heartbeat \
  -H "Content-Type: application/json" \
  -d '{
    "versao": "1.0.0",
    "metadados": {
      "uptime": "5h30m",
      "metricas": {
        "cpu": 35.2,
        "memoria": 48.5,
        "requisicoes": 2541,
        "erros": 0
      },
      "timestamp": "2024-11-17T12:00:00Z"
    }
  }'
```

**Resposta (200 OK):**

```json
{
  "success": true,
  "message": "Heartbeat registrado com sucesso",
  "ultimoHeartbeat": "2024-11-17T12:00:00Z",
  "proximoHeartbeatEm": "2024-11-18T00:00:00Z",
  "diasSemHeartbeat": 0
}
```

#### Passo 4: MCP Server Registra Segundo Heartbeat (T3 - 12 horas depois)

```bash
curl -X POST http://localhost:8080/api/v1/mcp-servers/507f1f77bcf86cd799439011/heartbeat \
  -H "Content-Type: application/json" \
  -d '{
    "versao": "1.0.0",
    "metadados": {
      "uptime": "17h35m",
      "metricas": {
        "cpu": 42.1,
        "memoria": 52.3,
        "requisicoes": 8923,
        "erros": 1
      },
      "timestamp": "2024-11-18T00:00:00Z"
    }
  }'
```

**Status do MCP Registry:**
- ✅ MCP Status: **ATIVO** (com heartbeats regulares)
- ✅ Último Heartbeat: 2024-11-18T00:00:00Z
- ✅ Dias sem Heartbeat: 0

#### Passo 5a: Cenário Normal - CronJob Valida Heartbeats (T6 - 6 horas depois)

A cada 6 horas, o primeiro CronJob executa:

**Timestamp: 2024-11-18T06:00:00Z**

```
[CronJob] Verificar Heartbeats Executado
├─ MCP "MCP DARP"
│  ├─ Status Atual: ATIVO
│  ├─ Último Heartbeat: 2024-11-18T00:00:00Z
│  ├─ Horas desde último HB: 6h
│  └─ Ação: ✅ NENHUMA (heartbeat recente)
└─ Todos os MCPs ATIVO têm heartbeat válido
```

#### Passo 5b: Cenário de Falha - MCP para de enviar Heartbeats

Imagine que o MCP Server para de funcionar em 2024-11-20T06:00:00Z e não envia mais heartbeats.

**Timeline de eventos:**

| Timestamp | Evento | Status | Ação |
|-----------|--------|--------|------|
| 2024-11-20T06:00:00Z | MCP para de enviar HB | ATIVO | - |
| 2024-11-20T12:00:00Z | CronJob #1 Executa | ATIVO | Nenhuma (ainda em 6h) |
| 2024-11-20T18:00:00Z | CronJob #1 Executa | ATIVO | Nenhuma (ainda em 12h) |
| 2024-11-21T00:00:00Z | **CronJob #1 Executa** | **ATIVO** | **→ DEPRECADO** (>12h sem HB) |
| | | | 📧 Alerta enviado: Equipe DARP |
| 2024-11-21T08:00:00Z | **CronJob #2 Executa** | DEPRECADO | Nenhuma (não atingiu 30 dias) |
| 2024-12-21T08:00:00Z | **CronJob #2 Executa** | DEPRECADO | 📧 Alerta de inatividade (30+ dias) |
| | | | Equipe DARP + Administradores |

#### Passo 6a: MCP Server é Restaurado (Cenário Feliz)

O MCP Server é reparado e volta a enviar heartbeats:

**Timestamp: 2024-12-21T10:00:00Z**

```bash
curl -X POST http://localhost:8080/api/v1/mcp-servers/507f1f77bcf86cd799439011/heartbeat \
  -H "Content-Type: application/json" \
  -d '{
    "versao": "1.0.1",
    "metadados": {
      "uptime": "0h45m",
      "metricas": {
        "cpu": 28.5,
        "memoria": 41.2,
        "requisicoes": 125,
        "erros": 0
      }
    }
  }'
```

**O que acontece no próximo CronJob (2024-12-21T12:00:00Z):**

```
[CronJob] Verificar Heartbeats Executado
├─ MCP "MCP DARP"
│  ├─ Status Atual: DEPRECADO
│  ├─ Último Heartbeat: 2024-12-21T10:00:00Z
│  ├─ Horas desde último HB: 2h
│  └─ Ação: ✅ RESTAURAR → ATIVO
└─ 📧 Alerta enviado: Equipe DARP - MCP restaurado!
```

#### Passo 6b: MCP é Permanentemente Inativado

Se o MCP não for restaurado e atingir 30 dias sem heartbeat:

**Timestamp: 2024-12-21T08:00:00Z (CronJob #2 - Inatividade)**

```
[CronJob] Verificar Inatividade Prolongada Executado
├─ MCP "MCP DARP"
│  ├─ Status Atual: DEPRECADO
│  ├─ Dias sem Heartbeat: 31 dias
│  ├─ Último Heartbeat: 2024-11-20T05:59:00Z
│  └─ Ação: 🚨 NOTIFICAR
│
├─ 📧 Notificação para EQUIPE DARP:
│  ├─ Título: "Atenção: seu MCP DARP pode ser inativado"
│  ├─ Mensagem: "Sem heartbeat há 31 dias"
│  ├─ Status: DEPRECADO → Será INATIVO
│  └─ Ação: Responder se deseja manter ativo
│
├─ 📧 Notificação para ADMINISTRADORES:
│  ├─ Título: "MCP DARP - Candidato a Inativação"
│  ├─ Dados: Sem heartbeat desde 2024-11-20
│  ├─ Responsável: Equipe DARP (darp-team@company.com)
│  └─ Ação: Aguardando resposta da equipe
│
└─ Registro de tentativa de contato em DB
```

**Time responsável pode responder de 2 formas:**

**Opção A: Equipe Dev Reativa o MCP**

```bash
# Enviar heartbeat para restaurar
curl -X POST http://localhost:8080/api/v1/mcp-servers/507f1f77bcf86cd799439011/heartbeat
```

**Opção B: Após 7 dias sem resposta**

Se nenhuma resposta em 7 dias, administrador pode inativar manualmente:

```bash
curl -X PATCH http://localhost:8080/api/v1/mcp-servers/507f1f77bcf86cd799439011 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "INATIVO",
    "motivo": "Inatividade prolongada - sem heartbeat por 38 dias",
    "inativadorPor": "admin@company.com"
  }'
```

---

### Resumo do Fluxo Temporal

```mermaid
timeline
    title Fluxo Completo de um MCP Server
    
    T0: Equipe Dev registra MCP
       : Status: PENDENTE_APROVACAO
    
    T1: Time de Aprovação aprova
       : Status: ATIVO
       : Equipe Dev notificada
    
    T2-T30: MCP envia heartbeats a cada 12h
           : Status: ATIVO
           : Último HB sempre atualizado
    
    T31: CronJob valida heartbeats (6h)
        : Status: continua ATIVO
        : Tudo OK
    
    T32: MCP para de funcionar
        : Para enviar heartbeats
    
    T35: CronJob detecta > 12h sem HB
        : Status: ATIVO → DEPRECADO
        : Alerta enviado à equipe
    
    T60: CronJob verifica inatividade
        : Detecta 29 dias sem HB
        : Status: ainda DEPRECADO
    
    T90: CronJob verifica inatividade
        : Detecta 31+ dias sem HB
        : Alerta de possível inativação
        : Equipe responsável notificada
    
    T97: Sem resposta da equipe
        : Admin inativa o MCP
        : Status: INATIVO
```

---

## 🔐 Segurança e Responsabilidades

### Equipes Envolvidas

| Papel | Responsabilidade | Ações Permitidas |
|------|-----------------|-----------------|
| **Equipe Dev** | Registrar e manter MCPs | POST registro, enviar heartbeat, visualizar status |
| **Time Aprovação** | Revisar e aprovar MCPs | PATCH status para ATIVO/REJEITADO |
| **Administradores** | Gerenciar lifecycle geral | Todas as operações + inativação final |
| **Sistema Registry** | Monitorar e executar regras | Executar cronjobs, enviar notificações |

### Autenticação e Autorização

```properties
# application.properties - Segurança
quarkus.oidc.enabled=true
quarkus.oidc.auth-server-url=https://auth.company.com/auth
quarkus.oidc.client-id=mcp-registry
quarkus.oidc.credentials.secret=${OIDC_CLIENT_SECRET}

# Roles
mcp.roles.developer=developer
mcp.roles.approver=approver
mcp.roles.admin=admin
```

---

## 📊 Monitoramento e Observabilidade

### Métricas Disponíveis

O MCP Registry expõe métricas no endpoint `/q/metrics`:

```properties
# Métricas de Negócio
mcp_servers_total{status="ATIVO"} 25
mcp_servers_total{status="DEPRECADO"} 3
mcp_servers_total{status="PENDENTE_APROVACAO"} 2
mcp_servers_total{status="INATIVO"} 1

# Métricas de Heartbeat
mcp_heartbeat_total{status="success"} 1543
mcp_heartbeat_total{status="failure"} 12
mcp_heartbeat_received_total 1555
mcp_heartbeat_missing_total 3

# Métricas de Cronjobs
scheduler_heartbeat_executions_total 48
scheduler_heartbeat_duration_max_seconds 3.5
scheduler_inactivity_executions_total 48
scheduler_inactivity_duration_max_seconds 2.1

# Métricas de API REST
http_requests_total{method="POST",endpoint="/heartbeat",status="200"} 1543
http_requests_total{method="PATCH",endpoint="/mcp-servers",status="200"} 87
http_requests_total{method="POST",endpoint="/mcp-servers",status="201"} 31
```

### Alertas Recomendados (Prometheus/AlertManager)

```yaml
groups:
  - name: mcp_registry
    interval: 30s
    rules:
      # Alerta: Muitos MCPs sem heartbeat
      - alert: HighDeprecatedMcpServers
        expr: increase(mcp_servers_total{status="DEPRECADO"}[1h]) > 3
        for: 5m
        annotations:
          summary: "Alto número de MCPs DEPRECADOS"
          
      # Alerta: Cronjob falhando
      - alert: CronjobHeartbeatFailure
        expr: rate(scheduler_heartbeat_executions_total{status="error"}[5m]) > 0
        for: 10m
        annotations:
          summary: "CronJob de Heartbeat apresentando erros"
          
      # Alerta: Rejeições em massa
      - alert: HighRejectionRate
        expr: rate(mcp_servers_total{status="REJEITADO"}[1h]) > 5
        for: 5m
        annotations:
          summary: "Alta taxa de rejeição de MCPs"
```

---

## 🔧 Troubleshooting

### Problema: MCP não recebe aprovação

**Diagnóstico:**
```bash
# Verificar status
curl http://localhost:8080/api/v1/mcp-servers/{id}

# Verificar logs
docker logs mcp-registry | grep "6918cf3676d9152f05fee4be"
```

**Solução:**
- Verificar se o time de aprovação foi notificado
- Verificar logs de notificação
- Contactar administrador se houver bloqueio

### Problema: Heartbeat não é aceito

**Codes de Erro:**

| Código | Causa | Solução |
|--------|-------|--------|
| 404 | MCP não encontrado | Verificar ID do MCP |
| 409 | MCP não está ATIVO | Aguardar aprovação ou verificar status |
| 400 | Payload inválido | Validar formato JSON |
| 503 | Registry indisponível | Aguardar e tentar novamente |

**Diagnóstico:**
```bash
curl -v -X POST http://localhost:8080/api/v1/mcp-servers/{id}/heartbeat \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### Problema: CronJob não executa

**Checklist:**
```properties
# Verificar em application.properties
quarkus.scheduler.enabled=true
quarkus.scheduler.start-mode=halted  # Mudar para "running" se necessário

# Verificar cron expressions
# Usar: https://crontab.guru/
```

**Verificar em logs:**
```bash
docker logs mcp-registry | grep -i "scheduler\|cron"
```

---

## 📚 Referências Adicionais

### Documentação de Padrões

- [Quarkus Scheduling Guide](https://quarkus.io/guides/scheduler)
- [Quarkus MongoDB Guide](https://quarkus.io/guides/mongodb-panache)
- [Jakarta REST Specification](https://jakarta.ee/specifications/restful-web-services/)
- [Cron Expression Format](https://crontab.guru/)

### Ferramentas Úteis

- **Postman:** Coleção de testes de API
- **MongoDB Compass:** Visualizar dados
- **Grafana:** Dashboard de métricas
- **AlertManager:** Gerenciar alertas

### Contato e Suporte

- **Issues:** [GitHub Issues](https://github.com/darpbr/mcp-registry/issues)
- **Email:** registry-team@darp.com.br
- **Slack:** #mcp-registry-support

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

### Implementação de Novos Endpoints

#### 1. Adicionar novo endpoint em `McpServerResource.java`

```java
@Path("/api/v1/mcp-servers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class McpServerResource {
    
    @Inject
    McpService mcpService;
    
    @POST
    @Path("/{id}/custom-action")
    @Transactional
    public Response executeCustomAction(
        @PathParam("id") String id,
        CustomActionRequest request) {
        
        try {
            McpServer result = mcpService.executeCustomAction(id, request);
            return Response.ok(result).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }
}
```

#### 2. Adicionar lógica em `McpService.java`

```java
@ApplicationScoped
public class McpService {
    
    @Inject
    McpServerRepository repository;
    
    public McpServer executeCustomAction(String id, CustomActionRequest request) {
        McpServer server = repository.findById(id);
        if (server == null) {
            throw new NotFoundException("MCP Server não encontrado: " + id);
        }
        
        // Implementar lógica de negócio
        server.setCustomField(request.getValue());
        server.setAtualizadoEm(LocalDateTime.now());
        
        return repository.persist(server);
    }
}
```

#### 3. Adicionar testes em `McpServerResourceTest.java`

```java
@QuarkusTest
public class McpServerResourceTest {
    
    @Test
    public void testCustomAction() {
        String mcpId = "test-id-123";
        CustomActionRequest request = new CustomActionRequest("value");
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/mcp-servers/{id}/custom-action", mcpId)
        .then()
            .statusCode(200);
    }
}
```

### Commits

```bash
# Format: type(scope): description
git commit -m "feat(mcp-server): add heartbeat monitoring"
git commit -m "fix(validation): improve field validation"
git commit -m "docs(api): update endpoint documentation"
git commit -m "chore(deps): update quarkus to 3.30.0"
```

### Commits Relacionados a Cronjobs

```bash
git commit -m "feat(cronjobs): implement heartbeat verification scheduler"
git commit -m "feat(cronjobs): implement inactivity check scheduler"
git commit -m "feat(notifications): add email alerts for status changes"
```

---

## ✅ Testes

### Executar Testes Unitários

```bash
./mvnw test
```

### Executar Testes de Integração

```bash
./mvnw verify
```

### Testes Específicos para Cronjobs

```bash
./mvnw test -Dtest=*SchedulerTest
```

### Cobertura de Testes

```bash
./mvnw clean test jacoco:report
```

Relatório gerado em: `target/site/jacoco/index.html`

### Exemplo de Teste para CronJob

```java
@QuarkusTest
public class HeartbeatSchedulerTest {
    
    @Inject
    McpServerRepository repository;
    
    @Inject
    HeartbeatVerificationScheduler scheduler;
    
    @Test
    public void testHeartbeatVerification() {
        // Arrange
        McpServer server = new McpServer();
        server.setNome("Test MCP");
        server.setStatus(Status.ATIVO);
        server.setUltimoHeartbeat(
            LocalDateTime.now().minusHours(13) // > 12h sem HB
        );
        repository.persist(server);
        
        // Act
        scheduler.verificarHeartbeats();
        
        // Assert
        McpServer updated = repository.findById(server.getId());
        assertEquals(Status.DEPRECADO, updated.getStatus());
    }
}
```

### Teste de Integração do Heartbeat

```java
@QuarkusTest
public class HeartbeatIT {
    
    @Test
    public void testHeartbeatEndpoint() {
        HeartbeatRequest request = new HeartbeatRequest();
        request.setVersao("1.0.0");
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/mcp-servers/507f1f77bcf86cd799439011/heartbeat")
        .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", containsString("Heartbeat registrado"));
    }
}
```

---

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

### Docker Compose (Desenvolvimento)

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
      QUARKUS_SCHEDULER_START_MODE: running
    depends_on:
      - mongodb

volumes:
  mongodb_data:
```

Executar:

```bash
docker-compose up -d
```

### Docker Compose (Produção com Prometheus/Grafana)

```yaml
version: '3.8'
services:
  mongodb:
    image: mongo:latest
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: ${MONGODB_PASSWORD}
    volumes:
      - mongodb_data:/data/db
    networks:
      - mcp-network

  mcp-registry:
    image: mcp-registry:latest
    ports:
      - "8080:8080"
    environment:
      QUARKUS_MONGODB_CONNECTION_STRING: mongodb://root:${MONGODB_PASSWORD}@mongodb:27017/
      QUARKUS_SCHEDULER_START_MODE: running
    depends_on:
      - mongodb
    networks:
      - mcp-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/q/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    networks:
      - mcp-network

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD}
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - mcp-network

volumes:
  mongodb_data:
  prometheus_data:
  grafana_data:

networks:
  mcp-network:
```

### Kubernetes

```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/cronjob-heartbeat.yaml
kubectl apply -f k8s/cronjob-inactivity.yaml
```

#### Exemplo de Deployment (k8s/deployment.yaml)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mcp-registry
spec:
  replicas: 2
  selector:
    matchLabels:
      app: mcp-registry
  template:
    metadata:
      labels:
        app: mcp-registry
    spec:
      containers:
      - name: mcp-registry
        image: mcp-registry:latest
        ports:
        - containerPort: 8080
        env:
        - name: QUARKUS_MONGODB_CONNECTION_STRING
          valueFrom:
            secretKeyRef:
              name: mcp-secrets
              key: mongodb-connection-string
        - name: QUARKUS_SCHEDULER_START_MODE
          value: "running"
        livenessProbe:
          httpGet:
            path: /q/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /q/health/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

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

Desenvolvido com ❤ por DARP Team

Última atualização: Novembro de 2025
