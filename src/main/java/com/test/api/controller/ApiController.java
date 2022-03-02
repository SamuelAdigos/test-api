package com.test.api.controller;

import com.test.api.model.dto.KpiDTO;
import com.test.api.model.dto.MetricsDTO;
import com.test.api.model.dto.ProcessResponseDTO;
import com.test.api.model.data.ProcessedFile;
import com.test.api.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/file")
public class ApiController {

    @Autowired
    private FileService fileService;

    @GetMapping("/{date}")
    public ResponseEntity<ProcessResponseDTO> processFile(@PathVariable Integer date){
        return ResponseEntity.ok(fileService.processFile(date));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProcessedFile>> processFile(){
        return ResponseEntity.ok(fileService.getFiles());
    }

    @GetMapping("/{date}/metrics")
    public ResponseEntity<MetricsDTO> getMetrics(@PathVariable Integer date){
        return ResponseEntity.ok(fileService.getMetrics(date));
    }

    @GetMapping("/kpis")
    public ResponseEntity<KpiDTO> getKpis(){
        return ResponseEntity.ok(fileService.getKpis());
    }

}
