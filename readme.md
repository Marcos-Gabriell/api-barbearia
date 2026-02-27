# 💈 Sistema de Gerenciamento de Barbearia

[![Java](https://img.shields.io/badge/Java-11-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Latest-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![Status](https://img.shields.io/badge/Status-Em_Desenvolvimento-yellow)](https://github.com)

> 🚧 **Projeto em Desenvolvimento Ativo**
> 
> **Backend**: API REST completa em Java/Spring Boot
> 
> **Frontend**: Em desenvolvimento com Angular 19

---

## 📖 Sobre o Projeto

O **Sistema de Gerenciamento de Barbearia** é uma API REST completa e moderna, desenvolvida para transformar a gestão operacional de barbearias. A solução oferece controle total sobre equipes, serviços, agendas e atendimentos, através de uma arquitetura robusta e escalável.

### 💡 Visão Geral

Este sistema foi projetado para resolver os principais desafios enfrentados por barbearias modernas:

- **Gestão de Equipe**: Controle hierárquico com permissões granulares (DEV, ADMIN, STAFF)
- **Catálogo Inteligente**: Gerenciamento de serviços com notificações automáticas para responsáveis
- **Agenda Automatizada**: Cada usuário possui agenda própria com controle de disponibilidade
- **Agendamentos Duplos**: Sistema híbrido que suporta agendamento interno (balcão) e externo (cliente)
- **Segurança Avançada**: Autenticação JWT com refresh token e sistema de convites controlados
- **Comunicação Automática**: Notificações e lembretes por e-mail em processos críticos

### 🎯 Diferenciais

**Controle Total da Operação:**
O sistema não é apenas um CRUD. Ele entende o contexto do negócio e automatiza processos:
- Barbeiros são notificados automaticamente quando adicionados/removidos de serviços
- Cada usuário possui uma agenda pré-configurada (segunda a sábado, 8h às 18h)
- Sistema de convites garante que apenas pessoas autorizadas acessem a plataforma
- Lembretes automáticos reduzem faltas e aumentam a taxa de comparecimento

**Hierarquia e Segurança:**
- **DEV** tem controle total e pode criar administradores
- **ADMIN** gerencia a barbearia e cria membros da equipe (STAFF)
- **STAFF** acessa apenas sua agenda e realiza atendimentos
- Todo acesso é validado por token JWT com rotação automática de refresh token

**Processamento Inteligente:**
- E-mails são processados em background (não bloqueiam a aplicação)
- Algoritmo de "diff" detecta mudanças em serviços e notifica apenas os afetados
- Dados estruturados para futuras análises e relatórios

---

## 🛠️ Tecnologias

### Backend
<div align="left">
  <img src="https://img.shields.io/badge/Java-11-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/Spring_Boot-2.7-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring_Security-2.7-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Lombok-Latest-BC4521?style=for-the-badge&logo=lombok&logoColor=white" alt="Lombok"/>
</div>

### Database
<div align="left">
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Hibernate-JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white" alt="Hibernate"/>
</div>

### DevOps & Infrastructure
<div align="left">
  <img src="https://img.shields.io/badge/Docker-Latest-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/Docker_Compose-Latest-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker Compose"/>
</div>

### Security & Communication
<div align="left">
  <img src="https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT"/>
  <img src="https://img.shields.io/badge/JavaMail-SMTP-EA4335?style=for-the-badge&logo=gmail&logoColor=white" alt="JavaMail"/>
</div>

### Frontend (Em Desenvolvimento)
<div align="left">
  <img src="https://img.shields.io/badge/Angular-19-DD0031?style=for-the-badge&logo=angular&logoColor=white" alt="Angular"/>
  <img src="https://img.shields.io/badge/TypeScript-Latest-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript"/>
</div>

---

## 📊 Status do Projeto

### 🎉 Etapa 1: Fundação e Segurança ✅ **CONCLUÍDA**

#### 🔐 Autenticação e Autorização
- ✅ Login com JWT (Access Token + Refresh Token)
- ✅ Access Token com expiração de 15 minutos
- ✅ Refresh Token com expiração de 7 dias e rotação automática
- ✅ Logout com invalidação de tokens
- ✅ Middleware de validação de tokens em todas as requisições

#### 👥 Sistema de Convites
- ✅ Criação de usuários exclusivamente por convite
- ✅ Token de ativação único com validade de 24 horas
- ✅ Link de ativação enviado por e-mail
- ✅ Ativação de conta com definição de senha
- ✅ Expiração automática de convites não utilizados

#### 🔑 Recuperação de Senha
- ✅ Solicitação de recuperação por e-mail
- ✅ Código de verificação de 6 dígitos
- ✅ Validade de 15 minutos para o código
- ✅ Reset de senha seguro

---

### 🎉 Etapa 2: Gestão de Usuários ✅ **CONCLUÍDA**

#### 👤 Hierarquia de Permissões (RBAC)
- ✅ **DEV**: Controle total, único que pode criar ADMINs
- ✅ **ADMIN**: Gerencia a barbearia, cria e controla STAFF
- ✅ **STAFF**: Acesso à agenda e atendimentos

#### 📋 Funcionalidades de Gestão
- ✅ CRUD completo de usuários (DEV e ADMIN)
- ✅ Listagem com filtros e paginação
- ✅ Validação de permissões em cada operação
- ✅ Soft delete (exclusão lógica)

#### 🔧 Auto-Gestão de Perfil
- ✅ Usuário pode alterar seus próprios dados (nome, telefone, e-mail)
- ✅ Alteração de senha (com validação da senha atual)
- ✅ Notificações automáticas de segurança por e-mail
- ✅ Validação de dados críticos

---

### 🎉 Etapa 3: Catálogo Inteligente ✅ **CONCLUÍDA**

#### 📝 Gestão de Serviços
- ✅ CRUD completo de serviços
- ✅ Campos: nome, descrição, preço, duração
- ✅ Atribuição de múltiplos responsáveis (barbeiros)
- ✅ Validação de dados e permissões

#### 🧠 Notificações Inteligentes
- ✅ Algoritmo de comparação (diff) de mudanças na equipe
- ✅ Notificação automática quando barbeiro é adicionado ao serviço
- ✅ Notificação automática quando barbeiro é removido do serviço
- ✅ Notificação de atualização apenas se houver mudança de preço/duração
- ✅ Processamento assíncrono de e-mails (não bloqueia a aplicação)

---

### 🎉 Etapa 4: Sistema de Agenda ✅ **CONCLUÍDA**

#### 📅 Agenda Padrão
- ✅ Cada usuário recebe agenda automaticamente ao ser criado
- ✅ Configuração padrão: Segunda a Sábado, 8h às 18h
- ✅ Domingo inativo por padrão
- ✅ Todos os dias da semana obrigatórios (não podem ser excluídos)

#### ⚙️ Controle de Disponibilidade
- ✅ Ativar/desativar dias específicos
- ✅ Alterar horário de início e fim por dia
- ✅ Pausas configuráveis (até 3 por dia)
- ✅ Validação de conflitos de horários
- ✅ Controle hierárquico (DEV controla todos, ADMIN controla STAFF)

---

### 🚧 Etapa 5: Sistema de Agendamentos **EM DESENVOLVIMENTO**

#### 📌 Agendamento Interno (Balcão)
- [ ] Criação de agendamento pelo ADMIN/STAFF
- [ ] Seleção de cliente (cadastrado ou avulso)
- [ ] Seleção de serviço e barbeiro
- [ ] Validação de disponibilidade em tempo real
- [ ] Confirmação automática

#### 🌐 Agendamento Externo (Cliente)
- [ ] Cadastro simplificado do cliente (sem login obrigatório)
- [ ] Seleção de serviço disponível
- [ ] Visualização de horários disponíveis
- [ ] Confirmação do agendamento
- [ ] E-mail de confirmação com link de cancelamento

#### 🔍 Gestão de Agendamentos
- [ ] Listagem com filtros (data, status, barbeiro, cliente)
- [ ] Paginação e ordenação
- [ ] Filtro por status: Pendente, Confirmado, Cancelado, Concluído
- [ ] Alteração de status
- [ ] Cancelamento via link tokenizado (enviado por e-mail)

#### 🔔 Notificações e Lembretes
- [ ] E-mail de confirmação ao criar agendamento
- [ ] Lembrete automático 24 horas antes
- [ ] Lembrete automático 2 horas antes
- [ ] Notificação de cancelamento
- [ ] Notificação de alteração

---

### 📋 Etapa 6: Dashboard Inteligente **PLANEJADO**

#### 📈 Métricas e Análises
- [ ] Serviços mais vendidos
- [ ] Performance por barbeiro (quantidade de atendimentos)
- [ ] Taxa de ocupação da agenda
- [ ] Horários de pico
- [ ] Análise de faturamento por período
- [ ] Taxa de cancelamento
- [ ] Receita por barbeiro
- [ ] Comparativo mensal/semanal

#### 📊 Visualizações
- [ ] Gráficos interativos
- [ ] Exportação de relatórios (PDF/Excel)
- [ ] Filtros personalizados
- [ ] Dashboard customizável

---

### 📋 Etapa 7: Qualidade e Infraestrutura **PLANEJADO**

#### 🧪 Testes
- [ ] Testes unitários (cobertura > 80%)
- [ ] Testes de integração
- [ ] Testes de segurança
- [ ] Testes de carga e performance

#### 🚀 CI/CD
- [ ] Pipeline de integração contínua
- [ ] Testes automatizados no pipeline
- [ ] Deploy automatizado
- [ ] Versionamento semântico
- [ ] Documentação automática da API

---

## 🚀 Instalação e Execução

### Pré-requisitos

```
☕ Java 11 ou superior
🐳 Docker & Docker Compose
📦 Maven 3.6+
```

### Configuração

**1. Clone o repositório**
```bash
git clone https://github.com/Marcos-Gabriell/api-barbearia.git
cd barbershop-backend
```

**2. Configure as variáveis de ambiente**

Crie o arquivo `.env` na raiz do projeto:

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=barbershop_db
DB_USER=postgres
DB_PASSWORD=sua_senha_segura

# JWT
JWT_SECRET=sua-chave-secreta-muito-segura-mude-isso
JWT_EXPIRATION=900000
REFRESH_TOKEN_EXPIRATION=604800000

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=seu-email@gmail.com
SMTP_PASSWORD=sua-senha-de-app
```

**3. Inicie o banco de dados**
```bash
docker-compose up -d postgres
```

**4. Execute a aplicação**
```bash
./mvnw spring-boot:run
```

**5. Acesse a API**
```
http://localhost:8080
```

---

## 🏗️ Arquitetura

### Princípios Arquiteturais

**Separação de Responsabilidades:**
- Controllers apenas expõem endpoints
- Services contêm lógica de negócio
- Repositories acessam dados
- DTOs para transferência de dados

**Event-Driven:**
- Notificações desacopladas via eventos
- Processamento assíncrono de e-mails

**Segurança em Camadas:**
- Validação de entrada (Bean Validation)
- Autenticação JWT
- Autorização RBAC
- Tokens de uso único

---

## 🔒 Segurança

### Autenticação
- JWT com assinatura HMAC-SHA256
- Access Token de curta duração (15 min)
- Refresh Token de longa duração (7 dias) com rotação

### Autorização
- RBAC granular (DEV > ADMIN > STAFF)
- Validação de permissões em cada endpoint
- Proteção contra escalada de privilégios

### Proteções
- Tokens de uso único (convites, recuperação)
- Validação rigorosa de entrada
- Headers de segurança (CORS, CSP)
- Proteção contra CSRF
- Rate limiting (planejado)

---

## 📧 Sistema de Notificações

### E-mails Transacionais

**Autenticação:**
- Convite de novo usuário (com link de ativação)
- Ativação de conta confirmada
- Recuperação de senha (código de verificação)
- Alterações de segurança no perfil

**Catálogo:**
- Barbeiro adicionado a um serviço
- Barbeiro removido de um serviço
- Atualização de serviço (preço/duração)

**Agendamentos (planejado):**
- Confirmação de agendamento
- Lembrete 24h antes
- Lembrete 2h antes
- Cancelamento de agendamento

### Templates
- HTML responsivo
- Design profissional
- Links tokenizados e seguros
- Processamento assíncrono

---

## 🤝 Contribuindo

Contribuições são muito bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

### Padrões de Código
- Seguir convenções Java/Spring Boot
- Documentar métodos públicos
- Manter cobertura de testes alta
- Code review obrigatório

---

## 📞 Suporte

Para dúvidas, sugestões ou reportar bugs, abra uma [issue](../../issues) no repositório.

---
