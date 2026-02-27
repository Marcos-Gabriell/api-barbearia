package br.com.barbearia.apibarbearia.appointment.service;

import br.com.barbearia.apibarbearia.appointment.entity.Appointment;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import br.com.barbearia.apibarbearia.appointment.repository.AppointmentRepository;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AppointmentPdfService {

    private final AppointmentRepository appointmentRepository;

    @Value("${app.business.name:Barbearia Premium}")
    private String businessName;

    @Value("${app.business.tagline:Estilo & Precisão}")
    private String businessTagline;

    @Value("${app.business.cnpj:00.000.000/0001-00}")
    private String businessCnpj;

    private static final Color C_DARK   = new Color(33, 37, 41);
    private static final Color C_GRAY   = new Color(108, 117, 125);
    private static final Color C_LINE   = new Color(222, 226, 230);
    private static final Color C_ACCENT = new Color(16, 185, 129);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Font fTitle() { return new Font(Font.HELVETICA, 14, Font.BOLD, C_DARK); }
    private Font fSub()   { return new Font(Font.HELVETICA, 9, Font.NORMAL, C_GRAY); }
    private Font fLbl()   { return new Font(Font.HELVETICA, 8, Font.BOLD, C_GRAY); }
    private Font fVal()   { return new Font(Font.HELVETICA, 10, Font.NORMAL, C_DARK); }
    private Font fStatus(Color c) { return new Font(Font.HELVETICA, 10, Font.BOLD, c); }

    // BUSCA POR ID
    public byte[] generateReceipt(Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));
        return buildPdf(a);
    }

    // BUSCA POR CÓDIGO (O que estava faltando)
    public byte[] generateReceiptByCode(String code) {
        Appointment a = appointmentRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado com código: " + code));
        return buildPdf(a);
    }

    private byte[] buildPdf(Appointment a) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 45, 45, 45, 45);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // HEADER COM LOGO
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1, 2});

            try {
                InputStream is = new ClassPathResource("static/images/logo.png").getInputStream();
                Image logo = Image.getInstance(is.readAllBytes());
                logo.scaleToFit(70, 70);
                PdfPCell logoCell = new PdfPCell(logo);
                logoCell.setBorder(Rectangle.NO_BORDER);
                header.addCell(logoCell);
            } catch (Exception e) {
                header.addCell(cellNoBorder(new Phrase(businessName, fTitle())));
            }

            PdfPCell infoEmpresa = new PdfPCell();
            infoEmpresa.setBorder(Rectangle.NO_BORDER);
            infoEmpresa.addElement(p(businessName.toUpperCase(), fTitle(), Element.ALIGN_RIGHT));
            infoEmpresa.addElement(p(businessTagline, fSub(), Element.ALIGN_RIGHT));
            infoEmpresa.addElement(p("CNPJ: " + businessCnpj, fSub(), Element.ALIGN_RIGHT));
            header.addCell(infoEmpresa);
            doc.add(header);

            doc.add(new Chunk(new LineSeparator(1f, 100, C_LINE, Element.ALIGN_CENTER, -5)));
            gap(doc, 20);

            // TÍTULO E STATUS
            PdfPTable titleTable = new PdfPTable(2);
            titleTable.setWidthPercentage(100);
            titleTable.addCell(cellNoBorder(new Phrase("COMPROVANTE DE AGENDAMENTO", fTitle())));

            String statusTxt = a.getStatus() == AppointmentStatus.CONFIRMED ? "CONFIRMADO" : a.getStatus().toString();
            Color statusColor = a.getStatus() == AppointmentStatus.CANCELLED ? Color.RED : C_ACCENT;
            PdfPCell stCell = cellNoBorder(new Phrase(statusTxt, fStatus(statusColor)));
            stCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            titleTable.addCell(stCell);
            doc.add(titleTable);

            gap(doc, 5);
            doc.add(p("Código: " + a.getCode(), fSub(), Element.ALIGN_LEFT));
            gap(doc, 15);

            // TABELA DE DADOS
            PdfPTable body = new PdfPTable(2);
            body.setWidthPercentage(100);
            addInfoRow(body, "CLIENTE", a.getClientName());
            addInfoRow(body, "SERVIÇO", a.getServiceName());
            addInfoRow(body, "PROFISSIONAL", a.getProfessionalName());
            addInfoRow(body, "DATA", a.getStartAt().format(DATE_FMT));
            addInfoRow(body, "HORÁRIO", a.getStartAt().format(TIME_FMT) + " - " + a.getEndAt().format(TIME_FMT));
            doc.add(body);

            gap(doc, 30);

            // ASSINATURA / AUTENTICAÇÃO
            PdfPTable auth = new PdfPTable(1);
            auth.setWidthPercentage(100);
            PdfPCell authCell = new PdfPCell();
            authCell.setBackgroundColor(new Color(248, 249, 250));
            authCell.setBorder(Rectangle.LEFT);
            authCell.setBorderColor(C_ACCENT);
            authCell.setBorderWidth(3f);
            authCell.setPadding(12);
            authCell.addElement(p("ASSINATURA DIGITAL", fLbl(), Element.ALIGN_LEFT));
            authCell.addElement(p(sha256(a), new Font(Font.COURIER, 7, Font.NORMAL, C_GRAY), Element.ALIGN_LEFT));
            authCell.addElement(p("Documento gerado em " + LocalDateTime.now().format(DT_FMT), fSub(), Element.ALIGN_LEFT));
            auth.addCell(authCell);
            doc.add(auth);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar PDF", e);
        }
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell cl = new PdfPCell(new Phrase(label, fLbl()));
        cl.setBorder(Rectangle.BOTTOM); cl.setBorderColor(C_LINE); cl.setPadding(8);
        table.addCell(cl);

        PdfPCell cv = new PdfPCell(new Phrase(value != null ? value : "-", fVal()));
        cv.setBorder(Rectangle.BOTTOM); cv.setBorderColor(C_LINE); cv.setPadding(8);
        cv.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cv);
    }

    private PdfPCell cellNoBorder(Phrase p) {
        PdfPCell c = new PdfPCell(p); c.setBorder(Rectangle.NO_BORDER); return c;
    }

    private Paragraph p(String t, Font f, int a) {
        Paragraph p = new Paragraph(t, f); p.setAlignment(a); return p;
    }

    private void gap(Document d, float h) throws DocumentException {
        Paragraph p = new Paragraph(" "); p.setLeading(h); d.add(p);
    }

    private String sha256(Appointment a) {
        try {
            String raw = a.getCode() + "|" + a.getId() + "|" + a.getStartAt();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString().toUpperCase();
        } catch (Exception e) { return "AUTH-NOT-AVAILABLE"; }
    }
}