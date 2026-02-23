package br.com.barbearia.apibarbearia.appointment.controller;

import br.com.barbearia.apibarbearia.appointment.dto.request.CreateAppointmentInternalRequest;
import br.com.barbearia.apibarbearia.appointment.dto.response.AppointmentCreatedResponse;
import br.com.barbearia.apibarbearia.appointment.dto.response.AppointmentSlotResponse;
import br.com.barbearia.apibarbearia.appointment.dto.response.ProfessionalSimpleResponse;
import br.com.barbearia.apibarbearia.appointment.service.AppointmentPublicService;
import br.com.barbearia.apibarbearia.catalog.entity.CatalogItem;
import br.com.barbearia.apibarbearia.catalog.repository.CatalogRepository;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public/appointments")
@RequiredArgsConstructor
public class PublicAppointmentController {

    private final AppointmentPublicService publicService;

    private final CatalogRepository catalogRepository;


    @GetMapping("/services/{serviceId}/professionals")
    public ResponseEntity<List<ProfessionalSimpleResponse>> getProfessionalsByService(
            @PathVariable Long serviceId
    ) {
        CatalogItem service = catalogRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado."));

        List<ProfessionalSimpleResponse> professionals = service.getResponsibles().stream()
                .filter(User::isActive)
                .map(u -> ProfessionalSimpleResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(professionals);
    }

    @GetMapping("/slots")
    public ResponseEntity<List<AppointmentSlotResponse>> getAvailableSlots(
            @RequestParam Long serviceId,
            @RequestParam Long professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AppointmentSlotResponse> slots = publicService.listSlots(serviceId, professionalId, date);
        return ResponseEntity.ok(slots);
    }

    @PostMapping
    public ResponseEntity<AppointmentCreatedResponse> create(
            @Valid @RequestBody CreateAppointmentInternalRequest req
    ) {
        AppointmentCreatedResponse response = publicService.createPublic(req);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(@RequestParam("token") String token) {
        publicService.cancelByToken(token);
        return ResponseEntity.noContent().build();
    }


}
