package com.biblioteca.utils;

import com.biblioteca.dao.Conexao;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;

public class EmailService {

    private String username;
    private String password;

    private void carregarCredenciais() {
        String sql = "SELECT email_remetente, senha_app FROM configuracoes WHERE id = 1";
        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                this.username = rs.getString("email_remetente");
                this.password = rs.getString("senha_app");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar credenciais: " + e.getMessage());
        }
    }

    public void enviarConfirmacaoEmprestimo(String destinatario, String nomeUsuario, String tituloLivro, String dataDevolucao) {
        carregarCredenciais();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            System.err.println("ERRO: E-mail ou Senha de App não configurados no banco de dados.");
            return;
        }

        LocalDate data = LocalDate.parse(dataDevolucao);
        String dataBR = DateUtils.format(data);

        new Thread(() -> {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username, "Grupo Código"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
                message.setSubject("📖 Confirmação de Empréstimo - " + tituloLivro);
                String corpoHTML = "<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>"
                        + "<div style='background-color: #003366; color: white; padding: 20px; text-align: center;'>"
                        + "  <h1>Biblioteca Marcus Félix</h1>"
                        + "</div>"
                        + "<div style='padding: 20px; border: 1px solid #ddd; border-top: none;'>"
                        + "  <p>Olá, <strong>" + nomeUsuario + "</strong>,</p>"
                        + "  <p>Confirmamos a retirada do seguinte item do nosso acervo:</p>"
                        + "  <div style='background-color: #f9f9f9; padding: 15px; border-left: 5px solid #003366; margin: 20px 0;'>"
                        + "    <strong>Livro:</strong> " + tituloLivro + "<br>"
                        + "    <strong>Data Prevista para Devolução:</strong> <span style='color: #d9534f; font-weight: bold;'>" + dataBR + "</span>"
                        + "  </div>"
                        + "  <p>Lembre-se: a devolução no prazo evita bloqueio de perfil e permite que outras pessoas utilizem o material.</p>"
                        + "  <hr style='border: 0; border-top: 1px solid #eee;'>"
                        + "  <p style='font-size: 12px; color: #777;'>Este é um e-mail automático, por favor não responda.</p>"
                        + "</div>"
                        + "</body></html>";
                message.setContent(corpoHTML, "text/html; charset=utf-8");

                Transport.send(message);
                System.out.println("E-mail profissional enviado!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
