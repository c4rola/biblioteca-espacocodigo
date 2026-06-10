package com.biblioteca.utils;

import com.biblioteca.dao.LivroDAO;
import com.biblioteca.model.Livro;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVUtils {

    public static void importarLivros(String caminho) {
        LivroDAO dao = new LivroDAO();
        String linha;
        String divisor = ";";

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            br.readLine();

            while ((linha = br.readLine()) != null) {
                String[] colunas = linha.split(divisor);

                if (colunas.length >= 3) {
                    String codigo = colunas[0].trim();
                    String titulo = colunas[1].trim();
                    String autor = colunas[2].trim();

                    Livro livro = new Livro(codigo, titulo, autor);
                    dao.salvar(livro);
                }
            }
            System.out.println("Importação do CSV finalizada com sucesso!");

        } catch (IOException e) {
            System.err.println("Erro ao processar o arquivo CSV: " + e.getMessage());
        }
    }
}