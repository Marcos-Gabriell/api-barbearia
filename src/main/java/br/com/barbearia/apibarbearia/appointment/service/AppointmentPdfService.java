package br.com.barbearia.apibarbearia.appointment.service;

import br.com.barbearia.apibarbearia.appointment.entity.Appointment;
import br.com.barbearia.apibarbearia.appointment.entity.enums.AppointmentStatus;
import br.com.barbearia.apibarbearia.appointment.repository.AppointmentRepository;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AppointmentPdfService {

    private final AppointmentRepository appointmentRepository;

    @Value("${app.business.name:Barbearia Premium}")
    private String businessName;

    @Value("${app.business.address:}")
    private String businessAddress;

    @Value("${app.business.phone:}")
    private String businessPhone;

    @Value("${app.business.cnpj:}")
    private String businessCnpj;

    // Cores do tema
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(16, 185, 129);     // #10b981
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(30, 41, 59);      // #1e293b
    private static final DeviceRgb MUTED_COLOR = new DeviceRgb(100, 116, 139);       // #64748b
    private static final DeviceRgb DANGER_COLOR = new DeviceRgb(239, 68, 68);        // #ef4444
    private static final DeviceRgb WARNING_COLOR = new DeviceRgb(245, 158, 11);      // #f59e0b
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(248, 250, 252);          // #f8fafc
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(226, 232, 240);      // #e2e8f0

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    /**
     * Gera o PDF do comprovante de agendamento
     */
    public byte[] generateReceipt(Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        return generatePdf(a);
    }

    /**
     * Gera o PDF do comprovante pelo código do agendamento
     */
    public byte[] generateReceiptByCode(String code) {
        Appointment a = appointmentRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        return generatePdf(a);
    }

    private byte[] generatePdf(Appointment a) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ========== HEADER ==========
            addHeader(document, fontBold, fontRegular, a);

            // ========== STATUS BADGE ==========
            addStatusBadge(document, fontBold, a);

            // ========== DADOS DO AGENDAMENTO ==========
            addAppointmentDetails(document, fontBold, fontRegular, a);

            // ========== DADOS DO CLIENTE ==========
            addClientDetails(document, fontBold, fontRegular, a);

            // ========== AUDITORIA ==========
            addAuditSection(document, fontBold, fontRegular, a);

            // ========== ASSINATURA DIGITAL ==========
            addDigitalSignature(document, fontBold, fontRegular, a);

            // ========== FOOTER ==========
            addFooter(document, fontRegular);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    private void addHeader(Document document, PdfFont fontBold, PdfFont fontRegular, Appointment a) {
        // Tabela para header com logo e info
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // Coluna da logo/nome
        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        Paragraph businessParagraph = new Paragraph(businessName)
                .setFont(fontBold)
                .setFontSize(24)
                .setFontColor(PRIMARY_COLOR);
        logoCell.add(businessParagraph);

        if (businessAddress != null && !businessAddress.isEmpty()) {
            logoCell.add(new Paragraph(businessAddress)
                    .setFont(fontRegular)
                    .setFontSize(9)
                    .setFontColor(MUTED_COLOR));
        }

        if (businessPhone != null && !businessPhone.isEmpty()) {
            logoCell.add(new Paragraph("Tel: " + businessPhone)
                    .setFont(fontRegular)
                    .setFontSize(9)
                    .setFontColor(MUTED_COLOR));
        }

        headerTable.addCell(logoCell);

        // Coluna do código/data
        Cell infoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        infoCell.add(new Paragraph("COMPROVANTE DE AGENDAMENTO")
                .setFont(fontBold)
                .setFontSize(11)
                .setFontColor(MUTED_COLOR));

        infoCell.add(new Paragraph("Código: " + a.getCode())
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(5));

        infoCell.add(new Paragraph("Emitido em: " + LocalDateTime.now().format(DATETIME_FORMATTER))
                .setFont(fontRegular)
                .setFontSize(9)
                .setFontColor(MUTED_COLOR)
                .setMarginTop(5));

        headerTable.addCell(infoCell);
        document.add(headerTable);

        // Linha divisória
        document.add(new LineSeparator(new SolidBorder(BORDER_COLOR, 1))
                .setMarginBottom(20));
    }

    private void addStatusBadge(Document document, PdfFont fontBold, Appointment a) {
        DeviceRgb statusColor;
        String statusText;

        switch (a.getStatus()) {
            case CONFIRMED:
                statusColor = PRIMARY_COLOR;
                statusText = "✓ CONFIRMADO";
                break;
            case CANCELLED:
                statusColor = DANGER_COLOR;
                statusText = "✗ CANCELADO";
                break;
            case NO_SHOW:
                statusColor = MUTED_COLOR;
                statusText = "⊘ NÃO COMPARECEU";
                break;
            default:
                statusColor = WARNING_COLOR;
                statusText = "◷ PENDENTE";
        }

        Table statusTable = new Table(1)
                .useAllAvailableWidth()
                .setMarginBottom(20);

        Cell statusCell = new Cell()
                .setBackgroundColor(statusColor)
                .setBorder(Border.NO_BORDER)
                .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(8))
                .setPadding(12)
                .setTextAlignment(TextAlignment.CENTER);

        statusCell.add(new Paragraph(statusText)
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(ColorConstants.WHITE));

        statusTable.addCell(statusCell);
        document.add(statusTable);
    }

    private void addAppointmentDetails(Document document, PdfFont fontBold, PdfFont fontRegular, Appointment a) {
        document.add(new Paragraph("DETALHES DO AGENDAMENTO")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(10));

        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setBackgroundColor(LIGHT_BG)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setMarginBottom(20);

        // Serviço
        addDetailRow(detailsTable, fontBold, fontRegular, "Serviço", a.getServiceName());

        // Profissional
        addDetailRow(detailsTable, fontBold, fontRegular, "Profissional", a.getProfessionalName());

        // Data
        addDetailRow(detailsTable, fontBold, fontRegular, "Data", a.getStartAt().format(DATE_FORMATTER));

        // Horário
        String horario = a.getStartAt().format(TIME_FORMATTER) + " - " + a.getEndAt().format(TIME_FORMATTER);
        addDetailRow(detailsTable, fontBold, fontRegular, "Horário", horario);

        // Duração
        addDetailRow(detailsTable, fontBold, fontRegular, "Duração", a.getDurationMinutes() + " minutos");

        document.add(detailsTable);
    }

    private void addClientDetails(Document document, PdfFont fontBold, PdfFont fontRegular, Appointment a) {
        document.add(new Paragraph("DADOS DO CLIENTE")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(10));

        Table clientTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setBackgroundColor(LIGHT_BG)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setMarginBottom(20);

        addDetailRow(clientTable, fontBold, fontRegular, "Nome", a.getClientName());
        addDetailRow(clientTable, fontBold, fontRegular, "E-mail", a.getClientEmail());
        addDetailRow(clientTable, fontBold, fontRegular, "Telefone", formatPhone(a.getClientPhone()));

        document.add(clientTable);
    }

    private void addAuditSection(Document document, PdfFont fontBold, PdfFont fontRegular, Appointment a) {
        document.add(new Paragraph("HISTÓRICO DE AÇÕES")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(10));

        Table auditTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2}))
                .useAllAvailableWidth()
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setMarginBottom(20);

        // Header
        auditTable.addHeaderCell(createHeaderCell(fontBold, "Ação"));
        auditTable.addHeaderCell(createHeaderCell(fontBold, "Executado por"));
        auditTable.addHeaderCell(createHeaderCell(fontBold, "Data/Hora"));

        // Criação
        auditTable.addCell(createAuditCell(fontRegular, "Criação"));
        auditTable.addCell(createAuditCell(fontRegular, a.getCreatedByDescription()));
        auditTable.addCell(createAuditCell(fontRegular, a.getCreatedAt().format(DATETIME_FORMATTER)));

        // Confirmação (se houver)
        if (a.getConfirmedAt() != null) {
            auditTable.addCell(createAuditCell(fontRegular, "Confirmação"));
            auditTable.addCell(createAuditCell(fontRegular, a.getConfirmedByDescription()));
            auditTable.addCell(createAuditCell(fontRegular, a.getConfirmedAt().format(DATETIME_FORMATTER)));
        }

        // Cancelamento (se houver)
        if (a.getCanceledAt() != null && a.getStatus() == AppointmentStatus.CANCELLED) {
            auditTable.addCell(createAuditCell(fontRegular, "Cancelamento"));
            auditTable.addCell(createAuditCell(fontRegular, a.getCanceledByDescription()));
            auditTable.addCell(createAuditCell(fontRegular, a.getCanceledAt().format(DATETIME_FORMATTER)));

            // Motivo do cancelamento
            if (a.getCancelMessage() != null && !a.getCancelMessage().isEmpty()) {
                Cell motivoCell = new Cell(1, 3)
                        .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                        .setBackgroundColor(new DeviceRgb(254, 242, 242)) // bg-red-50
                        .setPadding(8);
                motivoCell.add(new Paragraph("Motivo: " + a.getCancelMessage())
                        .setFont(fontRegular)
                        .setFontSize(9)
                        .setFontColor(DANGER_COLOR));
                auditTable.addCell(motivoCell);
            }
        }

        // No-show (se houver)
        if (a.getNoShowAt() != null) {
            auditTable.addCell(createAuditCell(fontRegular, "No-Show"));
            String noShowBy = a.getNoShowByUsername() != null
                    ? a.getNoShowByUsername() + " (" + a.getNoShowByRole() + ")"
                    : "Sistema";
            auditTable.addCell(createAuditCell(fontRegular, noShowBy));
            auditTable.addCell(createAuditCell(fontRegular, a.getNoShowAt().format(DATETIME_FORMATTER)));
        }

        document.add(auditTable);
    }

    private void addDigitalSignature(Document document, PdfFont fontBold, PdfFont fontRegular, Appointment a) {
        document.add(new LineSeparator(new SolidBorder(BORDER_COLOR, 1))
                .setMarginTop(20)
                .setMarginBottom(20));

        Table signatureTable = new Table(1)
                .useAllAvailableWidth()
                .setBackgroundColor(new DeviceRgb(240, 253, 244)) // bg-green-50
                .setBorder(new SolidBorder(PRIMARY_COLOR, 1))
                .setMarginBottom(20);

        Cell signatureCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        signatureCell.add(new Paragraph("✓ ASSINATURA DIGITAL DA BARBEARIA")
                .setFont(fontBold)
                .setFontSize(11)
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(8));

        // Hash simulado baseado no código e data
        String hash = generateHash(a);
        signatureCell.add(new Paragraph("Hash: " + hash)
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(MUTED_COLOR));

        signatureCell.add(new Paragraph("Documento gerado eletronicamente em " + LocalDateTime.now().format(DATETIME_FORMATTER))
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(MUTED_COLOR)
                .setMarginTop(4));

        if (businessCnpj != null && !businessCnpj.isEmpty()) {
            signatureCell.add(new Paragraph("CNPJ: " + businessCnpj)
                    .setFont(fontRegular)
                    .setFontSize(8)
                    .setFontColor(MUTED_COLOR)
                    .setMarginTop(4));
        }

        signatureTable.addCell(signatureCell);
        document.add(signatureTable);
    }

    private void addFooter(Document document, PdfFont fontRegular) {
        document.add(new Paragraph("Este documento é um comprovante de agendamento e não possui valor fiscal.")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(MUTED_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

        document.add(new Paragraph("Em caso de dúvidas, entre em contato conosco.")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(MUTED_COLOR)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private void addDetailRow(Table table, PdfFont fontBold, PdfFont fontRegular, String label, String value) {
        Cell labelCell = new Cell()
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(10)
                .setBackgroundColor(LIGHT_BG);
        labelCell.add(new Paragraph(label)
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(MUTED_COLOR));
        table.addCell(labelCell);

        Cell valueCell = new Cell()
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(10)
                .setBackgroundColor(ColorConstants.WHITE);
        valueCell.add(new Paragraph(value != null ? value : "-")
                .setFont(fontRegular)
                .setFontSize(10)
                .setFontColor(SECONDARY_COLOR));
        table.addCell(valueCell);
    }

    private Cell createHeaderCell(PdfFont fontBold, String text) {
        Cell cell = new Cell()
                .setBackgroundColor(SECONDARY_COLOR)
                .setBorder(Border.NO_BORDER)
                .setPadding(10);
        cell.add(new Paragraph(text)
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(ColorConstants.WHITE));
        return cell;
    }

    private Cell createAuditCell(PdfFont fontRegular, String text) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(8)
                .setBackgroundColor(ColorConstants.WHITE);
        cell.add(new Paragraph(text != null ? text : "-")
                .setFont(fontRegular)
                .setFontSize(9)
                .setFontColor(SECONDARY_COLOR));
        return cell;
    }

    private String formatPhone(String phone) {
        if (phone == null || phone.isEmpty()) return "-";
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return String.format("(%s) %s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 7),
                    digits.substring(7));
        }
        return phone;
    }

    private String generateHash(Appointment a) {
        // Gera um hash simples para assinatura digital
        String data = a.getCode() + a.getCreatedAt().toString() + a.getServiceId();
        int hash = data.hashCode();
        return String.format("SHA256:%08X-%08X-%08X-%08X",
                hash,
                (hash * 31) & 0xFFFFFFFF,
                (hash * 37) & 0xFFFFFFFF,
                (hash * 41) & 0xFFFFFFFF);
    }
}