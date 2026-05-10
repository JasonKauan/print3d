# Print3D

Sistema de gerenciamento para entidades e associações que utilizam impressoras 3D de forma compartilhada.

O objetivo do projeto é centralizar o controle de membros, produção, catálogo de produtos, estoque, vendas e repasses financeiros em uma única plataforma web responsiva.

---

# 📌 Sobre o Projeto

O Print3D foi criado para resolver problemas comuns em entidades que trabalham com produção compartilhada em impressoras 3D:

* Controle de membros ativos e inativos
* Registro de quem produziu cada item
* Controle de estoque
* Catálogo visual com fotos obrigatórias
* Controle financeiro e repasses automáticos
* Histórico de vendas
* Painel administrativo com métricas em tempo real

O sistema foi pensado para funcionar tanto no computador quanto no celular.

---

# 🚀 Tecnologias Utilizadas

## Backend

* Java 17
* Spring Boot
* Spring Security
* JWT Authentication
* JPA / Hibernate
* PostgreSQL

## Frontend

* React
* TailwindCSS
* Zustand
* Vite

## Outros

* Cloudinary (upload de imagens)
* REST API
* PWA (Progressive Web App)

---

# 🧩 Funcionalidades

## 📊 Dashboard

* Total de membros ativos
* Quantidade de impressões registradas
* Total vendido
* Repasses pendentes
* Produtos cadastrados
* Últimas impressões registradas

---

## 👥 Gestão de Membros

* Cadastro de membros
* Edição de dados
* Remoção de membros
* Status ativo/inativo
* Controle de entrada e saída

### Campos

* Nome
* Email
* Status
* Data de entrada
* Data de saída

---

## 🖨️ Controle de Impressões

Registro de toda produção realizada nas impressoras.

### Campos

* Membro responsável
* Produto produzido
* Quantidade
* Tempo de impressão
* Data
* Observações

---

## 🛍️ Catálogo de Produtos

Catálogo visual com imagens obrigatórias.

### Recursos

* Upload de imagem
* Captura via câmera no celular
* Controle de estoque
* Valor do produto
* Descrição

### Campos

* Nome
* Descrição
* Foto
* Preço
* Estoque

---

## 💰 Financeiro

Controle completo de vendas e repasses.

### Regras de negócio

O sistema calcula automaticamente:

* 70% → produtor
* 30% → entidade

### Recursos

* Registro de vendas
* Controle de repasses
* Status pendente/pago
* Resumo financeiro por membro

---

# 🗂️ Estrutura do Projeto

## Backend

```bash
src/
├── main/java/com/print3d/
│
├── config/
├── controller/
├── service/
├── repository/
├── dto/
├── model/
├── security/
└── Print3dApplication.java
```

---

## Frontend

```bash
src/
├── components/
├── pages/
├── services/
├── hooks/
├── store/
└── utils/
```

---

# 🛢️ Banco de Dados

## Tabelas principais

### membros

```sql
id
nome
email
status
data_entrada
data_saida
criado_em
```

### produtos

```sql
id
nome
descricao
foto_url
preco
estoque
criado_em
```

### impressoes

```sql
id
membro_id
produto_nome
quantidade
tempo_impressao
data_impressao
observacao
```

### vendas

```sql
id
membro_id
produto_nome
quantidade
valor_total
repasse
data_venda
status
```

---

# 🔐 Autenticação

O sistema utiliza autenticação JWT.

### Fluxo

1. Usuário faz login
2. Backend gera token JWT
3. Frontend armazena token
4. Todas requisições usam:

```http
Authorization: Bearer TOKEN
```

---

# 🌐 Endpoints da API

## Auth

```http
POST /auth/login
POST /auth/refresh
```

## Membros

```http
GET    /membros
GET    /membros/{id}
POST   /membros
PUT    /membros/{id}
DELETE /membros/{id}
```

## Produtos

```http
GET    /produtos
GET    /produtos/{id}
POST   /produtos
PUT    /produtos/{id}
DELETE /produtos/{id}
```

## Impressões

```http
GET    /impressoes
POST   /impressoes
PUT    /impressoes/{id}
DELETE /impressoes/{id}
```

## Financeiro

```http
GET    /vendas
GET    /vendas/resumo
POST   /vendas
PATCH  /vendas/{id}/status
```

---

# 📸 Upload de Imagens

As imagens dos produtos são armazenadas no Cloudinary.

## Fluxo

1. Usuário seleciona/tira foto
2. Frontend envia multipart/form-data
3. Backend faz upload no Cloudinary
4. URL é salva no banco
5. Produto aparece no catálogo

---

# ⚙️ Como Rodar o Projeto

# Pré-requisitos

* Java 17+
* Node.js 18+
* PostgreSQL
* Conta no Cloudinary

---

# Backend

## Clone o projeto

```bash
git clone https://github.com/JasonKauan/print3d
```

## Configure o application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/print3d
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA

cloudinary.cloud-name=SEU_CLOUD_NAME
cloudinary.api-key=SUA_API_KEY
cloudinary.api-secret=SEU_API_SECRET
```

## Rode a aplicação

```bash
./mvnw spring-boot:run
```

Backend:

```bash
http://localhost:8080
```

---

# Frontend

## Instale as dependências

```bash
npm install
```

## Configure a URL da API

```env
VITE_API_URL=http://localhost:8080/api/v1
```

## Rode o frontend

```bash
npm run dev
```

Frontend:

```bash
http://localhost:5173
```

---

# 📱 Responsividade

O sistema foi desenvolvido com foco em mobile-first:

* Compatível com celular
* Upload usando câmera traseira
* Interface responsiva
* Estrutura preparada para PWA

---

# 🔮 Roadmap

## MVP

* [x] Gestão de membros
* [x] Registro de impressões
* [x] Catálogo com fotos
* [x] Financeiro
* [x] Dashboard

## Próximas versões

* [ ] Controle de múltiplas impressoras
* [ ] Relatórios em PDF
* [ ] Notificações via WhatsApp
* [ ] Histórico de estoque
* [ ] App mobile
* [ ] Relatórios avançados

---

# 🧠 Objetivo do Projeto

O Print3D busca simplificar o gerenciamento de produção compartilhada em impressoras 3D, oferecendo uma plataforma simples, visual e acessível mesmo para usuários com baixo nível técnico.

---

# 📄 Licença

Este projeto está sob a licença MIT.
