# Sistemas Distribuídos

Laboratório da disciplina de Sistemas Distribuídos (UTFPR): cliente e servidor TCP em Java que trocam mensagens JSON simples (`read` e `write`) para ler uma frase aleatória de um arquivo ou adicionar uma nova (`fortune-br.txt`).

## Como rodar

Requer JDK 17 ou superior. Em dois terminais, a partir de `a_lab2/src`:

```bash
javac Servidor.java Cliente.java
java Servidor    # porta 1025
java Cliente     # menu interativo
```

O servidor lê `fortune-br.txt` do diretório em que for executado. O código compila (verificado), mas é um exercício de aula: não há tratamento robusto de erros nem testes automatizados.
