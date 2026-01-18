# 💈 Sistema Inteligente de Gerenciamento e Agendamentos para Barbearia (Back-end)

Backend de um sistema **inteligente e estratégico** de gerenciamento e agendamentos para barbearias, projetado para organizar a operação diária, otimizar o uso de dados e apoiar o crescimento do negócio com decisões mais eficientes.

> 🚧 **Projeto em desenvolvimento**  
> Este repositório representa **exclusivamente o back-end da aplicação**.  
> O **front-end será desenvolvido com Angular 19**.

---

## 🎯 Objetivo do Projeto

Criar uma **API moderna, segura e escalável** que permita à barbearia:

- Controlar totalmente seus agendamentos
- Oferecer agendamento online simples para clientes
- Organizar serviços, horários e equipe
- Utilizar dados operacionais para melhorar performance
- Crescer com base em informações reais e estratégicas

O sistema foi pensado para ser **100% orientado a dados**, permitindo evolução contínua conforme o uso.

---

## 🧩 Visão Geral do Sistema

O sistema funciona em dois grandes fluxos integrados:

### 🔒 Fluxo Interno (Barbearia)
- Acesso autenticado para administradores e equipe
- Gestão completa da agenda
- Organização de serviços e horários
- Dashboard inteligente e relatórios estratégicos

### 🌐 Fluxo Externo (Cliente)
- Agendamento rápido e intuitivo
- Confirmações e lembretes automáticos
- Cancelamento facilitado e seguro

---

## 👥 Tipos de Usuário

### 👑 Administrador (ADMIN)
- Controle total do sistema
- Gestão de usuários
- Configuração de serviços e horários
- Acesso completo a dashboards e relatórios

### ✂️ Staff (STAFF)
- Visualização da agenda
- Acompanhamento dos agendamentos
- Cancelamento e finalização de atendimentos
- Comunicação com clientes

---

## 🗓️ Funcionalidades Principais

### 📌 Agendamentos
- Visualização da agenda por:
    - Dia
    - Semana
    - Período personalizado
- Criação manual de agendamentos
- Cancelamento e finalização de atendimentos
- Bloqueio de datas e faixas de horário específicas

### 🧾 Catálogo de Serviços
- Cadastro de serviços com:
    - Nome
    - Duração
    - Preço
- Ativação e desativação de serviços
- Definição de horários disponíveis por serviço

---

## 📊 Dashboard Inteligente

Dashboard desenvolvido para **análise estratégica**, contendo:

- Total de agendamentos do dia
- Serviços mais realizados
- Quantidade de cancelamentos
- Visão geral da operação

> ⚠️ O sistema **não possui módulo financeiro**.  
> O foco é **controle de agenda e performance operacional**.

---

## 📈 Relatórios

Relatórios detalhados de agendamentos por:

- Dia
- Semana
- Mês
- Ano

Esses dados permitem identificar padrões, horários mais movimentados e oportunidades de melhoria.

---

## ✉️ Comunicação por E-mail

O sistema enviará e-mails automáticos para clientes e barbeiros:

- Confirmação do agendamento
- Notificação para o barbeiro
- Lembrete ao cliente **30 minutos antes do atendimento**
- Notificação de cancelamento

---

## 🔗 Cancelamento via Link Seguro

- O **link de cancelamento** será enviado **junto com o e-mail de confirmação do agendamento**
- Link único e seguro, vinculado ao agendamento
- O cliente poderá cancelar **até 30 minutos antes do horário marcado**
- Após o cancelamento:
    - O barbeiro será notificado
    - O cliente poderá entrar em contato via WhatsApp, se desejar

O barbeiro também poderá cancelar o agendamento, e o cliente será informado com a mensagem definida pelo profissional.

---

## 🔐 Segurança e Autenticação

- Autenticação com **JWT**
- Controle de acesso por perfil (ADMIN / STAFF)
- Proteção de rotas com **Spring Security**
- Arquitetura preparada para crescimento e escalabilidade

---

## 🛠️ Tecnologias Utilizadas

### Back-end
- **Java 11**
- **Spring Boot**
- **Spring Security**
- **JWT (JSON Web Token)**
- **JPA / Hibernate**
- **PostgreSQL**
- **Docker**
- **Maven**

### Front-end (Planejado)
- **Angular 19**

---

## 🐳 Containerização

O projeto utiliza **Docker** para:
- Padronização do ambiente
- Facilidade de execução
- Integração com banco PostgreSQL

---

## 🗺️ Linha do Tempo do Desenvolvimento

- ✅ Estrutura inicial do projeto
- ✅ Configuração do Spring Boot
- ✅ Autenticação e autorização (JWT + Security)
- ✅ Modelagem de usuários e papéis
- 🚧 Módulo de agendamentos
- 🚧 Catálogo de serviços
- 🚧 Dashboard inteligente
- 🚧 Relatórios estratégicos
- 🔜 Envio de e-mails
- 🔜 Desenvolvimento do front-end Angular 19

---

## 📌 Status do Projeto

🚧 **Em desenvolvimento ativo**

O sistema está sendo construído com foco em:
- Organização
- Performance
- Inteligência de dados
- Evolução contínua do negócio

---

## 🤝 Contribuição

Sugestões e melhorias são bem-vindas.  
Pull requests e issues podem ser abertos conforme a evolução do projeto.

---

## 📄 Licença

Este projeto está sob licença **MIT**.
