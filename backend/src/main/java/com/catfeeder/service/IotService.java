package com.catfeeder.service;

import com.catfeeder.dto.CatFeaturesDTO;
import com.catfeeder.dto.FeedingEventDTO;
import com.catfeeder.dto.HeartbeatDTO;
import com.catfeeder.dto.SensorDataDTO;
import com.catfeeder.entity.Cat;
import com.catfeeder.entity.CatCapture;
import com.catfeeder.entity.FeedingRecord;
import com.catfeeder.repository.CatCaptureRepository;
import com.catfeeder.repository.FeedingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
public class IotService {

    @Autowired
    private FeederService feederService;

    @Autowired
    private CatService catService;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private CatCaptureRepository catCaptureRepository;

    @Autowired
    private FeedingRecordRepository feedingRecordRepository;

    @Autowired
    private AlertService alertService;

    @Transactional
    public Map<String, Object> processSensorData(SensorDataDTO dto) {
        LocalDateTime recordTime = parseTimestamp(dto.getTimestamp());

        feederService.recordSensorData(
                dto.getFeederId(),
                dto.getFoodLevel(),
                dto.getWaterLevel(),
                dto.getTemperature(),
                dto.getBatteryLevel(),
                dto.getInfraredTriggered(),
                recordTime
        );

        return Map.of(
                "success", true,
                "message", "传感器数据已处理"
        );
    }

    @Transactional
    public Map<String, Object> processCatCapture(String feederId, MultipartFile image,
                                                  String timestamp, CatFeaturesDTO catFeatures) {
        LocalDateTime captureTime = parseTimestamp(timestamp);
        String imageUrl = imageStorageService.saveImage(image, feederId);

        CatCapture capture = new CatCapture();
        capture.setFeederCode(feederId);
        capture.setImageUrl(imageUrl);
        capture.setCaptureTime(captureTime);

        boolean isNewCat = false;
        Cat recognizedCat = null;

        if (catFeatures != null && catFeatures.getFurColor() != null) {
            capture.setFurColor(catFeatures.getFurColor());
            capture.setFurPattern(catFeatures.getFurPattern());
            capture.setBodyType(catFeatures.getBodyType());
            capture.setEyeColor(catFeatures.getEyeColor());

            Optional<Cat> catOpt = catService.identifyCat(catFeatures);

            if (catOpt.isPresent()) {
                recognizedCat = catOpt.get();
                capture.setCatId(recognizedCat.getId());
                capture.setRecognized(true);
                catService.registerCatVisit(recognizedCat.getId());
            } else {
                recognizedCat = catService.createCatProfile(catFeatures, imageUrl);
                capture.setCatId(recognizedCat.getId());
                capture.setRecognized(true);
                capture.setIsNewCat(true);
                isNewCat = true;

                var feederOpt = feederService.getFeederByCode(feederId);
                String feederName = feederOpt.map(f -> f.getName()).orElse(feederId);
                alertService.createNewCatAlert(feederName, recognizedCat.getName(), imageUrl);
            }
        }

        catCaptureRepository.save(capture);

        return Map.of(
                "success", true,
                "catRecognized", recognizedCat != null,
                "isNewCat", isNewCat,
                "catId", recognizedCat != null ? recognizedCat.getId() : null,
                "catName", recognizedCat != null ? recognizedCat.getName() : null,
                "imageUrl", imageUrl
        );
    }

    @Transactional
    public Map<String, Object> processFeedingEvent(FeedingEventDTO dto) {
        LocalDateTime feedingTime = parseTimestamp(dto.getTimestamp());

        FeedingRecord record = new FeedingRecord();
        record.setFeederCode(dto.getFeederId());
        record.setCatId(dto.getCatId());
        record.setAmount(dto.getAmount());
        record.setFeedingTime(feedingTime);

        feedingRecordRepository.save(record);

        if (dto.getCatId() != null) {
            catService.registerCatVisit(dto.getCatId());
        }

        return Map.of(
                "success", true,
                "message", "喂食事件已记录"
        );
    }

    @Transactional
    public Map<String, Object> processHeartbeat(HeartbeatDTO dto) {
        feederService.updateFeederStatus(
                dto.getFeederId(),
                dto.getFoodLevel(),
                dto.getWaterLevel(),
                dto.getBatteryLevel(),
                dto.getStatus()
        );

        return Map.of(
                "success", true,
                "message", "心跳已接收",
                "serverTime", LocalDateTime.now().toString()
        );
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
