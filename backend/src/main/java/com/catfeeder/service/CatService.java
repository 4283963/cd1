package com.catfeeder.service;

import com.catfeeder.dto.CatFeaturesDTO;
import com.catfeeder.entity.Cat;
import com.catfeeder.entity.CatCapture;
import com.catfeeder.repository.CatCaptureRepository;
import com.catfeeder.repository.CatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatService {

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private CatCaptureRepository catCaptureRepository;

    public Optional<Cat> identifyCat(CatFeaturesDTO features) {
        if (features == null) {
            return Optional.empty();
        }

        List<Cat> candidates = catRepository.findByFurColorAndFurPatternAndBodyType(
            features.getFurColor(),
            features.getFurPattern(),
            features.getBodyType()
        );

        if (candidates.size() == 1) {
            return Optional.of(candidates.get(0));
        }

        return Optional.empty();
    }

    @Transactional
    public Cat createCatProfile(CatFeaturesDTO features, String imageUrl) {
        Cat cat = new Cat();
        cat.setCatCode("CAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        cat.setName("新猫咪-" + cat.getCatCode().substring(4, 8));
        cat.setFurColor(features.getFurColor());
        cat.setFurPattern(features.getFurPattern());
        cat.setBodyType(features.getBodyType());
        cat.setEyeColor(features.getEyeColor());
        cat.setGender(features.getGender());
        cat.setAvatarUrl(imageUrl);
        cat.setIsNew(true);
        cat.setFirstSeenTime(LocalDateTime.now());
        cat.setLastSeenTime(LocalDateTime.now());
        cat.setVisitCount(1);
        return catRepository.save(cat);
    }

    @Transactional
    public Cat registerCatVisit(Long catId) {
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> new RuntimeException("猫咪不存在"));
        cat.setLastSeenTime(LocalDateTime.now());
        cat.setVisitCount(cat.getVisitCount() + 1);
        cat.setIsNew(false);
        return catRepository.save(cat);
    }

    @Transactional
    public Cat updateCat(Long catId, Cat catUpdate) {
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> new RuntimeException("猫咪不存在"));
        
        if (catUpdate.getName() != null) cat.setName(catUpdate.getName());
        if (catUpdate.getFurColor() != null) cat.setFurColor(catUpdate.getFurColor());
        if (catUpdate.getFurPattern() != null) cat.setFurPattern(catUpdate.getFurPattern());
        if (catUpdate.getBodyType() != null) cat.setBodyType(catUpdate.getBodyType());
        if (catUpdate.getEyeColor() != null) cat.setEyeColor(catUpdate.getEyeColor());
        if (catUpdate.getGender() != null) cat.setGender(catUpdate.getGender());
        if (catUpdate.getDescription() != null) cat.setDescription(catUpdate.getDescription());
        if (catUpdate.getIsNeutered() != null) cat.setIsNeutered(catUpdate.getIsNeutered());
        if (catUpdate.getEstimatedAge() != null) cat.setEstimatedAge(catUpdate.getEstimatedAge());
        
        cat.setIsNew(false);
        return catRepository.save(cat);
    }

    public Optional<Cat> getCatById(Long id) {
        return catRepository.findById(id);
    }

    public Optional<Cat> getCatByCode(String catCode) {
        return catRepository.findByCatCode(catCode);
    }

    public List<Cat> getAllCats() {
        return catRepository.findAllByOrderByVisitCountDesc();
    }

    public List<Cat> getNewCats() {
        return catRepository.findByIsNewTrue();
    }

    public List<Cat> getRecentlySeenCats(int hours) {
        LocalDateTime time = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        return catRepository.findRecentlySeenCats(time);
    }

    public long countNewCatsToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return catRepository.countNewCatsToday(startOfDay);
    }

    public List<CatCapture> getCatCaptures(Long catId) {
        return catCaptureRepository.findByCatIdOrderByCaptureTimeDesc(catId);
    }

    public List<CatCapture> getUnrecognizedCaptures() {
        return catCaptureRepository.findUnrecognizedCaptures();
    }

    @Transactional
    public CatCapture assignCatToCapture(Long captureId, Long catId) {
        CatCapture capture = catCaptureRepository.findById(captureId)
                .orElseThrow(() -> new RuntimeException("抓拍记录不存在"));
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> new RuntimeException("猫咪不存在"));
        
        capture.setCatId(catId);
        capture.setRecognized(true);
        capture.setIsNewCat(false);
        
        cat.setLastSeenTime(capture.getCaptureTime());
        cat.setVisitCount(cat.getVisitCount() + 1);
        cat.setIsNew(false);
        catRepository.save(cat);
        
        return catCaptureRepository.save(capture);
    }

    public long getTotalCatCount() {
        return catRepository.count();
    }
}
