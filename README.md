# Library Project JDBC

Aplicação de linha de comando para gerenciamento de biblioteca, com foco em operações de usuários, livros e empréstimos.  
O projeto foi desenvolvido para praticar Java com JDBC, aplicação do padrão Repository/DAO e separação entre persistência e regras de negócio.

## Objetivo

Disponibilizar uma base simples e organizada para operações de biblioteca em ambiente local, incluindo:

- cadastro e manutenção de usuários;
- cadastro e manutenção de livros;
- controle de empréstimos e devoluções.

## Tecnologias

- Java 21
- Maven
- MySQL 8+
- JDBC (MySQL Connector/J)

## Pré-requisitos

Antes de executar o projeto, certifique-se de ter:

- JDK 21 instalado;
- MySQL em execução;
- Maven disponível no ambiente (ou IDE com suporte a projetos Maven).

## Configuração do banco de dados

1. Crie um banco no MySQL (exemplo: `library`).
2. Execute o script `src/main/resources/schema.sql`.
3. Crie o arquivo `src/main/resources/db.properties` com as credenciais locais:

```properties
durl=jdbc:mysql://localhost:3306/library
user=seu_usuario
******
```

> O arquivo `db.properties` está no `.gitignore` para evitar versionamento de credenciais.

## Como executar

Você pode executar a aplicação pela classe principal:

- `application.Program`

Ao iniciar, o menu de terminal permite:

- cadastrar, buscar, listar, atualizar e remover usuários;
- cadastrar, buscar, listar, atualizar e remover livros;
- registrar empréstimos, devoluções e consultas de empréstimos.

## Estrutura do projeto

- `application`: ponto de entrada e interface de terminal.
- `database`: fábrica de conexão JDBC.
- `model`: entidades de domínio.
- `repository`: contratos e implementações de acesso a dados (JDBC/DAO).
- `services`: regras de negócio e validações.
- `exception`: exceções de domínio.
- `test`: classes de teste para repositórios e serviços.
