# Indexing Documents to a Vector Store with Azure Document Intelligence and Spring AI

## Overview
This application uses Azure Document Intelligence to analyze documents and index the data into a vector store using Spring AI.

## Dependencies
The following key dependencies are used in this project:
- Spring Boot Starter Web
- Azure AI Form Recognizer (version 4.1.9)
- Spring AI PGVector Store Spring Boot Starter
- Spring AI Ollama Spring Boot Starter
- Spring AI OpenAI Spring Boot Starter

## Prerequisites
1. Azure Document Intelligence service is configured in Azure.
2. A local vector store is set up. This application uses a Postgres database with the pgvector extension.

## Steps to Run the Application
1. Clone the repository:
    ```sh
    git clone <repository-url>
    cd azure-document-intelligence-indexer
    ```

2. Set up environment variables for Azure Document Intelligence:
    ```sh
    export DOCUMENT_INTELLIGENCE_ENDPOINT=<your-endpoint>
    export DOCUMENT_INTELLIGENCE_KEY=<your-key>
    ```

3. Ensure that your Postgres database with the pgvector extension is running and accessible.

4. Update the `application.yml` file with the specific profile

5. Build and run the application using Maven:
    ```sh
    mvn clean install
    mvn spring-boot:run
    ```

6. The application will start and be accessible at `http://localhost:8080`.