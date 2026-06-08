package com.catfeeder.config;

import com.catfeeder.entity.Cat;
import com.catfeeder.entity.Feeder;
import com.catfeeder.repository.CatRepository;
import com.catfeeder.repository.FeederRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(FeederRepository feederRepository, CatRepository catRepository) {
        return args -> {
            if (feederRepository.count() == 0) {
                Feeder feeder1 = new Feeder();
                feeder1.setFeederCode("feeder-001");
                feeder1.setName("1号楼喂养点");
                feeder1.setLocation("1号楼东侧花园");
                feeder1.setLongitude(new BigDecimal("116.4074"));
                feeder1.setLatitude(new BigDecimal("39.9042"));
                feeder1.setFoodCapacity(100);
                feeder1.setCurrentFoodLevel(75);
                feeder1.setWaterCapacity(100);
                feeder1.setCurrentWaterLevel(60);
                feeder1.setBatteryLevel(85);
                feeder1.setStatus("online");
                feeder1.setFoodAlert(false);
                feeder1.setWaterAlert(false);
                feeder1.setLastHeartbeat(LocalDateTime.now());
                feederRepository.save(feeder1);

                Feeder feeder2 = new Feeder();
                feeder2.setFeederCode("feeder-002");
                feeder2.setName("3号楼喂养点");
                feeder2.setLocation("3号楼南侧草坪");
                feeder2.setLongitude(new BigDecimal("116.4080"));
                feeder2.setLatitude(new BigDecimal("39.9050"));
                feeder2.setFoodCapacity(100);
                feeder2.setCurrentFoodLevel(15);
                feeder2.setWaterCapacity(100);
                feeder2.setCurrentWaterLevel(45);
                feeder2.setBatteryLevel(70);
                feeder2.setStatus("online");
                feeder2.setFoodAlert(true);
                feeder2.setWaterAlert(false);
                feeder2.setLastHeartbeat(LocalDateTime.now().minusMinutes(5));
                feederRepository.save(feeder2);

                Feeder feeder3 = new Feeder();
                feeder3.setFeederCode("feeder-003");
                feeder3.setName("5号楼喂养点");
                feeder3.setLocation("5号楼北门");
                feeder3.setLongitude(new BigDecimal("116.4065"));
                feeder3.setLatitude(new BigDecimal("39.9035"));
                feeder3.setFoodCapacity(100);
                feeder3.setCurrentFoodLevel(50);
                feeder3.setWaterCapacity(100);
                feeder3.setCurrentWaterLevel(18);
                feeder3.setBatteryLevel(95);
                feeder3.setStatus("online");
                feeder3.setFoodAlert(false);
                feeder3.setWaterAlert(true);
                feeder3.setLastHeartbeat(LocalDateTime.now().minusMinutes(2));
                feederRepository.save(feeder3);

                System.out.println("初始化喂养机数据完成");
            }

            if (catRepository.count() == 0) {
                Cat cat1 = new Cat();
                cat1.setCatCode("CAT-A1B2");
                cat1.setName("大橘");
                cat1.setFurColor("orange");
                cat1.setFurPattern("tabby");
                cat1.setBodyType("fat");
                cat1.setEyeColor("yellow");
                cat1.setGender("male");
                cat1.setEstimatedAge(3);
                cat1.setDescription("小区明星猫，性格温顺，特别能吃");
                cat1.setAvatarUrl("/api/images/captures/cat_orange_01.jpg");
                cat1.setIsNeutered(true);
                cat1.setIsNew(false);
                cat1.setFirstSeenTime(LocalDateTime.now().minusMonths(6));
                cat1.setLastSeenTime(LocalDateTime.now().minusHours(2));
                cat1.setVisitCount(128);
                catRepository.save(cat1);

                Cat cat2 = new Cat();
                cat2.setCatCode("CAT-C3D4");
                cat2.setName("小黑");
                cat2.setFurColor("black");
                cat2.setFurPattern("solid");
                cat2.setBodyType("slim");
                cat2.setEyeColor("green");
                cat2.setGender("female");
                cat2.setEstimatedAge(2);
                cat2.setDescription("胆子比较小，通常晚上出没");
                cat2.setAvatarUrl("/api/images/captures/cat_black_01.jpg");
                cat2.setIsNeutered(false);
                cat2.setIsNew(false);
                cat2.setFirstSeenTime(LocalDateTime.now().minusMonths(3));
                cat2.setLastSeenTime(LocalDateTime.now().minusHours(5));
                cat2.setVisitCount(56);
                catRepository.save(cat2);

                Cat cat3 = new Cat();
                cat3.setCatCode("CAT-E5F6");
                cat3.setName("三花");
                cat3.setFurColor("calico");
                cat3.setFurPattern("tricolor");
                cat3.setBodyType("normal");
                cat3.setEyeColor("yellow");
                cat3.setGender("female");
                cat3.setEstimatedAge(1);
                cat3.setDescription("新来的三花猫，很亲人");
                cat3.setAvatarUrl("/api/images/captures/cat_calico_01.jpg");
                cat3.setIsNeutered(false);
                cat3.setIsNew(true);
                cat3.setFirstSeenTime(LocalDateTime.now().minusDays(2));
                cat3.setLastSeenTime(LocalDateTime.now().minusHours(8));
                cat3.setVisitCount(8);
                catRepository.save(cat3);

                Cat cat4 = new Cat();
                cat4.setCatCode("CAT-G7H8");
                cat4.setName("奶牛");
                cat4.setFurColor("black_white");
                cat4.setFurPattern("tuxedo");
                cat4.setBodyType("normal");
                cat4.setEyeColor("yellow");
                cat4.setGender("male");
                cat4.setEstimatedAge(4);
                cat4.setDescription("胖乎乎的奶牛猫，很懒");
                cat4.setAvatarUrl("/api/images/captures/cat_tuxedo_01.jpg");
                cat4.setIsNeutered(true);
                cat4.setIsNew(false);
                cat4.setFirstSeenTime(LocalDateTime.now().minusYears(1));
                cat4.setLastSeenTime(LocalDateTime.now().minusDays(1));
                cat4.setVisitCount(256);
                catRepository.save(cat4);

                System.out.println("初始化猫咪数据完成");
            }
        };
    }
}
