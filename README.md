# Sistema de Gestão de Salas de Coworking (Spring Boot)

Projeto da disciplina de **Programação III**, ministrada pelo professor **Alisson Ferreira da Silva**.
Esta é a **migração para Spring Boot** da API que o grupo havia desenvolvido com o framework educacional *Mini Spring*. O tema, as funcionalidades e as regras de negócio foram mantidos.

> **Versão anterior (Mini Spring):** preservada no próprio repositório, na tag `v1-mini-spring` (e/ou na branch `mini-spring`), para comparação.

## Integrantes - Grupo 4
* Gabriel Cavalcante
* Hewerton Manoel
* Lucas Guilherme
* Marcos Vinicius
* Silvano Antonio

## Descrição do Domínio
O sistema gerencia o cadastro e o controle de **Salas de Coworking**. A entidade principal (`Sala`) possui:
* `id`: identificador único (UUID), gerado automaticamente pelo servidor.
* `nome`: nome da sala (obrigatório, sem aceitar vazio ou apenas espaços).
* `capacidadePessoas`: capacidade da sala (maior que zero e, por regra de negócio, no máximo 100).

## Regras de Negócio
1. **Capacidade máxima:** nenhuma sala pode ser cadastrada ou atualizada com mais de **100 pessoas**. Violação retorna `422 Unprocessable Entity`.
2. **Validação de entrada:** nome vazio/só espaços ou capacidade ausente, zero ou negativa retornam `400 Bad Request`. As mesmas validações valem no cadastro e na atualização.
3. **ID automático e preservado:** o ID é gerado no cadastro e mantido nas atualizações. O cliente não pode enviá-lo.
4. **Operação rejeitada não altera dados:** a validação ocorre antes de qualquer modificação.
5. **Armazenamento em memória:** os dados são perdidos ao reiniciar a aplicação.

## Como Executar

**Pré-requisitos:** JDK 21 e Maven.

```bash
# Rodar a aplicação (http://localhost:8080)
mvn spring-boot:run

# Rodar os testes
mvn test

# Gerar o JAR e executar
mvn clean package
java -jar target/salas-spring-boot-1.0.0.jar
```

Pela IDE (Eclipse/IntelliJ): execute o método `main` da classe `br.com.coworking.SalasApplication`.

Para testar a API, use o arquivo **`requests.http`** (extensão *REST Client* do VS Code) ou o Postman/Insomnia.

## Rotas da API

| Método | Rota | Descrição | Sucesso |
| :--- | :--- | :--- | :--- |
| POST | `/salas` | Cadastra uma sala | `201 Created` + header `Location` |
| GET | `/salas` | Lista todas as salas | `200 OK` |
| GET | `/salas?nome={termo}` | Filtra por parte do nome (ignora maiúsculas/minúsculas) | `200 OK` |
| GET | `/salas/{id}` | Busca uma sala pelo ID | `200 OK` |
| PUT | `/salas/{id}` | Atualiza uma sala | `200 OK` |
| DELETE | `/salas/{id}` | Remove uma sala | `204 No Content` |

Erros: `400` entrada inválida · `404` sala inexistente · `422` regra de negócio violada. As respostas de erro seguem o formato `ProblemDetail` (JSON do Spring).

## Estrutura do Projeto

```
src/main/java/br/com/coworking
├── SalasApplication.java      ponto de entrada (Spring Boot)
├── controller/                camada HTTP
├── service/                   regras de negócio
├── repository/                interface + implementação em memória
├── model/                     entidade Sala
├── dto/                       SalaInput (DTO de entrada)
└── exception/                 exceções de negócio e tratamento global de erros
```

## Principais Mudanças em Relação ao Mini Spring

| Responsabilidade | Mini Spring | Spring Boot |
| :--- | :--- | :--- |
| Inicialização e servidor | `MiniApplication` + Jetty configurado à mão | `@SpringBootApplication` + Tomcat embutido (auto-configuração) |
| Criação e injeção de objetos | `ComponentScanner` + `ApplicationContext` próprios | Container IoC do Spring (injeção por construtor mantida) |
| Rotas | `@Route(method, path)` + `Router` | `@RequestMapping`, `@GetMapping`, `@PostMapping`... |
| Dados da requisição | Anotações e *binding* próprios | `@PathVariable`, `@RequestParam`, `@RequestBody` do Spring MVC |
| Validação | `Validator` e anotações próprias | Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`, `@Positive`) |
| Respostas e erros | `HttpResult` e `HttpException` | `ResponseEntity` e `@RestControllerAdvice` |
| Configuração e build | `AppConfig` + plugin shade | `application.properties` + `spring-boot-maven-plugin` |
| Testes | Jetty real em porta livre | `@SpringBootTest` + `MockMvc` e teste unitário sem Spring |

O repository passou a ser sincronizado (`synchronized`) para ser seguro com várias requisições simultâneas.

## Uso de Inteligência Artificial
Foi utilizada a ferramenta de IA **Claude (Anthropic)** como apoio na migração do projeto, na escrita dos testes e da documentação. O código foi revisado pelo grupo, e todos os integrantes devem ser capazes de explicá-lo.
