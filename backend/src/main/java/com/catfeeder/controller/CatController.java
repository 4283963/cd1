package com.catfeeder.controller;

import com.catfeeder.entity.Cat;
import com.catfeeder.entity.CatCapture;
import com.catfeeder.service.CatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/cats")
public class CatController {

    @Autowired
    private CatService catService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCats(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "hours", required = false, defaultValue = "24") int hours) {
        Map<String, Object> result = new HashMap<>();
        List<Cat> cats;

        if ("new".equals(type)) {
            cats = catService.getNewCats();
        } else if ("recent".equals(type)) {
            cats = catService.getRecentlySeenCats(hours);
        } else {
            cats = catService.getAllCats();
        }

        result.put("total", cats.size());
        result.put("cats", cats);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cat> getCatById(@PathVariable Long id) {
        Optional<Cat> cat = catService.getCatById(id);
        return cat.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{catCode}")
    public ResponseEntity<Cat> getCatByCode(@PathVariable String catCode) {
        Optional<Cat> cat = catService.getCatByCode(catCode);
        return cat.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cat> updateCat(@PathVariable Long id, @RequestBody Cat catUpdate) {
        Cat updatedCat = catService.updateCat(id, catUpdate);
        return ResponseEntity.ok(updatedCat);
    }

    @GetMapping("/{id}/captures")
    public ResponseEntity<Map<String, Object>> getCatCaptures(@PathVariable Long id) {
        List<CatCapture> captures = catService.getCatCaptures(id);
        Map<String, Object> result = new HashMap<>();
        result.put("total", captures.size());
        result.put("captures", captures);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unrecognized")
    public ResponseEntity<Map<String, Object>> getUnrecognizedCaptures() {
        List<CatCapture> captures = catService.getUnrecognizedCaptures();
        Map<String, Object> result = new HashMap<>();
        result.put("total", captures.size());
        result.put("captures", captures);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/captures/{captureId}/assign")
    public ResponseEntity<CatCapture> assignCatToCapture(
            @PathVariable Long captureId,
            @RequestParam Long catId) {
        CatCapture capture = catService.assignCatToCapture(captureId, catId);
        return ResponseEntity.ok(capture);
    }

    @GetMapping("/stats/new-today")
    public ResponseEntity<Map<String, Object>> getNewCatsTodayCount() {
        long count = catService.countNewCatsToday();
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(result);
    }
}
