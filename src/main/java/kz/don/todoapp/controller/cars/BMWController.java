package kz.don.todoapp.controller.cars;

import kz.don.todoapp.dto.cars.BMWDTO;
import kz.don.todoapp.entity.cars.BMW;
import kz.don.todoapp.mappers.cars.BMWMapper;
import kz.don.todoapp.service.cars.BMWService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bmw")
@Slf4j
public class BMWController {

    private final BMWService bmwService;
    private final BMWMapper bmwMapper;

    @PostMapping
    public ResponseEntity<String> saveBmw(@RequestBody BMWDTO bmwDTO) {
        BMW bmw = bmwMapper.toEntity(bmwDTO);

        log.info("is m series? : {}", bmwDTO.isMSeries());

        bmwService.save(bmw);
        return ResponseEntity.ok("bmw v dto? bmw na sto.");
    }

    @GetMapping
    public ResponseEntity<BMWDTO> getBmw(@RequestParam String uuid) {
        BMWDTO bmwdto = bmwService.getBmw(UUID.fromString(uuid));
        return ResponseEntity.ok(bmwdto);
    }
}
