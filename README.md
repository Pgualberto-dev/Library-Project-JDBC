# Library JDBC

Aplicação de terminal para gerenciar usuários, livros e empréstimos de uma biblioteca. O projeto foi criado para praticar Java, JDBC, padrão Repository/DAO e uma camada de regras de negócio antes de avançar para Spring.

## Requisitos

- Java 21
- MySQL 8+
- Maven, ou IntelliJ IDEA com suporte a Maven

## Configuração do banco

1. Crie um banco MySQL, por exemplo `library`.
2. Execute `src/main/resources/schema.sql` nesse banco.
3. Crie `src/main/resources/db.properties` com as credenciais locais:

```properties
durl=jdbc:mysql://localhost:3306/library
user=seu_usuario
password=sua_senha
```

O arquivo `db.properties` é ignorado pelo Git para não publicar credenciais.

## Executando

Execute a classe `application.Program` pela IDE. O menu de terminal permite cadastrar, buscar, listar, atualizar e excluir usuários e livros, além de criar, devolver e consultar empréstimos.

## Estrutura

- `model`: entidades do domínio.
- `repository`: contratos e implementações JDBC (DAO).
- `services`: validações e regras de negócio.
- `application`: inicialização e interface de terminal.
- `test`: testes manuais de repositórios e serviços.
