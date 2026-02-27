package br.com.barbearia.apibarbearia.appointment.controller;

import br.com.barbearia.apibarbearia.appointment.dto.request.*;
import br.com.barbearia.apibarbearia.appointment.dto.response.AppointmentDetailResponse;
import br.com.barbearia.apibarbearia.appointment.service.AppointmentPdfService;
import br.com.barbearia.apibarbearia.appointment.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentAdminController {

    private final AppointmentService appointmentService;
    private final AppointmentPdfService pdfService;

    @GetMapping
    public ResponseEntity<Page<AppointmentDetailResponse>> list(
            AppointmentFilterRequest filter,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        return ResponseEntity.ok(appointmentService.list(filter, requesterId, requesterRole));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDetailResponse> getDetails(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        return ResponseEntity.ok(appointmentService.getDetails(id, requesterId, requesterRole));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<AppointmentDetailResponse> getDetailsByCode(
            @PathVariable String code,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        return ResponseEntity.ok(appointmentService.getDetailsByCode(code, requesterId, requesterRole));
    }

    @PostMapping
    public ResponseEntity<AppointmentDetailResponse> createInternal(
            @Valid @RequestBody CreateAppointmentInternalRequest req,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        AppointmentDetailResponse response = appointmentService.createInternal(req, requesterId, requesterRole);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) CancelAppointmentInternalRequest req,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        appointmentService.cancelInternal(id, req, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Void> confirmArrival(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        appointmentService.confirmArrival(id, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/no-show")
    public ResponseEntity<Void> markNoShow(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        appointmentService.markNoShow(id, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        appointmentService.getDetails(id, requesterId, requesterRole);

        byte[] pdfBytes = pdfService.generateReceipt(id);

        return buildPdfResponse(pdfBytes, "comprovante-agendamento-" + id + ".pdf");
    }


    @GetMapping("/code/{code}/pdf")
    public ResponseEntity<Resource> downloadPdfByCode(
            @PathVariable String code,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        appointmentService.getDetailsByCode(code, requesterId, requesterRole);

        byte[] pdfBytes = pdfService.generateReceiptByCode(code);

        return buildPdfResponse(pdfBytes, "comprovante-" + code + ".pdf");
    }

    @GetMapping("/{id}/pdf/view")
    public ResponseEntity<Resource> viewPdf(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader("X-User-Role") String requesterRole
    ) {
        appointmentService.getDetails(id, requesterId, requesterRole);

        byte[] pdfBytes = pdfService.generateReceipt(id);

        return buildPdfResponseInline(pdfBytes, "comprovante-agendamento-" + id + ".pdf");
    }

    private ResponseEntity<Resource> buildPdfResponse(byte[] pdfBytes, String filename) {
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    private ResponseEntity<Resource> buildPdfResponseInline(byte[] pdfBytes, String filename) {
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}