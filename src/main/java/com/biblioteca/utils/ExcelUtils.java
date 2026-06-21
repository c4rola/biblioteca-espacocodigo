package com.biblioteca.utils;

import com.biblioteca.dao.LivroDAO;
import com.biblioteca.model.Livro;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ExcelUtils {

    public static int importarLivros(String caminho) {
        LivroDAO dao = new LivroDAO();
        int contagemModificados = 0;

        try (FileInputStream fis = new FileInputStream(new File(caminho));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet aba = workbook.getSheetAt(0);

            for (int i = 1; i <= aba.getLastRowNum(); i++) {
                Row linha = aba.getRow(i);
                if (linha == null) continue;

                String codigo = getConteudoCelula(linha.getCell(0)).trim();
                String titulo = getConteudoCelula(linha.getCell(1)).trim();
                String autor = getConteudoCelula(linha.getCell(2)).trim();
                String tipo = getConteudoCelula(linha.getCell(3)).trim();

                if ("PERDIDO".equalsIgnoreCase(tipo)) {
                    continue;
                }

                if (codigo.isEmpty() || titulo.isEmpty()) continue;

                List<Livro> livrosExistentes = dao.buscarComFiltros(null, null, codigo, null);

                if (!livrosExistentes.isEmpty()) {

                    Livro livroExistente = livrosExistentes.get(0);
                    livroExistente.setTitulo(titulo);
                    livroExistente.setAutor(autor);

                    boolean atualizou = dao.atualizar(livroExistente);
                    if (atualizou) contagemModificados++;
                } else {

                    Livro novoLivro = new Livro(codigo, titulo, autor);
                    boolean salvou = dao.salvar(novoLivro);
                    if (salvou) contagemModificados++;
                }
            }
            return contagemModificados;

        } catch (IOException e) {
            System.err.println("Erro ao processar o arquivo Excel: " + e.getMessage());
            return -1;
        }
    }

    private static String getConteudoCelula(Cell celula) {
        if (celula == null) return "";

        switch (celula.getCellType()) {
            case STRING:
                return celula.getStringCellValue();
            case NUMERIC:
                double valor = celula.getNumericCellValue();
                if (valor == (long) valor) {
                    return String.format("%d", (long) valor);
                }
                return String.valueOf(valor);
            default:
                return "";
        }
    }
}