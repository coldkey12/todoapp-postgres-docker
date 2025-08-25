package kz.don.todoapp.controller.cars;

import kz.don.todoapp.dto.cars.MercDTO;
import kz.don.todoapp.entity.cars.MercedesBenz;
import kz.don.todoapp.mappers.cars.MercMapper;
import kz.don.todoapp.service.cars.MercedesBenzService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merc")
@Slf4j
public class MercController {

    private final MercMapper mercMapper;
    private final MercedesBenzService mercedesBenzService;

    @PostMapping
    public ResponseEntity<String> saveMerc(@RequestBody MercDTO mercDTO) {

        MercedesBenz mercedesBenz = mercMapper.toEntity(mercDTO);

        mercedesBenzService.save(mercedesBenz);
        return ResponseEntity.ok("saved ur merc");
    }

    @GetMapping
    public ResponseEntity<MercDTO> getMercMileage(@RequestParam String uuid) {
        MercDTO mercDTO = mercedesBenzService.mileage(UUID.fromString(uuid));
        return ResponseEntity.ok(mercDTO);
    }
}
