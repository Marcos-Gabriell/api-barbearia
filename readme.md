# 💈 Sistema Inteligente de Gerenciamento e Agendamentos para Barbearia (Back-end)

Backend de um sistema **inteligente e estratégico** de gerenciamento e agendamentos para barbearias, projetado para organizar a operação diária, otimizar o uso de dados e apoiar o crescimento do negócio.

> 🚧 **Projeto em desenvolvimento**
> Este repositório contém o **back-end (API)**.
> O front-end está sendo desenvolvido em **Angular 19**.

---

## 📍 Status Atual do Projeto: Fase de Fundação Concluída

Estamos no estágio de **Consolidação da Segurança e Gestão de Identidade**.
Acabamos de finalizar a refatoração completa do sistema de autenticação, implementando fluxos seguros de convite, recuperação de conta e hierarquia de permissões.

**O que foi entregue recentemente:**
- ✅ Sistema de Autenticação Robusto (Token + Refresh Token)
- ✅ Gestão de Usuários por Convite (E-mail)
- ✅ Painel "Meu Perfil" (Auto-gestão de dados)
- ✅ Hierarquia de Acesso (RBAC)

---

## 🎯 Objetivo do Projeto

Criar uma **API moderna, segura e escalável** que permita à barbearia controlar agendamentos, equipe e métricas, sendo **100% orientada a dados**.

---

## 🔐 Segurança e Gestão de Identidade (Novo)

O sistema conta agora com um módulo de segurança avançado:

### 🛡️ Autenticação e Sessão
- **JWT & Refresh Token:** Implementação completa de tokens de acesso com rotação de refresh token para maior segurança.
- **Validação de Token:** Middleware de segurança para validação de integridade e expiração.

### 👥 Hierarquia de Papéis (RBAC)
O sistema possui 3 níveis de acesso bem definidos:

1.  **💻 DEV (Supremo):**
    - Acesso irrestrito.
    - Único capaz de criar administradores.
2.  **👑 ADMIN:**
    - Gerencia a barbearia.
    - Cria e gerencia membros da equipe (STAFF).
    - Acesso a relatórios e configurações.
3.  **✂️ STAFF:**
    - Visualiza agenda e realiza atendimentos.
    - Dados limitados à operação diária.

### 📩 Fluxo de Cadastro e Convites
Não há cadastro público aberto. O acesso é controlado via convite:
1.  Admin envia um **convite por e-mail** para o novo usuário.
2.  O e-mail contém um **Token de Ativação** (validade de 24h).
3.  O usuário acessa o link, define sua senha e ativa a conta.

### 👤 Área do Usuário (Meus Dados)
- **Auto-gestão:** O usuário logado pode alterar seu próprio nome, telefone, e-mail e senha.
- **Segurança:** Toda alteração de dados sensíveis dispara uma **notificação de segurança por e-mail**.
- **Recuperação:** Fluxo de "Esqueci minha senha" com envio de código de verificação.

---

## 🧩 Funcionalidades Gerais

### 📌 Agendamentos (Em Breve)
- Visualização de agenda (Dia/Semana).
- Cancelamento seguro via link (Tokenizado).
- Bloqueio de horários.

### 📊 Dashboard e Relatórios
- Análise de performance e serviços mais vendidos.

### ✉️ Notificações
- Padronização de templates de e-mail (HTML) para:
    - Boas-vindas/Convite.
    - Recuperação de senha.
    - Aviso de alteração de dados.
    - Agendamentos (Futuro).

---

## 🛠️ Tecnologias Utilizadas

### Back-end
- **Java 17+**
- **Spring Boot 3**
- **Spring Security** (Gerenciamento de acesso avançado)
- **JWT** (Access + Refresh Token)
- **Java Mail Sender** (Envio de e-mails transacionais)
- **JPA / Hibernate**
- **PostgreSQL**
- **Docker**
- **Lombok**

---

## 🗺️ Linha do Tempo do Desenvolvimento

- ✅ **Estrutura inicial do projeto**
- ✅ **Configuração do Spring Boot & Docker**
- ✅ **Modelagem de Banco de Dados**
- ✅ **Módulo de Segurança (Auth/Refresh Token)**
- ✅ **Módulo de Usuários (CRUD Completo)**
- ✅ **Fluxo de Convites e E-mails Transacionais**
- ✅ **Funcionalidade "Esqueci Minha Senha" & "Meu Perfil"**
- 🚧 **Catálogo de Serviços** *(Próximo Passo)*
- 🚧 **Módulo de Agendamentos**
- 🔜 **Dashboard inteligente**
- 🔜 **Integração com WhatsApp (Planejado)**

---

## 🤝 Contribuição

Sugestões e melhorias são bem-vindas.

## 📄 Licença

Este projeto está sob licença **MIT**.
