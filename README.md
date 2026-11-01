# Sistema de Gestão de Salas de Coworking

Projeto desenvolvido para a disciplina de Engenharia de Software, focado na criação de uma API REST utilizando um framework educacional customizado ("Mini Spring") e rodando no Java 21.

## 👥 Integrantes - Grupo 4
* Gabriel Cavalcante
* Hewerton Manoel
* Lucas Guilherme
* Marcos Vinicius
* Silvano Antonio

## 🏢 Descrição do Domínio
O sistema gerencia o cadastro e o controle de **Salas de Coworking**. A entidade principal (`Sala`) armazena as seguintes informações:
* `id`: Identificador único (UUID) gerado automaticamente.
* `nome`: Nome da sala (obrigatório, não pode ser vazio ou conter apenas espaços).
* `capacidadePessoas`: Capacidade máxima da sala (deve ser um valor numérico positivo).

## ⚖️ Regras de Negócio Implementadas
1. **Capacidade Máxima:** Nenhuma sala de coworking pode ser cadastrada ou atualizada com uma capacidade superior a **100 pessoas**. Caso esse limite seja excedido, a API rejeita a operação e retorna o status `422 Unprocessable Entity`.
2. **Validação de Entrada:** Tentativas de cadastro com nome vazio ou capacidade zerada/negativa retornam `400 Bad Request`.
3. **Persistência de ID:** O ID gerado na criação (`201 Created`) é preservado integralmente durante operações de atualização. Os dados são mantidos em memória durante a execução do servidor.

## 🚀 Instruções de Execução

### Pré-requisitos
* Java 21 instalado (JDK 21).
* Maven instalado.
* IDE (IntelliJ IDEA, VS Code ou Eclipse).

### Como rodar
1. Clone o repositório para a sua máquina local.
2. Abra a pasta do projeto na sua IDE de preferência.
3. Navegue até o ficheiro principal: `src/main/java/br/com/minispring/exemplo/Application.java`.
4. Execute o método `main()` da classe `Application`.
5. O servidor será iniciado e estará a escutar na porta `8080` (`http://localhost:8080`).

### Como testar
O projeto não utiliza interface gráfica (Frontend) ou Swagger. Todos os testes de validação, sucesso e erro estão mapeados no ficheiro **`requests.http`** localizado na raiz do projeto. 
Recomenda-se a utilização da extensão *REST Client* (VS Code) ou ferramentas como *Postman* / *Insomnia* para executar os testes interativos contra a rota `/salas`.

## 🛣️ Rotas da API

Todas as rotas respondem no caminho base `/salas`.

| Método | Rota | Descrição | Status de Sucesso |
| :--- | :--- | :--- | :--- |
| **POST** | `/salas` | Cadastra uma nova sala de coworking. | `201 Created` |
| **GET** | `/salas` | Lista todas as salas cadastradas. | `200 OK` |
| **GET** | `/salas?nome={termo}` | Filtra as salas por parte do nome (Ignorando Maiúsculas/Minúsculas). | `200 OK` |
| **GET** | `/salas/{id}` | Busca uma sala específica através do seu ID. | `200 OK` |
| **PUT** | `/salas/{id}` | Atualiza os dados de uma sala existente. | `200 OK` |
| **DELETE** | `/salas/{id}` | Remove uma sala do sistema. | `204 No Content` |